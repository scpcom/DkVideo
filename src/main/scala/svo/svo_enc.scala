package svo

import chisel3._
import chisel3.util.Cat
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

class svo_enc(
    val vp: VideoParams = VideoConsts.m800x600.params,
    val sp: SvoParams = SvoConsts.DefaultParams
  ) extends RawModule {
val io = IO(new Bundle {
  val clk = Input(Clock())
  val resetn = Input(Bool())

  // input stream
  //   tuser[0] ... start of frame
  val in_axis_tvalid = Input(Bool())
  val in_axis_tready = Output(Bool())
  val in_axis_tdata = Input(UInt(sp.SVO_BITS_PER_PIXEL.W))
  val in_axis_tuser = Input(UInt(1.W))

  // output stream
  //   tuser[0] ... start of frame
  //   tuser[1] ... hsync
  //   tuser[2] ... vsync
  //   tuser[3] ... blank
  val out_axis_tvalid = Output(Bool())
  val out_axis_tready = Input(Bool())
  val out_axis_tdata = Output(UInt(sp.SVO_BITS_PER_PIXEL.W))
  val out_axis_tuser = Output(UInt(4.W))
})

  val SVO_HOR_TOTAL = vp.H_FRONT + vp.H_SYNC + vp.H_BACK + vp.H_DISPLAY
  val SVO_VER_TOTAL = vp.V_TOP + vp.V_SYNC + vp.V_BOTTOM + vp.V_DISPLAY

  withClockAndReset(io.clk, ~io.resetn) {
  // NOTE: The following statements are auto generated based on existing output reg of the original verilog source
  val in_axis_tready_out_reg = RegInit(false.B)
  io.in_axis_tready := in_axis_tready_out_reg
  val out_axis_tvalid_out_reg = RegInit(false.B)
  io.out_axis_tvalid := out_axis_tvalid_out_reg
  val out_axis_tdata_out_reg = RegInit(0.U(sp.SVO_BITS_PER_PIXEL.W))
  io.out_axis_tdata := out_axis_tdata_out_reg
  val out_axis_tuser_out_reg = RegInit(0.U(4.W))
  io.out_axis_tuser := out_axis_tuser_out_reg

  val hcursor = RegInit(0.U(sp.SVO_XYBITS.W))
  val vcursor = RegInit(0.U(sp.SVO_XYBITS.W))

  val ctrl_fifo = Reg(Vec(4, Vec(4, Bool()))) 
  val ctrl_fifo_wraddr = RegInit(0.U(2.W))
  val ctrl_fifo_rdaddr = RegInit(0.U(2.W))

  val pixel_fifo = Reg(Vec(8, Vec(sp.SVO_BITS_PER_PIXEL+1, Bool())))
  val pixel_fifo_wraddr = RegInit(0.U(3.W))
  val pixel_fifo_rdaddr = RegInit(0.U(3.W))

  val out_fifo = Reg(Vec(4, UInt(((sp.SVO_BITS_PER_PIXEL+3)+1).W)))
  val out_fifo_wraddr = RegInit(0.U(2.W))
  val out_fifo_rdaddr = RegInit(0.U(2.W))

  val ctrl_fifo_fill = WireDefault(UInt(2.W), ctrl_fifo_wraddr-ctrl_fifo_rdaddr) 
  val pixel_fifo_fill = WireDefault(UInt(3.W), pixel_fifo_wraddr-pixel_fifo_rdaddr) 
  val out_fifo_fill = WireDefault(UInt(2.W), out_fifo_wraddr-out_fifo_rdaddr) 
  val is_hsync = RegInit(false.B)
  val is_vsync = RegInit(false.B)
  val is_blank = RegInit(false.B)

  when (ctrl_fifo_wraddr+1.U(2.W) =/= ctrl_fifo_rdaddr) {
    is_blank := false.B
    is_hsync := false.B
    is_vsync := false.B

    when(hcursor < vp.H_FRONT.U) {
      is_blank := true.B
    } .elsewhen (hcursor < (vp.H_FRONT+vp.H_SYNC).U) {
      is_blank := true.B
      is_hsync := true.B
    } .elsewhen (hcursor < ((vp.H_FRONT+vp.H_SYNC)+vp.H_BACK).U) {
      is_blank := true.B
    }

    when(vcursor < vp.V_TOP.U) {
      is_blank := true.B
    } .elsewhen (vcursor < (vp.V_TOP+vp.V_SYNC).U) {
      is_blank := true.B
      is_vsync := true.B
    } .elsewhen (vcursor < ((vp.V_TOP+vp.V_SYNC)+vp.V_BOTTOM).U) {
      is_blank := true.B
    }

    ctrl_fifo(ctrl_fifo_wraddr) := Cat(is_blank, is_vsync, is_hsync, ( !(hcursor =/= 0.U)) && ( !(vcursor =/= 0.U))).asTypeOf(Vec(4, Bool()))
    ctrl_fifo_wraddr := ctrl_fifo_wraddr+1.U

    when(hcursor === (SVO_HOR_TOTAL-1).U) {
      hcursor := 0.U
      vcursor := Mux(vcursor === (SVO_VER_TOTAL-1).U,  0.U, vcursor+1.U)
    } .otherwise {
      hcursor := hcursor+1.U
    }
  }

    when(io.in_axis_tvalid && in_axis_tready_out_reg) {
      pixel_fifo(pixel_fifo_wraddr) := Cat(io.in_axis_tuser, io.in_axis_tdata).asTypeOf(Vec(sp.SVO_BITS_PER_PIXEL+1, Bool()))
      pixel_fifo_wraddr := pixel_fifo_wraddr+1.U
    }
    in_axis_tready_out_reg := (pixel_fifo_wraddr+2.U(3.W) =/= pixel_fifo_rdaddr) && (pixel_fifo_wraddr+1.U(3.W) =/= pixel_fifo_rdaddr)

    when(((ctrl_fifo_rdaddr =/= ctrl_fifo_wraddr) && (pixel_fifo_rdaddr =/= pixel_fifo_wraddr)) && (out_fifo_wraddr+1.U(2.W) =/= out_fifo_rdaddr)) {
      when(ctrl_fifo(ctrl_fifo_rdaddr)(0) && ( !pixel_fifo(pixel_fifo_rdaddr)(sp.SVO_BITS_PER_PIXEL))) {
        // drop pixels until frame start is in sync
        pixel_fifo_rdaddr := pixel_fifo_rdaddr+1.U
      } .elsewhen (ctrl_fifo(ctrl_fifo_rdaddr)(3)) {
        out_fifo(out_fifo_wraddr) := Cat(ctrl_fifo(ctrl_fifo_rdaddr).asUInt, (VecInit.tabulate(sp.SVO_BITS_PER_PIXEL)(_ => false.B)).asUInt)
        out_fifo_wraddr := out_fifo_wraddr+1.U
        ctrl_fifo_rdaddr := ctrl_fifo_rdaddr+1.U
      } .otherwise {
        out_fifo(out_fifo_wraddr) := Cat(ctrl_fifo(ctrl_fifo_rdaddr).asUInt, pixel_fifo(pixel_fifo_rdaddr).asUInt(sp.SVO_BITS_PER_PIXEL-1,0))
        out_fifo_wraddr := out_fifo_wraddr+1.U
        ctrl_fifo_rdaddr := ctrl_fifo_rdaddr+1.U
        pixel_fifo_rdaddr := pixel_fifo_rdaddr+1.U
      }
    }

  val next_out_fifo_rdaddr = RegInit(0.U(2.W))
  val wait_for_fifos = RegInit(0.U(2.W))

  when ((wait_for_fifos < 3.U) || (out_fifo_fill === 0.U)) {
    when(((ctrl_fifo_fill < 3.U) || (pixel_fifo_fill < 6.U)) || (out_fifo_fill < 3.U)) {
      wait_for_fifos := 0.U
    } .otherwise {
      wait_for_fifos := wait_for_fifos+1.U
    }
  } .otherwise {
    next_out_fifo_rdaddr := out_fifo_rdaddr
    when(out_axis_tvalid_out_reg && io.out_axis_tready) {
      next_out_fifo_rdaddr := next_out_fifo_rdaddr+1.U
    }

    out_axis_tvalid_out_reg := next_out_fifo_rdaddr =/= out_fifo_wraddr
    // NOTE: The following statements are auto generated due to the use of concatenation: you may hence want to refactor it
    val auto_concat = Wire(new Bundle { 
      val out_axis_tuser_out_reg = UInt(4.W)
      val out_axis_tdata_out_reg = UInt(sp.SVO_BITS_PER_PIXEL.W)
    }) 
    auto_concat := out_fifo(next_out_fifo_rdaddr).asTypeOf(auto_concat)
    out_axis_tuser_out_reg := auto_concat.out_axis_tuser_out_reg
    out_axis_tdata_out_reg := auto_concat.out_axis_tdata_out_reg

    out_fifo_rdaddr := next_out_fifo_rdaddr
  }
  } //withClockAndReset(clk, ~resetn)
}
