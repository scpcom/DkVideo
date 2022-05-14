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

class svo_tcard(
    val vp: VideoParams = VideoConsts.m800x600.params,
    val sp: SvoParams = SvoConsts.DefaultParams
  ) extends RawModule {
val io = IO(new Bundle {
  val clk = Input(Clock())
  val resetn = Input(Bool())

  // output stream
  //   tuser[0] ... start of frame
  val out_axis_tvalid = Output(Bool())
  val out_axis_tready = Input(Bool())
  val out_axis_tdata = Output(UInt(sp.SVO_BITS_PER_PIXEL.W))
  val out_axis_tuser = Output(UInt(1.W))
})

  val HOFFSET = ((32-(vp.H_DISPLAY%32))%32)/2
  val VOFFSET = ((32-(vp.V_DISPLAY%32))%32)/2

  val HOR_CELLS = (vp.H_DISPLAY+31)/32
  val VER_CELLS = (vp.V_DISPLAY+31)/32

  val BAR_W = ((HOR_CELLS-8)-(HOR_CELLS%2))/2

  val X1 = 2.U
  val X2 = (2+BAR_W).U
  val X3 = ((HOR_CELLS-4)-BAR_W).U
  val X4 = (HOR_CELLS-4).U

  withClockAndReset(io.clk, ~io.resetn) {
  val out_axis_tvalid_out_reg = RegInit(false.B)
  io.out_axis_tvalid := out_axis_tvalid_out_reg
  val out_axis_tdata_out_reg = RegInit(0.U(sp.SVO_BITS_PER_PIXEL.W))
  io.out_axis_tdata := out_axis_tdata_out_reg
  val out_axis_tuser_out_reg = RegInit(0.U(1.W))
  io.out_axis_tuser := out_axis_tuser_out_reg

  def best_y_params(n: UInt, which: UInt): UInt = {
  val out_y_params = Wire(UInt(4.W))
  val best_y_blk = Wire(UInt(4.W))
  val best_y_off = Wire(UInt(4.W))
  val best_y_gap = Wire(UInt(4.W))

  best_y_blk := 0.U
  best_y_gap := 0.U
  best_y_off := 0.U

  when(vp.V_DISPLAY.U === 480.U) {
    best_y_blk := 3.U
    best_y_gap := 1.U
    best_y_off := 1.U
  }

  when(vp.V_DISPLAY.U === 600.U) {
    best_y_blk := 3.U
    best_y_gap := 2.U
    best_y_off := 2.U
  }

  when(vp.V_DISPLAY.U === 720.U) {
    best_y_blk := 4.U
    best_y_gap := 2.U
    best_y_off := 2.U
  }

  when(vp.V_DISPLAY.U === 768.U) {
    best_y_blk := 4.U
    best_y_gap := 3.U
    best_y_off := 2.U
  }

  when(vp.V_DISPLAY.U === 1080.U) {
    best_y_blk := 6.U
    best_y_gap := 2.U
    best_y_off := 5.U
  }

  when(which === 1.U) {
    out_y_params := best_y_blk
  }
  .elsewhen(which === 2.U) {
    out_y_params := best_y_gap
  }
  .otherwise {
    out_y_params := best_y_off
  }
  out_y_params
  }

  val Y_BLK = best_y_params(VER_CELLS.U, 1.U)
  val Y_GAP = best_y_params(VER_CELLS.U, 2.U)
  val Y_OFF = best_y_params(VER_CELLS.U, 3.U)

  val Y1 = ((0.U*Y_BLK)+(0.U*Y_GAP))+Y_OFF
  val Y2 = ((1.U*Y_BLK)+(0.U*Y_GAP))+Y_OFF
  val Y3 = ((1.U*Y_BLK)+(1.U*Y_GAP))+Y_OFF
  val Y4 = ((2.U*Y_BLK)+(1.U*Y_GAP))+Y_OFF
  val Y5 = ((2.U*Y_BLK)+(2.U*Y_GAP))+Y_OFF
  val Y6 = ((3.U*Y_BLK)+(2.U*Y_GAP))+Y_OFF

  val hcursor = RegInit(0.U(sp.SVO_XYBITS.W))
  val vcursor = RegInit(0.U(sp.SVO_XYBITS.W))

  val x = RegInit(0.U(((sp.SVO_XYBITS-6)+1).W))
  val y = RegInit(0.U(((sp.SVO_XYBITS-6)+1).W))
  val xoff = RegInit(HOFFSET.U(5.W))
  val yoff = RegInit(VOFFSET.U(5.W))

  val rng = RegInit(0.U(32.W))
  val rna = RegInit(0.U(32.W))
  val rnb = RegInit(0.U(32.W))
  val rnc = RegInit(0.U(32.W))
  val r = RegInit(0.U(sp.SVO_BITS_PER_RED.W))
  val g = RegInit(0.U(sp.SVO_BITS_PER_GREEN.W))
  val b = RegInit(0.U(sp.SVO_BITS_PER_BLUE.W))

  val bolt_bitmap = WireDefault(Vec(32*32, Bool()),
      Cat("b00000000000000000000000000000000".U(32.W),
          "b01111111000000000000000001111111".U(32.W),
          "b01111100000000000000000000011111".U(32.W),
          "b01110000000000000000000000000111".U(32.W),
          "b01100000000000000000000000000011".U(32.W),
          "b01100000000000000000000000000011".U(32.W),
          "b01000000000000000000000000000001".U(32.W),
          "b01000000000000000000000000000001".U(32.W),
          "b00000000000000000000000000000000".U(32.W),
          "b00000000000000000000000000000000".U(32.W),
          "b00000000000000000000000000000000".U(32.W),
          "b00000000000000000000000000000000".U(32.W),
          "b00000000000000111100000000000000".U(32.W),
          "b00000000000001111110000000000000".U(32.W),
          "b00000000000011111111000000000000".U(32.W),
          "b00000000000011111111000000000000".U(32.W),
          "b00000000000011111111000000000000".U(32.W),
          "b00000000000011111111000000000000".U(32.W),
          "b00000000000001111110000000000000".U(32.W),
          "b00000000000000111100000000000000".U(32.W),
          "b00000000000000000000000000000000".U(32.W),
          "b00000000000000000000000000000000".U(32.W),
          "b00000000000000000000000000000000".U(32.W),
          "b00000000000000000000000000000000".U(32.W),
          "b00000000000000000000000000000000".U(32.W),
          "b01000000000000000000000000000001".U(32.W),
          "b01000000000000000000000000000001".U(32.W),
          "b01100000000000000000000000000011".U(32.W),
          "b01100000000000000000000000000011".U(32.W),
          "b01110000000000000000000000000111".U(32.W),
          "b01111100000000000000000000011111".U(32.W),
          "b01111111000000000000000001111111".U(32.W)).asTypeOf(Vec(1024, Bool())))

  def xorrng(a: UInt, b: UInt): UInt = {
    val va = VecInit(a.asBools)
    val vb = VecInit(b.asBools)
    val vo = VecInit((~0.U(32.W)).asBools)
    for(i <- 0 until 32){
      vo(i) := va(i) ^ vb(i)
    }
    vo.asUInt
  }

  when (( !out_axis_tvalid_out_reg) || io.out_axis_tready) {
    when(hcursor === 0.U) {
      rna := xorrng(0.U((32-(((sp.SVO_XYBITS-6)+1))).W) ## y, 123456789.U(32.W))
    }
    .otherwise {
      rna := rng
    }

    rnb := xorrng(rna, (rna << 13))
    rnc := xorrng(rnb, 0.U(17.W) ## (rnb >> 17))
    rng := xorrng(rnc, (rnc << 5))

    when(( !(xoff =/= 0.U)) || (hcursor === 0.U)) {
      when(rng(8,0) === 0.U) {
        r := 32.U
        g := 32.U
        b := 32.U
      }
      .otherwise {
        r := ((16.U*rng(0))+(16.U*rng(1)))+(31.U*rng(2))
        g := ((16.U*rng(3))+(16.U*rng(4)))+(31.U*rng(5))
        b := ((16.U*rng(6))+(16.U*rng(7)))+(31.U*rng(8))
      }
    }

    when((xoff.andR()) || (yoff.andR())) {
      r := 0.U
      g := 0.U
      b := 0.U
    }

    when(vp.V_DISPLAY.U >= 480.U) {
      when((((X1 < x) && (x <= X2)) && (Y1 < y)) && (y <= Y2)) {
        r := 63.U
        g := 0.U
        b := 0.U
      }

      when((((X1 < x) && (x <= X2)) && (Y3 < y)) && (y <= Y4)) {
        r := 0.U
        g := 63.U
        b := 0.U
      }

      when((((X1 < x) && (x <= X2)) && (Y5 < y)) && (y <= Y6)) {
        r := 0.U
        g := 0.U
        b := 63.U
      }

      when((((X3 < x) && (x <= X4)) && (Y1 < y)) && (y <= Y2)) {
        r := 0.U
        g := 63.U
        b := 63.U
      }

      when((((X3 < x) && (x <= X4)) && (Y3 < y)) && (y <= Y4)) {
        r := 63.U
        g := 0.U
        b := 63.U
      }

      when((((X3 < x) && (x <= X4)) && (Y5 < y)) && (y <= Y6)) {
        r := 63.U
        g := 63.U
        b := 0.U
      }

      when((xoff.andR()) && ((x === X2) || (x === X4))) {
        r := 0.U
        g := 0.U
        b := 0.U
      }

      when((yoff.andR()) && (((y === Y2) || (y === Y4)) || (y === Y6))) {
        r := 0.U
        g := 0.U
        b := 0.U
      }
    }

    out_axis_tvalid_out_reg := true.B
    when(((x === 1.U) || (x === (HOR_CELLS-2).U)) && ((y === 1.U) || (y === (VER_CELLS-2).U))) {
      out_axis_tdata_out_reg := Mux(bolt_bitmap(Cat(yoff, xoff)),  ~0.U(sp.SVO_BITS_PER_PIXEL.W), 0.U)
    } .otherwise {
      out_axis_tdata_out_reg := Cat(b, g, r)
    }
    out_axis_tuser_out_reg := ( !(hcursor =/= 0.U)) && ( !(vcursor =/= 0.U))

    when(hcursor === (vp.H_DISPLAY-1).U) {
      hcursor := 0.U
      x := 0.U
      xoff := HOFFSET.U(5.W)
      when(vcursor === (vp.V_DISPLAY-1).U) {
        vcursor := 0.U
        y := 0.U
        yoff := VOFFSET.U(5.W)
      } .otherwise {
        vcursor := vcursor+1.U
        when(yoff.andR()) {
          y := y+1.U
        }
        yoff := yoff+1.U
      }
    } .otherwise {
      hcursor := hcursor+1.U
      when(xoff.andR()) {
        x := x+1.U
      }
      xoff := xoff+1.U
    }
  }
  } //withClockAndReset(clk, ~resetn)
}
