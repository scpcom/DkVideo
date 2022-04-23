package dkvideo

import chisel3._
import chisel3.util.Cat
import hdmicore.{VideoHdmi}
import hdmicore.video.{VideoParams, HVSync}

// ---------------------------------------------------------------------
// File name         : testpattern.v
// Module name       : testpattern
// Created by        : Caojie
// Module Description:
//						I_mode[2:0] = "000" : color bar
//						I_mode[2:0] = "001" : net grid
//						I_mode[2:0] = "010" : gray
//						I_mode[2:0] = "011" : single color
// ---------------------------------------------------------------------
// Release history
// VERSION |   Date      | AUTHOR  |    DESCRIPTION
// --------------------------------------------------------------------
//   1.0   | 24-Sep-2009 | Caojie  |    initial
// --------------------------------------------------------------------

class testpattern(vp: VideoParams) extends RawModule {
  val io = IO(new Bundle {
    val I_pxl_clk = Input(Clock()) //pixel clock
    val I_rst_n = Input(Bool()) //low active
    val I_mode = Input(UInt(3.W)) //data select
    val I_single_r = Input(UInt(8.W))
    val I_single_g = Input(UInt(8.W))
    val I_single_b = Input(UInt(8.W))
    val I_rd_hres = Input(UInt(12.W)) //hor resolution
    val I_rd_vres = Input(UInt(12.W)) //ver resolution
    val I_hs_pol = Input(Bool()) //HS polarity , 0:�����ԣ�1��������
    val I_vs_pol = Input(Bool()) //VS polarity , 0:�����ԣ�1��������
    val videoSig = Output(new VideoHdmi())
  })

  withClockAndReset(io.I_pxl_clk, ~io.I_rst_n) {
 //====================================================
  val N = 3 //delay N clocks

  val WHITE   = Cat(255.U(8.W), 255.U(8.W), 255.U(8.W)) //{B,G,R}
  val YELLOW  = Cat(0.U(8.W),   255.U(8.W), 255.U(8.W))
  val CYAN    = Cat(255.U(8.W), 255.U(8.W), 0.U(8.W)  )
  val GREEN   = Cat(0.U(8.W),   255.U(8.W), 0.U(8.W)  )
  val MAGENTA = Cat(255.U(8.W), 0.U(8.W),   255.U(8.W))
  val RED     = Cat(0.U(8.W),   0.U(8.W),   255.U(8.W))
  val BLUE    = Cat(255.U(8.W), 0.U(8.W),   0.U(8.W)  )
  val BLACK   = Cat(0.U(8.W),   0.U(8.W),   0.U(8.W)  )

  //====================================================
  val Pout_de_w = Reg(Bool())
  val Pout_hs_w = Wire(Bool())
  val Pout_vs_w = Wire(Bool())

  val Pout_de_dn = RegInit(0.U(N.W))

  //-------------------------
  //Color bar //8ɫ����
  val Color_trig_num = RegInit(0.U(12.W))
  val Color_trig = RegInit(false.B)
  val Color_cnt = RegInit(0.U(4.W))
  val Color_bar = RegInit(0.U(24.W))

  //----------------------------
  //Net grid //32����
  val Net_h_trig = RegInit(false.B)
  val Net_v_trig = RegInit(false.B)
  val Net_pos = Wire(UInt(2.W))
  val Net_grid = RegInit(0.U(24.W))

  //----------------------------
  //Gray  //�ڰ׻ҽ�
  val Gray = RegInit(0.U(24.W))
  val Gray_d1 = RegInit(0.U(24.W))

  //-----------------------------
  val Single_color = Wire(UInt(24.W))

  //-------------------------------
  val Data_sel = Wire(UInt(24.W))

  //-------------------------------
  val Data_tmp = RegInit(0.U(24.W))  /*synthesis syn_keep=1*/

  //==============================================================================
  //Generate HS, VS, DE signals

  val tp_sync = Module(new HVSync(vp))
  Pout_de_w := (tp_sync.io.hpos < io.I_rd_hres) && (tp_sync.io.vpos < io.I_rd_vres)
  Pout_hs_w := tp_sync.io.hsync
  Pout_vs_w := tp_sync.io.vsync

  //-------------------------------------------------------------

  Pout_de_dn := RegNext(RegNext(Pout_de_w)) ## RegNext(Pout_de_w) ## Pout_de_w

  //=================================================================================
  //Test Pattern
  val De_delay = (N).U(3.W)
  val De_hcnt = tp_sync.io.hpos
  val De_vcnt = tp_sync.io.vpos

  //---------------------------------------------------
  //Color bar
  //---------------------------------------------------

  when (Pout_de_dn(1) === false.B) {
    Color_trig_num := io.I_rd_hres(11,3) //8ɫ�������
  } .elsewhen ((Color_trig === true.B) && (Pout_de_dn(1) === true.B)) {
    Color_trig_num := Color_trig_num+io.I_rd_hres(11,3)
  } .otherwise {
    Color_trig_num := Color_trig_num
  }
  when (De_hcnt === (Color_trig_num-"b1".U(1.W))) {
    Color_trig := true.B
  } .otherwise {
    Color_trig := false.B
  }
  when (Pout_de_dn(1) === false.B) {
    Color_cnt := 0.U(3.W)
  } .elsewhen ((Color_trig === true.B) && (Pout_de_dn(1) === true.B)) {
    Color_cnt := Color_cnt+"b1".U(1.W)
  } .otherwise {
    Color_cnt := Color_cnt
  }
  when (Pout_de_dn(2) === true.B) {
    when(Color_cnt === 0.U(3.W)) {
      Color_bar := WHITE
    } .elsewhen (Color_cnt === 1.U(3.W)) {
      Color_bar := YELLOW
    } .elsewhen (Color_cnt === 2.U(3.W)) {
      Color_bar := CYAN
    } .elsewhen (Color_cnt === 3.U(3.W)) {
      Color_bar := GREEN
    } .elsewhen (Color_cnt === 4.U(3.W)) {
      Color_bar := MAGENTA
    } .elsewhen (Color_cnt === 5.U(3.W)) {
      Color_bar := RED
    } .elsewhen (Color_cnt === 6.U(3.W)) {
      Color_bar := BLUE
    } .elsewhen (Color_cnt === 7.U(3.W)) {
      Color_bar := BLACK
    } .otherwise {
      Color_bar := BLACK
    }
  } .otherwise {
    Color_bar := BLACK
  }

  //---------------------------------------------------
  //Net grid
  //---------------------------------------------------

  when (((De_hcnt(4,0) === De_delay) || (De_hcnt >= (io.I_rd_hres-"b1".U(1.W)-De_delay))) && (Pout_de_dn(1) === true.B)) {
    Net_h_trig := true.B
  } .otherwise {
    Net_h_trig := false.B
  }
  when (((De_vcnt(4,0) === 0.U(5.W)) || (De_vcnt === (io.I_rd_vres-"b1".U(1.W)))) && (Pout_de_dn(1) === true.B)) {
    Net_v_trig := true.B
  } .otherwise {
    Net_v_trig := false.B
  }
  Net_pos := Cat(Net_v_trig, Net_h_trig)
  when (Pout_de_dn(2) === true.B) {
    when(Net_pos === "b00".U(2.W)) {
      Net_grid := BLACK
    } .elsewhen (Net_pos === "b01".U(2.W)) {
      Net_grid := RED
    } .elsewhen (Net_pos === "b10".U(2.W)) {
      Net_grid := RED
    } .elsewhen (Net_pos === "b11".U(2.W)) {
      Net_grid := RED
    } .otherwise {
      Net_grid := BLACK
    }
  } .otherwise {
    Net_grid := BLACK
  }

  //---------------------------------------------------
  //Gray
  //---------------------------------------------------

  val Gray_ch = Mux(tp_sync.io.hpos < io.I_rd_hres, De_hcnt(7,0), 0.U(8.W))
  Gray := Cat(Gray_ch, Gray_ch, Gray_ch)
  Gray_d1 := Gray

  //---------------------------------------------------
  //Single color
  //---------------------------------------------------
  Single_color := Cat(io.I_single_b, io.I_single_g, io.I_single_r)

  //============================================================
  Data_sel := Mux((io.I_mode(2,0) === "b000".U(3.W)), Color_bar,
              Mux((io.I_mode(2,0) === "b001".U(3.W)), Net_grid,
              Mux((io.I_mode(2,0) === "b010".U(3.W)), Gray_d1,
              Mux((io.I_mode(2,0) === "b011".U(3.W)), Single_color, GREEN))))

  //---------------------------------------------------

  Data_tmp := Data_sel

  io.videoSig.de := Pout_de_w
  io.videoSig.hsync := Mux(io.I_hs_pol, ~Pout_hs_w, Pout_hs_w)
  io.videoSig.vsync := Mux(io.I_vs_pol, ~Pout_vs_w, Pout_vs_w)
  io.videoSig.pixel.red   := Mux(Pout_de_w, Data_tmp(7,0), "h00".U(8.W))
  io.videoSig.pixel.green := Mux(Pout_de_w, Data_tmp(15,8), "h00".U(8.W))
  io.videoSig.pixel.blue  := Mux(Pout_de_w, Data_tmp(23,16), "hff".U(8.W))
  } // withClockAndReset(io.I_pxl_clk, ~io.I_rst_n)
}
