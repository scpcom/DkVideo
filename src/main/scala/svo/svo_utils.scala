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


// ----------------------------------------------------------------------
// module svo_axis_pipe
//
// this core is a simple helper for creating video pipeline cores with
// an axi stream interface.
// ----------------------------------------------------------------------

class svo_axis_pipe(
    val TDATA_WIDTH: Int = 8, 
    val TUSER_WIDTH: Int = 1
  ) extends RawModule {
val io = IO(new Bundle {
  val clk = Input(Clock())
  val resetn = Input(Bool())

	// axis input stream
  val in_axis_tvalid = Input(Bool())
  val in_axis_tready = Output(Bool())
  val in_axis_tdata = Input(UInt(TDATA_WIDTH.W))
  val in_axis_tuser = Input(UInt(TUSER_WIDTH.W))

	// axis output stream
  val out_axis_tvalid = Output(Bool())
  val out_axis_tready = Input(Bool())
  val out_axis_tdata = Output(UInt(TDATA_WIDTH.W))
  val out_axis_tuser = Output(UInt(TUSER_WIDTH.W))

	// pipeline i/o
  val pipe_in_tdata = Output(UInt(TDATA_WIDTH.W))
  val pipe_out_tdata = Input(UInt(TDATA_WIDTH.W))
  val pipe_in_tuser = Output(UInt(TUSER_WIDTH.W))
  val pipe_out_tuser = Input(UInt(TUSER_WIDTH.W))
  val pipe_in_tvalid = Output(Bool())
  val pipe_out_tvalid = Input(Bool())
  val pipe_enable = Output(Bool())
})

  withClockAndReset(io.clk, ~io.resetn) {
  val tvalid_q0 = RegInit(false.B)
  val tvalid_q1 = RegInit(false.B)
  val tdata_q0 = Reg(UInt(TDATA_WIDTH.W)) 
  val tdata_q1 = Reg(UInt(TDATA_WIDTH.W)) 
  val tuser_q0 = Reg(UInt(TUSER_WIDTH.W)) 
  val tuser_q1 = Reg(UInt(TUSER_WIDTH.W)) 
  io.in_axis_tready :=  !tvalid_q1
  io.out_axis_tvalid := tvalid_q0 || tvalid_q1
  io.out_axis_tdata := Mux(tvalid_q1, tdata_q1, tdata_q0)
  io.out_axis_tuser := Mux(tvalid_q1, tuser_q1, tuser_q0)
  io.pipe_enable := io.in_axis_tvalid && io.in_axis_tready
  io.pipe_in_tdata := io.in_axis_tdata
  io.pipe_in_tuser := io.in_axis_tuser
  io.pipe_in_tvalid := io.in_axis_tvalid

    when(io.pipe_enable) {
      tdata_q0 := io.pipe_out_tdata
      tdata_q1 := tdata_q0
      tuser_q0 := io.pipe_out_tuser
      tuser_q1 := tuser_q0
      tvalid_q0 := io.pipe_out_tvalid
      tvalid_q1 := tvalid_q0 && ( !io.out_axis_tready)
    } .elsewhen (io.out_axis_tready) {
      when(tvalid_q1) {
        tvalid_q1 := false.B
      } .otherwise {
        tvalid_q0 := false.B
      }
    }
  } //withClockAndReset(clk, ~resetn)
}


// ----------------------------------------------------------------------
// module svo_buf
//
// just a buffer that adds an other ff layer to the stream.
// ----------------------------------------------------------------------

class svo_buf(
    val TUSER_WIDTH: Int = 1, 
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
  val in_axis_tuser = Input(UInt(TUSER_WIDTH.W))

  // output stream
  //   tuser[0] ... start of frame
  val out_axis_tvalid = Output(Bool())
  val out_axis_tready = Input(Bool())
  val out_axis_tdata = Output(UInt(sp.SVO_BITS_PER_PIXEL.W))
  val out_axis_tuser = Output(UInt(TUSER_WIDTH.W))
})

  withClockAndReset(io.clk, ~io.resetn) {
  val pipe_in_tdata = Wire(UInt(sp.SVO_BITS_PER_PIXEL.W))
  val pipe_out_tdata = Reg(UInt(sp.SVO_BITS_PER_PIXEL.W))
  val pipe_in_tuser = Wire(UInt(TUSER_WIDTH.W)) 
  val pipe_out_tuser = Reg(UInt(TUSER_WIDTH.W)) 
  val pipe_in_tvalid = Wire(Bool()) 
  val pipe_out_tvalid = RegInit(false.B)
  val pipe_enable = Wire(Bool()) 

  when (pipe_enable) {
    pipe_out_tdata := pipe_in_tdata
    pipe_out_tuser := pipe_in_tuser
    pipe_out_tvalid := pipe_in_tvalid
  }

  val svo_axis_pipe = Module(new svo_axis_pipe(
      TDATA_WIDTH = sp.SVO_BITS_PER_PIXEL,
      TUSER_WIDTH = TUSER_WIDTH
  ))
  svo_axis_pipe.io.clk := io.clk
  svo_axis_pipe.io.resetn := io.resetn

  svo_axis_pipe.io.in_axis_tvalid := io.in_axis_tvalid
  io.in_axis_tready := svo_axis_pipe.io.in_axis_tready
  svo_axis_pipe.io.in_axis_tdata := io.in_axis_tdata
  svo_axis_pipe.io.in_axis_tuser := io.in_axis_tuser

  io.out_axis_tvalid := svo_axis_pipe.io.out_axis_tvalid
  svo_axis_pipe.io.out_axis_tready := io.out_axis_tready
  io.out_axis_tdata := svo_axis_pipe.io.out_axis_tdata
  io.out_axis_tuser := svo_axis_pipe.io.out_axis_tuser

  pipe_in_tdata := svo_axis_pipe.io.pipe_in_tdata
  svo_axis_pipe.io.pipe_out_tdata := pipe_out_tdata
  pipe_in_tuser := svo_axis_pipe.io.pipe_in_tuser
  svo_axis_pipe.io.pipe_out_tuser := pipe_out_tuser
  pipe_in_tvalid := svo_axis_pipe.io.pipe_in_tvalid
  svo_axis_pipe.io.pipe_out_tvalid := pipe_out_tvalid
  pipe_enable := svo_axis_pipe.io.pipe_enable
  } //withClockAndReset(clk, ~resetn)
}


// ----------------------------------------------------------------------
// module svo_dim
//
// this core dims the video data (half each r/g/b sample value) when
// the enable input is high. it is also a nice demo of how to create
// simple pipelines that integrate with axi4 streams.
// ----------------------------------------------------------------------

class svo_dim(
    val vp: VideoParams = VideoConsts.m800x600.params,
    val sp: SvoParams = SvoConsts.DefaultParams
  ) extends RawModule {
val io = IO(new Bundle {
  val clk = Input(Clock())
  val resetn = Input(Bool())
  val enable = Input(Bool())

	// input stream
  //   tuser[0] ... start of frame
  val in_axis_tvalid = Input(Bool())
  val in_axis_tready = Output(Bool())
  val in_axis_tdata = Input(UInt(sp.SVO_BITS_PER_PIXEL.W))
  val in_axis_tuser = Input(UInt(1.W))

	// output stream
  //   tuser[0] ... start of frame
  val out_axis_tvalid = Output(Bool())
  val out_axis_tready = Input(Bool())
  val out_axis_tdata = Output(UInt(sp.SVO_BITS_PER_PIXEL.W))
  val out_axis_tuser = Output(UInt(1.W))
})

  withClockAndReset(io.clk, ~io.resetn) {
  val pipe_in_tdata = Wire(UInt(sp.SVO_BITS_PER_PIXEL.W))
  val pipe_out_tdata = Reg(UInt(sp.SVO_BITS_PER_PIXEL.W))
  val pipe_in_tuser = Wire(Bool()) 
  val pipe_out_tuser = Reg(Bool()) 
  val pipe_in_tvalid = Wire(Bool()) 
  val pipe_out_tvalid = RegInit(false.B)
  val pipe_enable = Wire(Bool()) 

  def idx_mux(c: Boolean, a: Int, b: Int) = if (c) a else b
  def svo_max(a: Int, b: Int) = idx_mux(a > b, a, b) //if (a > b) a else b

  //function [sp.SVO_BITS_PER_RED-1:0] svo_r;
  def svo_r(rgba: UInt): UInt = {
    //val svo_r = Wire(UInt(sp.SVO_BITS_PER_RED.W))
    //val rgba = IO(Input(Vec(sp.SVO_BITS_PER_PIXEL, Bool())))
    rgba(0+(sp.SVO_BITS_PER_RED-1),0).asTypeOf(UInt(sp.SVO_BITS_PER_RED.W))
  }
  //endfunction

  //function [SVO_BITS_PER_RED-1:0] svo_g;
  def svo_g(rgba: UInt): UInt = {
    //val svo_g = Wire(UInt(SVO_BITS_PER_RED.W))
    //val rgba = IO(Input(Vec(SVO_BITS_PER_PIXEL, Bool())))
    rgba(sp.SVO_BITS_PER_RED+(sp.SVO_BITS_PER_GREEN-1),sp.SVO_BITS_PER_RED).asTypeOf(UInt(sp.SVO_BITS_PER_RED.W))
  }
  //endfunction

  //function [SVO_BITS_PER_RED-1:0] svo_b;
  def svo_b(rgba: UInt): UInt = {
    //val svo_b = Wire(UInt(SVO_BITS_PER_RED.W))
    //val rgba = IO(Input(Vec(SVO_BITS_PER_PIXEL, Bool())))
    rgba((sp.SVO_BITS_PER_RED+sp.SVO_BITS_PER_GREEN)+(sp.SVO_BITS_PER_BLUE-1),sp.SVO_BITS_PER_RED+sp.SVO_BITS_PER_GREEN).asTypeOf(UInt(sp.SVO_BITS_PER_RED.W))
  }
  //endfunction

  //function [SVO_BITS_PER_RED-1:0] svo_a;
  def svo_a(rgba: UInt): UInt = {
    //val svo_a = Wire(UInt(SVO_BITS_PER_RED.W))
    //val rgba = IO(Input(Vec(SVO_BITS_PER_PIXEL, Bool())))
    rgba(
      idx_mux((sp.SVO_BITS_PER_ALPHA > 0), (sp.SVO_BITS_PER_RED+sp.SVO_BITS_PER_GREEN)+sp.SVO_BITS_PER_BLUE, 0)+(svo_max(sp.SVO_BITS_PER_ALPHA, 1)-1),
      idx_mux((sp.SVO_BITS_PER_ALPHA > 0), (sp.SVO_BITS_PER_RED+sp.SVO_BITS_PER_GREEN)+sp.SVO_BITS_PER_BLUE, 0)
    ).asTypeOf(UInt(sp.SVO_BITS_PER_RED.W))
  }
  //endfunction

  //function [SVO_BITS_PER_PIXEL-1:0] svo_rgba;
  def svo_rgba(r: UInt, g: UInt, b: UInt, a: UInt): UInt = {
    //val svo_rgba = Wire(UInt(SVO_BITS_PER_PIXEL.W))
    //val r = IO(Input(UInt(SVO_BITS_PER_RED.W)))
    //val g = IO(Input(UInt(SVO_BITS_PER_GREEN.W)))
    //val b = IO(Input(UInt(SVO_BITS_PER_BLUE.W)))
    //val a = IO(Input(UInt((SVO_BITS_PER_ALPHA.S.asTypeOf(???)).W)))
    Cat(a(sp.SVO_BITS_PER_ALPHA-1,0), b(sp.SVO_BITS_PER_BLUE-1,0), g(sp.SVO_BITS_PER_GREEN-1,0), r(sp.SVO_BITS_PER_RED-1,0))
  }
  //endfunction

  when (pipe_enable) {
    pipe_out_tdata := Mux(io.enable, svo_rgba(svo_r(pipe_in_tdata) >> 1, svo_g(pipe_in_tdata) >> 1, svo_b(pipe_in_tdata) >> 1, svo_a(pipe_in_tdata)), pipe_in_tdata)
    pipe_out_tuser := pipe_in_tuser
    pipe_out_tvalid := pipe_in_tvalid
  }

  val svo_axis_pipe = Module(new svo_axis_pipe(
      TDATA_WIDTH = sp.SVO_BITS_PER_PIXEL,
      TUSER_WIDTH = 1
  ))
  svo_axis_pipe.io.clk := io.clk
  svo_axis_pipe.io.resetn := io.resetn

  svo_axis_pipe.io.in_axis_tvalid := io.in_axis_tvalid
  io.in_axis_tready := svo_axis_pipe.io.in_axis_tready
  svo_axis_pipe.io.in_axis_tdata := io.in_axis_tdata
  svo_axis_pipe.io.in_axis_tuser := io.in_axis_tuser

  io.out_axis_tvalid := svo_axis_pipe.io.out_axis_tvalid
  svo_axis_pipe.io.out_axis_tready := io.out_axis_tready
  io.out_axis_tdata := svo_axis_pipe.io.out_axis_tdata
  io.out_axis_tuser := svo_axis_pipe.io.out_axis_tuser

  pipe_in_tdata := svo_axis_pipe.io.pipe_in_tdata
  svo_axis_pipe.io.pipe_out_tdata := pipe_out_tdata
  pipe_in_tuser := svo_axis_pipe.io.pipe_in_tuser.asTypeOf(pipe_in_tuser)
  svo_axis_pipe.io.pipe_out_tuser := pipe_out_tuser
  pipe_in_tvalid := svo_axis_pipe.io.pipe_in_tvalid
  svo_axis_pipe.io.pipe_out_tvalid := pipe_out_tvalid
  pipe_enable := svo_axis_pipe.io.pipe_enable
  } //withClockAndReset(clk, ~resetn)
}


// ----------------------------------------------------------------------
// module svo_overlay
//
// overlay one video stream ontop of another one
// ----------------------------------------------------------------------

class svo_overlay(
    val vp: VideoParams = VideoConsts.m800x600.params,
    val sp: SvoParams = SvoConsts.DefaultParams
  ) extends RawModule {
val io = IO(new Bundle {
  val clk = Input(Clock())
  val resetn = Input(Bool())
  val enable = Input(Bool())

	// input stream
  //   tuser[0] ... start of frame
  val in_axis_tvalid = Input(Bool())
  val in_axis_tready = Output(Bool())
  val in_axis_tdata = Input(UInt(sp.SVO_BITS_PER_PIXEL.W))
  val in_axis_tuser = Input(UInt(1.W))

	// overlay stream
  //   tuser[0] ... start of frame
  //   tuser[1] ... use overlay pixel
  val over_axis_tvalid = Input(Bool())
  val over_axis_tready = Output(Bool())
  val over_axis_tdata = Input(UInt(sp.SVO_BITS_PER_PIXEL.W))
  val over_axis_tuser = Input(UInt(2.W))

	// output stream
  //   tuser[0] ... start of frame
  val out_axis_tvalid = Output(Bool())
  val out_axis_tready = Input(Bool())
  val out_axis_tdata = Output(UInt(sp.SVO_BITS_PER_PIXEL.W))
  val out_axis_tuser = Output(UInt(1.W))
})

  val buf_in_axis_tvalid = Wire(Bool()) 
  val buf_in_axis_tready = Wire(Bool()) 
  val buf_in_axis_tdata = Wire(UInt(sp.SVO_BITS_PER_PIXEL.W))
  val buf_in_axis_tuser = Wire(UInt(1.W)) 

  val buf_over_axis_tvalid = Wire(Bool()) 
  val buf_over_axis_tready = Wire(Bool()) 
  val buf_over_axis_tdata = Wire(UInt(sp.SVO_BITS_PER_PIXEL.W))
  val buf_over_axis_tuser = Wire(Vec(2, Bool())) 

  val buf_out_axis_tvalid = Wire(Bool()) 
  val buf_out_axis_tready = Wire(Bool()) 
  val buf_out_axis_tdata = Wire(UInt(sp.SVO_BITS_PER_PIXEL.W))
  val buf_out_axis_tuser = Wire(UInt(1.W)) 

	// -------------------------------------------------------------------

  val active = WireDefault(Bool(), buf_in_axis_tvalid && buf_over_axis_tvalid) 
  val skip_in = WireDefault(Bool(), ( !buf_in_axis_tuser(0)) && buf_over_axis_tuser(0)) 
  val skip_over = WireDefault(Bool(), buf_in_axis_tuser(0) && ( !buf_over_axis_tuser(0))) 
  buf_in_axis_tready := active && (skip_in || (( !skip_over) && buf_out_axis_tready))
  buf_over_axis_tready := active && (skip_over || (( !skip_in) && buf_out_axis_tready))
  buf_out_axis_tvalid := (active && ( !skip_in)) && ( !skip_over)
  buf_out_axis_tdata := Mux(io.enable && buf_over_axis_tuser(1), buf_over_axis_tdata, buf_in_axis_tdata)
  buf_out_axis_tuser := Mux(io.enable && buf_over_axis_tuser(1), buf_over_axis_tuser.asTypeOf(UInt(1.W)), buf_in_axis_tuser)

	// -------------------------------------------------------------------

  val svo_buf_in = Module(new svo_buf(
    vp = vp,
    sp = sp
  ))
  svo_buf_in.io.clk := io.clk
  svo_buf_in.io.resetn := io.resetn

  svo_buf_in.io.in_axis_tvalid := io.in_axis_tvalid
  io.in_axis_tready := svo_buf_in.io.in_axis_tready
  svo_buf_in.io.in_axis_tdata := io.in_axis_tdata
  svo_buf_in.io.in_axis_tuser := io.in_axis_tuser

  buf_in_axis_tvalid := svo_buf_in.io.out_axis_tvalid
  svo_buf_in.io.out_axis_tready := buf_in_axis_tready
  buf_in_axis_tdata := svo_buf_in.io.out_axis_tdata
  buf_in_axis_tuser := svo_buf_in.io.out_axis_tuser

  val svo_buf_over = Module(new svo_buf(
    vp = vp,
    sp = sp
  ))
  svo_buf_over.io.clk := io.clk
  svo_buf_over.io.resetn := io.resetn

  svo_buf_over.io.in_axis_tvalid := io.over_axis_tvalid
  io.over_axis_tready := svo_buf_over.io.in_axis_tready
  svo_buf_over.io.in_axis_tdata := io.over_axis_tdata
  svo_buf_over.io.in_axis_tuser := io.over_axis_tuser

  buf_over_axis_tvalid := svo_buf_over.io.out_axis_tvalid
  svo_buf_over.io.out_axis_tready := buf_over_axis_tready
  buf_over_axis_tdata := svo_buf_over.io.out_axis_tdata
  buf_over_axis_tuser := svo_buf_over.io.out_axis_tuser.asTypeOf(buf_over_axis_tuser)

  val svo_buf_out = Module(new svo_buf(
    vp = vp,
    sp = sp
  ))
  svo_buf_out.io.clk := io.clk
  svo_buf_out.io.resetn := io.resetn

  svo_buf_out.io.in_axis_tvalid := buf_out_axis_tvalid
  buf_out_axis_tready := svo_buf_out.io.in_axis_tready
  svo_buf_out.io.in_axis_tdata := buf_out_axis_tdata
  svo_buf_out.io.in_axis_tuser := buf_out_axis_tuser

  io.out_axis_tvalid := svo_buf_out.io.out_axis_tvalid
  svo_buf_out.io.out_axis_tready := io.out_axis_tready
  io.out_axis_tdata := svo_buf_out.io.out_axis_tdata
  io.out_axis_tuser := svo_buf_out.io.out_axis_tuser
}


// ----------------------------------------------------------------------
// module svo_rect
//
// this core creates a video stream that contains a white rectangle with
// black outline. two additional tuser output fields are used to signal
// which pixels do belong to the rectangle.
// ----------------------------------------------------------------------

class svo_rect(
    val vp: VideoParams = VideoConsts.m800x600.params,
    val sp: SvoParams = SvoConsts.DefaultParams
  ) extends RawModule {
val io = IO(new Bundle {
  val clk = Input(Clock())
  val resetn = Input(Bool())
  val x1 = Input(UInt(12.W))
  val y1 = Input(Bool())
  val x2 = Input(Bool())
  val y2 = Input(Bool())

	// output stream
  //   tuser[0] ... start of frame
  //   tuser[1] ... pixel in rectange
  //   tuser[2] ... pixel on rect. border
  val out_axis_tvalid = Output(Bool())
  val out_axis_tready = Input(Bool())
  val out_axis_tdata = Output(UInt(sp.SVO_BITS_PER_PIXEL.W))
  val out_axis_tuser = Output(UInt(3.W))
})

  withClockAndReset(io.clk, ~io.resetn) {
  // NOTE: The following statements are auto generated based on existing output reg of the original verilog source
  val out_axis_tvalid_out_reg = Reg(Bool()) 
  io.out_axis_tvalid := out_axis_tvalid_out_reg
  val out_axis_tdata_out_reg = Reg(UInt(sp.SVO_BITS_PER_PIXEL.W))
  io.out_axis_tdata := out_axis_tdata_out_reg
  val out_axis_tuser_out_reg = Reg(Vec(3, Bool())) 
  io.out_axis_tuser := out_axis_tuser_out_reg.asUInt

  val x = RegInit(0.U(sp.SVO_XYBITS.W))
  val y = RegInit(0.U(sp.SVO_XYBITS.W))
  val on_x = Wire(Bool()) 
  val on_y = Wire(Bool()) 
  val in_x = RegInit(false.B)
  val in_y = RegInit(false.B)
  val border = Wire(Bool()) 

    when(out_axis_tvalid_out_reg && io.out_axis_tready) {
      when(x === (vp.H_DISPLAY-1).U) {
        x := 0.U
        y := Mux(y === (vp.V_DISPLAY-1).U, 0.U, y+1.U)
      } .otherwise {
        x := x+1.U
      }
    }

    when(x === io.x1) {
      in_x := true.B
    }
    when((y =/= 0.U) === io.y1) {
      in_y := true.B
    }

    on_x := (x === io.x1) || ((x =/= 0.U) === io.x2)
    on_y := ((y =/= 0.U) === io.y1) || ((y =/= 0.U) === io.y2)
    border := (in_x && in_y) && (on_x || on_y)

    out_axis_tvalid_out_reg := true.B
    out_axis_tdata_out_reg := (VecInit.tabulate(sp.SVO_BITS_PER_PIXEL)(_ =>  ~border)).asUInt
    out_axis_tuser_out_reg(0) := ( !(x =/= 0.U)) && ( !(y =/= 0.U))
    out_axis_tuser_out_reg(1) := in_x && in_y
    out_axis_tuser_out_reg(2) := border

    when((x =/= 0.U) === io.x2) {
      in_x := false.B
    }
    when(((y =/= 0.U) === io.y2) && ((x =/= 0.U) === io.x2)) {
      in_y := false.B
    }
  } //withClockAndReset(clk, ~resetn)
}
