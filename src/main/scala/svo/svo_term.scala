package svo

import chisel3._
import chisel3.util.{Cat,log2Ceil}
import hdmicore.video.{VideoParams, VideoConsts}
/*
 *  SVO - Simple Video Out FPGA Core
 *
 *  Copyright (C) 2014  Clifford Wolf <clifford@clifford.at>
 *  
 *  Permission to use, copy, modify, and/or distribute this software for any
 *  purpose with or without fee is hereby granted, provided that the above
 *  copyright notice and this permission notice appear in all copies.
 *  
 *  THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 *  WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 *  ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 *  WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 *  ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 *  OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 *
 */

// `timescale1ns/1ps

//import "svo_defines.vh"._

class svo_term(
    val vp: VideoParams = VideoConsts.m800x600.params,
    val sp: SvoParams = SvoConsts.DefaultParams,
    val MEM_DEPTH: Int = 2048
  ) extends RawModule {
val io = IO(new Bundle {
  // resetn clock domain: clk
  val clk = Input(Clock())
  val oclk = Input(Clock())
  val resetn = Input(Bool())

  // input stream
  //
  // clock domain: clk
  //
  val in_axis_tvalid = Input(Bool())
  val in_axis_tready = Output(Bool())
  val in_axis_tdata = Input(UInt(8.W))

  // output stream
  //   tuser[0] ... start of frame
  //
  // tdata[1:0] values:
  //   2'b00 ... no character
  //   2'b01 ... character background
  //   2'b10 ... character foreground
  //   2'b11 ... reserved
  //
  // clock domain: oclk
  //
  val out_axis_tvalid = Output(Bool())
  val out_axis_tready = Input(Bool())
  val out_axis_tdata = Output(UInt(2.W))
  val out_axis_tuser = Output(UInt(1.W))
})

  val pipeline_en = Wire(Bool()) 

  // --------------------------------------------------------------
  // Text Memory
  // --------------------------------------------------------------

  def svo_clog2(n: Int) = log2Ceil(n)

  val MEM_ABITS = svo_clog2(MEM_DEPTH)

  val g_request_remove_line_oclk = Wire(Bool())
  val g_mem = Wire(Vec(MEM_DEPTH, UInt(8.W)))
  val g_mem_start_GR = Wire(UInt(MEM_ABITS.W))
  val g_mem_stop_GR = Wire(UInt(MEM_ABITS.W))

  withClockAndReset(io.clk, ~io.resetn) {
  val mem = Reg(Vec(MEM_DEPTH, UInt(8.W))) 
  val mem_start = RegInit(0.U(MEM_ABITS.W))
  val mem_stop = RegInit(0.U(MEM_ABITS.W))

  val mem_portA_addr = Reg(UInt(MEM_ABITS.W)) 
  val mem_portA_rdata = Reg(UInt(8.W)) 
  val mem_portA_wdata = Reg(UInt(8.W)) 
  val mem_portA_wen = Reg(Bool()) 

  val mem_start_GR = Reg(UInt(MEM_ABITS.W)) 
  val mem_stop_GR = Reg(UInt(MEM_ABITS.W)) 

  def mem_bin2gray(in: UInt): UInt = {
	val temp = Reg(UInt((MEM_ABITS+1).W))
	val tout = VecInit(in.asBools)
	temp := in
	for(i <- 0 to MEM_ABITS-1){
		tout(i) := temp(i+1,i).xorR() // ^temp(i +: 2)
	}
	tout.asUInt
  }

  when(mem_portA_wen) {
    mem_portA_rdata := "b0".U
    mem(mem_portA_addr) := mem_portA_wdata
  } .otherwise {
    mem_portA_rdata := mem(mem_portA_addr)
  }

  mem_start_GR := mem_bin2gray(mem_start)
  mem_stop_GR := mem_bin2gray(mem_stop)
  g_mem := mem
  g_mem_start_GR := mem_start_GR
  g_mem_stop_GR := mem_stop_GR
  //} // withClockAndReset(io.clk, ~io.resetn)


  // --------------------------------------------------------------
  // Input Interface
  // --------------------------------------------------------------

  //withClockAndReset(io.clk, ~io.resetn) {
  val request_remove_line_syn1 = Reg(Bool()) 
  val request_remove_line_syn2 = Reg(Bool()) 
  val request_remove_line_syn3 = Reg(Bool()) 
  val request_remove_line = Reg(Bool()) 
  request_remove_line_syn1 := g_request_remove_line_oclk
  request_remove_line_syn2 := request_remove_line_syn1
  request_remove_line_syn3 := request_remove_line_syn2
  request_remove_line := request_remove_line_syn2 =/= request_remove_line_syn3

  val remove_line = RegInit(false.B)
  val next_mem_start = Wire(UInt(MEM_ABITS.W)) 
  val next_mem_stop = Wire(UInt(MEM_ABITS.W)) 
  next_mem_start := Mux(mem_start === (MEM_DEPTH-1).U, 0.U, mem_start+1.U)
  next_mem_stop := Mux(mem_stop === (MEM_DEPTH-1).U, 0.U, mem_stop+1.U)
  io.in_axis_tready := (next_mem_stop =/= mem_start) && ( !remove_line)
  mem_portA_wen := false.B
  mem_portA_wdata := io.in_axis_tdata
  mem_portA_addr := mem_start

  when(request_remove_line && (mem_start =/= mem_stop)) {
    mem_portA_addr := next_mem_start
    mem_start := next_mem_start
    remove_line := true.B
  }

    when(remove_line) {
      when((mem_portA_rdata === '\n'.toInt.U) || (mem_start === mem_stop)) {
        remove_line := false.B
      } .otherwise {
        mem_portA_addr := next_mem_start
        mem_start := next_mem_start
      }
    } .elsewhen (next_mem_stop === mem_start) {
      when(mem_portA_addr === mem_start) {
        mem_portA_addr := next_mem_start
        mem_start := next_mem_start
        remove_line := true.B
      }
    } .elsewhen (io.in_axis_tvalid && io.in_axis_tready) {
      when((io.in_axis_tdata >= 32.U) || (io.in_axis_tdata === '\n'.toInt.U)) {
        mem_stop := next_mem_stop
        mem_portA_addr := mem_stop
        mem_portA_wen := true.B
      } .elsewhen (io.in_axis_tdata === 4.U) {
        // EOT clears the screen
        mem_stop := mem_start
      } .elsewhen (io.in_axis_tdata === 8.U) {
        // BS removes the last char
        when(mem_stop =/= mem_start) {
          mem_stop := Mux(mem_stop === 0.U, (MEM_DEPTH-1).U(MEM_ABITS.W), mem_stop-1.U)
        }
      }
    }
  } // withClockAndReset(io.clk, ~io.resetn)


  // --------------------------------------------------------------
  // Font Memory
  // --------------------------------------------------------------
  val fontmem: UInt = Cat(
		"b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W),
		"b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b01100000".U(8.W), "b10010010".U(8.W), "b00001100".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W),
		"b00000000".U(8.W), "b00001100".U(8.W), "b00010000".U(8.W), "b00010000".U(8.W), "b00100000".U(8.W), "b00010000".U(8.W), "b00010000".U(8.W), "b00001100".U(8.W),
		"b00000000".U(8.W), "b00010000".U(8.W), "b00010000".U(8.W), "b00010000".U(8.W), "b00010000".U(8.W), "b00010000".U(8.W), "b00010000".U(8.W), "b00010000".U(8.W),
		"b00000000".U(8.W), "b00110000".U(8.W), "b00001000".U(8.W), "b00001000".U(8.W), "b00000100".U(8.W), "b00001000".U(8.W), "b00001000".U(8.W), "b00110000".U(8.W),
		"b00000000".U(8.W), "b00111100".U(8.W), "b00001000".U(8.W), "b00010000".U(8.W), "b00100000".U(8.W), "b00111100".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W),
		"b00111000".U(8.W), "b01000000".U(8.W), "b01110000".U(8.W), "b01001000".U(8.W), "b01001000".U(8.W), "b01001000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W),
		"b00000000".U(8.W), "b01000100".U(8.W), "b00101000".U(8.W), "b00010000".U(8.W), "b00101000".U(8.W), "b01000100".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W),
		"b00000000".U(8.W), "b01000100".U(8.W), "b10101010".U(8.W), "b10010010".U(8.W), "b10000010".U(8.W), "b10000010".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W),
		"b00000000".U(8.W), "b00010000".U(8.W), "b00101000".U(8.W), "b01000100".U(8.W), "b01000100".U(8.W), "b01000100".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W),
		"b00000000".U(8.W), "b01011000".U(8.W), "b00100100".U(8.W), "b00100100".U(8.W), "b00100100".U(8.W), "b00100100".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W),
		"b00000000".U(8.W), "b00010000".U(8.W), "b00010000".U(8.W), "b00010000".U(8.W), "b00010000".U(8.W), "b00111000".U(8.W), "b00010000".U(8.W), "b00000000".U(8.W),
		"b00000000".U(8.W), "b00011100".U(8.W), "b00100000".U(8.W), "b00011000".U(8.W), "b00000100".U(8.W), "b00111000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W),
		"b00000000".U(8.W), "b00000100".U(8.W), "b00000100".U(8.W), "b00000100".U(8.W), "b00001100".U(8.W), "b00110100".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W),
		"b00100000".U(8.W), "b00100000".U(8.W), "b00111000".U(8.W), "b00100100".U(8.W), "b00100100".U(8.W), "b01011000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W),
		"b00001000".U(8.W), "b00001000".U(8.W), "b00111000".U(8.W), "b01001000".U(8.W), "b01001000".U(8.W), "b00110100".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W),
		"b00000000".U(8.W), "b00111000".U(8.W), "b01000100".U(8.W), "b01000100".U(8.W), "b01000100".U(8.W), "b00111000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W),
		"b00000000".U(8.W), "b01001000".U(8.W), "b01001000".U(8.W), "b01001000".U(8.W), "b01001000".U(8.W), "b00110100".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W),
		"b00000000".U(8.W), "b10000010".U(8.W), "b10000010".U(8.W), "b10010010".U(8.W), "b10010010".U(8.W), "b01101101".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W),
		"b00000000".U(8.W), "b00010000".U(8.W), "b00010000".U(8.W), "b00010000".U(8.W), "b00010000".U(8.W), "b00010000".U(8.W), "b00010000".U(8.W), "b00011000".U(8.W),
		"b00000000".U(8.W), "b00100100".U(8.W), "b00010100".U(8.W), "b00001100".U(8.W), "b00010100".U(8.W), "b00100100".U(8.W), "b00000100".U(8.W), "b00000100".U(8.W),
		"b00001100".U(8.W), "b00010000".U(8.W), "b00010000".U(8.W), "b00010000".U(8.W), "b00010000".U(8.W), "b00000000".U(8.W), "b00010000".U(8.W), "b00000000".U(8.W),
		"b00000000".U(8.W), "b00010000".U(8.W), "b00010000".U(8.W), "b00010000".U(8.W), "b00010000".U(8.W), "b00000000".U(8.W), "b00010000".U(8.W), "b00000000".U(8.W),
		"b00000000".U(8.W), "b01000100".U(8.W), "b01000100".U(8.W), "b01000100".U(8.W), "b01001100".U(8.W), "b00110100".U(8.W), "b00000100".U(8.W), "b00000100".U(8.W),
		"b00111000".U(8.W), "b01000000".U(8.W), "b01111000".U(8.W), "b01000100".U(8.W), "b01000100".U(8.W), "b10111000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W),
		"b00000000".U(8.W), "b00001000".U(8.W), "b00001000".U(8.W), "b00001000".U(8.W), "b00011100".U(8.W), "b00001000".U(8.W), "b01001000".U(8.W), "b00110000".U(8.W),
		"b00000000".U(8.W), "b00111000".U(8.W), "b00000100".U(8.W), "b01111100".U(8.W), "b01000100".U(8.W), "b00111000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W),
		"b00000000".U(8.W), "b10110000".U(8.W), "b01001000".U(8.W), "b01001000".U(8.W), "b01001000".U(8.W), "b01110000".U(8.W), "b01000000".U(8.W), "b01000000".U(8.W),
		"b00000000".U(8.W), "b00111000".U(8.W), "b00000100".U(8.W), "b00000100".U(8.W), "b00000100".U(8.W), "b00111000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W),
		"b00000000".U(8.W), "b00110100".U(8.W), "b01001000".U(8.W), "b01001000".U(8.W), "b01001000".U(8.W), "b00111000".U(8.W), "b00001000".U(8.W), "b00001000".U(8.W),
		"b00000000".U(8.W), "b10111000".U(8.W), "b01000100".U(8.W), "b01000100".U(8.W), "b01111000".U(8.W), "b01000000".U(8.W), "b00111000".U(8.W), "b00000000".U(8.W),
		"b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00010000".U(8.W), "b00001000".U(8.W),
		"b11111110".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W),
		"b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b01000100".U(8.W), "b00101000".U(8.W), "b00010000".U(8.W),
		"b00000000".U(8.W), "b00111000".U(8.W), "b00100000".U(8.W), "b00100000".U(8.W), "b00100000".U(8.W), "b00100000".U(8.W), "b00100000".U(8.W), "b00111000".U(8.W),
		"b00000000".U(8.W), "b10000000".U(8.W), "b01000000".U(8.W), "b00100000".U(8.W), "b00010000".U(8.W), "b00001000".U(8.W), "b00000100".U(8.W), "b00000010".U(8.W),
		"b00000000".U(8.W), "b00111000".U(8.W), "b00001000".U(8.W), "b00001000".U(8.W), "b00001000".U(8.W), "b00001000".U(8.W), "b00001000".U(8.W), "b00111000".U(8.W),
		"b00000000".U(8.W), "b01111100".U(8.W), "b00000100".U(8.W), "b00001000".U(8.W), "b00010000".U(8.W), "b00100000".U(8.W), "b01000000".U(8.W), "b01111100".U(8.W),
		"b00000000".U(8.W), "b00010000".U(8.W), "b00010000".U(8.W), "b00010000".U(8.W), "b00010000".U(8.W), "b00101000".U(8.W), "b01000100".U(8.W), "b01000100".U(8.W),
		"b00000000".U(8.W), "b01000100".U(8.W), "b01000100".U(8.W), "b00101000".U(8.W), "b00010000".U(8.W), "b00101000".U(8.W), "b01000100".U(8.W), "b01000100".U(8.W),
		"b00000000".U(8.W), "b00101000".U(8.W), "b00101000".U(8.W), "b01010100".U(8.W), "b01010100".U(8.W), "b10000010".U(8.W), "b10000010".U(8.W), "b10000010".U(8.W),
		"b00000000".U(8.W), "b00010000".U(8.W), "b00010000".U(8.W), "b00101000".U(8.W), "b00101000".U(8.W), "b01000100".U(8.W), "b01000100".U(8.W), "b01000100".U(8.W),
		"b00000000".U(8.W), "b00111000".U(8.W), "b01000100".U(8.W), "b01000100".U(8.W), "b01000100".U(8.W), "b01000100".U(8.W), "b01000100".U(8.W), "b01000100".U(8.W),
		"b00000000".U(8.W), "b00010000".U(8.W), "b00010000".U(8.W), "b00010000".U(8.W), "b00010000".U(8.W), "b00010000".U(8.W), "b00010000".U(8.W), "b01111100".U(8.W),
		"b00000000".U(8.W), "b00111000".U(8.W), "b01000100".U(8.W), "b01000000".U(8.W), "b00111000".U(8.W), "b00000100".U(8.W), "b01000100".U(8.W), "b00111000".U(8.W),
		"b00000000".U(8.W), "b01000100".U(8.W), "b00100100".U(8.W), "b00010100".U(8.W), "b00111100".U(8.W), "b01000100".U(8.W), "b01000100".U(8.W), "b00111100".U(8.W),
		"b01100000".U(8.W), "b00111000".U(8.W), "b01000100".U(8.W), "b01000100".U(8.W), "b01000100".U(8.W), "b01000100".U(8.W), "b01000100".U(8.W), "b00111000".U(8.W),
		"b00000000".U(8.W), "b00001000".U(8.W), "b00001000".U(8.W), "b00001000".U(8.W), "b00111000".U(8.W), "b01001000".U(8.W), "b01001000".U(8.W), "b00111000".U(8.W),
		"b00000000".U(8.W), "b00111000".U(8.W), "b01000100".U(8.W), "b01000100".U(8.W), "b01000100".U(8.W), "b01000100".U(8.W), "b01000100".U(8.W), "b00111000".U(8.W),
		"b00000000".U(8.W), "b01000100".U(8.W), "b01000100".U(8.W), "b01100100".U(8.W), "b01010100".U(8.W), "b01010100".U(8.W), "b01001100".U(8.W), "b01000100".U(8.W),
		"b00000000".U(8.W), "b10000010".U(8.W), "b10000010".U(8.W), "b10000010".U(8.W), "b10010010".U(8.W), "b10101010".U(8.W), "b11000110".U(8.W), "b10000010".U(8.W),
		"b00000000".U(8.W), "b01111000".U(8.W), "b00001000".U(8.W), "b00001000".U(8.W), "b00001000".U(8.W), "b00001000".U(8.W), "b00001000".U(8.W), "b00001000".U(8.W),
		"b00000000".U(8.W), "b01000100".U(8.W), "b01000100".U(8.W), "b00100100".U(8.W), "b00011100".U(8.W), "b00100100".U(8.W), "b01000100".U(8.W), "b01000100".U(8.W),
		"b00000000".U(8.W), "b00011000".U(8.W), "b00100100".U(8.W), "b00100100".U(8.W), "b00100000".U(8.W), "b00100000".U(8.W), "b00100000".U(8.W), "b01110000".U(8.W),
		"b00000000".U(8.W), "b00111000".U(8.W), "b00010000".U(8.W), "b00010000".U(8.W), "b00010000".U(8.W), "b00010000".U(8.W), "b00010000".U(8.W), "b00111000".U(8.W),
		"b00000000".U(8.W), "b01000100".U(8.W), "b01000100".U(8.W), "b01000100".U(8.W), "b01111100".U(8.W), "b01000100".U(8.W), "b01000100".U(8.W), "b01000100".U(8.W),
		"b00000000".U(8.W), "b00111000".U(8.W), "b01000100".U(8.W), "b01000100".U(8.W), "b01110100".U(8.W), "b00000100".U(8.W), "b01000100".U(8.W), "b00111000".U(8.W),
		"b00000000".U(8.W), "b00000100".U(8.W), "b00000100".U(8.W), "b00000100".U(8.W), "b01111100".U(8.W), "b00000100".U(8.W), "b00000100".U(8.W), "b01111100".U(8.W),
		"b00000000".U(8.W), "b01111100".U(8.W), "b00000100".U(8.W), "b00000100".U(8.W), "b00111100".U(8.W), "b00000100".U(8.W), "b00000100".U(8.W), "b01111100".U(8.W),
		"b00000000".U(8.W), "b00111100".U(8.W), "b01000100".U(8.W), "b01000100".U(8.W), "b01000100".U(8.W), "b01000100".U(8.W), "b01000100".U(8.W), "b00111100".U(8.W),
		"b00000000".U(8.W), "b00111000".U(8.W), "b01000100".U(8.W), "b00000100".U(8.W), "b00000100".U(8.W), "b00000100".U(8.W), "b01000100".U(8.W), "b00111000".U(8.W),
		"b00000000".U(8.W), "b00111100".U(8.W), "b01000100".U(8.W), "b01000100".U(8.W), "b00111100".U(8.W), "b01000100".U(8.W), "b01000100".U(8.W), "b00111100".U(8.W),
		"b00000000".U(8.W), "b01000100".U(8.W), "b01000100".U(8.W), "b01000100".U(8.W), "b01111100".U(8.W), "b01000100".U(8.W), "b01000100".U(8.W), "b00111000".U(8.W),
		"b00000000".U(8.W), "b00111000".U(8.W), "b00000100".U(8.W), "b01110100".U(8.W), "b01010100".U(8.W), "b01110100".U(8.W), "b01000100".U(8.W), "b00111000".U(8.W),
		"b00000000".U(8.W), "b00010000".U(8.W), "b00000000".U(8.W), "b00010000".U(8.W), "b00100000".U(8.W), "b01000000".U(8.W), "b01000100".U(8.W), "b00111000".U(8.W),
		"b00000000".U(8.W), "b00000100".U(8.W), "b00001000".U(8.W), "b00010000".U(8.W), "b00100000".U(8.W), "b00010000".U(8.W), "b00001000".U(8.W), "b00000100".U(8.W),
		"b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b11111110".U(8.W), "b00000000".U(8.W), "b11111110".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W),
		"b00000000".U(8.W), "b00100000".U(8.W), "b00010000".U(8.W), "b00001000".U(8.W), "b00000100".U(8.W), "b00001000".U(8.W), "b00010000".U(8.W), "b00100000".U(8.W),
		"b00010000".U(8.W), "b00100000".U(8.W), "b00110000".U(8.W), "b00110000".U(8.W), "b00000000".U(8.W), "b00110000".U(8.W), "b00110000".U(8.W), "b00000000".U(8.W),
		"b00000000".U(8.W), "b00000000".U(8.W), "b00110000".U(8.W), "b00110000".U(8.W), "b00000000".U(8.W), "b00110000".U(8.W), "b00110000".U(8.W), "b00000000".U(8.W),
		"b00000000".U(8.W), "b00111000".U(8.W), "b01000100".U(8.W), "b01000000".U(8.W), "b01111000".U(8.W), "b01000100".U(8.W), "b01000100".U(8.W), "b00111000".U(8.W),
		"b00000000".U(8.W), "b00111000".U(8.W), "b01000100".U(8.W), "b01000100".U(8.W), "b00111000".U(8.W), "b01000100".U(8.W), "b01000100".U(8.W), "b00111000".U(8.W),
		"b00000000".U(8.W), "b00001000".U(8.W), "b00001000".U(8.W), "b00001000".U(8.W), "b00010000".U(8.W), "b00100000".U(8.W), "b01000000".U(8.W), "b01111100".U(8.W),
		"b00000000".U(8.W), "b00111000".U(8.W), "b01000100".U(8.W), "b01000100".U(8.W), "b00111100".U(8.W), "b00000100".U(8.W), "b01000100".U(8.W), "b00111000".U(8.W),
		"b00000000".U(8.W), "b00111000".U(8.W), "b01000100".U(8.W), "b01000000".U(8.W), "b00111100".U(8.W), "b00000100".U(8.W), "b00000100".U(8.W), "b01111100".U(8.W),
		"b00000000".U(8.W), "b01110000".U(8.W), "b00100000".U(8.W), "b00100000".U(8.W), "b01111100".U(8.W), "b00100100".U(8.W), "b00101000".U(8.W), "b00110000".U(8.W),
		"b00000000".U(8.W), "b00111000".U(8.W), "b01000100".U(8.W), "b01000000".U(8.W), "b00110000".U(8.W), "b01000000".U(8.W), "b01000100".U(8.W), "b00111000".U(8.W),
		"b00000000".U(8.W), "b01111100".U(8.W), "b00001000".U(8.W), "b00010000".U(8.W), "b00100000".U(8.W), "b01000000".U(8.W), "b01000100".U(8.W), "b00111000".U(8.W),
		"b00000000".U(8.W), "b00111000".U(8.W), "b00010000".U(8.W), "b00010000".U(8.W), "b00010000".U(8.W), "b00010000".U(8.W), "b00011000".U(8.W), "b00010000".U(8.W),
		"b00000000".U(8.W), "b00111000".U(8.W), "b01000100".U(8.W), "b01000100".U(8.W), "b01010100".U(8.W), "b01000100".U(8.W), "b01000100".U(8.W), "b00111000".U(8.W),
		"b00000000".U(8.W), "b00000010".U(8.W), "b00000100".U(8.W), "b00001000".U(8.W), "b00010000".U(8.W), "b00100000".U(8.W), "b01000000".U(8.W), "b10000000".U(8.W),
		"b00000000".U(8.W), "b00110000".U(8.W), "b00110000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W),
		"b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b11111110".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W),
		"b00010000".U(8.W), "b00100000".U(8.W), "b00110000".U(8.W), "b00110000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W),
		"b00000000".U(8.W), "b00010000".U(8.W), "b00010000".U(8.W), "b00010000".U(8.W), "b11111110".U(8.W), "b00010000".U(8.W), "b00010000".U(8.W), "b00010000".U(8.W),
		"b00000000".U(8.W), "b00010000".U(8.W), "b10010010".U(8.W), "b01010100".U(8.W), "b00111000".U(8.W), "b01010100".U(8.W), "b10010010".U(8.W), "b00010000".U(8.W),
		"b00000000".U(8.W), "b00001000".U(8.W), "b00010000".U(8.W), "b00100000".U(8.W), "b00100000".U(8.W), "b00100000".U(8.W), "b00010000".U(8.W), "b00001000".U(8.W),
		"b00000000".U(8.W), "b00100000".U(8.W), "b00010000".U(8.W), "b00001000".U(8.W), "b00001000".U(8.W), "b00001000".U(8.W), "b00010000".U(8.W), "b00100000".U(8.W),
		"b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00010000".U(8.W), "b00010000".U(8.W),
		"b00000000".U(8.W), "b01011100".U(8.W), "b00100010".U(8.W), "b01100010".U(8.W), "b00010100".U(8.W), "b00001000".U(8.W), "b00010100".U(8.W), "b00011000".U(8.W),
		"b00000000".U(8.W), "b00000000".U(8.W), "b01100100".U(8.W), "b01101000".U(8.W), "b00010000".U(8.W), "b00101100".U(8.W), "b01001100".U(8.W), "b00000000".U(8.W),
		"b00000000".U(8.W), "b00010000".U(8.W), "b00111100".U(8.W), "b01010000".U(8.W), "b00111000".U(8.W), "b00010100".U(8.W), "b01111000".U(8.W), "b00010000".U(8.W),
		"b00000000".U(8.W), "b00101000".U(8.W), "b00101000".U(8.W), "b11111110".U(8.W), "b00101000".U(8.W), "b11111110".U(8.W), "b00101000".U(8.W), "b00101000".U(8.W),
		"b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00101000".U(8.W), "b00101000".U(8.W),
		"b00000000".U(8.W), "b00010000".U(8.W), "b00000000".U(8.W), "b00010000".U(8.W), "b00010000".U(8.W), "b00010000".U(8.W), "b00010000".U(8.W), "b00010000".U(8.W),
		"b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W),
		"b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W),
		"b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W),
		"b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W),
		"b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W),
		"b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W),
		"b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W),
		"b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W),
		"b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W),
		"b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W),
		"b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W),
		"b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W),
		"b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W),
		"b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W),
		"b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W),
		"b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W),
		"b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W),
		"b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W),
		"b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W),
		"b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W),
		"b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W),
		"b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W),
		"b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W),
		"b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W),
		"b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W),
		"b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W),
		"b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W),
		"b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W),
		"b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W),
		"b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W),
		"b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W),
		"b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W),
		"b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W), "b00000000".U(8.W)
  )

  def font(c: UInt, x: UInt, y: UInt): Bool = {
    fontmem(Cat(c(7,0), y(2,0), x(2,0)))
  }


  withClockAndReset(io.oclk, ~io.resetn) {
  val mem_portB_addr = Reg(UInt(MEM_ABITS.W))
  val mem_portB_rdata = Reg(UInt(8.W))
  val mem_start_B1 = Reg(UInt(MEM_ABITS.W))
  val mem_stop_B1 = Reg(UInt(MEM_ABITS.W))
  val mem_start_B2 = Reg(UInt(MEM_ABITS.W))
  val mem_stop_B2 = Reg(UInt(MEM_ABITS.W))
  val mem_start_B3 = Reg(UInt(MEM_ABITS.W))
  val mem_stop_B3 = Reg(UInt(MEM_ABITS.W))
  val mem_start_B = Reg(UInt(MEM_ABITS.W))
  val mem_stop_B = Reg(UInt(MEM_ABITS.W))

  def mem_gray2bin(in: UInt): UInt = {
        val tout = VecInit(in.asBools)
        for(i <- 0 to MEM_ABITS-1){
                tout(i) := (in >> i).xorR()
        }
        tout.asUInt
  }

  when(pipeline_en) {
    mem_portB_rdata := Mux(mem_portB_addr =/= mem_stop_B, g_mem(mem_portB_addr), 0.U)
  }

  mem_start_B1 := g_mem_start_GR
  mem_start_B2 := mem_start_B1
  mem_start_B3 := mem_gray2bin(mem_start_B2)

  mem_stop_B1 := g_mem_stop_GR
  mem_stop_B2 := mem_stop_B1
  mem_stop_B3 := mem_gray2bin(mem_stop_B2)
  //} // withClockAndReset(io.oclk, ~io.resetn)

  // --------------------------------------------------------------
  // Video Pipeline
  // --------------------------------------------------------------

  //withClockAndReset(io.oclk, ~io.resetn) {
  val oresetn_q = Reg(UInt(4.W)) 
  val oresetn = Reg(Bool()) 
  // NOTE: The following statements are auto generated due to the use of concatenation: you may hence want to refactor it
  val auto_concat = Wire(new Bundle { 
    val oresetn = Bool()
    val oresetn_q = UInt(4.W)
  }) 

	// synchronize oresetn with oclk

  auto_concat := Cat(oresetn_q, io.resetn).asTypeOf(auto_concat)
  oresetn := auto_concat.oresetn
  oresetn_q := auto_concat.oresetn_q

  // --------------------------------------------------------------
  // Pipeline stage 1: basic video timing

  val p1_start_of_frame = Reg(Bool()) 
  val p1_start_of_line = Reg(Bool()) 
  val p1_valid = Reg(Bool()) 
  val p1_xpos = Reg(UInt(sp.SVO_XYBITS.W))
  val p1_ypos = Reg(UInt(sp.SVO_XYBITS.W))
  when( !oresetn) {
    p1_xpos := 0.U
    p1_ypos := 0.U
    p1_valid := false.B
  } .elsewhen (pipeline_en) {
    p1_valid := true.B
    p1_start_of_frame := ( !(p1_xpos =/= 0.U)) && ( !(p1_ypos =/= 0.U))
    p1_start_of_line :=  !(p1_xpos =/= 0.U)
    when(p1_xpos === (vp.H_DISPLAY-1).U) {
      p1_xpos := 0.U
      p1_ypos := Mux(p1_ypos === (vp.V_DISPLAY-1).U,  0.U, p1_ypos+1.U)
    } .otherwise {
      p1_xpos := p1_xpos+1.U
    }
  }

  // --------------------------------------------------------------
  // Pipeline stage 2: text memory addr generator
  val p2_x = Reg(UInt(3.W)) 
  val p2_y = Reg(UInt(3.W)) 
  val p2_start_of_frame = Reg(Bool()) 
  val p2_start_of_line = Reg(Bool()) 
  val p2_valid = Reg(Bool()) 
  val p2_found_end = Reg(Bool()) 
  val p2_last_req_remline = Reg(Bool()) 
  val p2_line_start_addr = Reg(UInt(MEM_ABITS.W)) 
  val next_mem_portB_addr = Wire(UInt(MEM_ABITS.W)) 
  val request_remove_line_oclk = Reg(Bool())
  next_mem_portB_addr := Mux(mem_portB_addr === (MEM_DEPTH-1).U, 0.U, mem_portB_addr+1.U)

  when( !oresetn) {
    p2_valid := false.B
    p2_found_end := true.B
    p2_last_req_remline := true.B
    request_remove_line_oclk := false.B
  } .elsewhen (pipeline_en) {
    p2_start_of_frame := p1_start_of_frame
    p2_start_of_line := p1_start_of_line
    p2_valid := p1_valid

    when(mem_portB_addr === mem_stop_B) {
      p2_found_end := true.B
    }

    when(p1_start_of_frame) {
      when(( !p2_found_end) && ( !p2_last_req_remline)) {
        request_remove_line_oclk :=  ~request_remove_line_oclk
        p2_last_req_remline := true.B
      } .otherwise {
        p2_last_req_remline := false.B
      }

      mem_stop_B := mem_stop_B3
      mem_start_B := mem_start_B3
      mem_portB_addr := mem_start_B3
      p2_line_start_addr := mem_start_B3
      p2_found_end := false.B
      p2_x := 0.U
      p2_y := 0.U
    } .elsewhen (p1_start_of_line) {
      when(p2_y === 7.U) {
        when(mem_portB_addr =/= mem_stop_B) {
          mem_portB_addr := next_mem_portB_addr
          p2_line_start_addr := next_mem_portB_addr
        } .otherwise {
          p2_line_start_addr := mem_stop_B
        }
      } .otherwise {
        mem_portB_addr := p2_line_start_addr
      }
      p2_x := 0.U
      p2_y := p2_y+1.U
    } .otherwise {
      when(p2_x === 7.U) {
        when((mem_portB_addr =/= mem_stop_B) && (mem_portB_rdata =/= '\n'.toInt.U)) {
          mem_portB_addr := next_mem_portB_addr
        }
      }
      p2_x := p2_x+1.U
    }
  }
  g_request_remove_line_oclk := request_remove_line_oclk

  // --------------------------------------------------------------
  // Pipeline stage 3: wait for memory
  val p3_x = Reg(UInt(3.W)) 
  val p3_y = Reg(UInt(3.W)) 
  val p3_start_of_frame = Reg(Bool()) 
  val p3_start_of_line = Reg(Bool()) 
  val p3_valid = Reg(Bool()) 

  when( !oresetn) {
    p3_valid := false.B
  } .elsewhen (pipeline_en) {
    p3_x := p2_x
    p3_y := p2_y
    p3_start_of_frame := p2_start_of_frame
    p3_start_of_line := p2_start_of_line
    p3_valid := p2_valid
  }

  // --------------------------------------------------------------
  // Pipeline stage 4: read char

  val p4_c = Reg(UInt(8.W)) 
  val p4_x = Reg(UInt(3.W)) 
  val p4_y = Reg(UInt(3.W)) 
  val p4_start_of_frame = Reg(Bool()) 
  val p4_valid = Reg(Bool()) 

  when( !oresetn) {
    p4_valid := false.B
  } .elsewhen (pipeline_en) {
    p4_c := mem_portB_rdata
    p4_x := p3_x
    p4_y := p3_y
    p4_start_of_frame := p3_start_of_frame
    p4_valid := p3_valid
  }

  // --------------------------------------------------------------
  // Pipeline stage 5: font lookup

  val p5_outval = Reg(UInt(2.W)) 
  val p5_start_of_frame = Reg(Bool()) 
  val p5_valid = Reg(Bool()) 

  when( !oresetn) {
    p5_valid := false.B
  } .elsewhen (pipeline_en) {
    when((32.U <= p4_c) && (p4_c < 128.U)) {
      p5_outval := Mux(font(p4_c, p4_x, p4_y), "b10".U(2.W), "b01".U(2.W))
    } .otherwise {
      p5_outval := 0.U
    }
    p5_start_of_frame := p4_start_of_frame
    p5_valid := p4_valid
  }

  // --------------------------------------------------------------
  // Pipeline output stage
  pipeline_en := ( !p5_valid) || io.out_axis_tready
  io.out_axis_tvalid := p5_valid
  io.out_axis_tdata := p5_outval
  io.out_axis_tuser := p5_start_of_frame
  } //withClockAndReset(io.oclk, ~io.resetn)
}
