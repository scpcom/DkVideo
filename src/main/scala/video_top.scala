package dkvideo

import chisel3._
import chisel3.util.Cat
import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}

import fpgamacro.gowin.{CLKDIV, TLVDS_OBUF}
import fpgamacro.gowin.{Oser10Module}
import fpgamacro.gowin.{TMDS_PLLVR,GW_PLLVR}
import hdmicore.video.{VideoParams, HVSync}
import hdmicore.{Rgb2Tmds, TMDSDiff, DiffPair}
import dkvideo.video.{VideoMode, VideoConsts}
import hdl.dvi_tx.DVI_TX_Top
import hdl.video_frame_buffer.Video_Frame_Buffer_Top
import hdl.hyperram_memory_interface.HyperRAM_Memory_Interface_Top
import ov2640.OV2640_Controller

// ==============0ooo===================================================0ooo===========
// =  Copyright (C) 2014-2020 Gowin Semiconductor Technology Co.,Ltd.
// =                     All rights reserved.
// ====================================================================================
//
//  __      __      __
//  \ \    /  \    / /   [File name   ] video_top.v
//   \ \  / /\ \  / /    [Description ] Video demo
//    \ \/ /  \ \/ /     [Timestamp   ] Friday May 26 14:00:30 2019
//     \  /    \  /      [version     ] 1.0.0
//      \/      \/
//
// ==============0ooo===================================================0ooo===========
// Code Revision History :
// ----------------------------------------------------------------------------------
// Ver:    |  Author    | Mod. Date    | Changes Made:
// ----------------------------------------------------------------------------------
// V1.0    | Caojie     | 11/22/19     | Initial version
// ----------------------------------------------------------------------------------
// ==============0ooo===================================================0ooo===========

class video_top(gowinDviTx: Boolean = true, rd_width: Int = 800, rd_height: Int = 600, rd_halign: Int = 0, rd_valign: Int = 0, vmode: VideoMode = VideoConsts.m1280x720) extends RawModule {
  val I_clk = IO(Input(Clock())) //27Mhz
  val I_rst_n = IO(Input(Bool()))
  val O_led = IO(Output(UInt(2.W)))
  val SDA = IO(Output(Bool())) // Inout
  val SCL = IO(Output(Bool())) // Inout
  val VSYNC = IO(Input(Bool()))
  val HREF = IO(Input(Bool()))
  val PIXDATA = IO(Input(UInt(10.W)))
  val PIXCLK = IO(Input(Clock()))
  val XCLK = IO(Output(Clock()))
  val O_hpram_ck = IO(Output(UInt(1.W)))
  val O_hpram_ck_n = IO(Output(UInt(1.W)))
  val O_hpram_cs_n = IO(Output(UInt(1.W)))
  val O_hpram_reset_n = IO(Output(UInt(1.W)))
  val IO_hpram_dq = IO(Input(UInt(8.W))) // Inout
  val IO_hpram_rwds = IO(Input(UInt(1.W))) // Inout
  val O_tmds = IO(Output(new TMDSDiff()))

  //==================================================
  val vp = vmode.params
  val vp_H_TOTAL = vp.H_DISPLAY+vp.H_FRONT+vp.H_SYNC+vp.H_BACK
  val vp_V_TOTAL = vp.V_DISPLAY+vp.V_TOP+vp.V_SYNC+vp.V_BOTTOM
  /* set val rd_vp = vp for full screen */
  val rd_vp = VideoParams(
      H_DISPLAY = rd_width, H_FRONT = vp.H_FRONT,
      H_SYNC = vp.H_SYNC, H_BACK = vp.H_BACK,
      V_SYNC = vp.V_SYNC,  V_BACK = vp.V_BACK,
      V_TOP = vp.V_TOP, V_DISPLAY = rd_height,
      V_BOTTOM = vp.V_BOTTOM)
  val rd_hres = rd_vp.H_DISPLAY // 800
  val rd_vres = rd_vp.V_DISPLAY // 600
  val syn_hs_pol = 1   //HS polarity , 0:负极性，1：正极性
  val syn_vs_pol = 1   //VS polarity , 0:负极性，1：正极性

  val running = Wire(Bool())

  //--------------------------
  val tp0_vs_in = Wire(Bool())
  val tp0_hs_in = Wire(Bool())
  val tp0_de_in = Wire(Bool())
  val tp0_data_r = Wire(UInt(8.W))  /*synthesis syn_keep=1*/
  val tp0_data_g = Wire(UInt(8.W))  /*synthesis syn_keep=1*/
  val tp0_data_b = Wire(UInt(8.W))  /*synthesis syn_keep=1*/

  //--------------------------
  val cam_data = Wire(UInt(16.W))
  val cam_de_in = Wire(Bool())

  //-------------------------
  //frame buffer in
  val ch0_vfb_clk_in = Wire(Clock())
  val ch0_vfb_vs_in = Wire(Bool())
  val ch0_vfb_de_in = Wire(Bool())
  val ch0_vfb_data_in = Wire(UInt(16.W))

  //-------------------
  //syn_code
  val syn_off0_re = Wire(Bool())  // ofifo read enable signal
  val syn_off0_vs = Wire(Bool())
  val syn_off0_hs = Wire(Bool())

  val off0_syn_de = Wire(Bool())
  val off0_syn_data = Wire(UInt(16.W))

  //-------------------------------------
  //Hyperram
  val dma_clk = Wire(Clock())

  val memory_clk = Wire(Clock())
  val mem_pll_lock = Wire(Bool())

  //-------------------------------------------------
  //memory interface
  val cmd = Wire(Bool())
  val cmd_en = Wire(Bool())
  val addr = Wire(UInt(22.W))     //[ADDR_WIDTH-1:0]
  val wr_data = Wire(UInt(32.W))  //[DATA_WIDTH-1:0]
  val data_mask = Wire(UInt(4.W))
  val rd_data_valid = Wire(Bool())
  val rd_data = Wire(UInt(32.W))  //[DATA_WIDTH-1:0]
  val init_calib = Wire(Bool())

  //------------------------------------------
  //rgb data
  val rgb_vs = Wire(Bool())
  val rgb_hs = Wire(Bool())
  val rgb_de = Wire(Bool())
  val rgb_data = Wire(UInt(24.W))

  //------------------------------------
  //HDMI TX
  val serial_clk = Wire(Clock())
  val pll_lock = Wire(Bool())

  val hdmi_rst_n = Wire(Bool())

  val pix_clk = Wire(Clock())

  val clk_12M = Wire(Clock())

  val TMDS_PLLVR_inst = Module(new TMDS_PLLVR(vmode.pll))
  TMDS_PLLVR_inst.io.clkin := I_clk //input clk
  serial_clk := TMDS_PLLVR_inst.io.clkout //output clk
  clk_12M := TMDS_PLLVR_inst.io.clkoutd //output clkoutd
  pll_lock := TMDS_PLLVR_inst.io.lock //output lock
  hdmi_rst_n := I_rst_n & pll_lock

  val u_clkdiv = Module(new CLKDIV)
  u_clkdiv.io.RESETN := hdmi_rst_n
  u_clkdiv.io.HCLKIN := serial_clk //clk  x5
  pix_clk := u_clkdiv.io.CLKOUT //clk  x1
  u_clkdiv.io.CALIB := "b1".U(1.W)

  XCLK := clk_12M

  //============================================================================
  //I_clk

  val g_cnt_vs = Wire(UInt(10.W))
  val tp_pxl_clk = PIXCLK

  withClockAndReset(tp_pxl_clk, ~hdmi_rst_n) {
    val cnt_vs = RegInit(0.U(10.W))
    val run_cnt = RegInit(0.U(32.W))
    val vs_r = Reg(Bool())

    g_cnt_vs := cnt_vs

    //========================================================================
    //LED test
    when (run_cnt >= "d27_000_000".U(32.W)) {
      run_cnt := 0.U(32.W)
    } .otherwise {
      run_cnt := run_cnt+"b1".U(1.W)
    }
    running := (Mux((run_cnt < "d13_500_000".U(32.W)), "b1".U(1.W), "b0".U(1.W)) =/= 0.U)
    O_led := ~init_calib ## running

    //========================================================================
    //testpattern
    val testpattern_inst = Module(new testpattern(vp))
    testpattern_inst.io.I_pxl_clk := tp_pxl_clk //pixel clock
    testpattern_inst.io.I_rst_n := I_rst_n //low active
    testpattern_inst.io.I_mode := 0.U(1.W) ## cnt_vs(7,6) //data select
    testpattern_inst.io.I_single_r := 0.U(8.W)
    testpattern_inst.io.I_single_g := 255.U(8.W)
    testpattern_inst.io.I_single_b := 0.U(8.W)                             //800x600    //1024x768   //1280x720
    testpattern_inst.io.I_rd_hres := rd_hres.U(12.W)     //hor resolution  // 16'd800   // 16'd1024  // 16'd1280
    testpattern_inst.io.I_rd_vres := rd_vres.U(12.W)     //ver resolution  // 16'd600   // 16'd768   // 16'd720
    testpattern_inst.io.I_hs_pol := syn_hs_pol.U(1.W)    //HS polarity , 0:negetive ploarity，1：positive polarity
    testpattern_inst.io.I_vs_pol := syn_vs_pol.U(1.W)    //VS polarity , 0:negetive ploarity，1：positive polarity
    tp0_de_in := testpattern_inst.io.videoSig.de
    tp0_hs_in := testpattern_inst.io.videoSig.hsync
    tp0_vs_in := testpattern_inst.io.videoSig.vsync
    tp0_data_r := testpattern_inst.io.videoSig.pixel.red
    tp0_data_g := testpattern_inst.io.videoSig.pixel.green
    tp0_data_b := testpattern_inst.io.videoSig.pixel.blue
    vs_r := tp0_vs_in
    when (cnt_vs === "h3ff".U(10.W)) {
      cnt_vs := cnt_vs
    } .elsewhen (vs_r && ( !tp0_vs_in)) { //vs24 falling edge
      cnt_vs := cnt_vs+"b1".U(1.W)
    } .otherwise {
      cnt_vs := cnt_vs
    }
  } // withClockAndReset(tp_pxl_clk, ~I_rst_n)

  //============================================================================
  withClockAndReset(PIXCLK, ~hdmi_rst_n) {
    val pixdata_d1 = RegInit(0.U(10.W))
    val pixdata_d2 = RegInit(0.U(10.W))
    val hcnt = RegInit(false.B)
    val cam_mode = "h08".U(8.W) // 08:RGB565  04:RAW10

    val u_OV2640_Controller = Module(new OV2640_Controller(rd_vp))
    u_OV2640_Controller.clock := clk_12M
    u_OV2640_Controller.io.clk := clk_12M // 24Mhz clock signal
    u_OV2640_Controller.io.resend := "b0".U(1.W) // Reset signal
    u_OV2640_Controller.io.mode := cam_mode // 08:RGB565  04:RAW10
    //u_OV2640_Controller.io.config_finished := () // Flag to indicate that the configuration is finished
    SCL := u_OV2640_Controller.io.sioc // SCCB interface - clock signal
    SDA := u_OV2640_Controller.io.siod // SCCB interface - data signal
    //u_OV2640_Controller.io.reset := () // RESET signal for OV7670
    //u_OV2640_Controller.io.pwdn := () // PWDN signal for OV7670

    //I_clk
    when (HREF) {
      when (!hcnt) {
        pixdata_d1 := PIXDATA
      } .otherwise {
        pixdata_d2 := PIXDATA
      }

      hcnt :=  ~hcnt
    } .otherwise {
      hcnt := false.B
    }

    when (cam_mode === "h08".U(8.W)) {
      //cam_data := Cat(pixdata_d1(9,5),pixdata_d1(4,2),PIXDATA(9,7),PIXDATA(6,2)) //RGB565
      //cam_data := Cat(PIXDATA(9,5),PIXDATA(4,2),pixdata_d1(9,7),pixdata_d1(6,2)) //RGB565
      cam_data := Cat(pixdata_d1(9,5), pixdata_d1(4,2) ## pixdata_d2(9,7), pixdata_d2(6,2)) //RGB565
      cam_de_in := hcnt
    } .otherwise {
      cam_data := Cat(PIXDATA(9,5), PIXDATA(9,4), PIXDATA(9,5)) //RAW10
      cam_de_in := HREF
    }
  } //withClockAndReset(PIXCLK, ~I_rst_n)

  //================================================
  //data width 16bit
  ch0_vfb_clk_in := tp_pxl_clk // Mux((g_cnt_vs <= "h1ff".U(10.W)), I_clk, PIXCLK)
  ch0_vfb_vs_in := Mux((g_cnt_vs <= "h1ff".U(10.W)),  ~tp0_vs_in, VSYNC) //negative
  ch0_vfb_de_in := Mux((g_cnt_vs <= "h1ff".U(10.W)), tp0_de_in, cam_de_in) //HREF or hcnt
  ch0_vfb_data_in := Mux((g_cnt_vs <= "h1ff".U(10.W)), Cat(tp0_data_r(7,3), tp0_data_g(7,2), tp0_data_b(7,3)), cam_data) // RGB565

  // assign ch0_vfb_clk_in  = PIXCLK;
  // assign ch0_vfb_vs_in   = VSYNC;  //negative
  // assign ch0_vfb_de_in   = HREF;//hcnt;
  // assign ch0_vfb_data_in = cam_data; // RGB565


  //================================================
  //SRAM 控制模块
  val Video_Frame_Buffer_Top_inst = Module(new Video_Frame_Buffer_Top)
  Video_Frame_Buffer_Top_inst.io.I_rst_n := init_calib //rst_n            ),
  Video_Frame_Buffer_Top_inst.io.I_dma_clk := dma_clk //sram_clk         ),
  Video_Frame_Buffer_Top_inst.io.I_wr_halt := 0.U(1.W) //1:halt,  0:no halt
  Video_Frame_Buffer_Top_inst.io.I_rd_halt := 0.U(1.W) //1:halt,  0:no halt
  // video data input
  Video_Frame_Buffer_Top_inst.io.I_vin0_clk := ch0_vfb_clk_in
  Video_Frame_Buffer_Top_inst.io.I_vin0_vs_n := ch0_vfb_vs_in
  Video_Frame_Buffer_Top_inst.io.I_vin0_de := ch0_vfb_de_in
  Video_Frame_Buffer_Top_inst.io.I_vin0_data := ch0_vfb_data_in
  // video data output
  Video_Frame_Buffer_Top_inst.io.I_vout0_clk := pix_clk
  Video_Frame_Buffer_Top_inst.io.I_vout0_vs_n :=  ~syn_off0_vs
  Video_Frame_Buffer_Top_inst.io.I_vout0_de := syn_off0_re
  off0_syn_de := Video_Frame_Buffer_Top_inst.io.O_vout0_den
  off0_syn_data := Video_Frame_Buffer_Top_inst.io.O_vout0_data
  // ddr write request
  cmd := Video_Frame_Buffer_Top_inst.io.O_cmd
  cmd_en := Video_Frame_Buffer_Top_inst.io.O_cmd_en
  addr := Video_Frame_Buffer_Top_inst.io.O_addr //[ADDR_WIDTH-1:0]
  wr_data := Video_Frame_Buffer_Top_inst.io.O_wr_data //[DATA_WIDTH-1:0]
  data_mask := Video_Frame_Buffer_Top_inst.io.O_data_mask
  Video_Frame_Buffer_Top_inst.io.I_rd_data_valid := rd_data_valid
  Video_Frame_Buffer_Top_inst.io.I_rd_data := rd_data //[DATA_WIDTH-1:0]
  Video_Frame_Buffer_Top_inst.io.I_init_calib := init_calib

  //================================================
  //HyperRAM ip
  val GW_PLLVR_inst = Module(new GW_PLLVR)
  memory_clk := GW_PLLVR_inst.io.clkout //output clkout
  mem_pll_lock := GW_PLLVR_inst.io.lock //output lock
  GW_PLLVR_inst.io.clkin := I_clk //input clkin


  val HyperRAM_Memory_Interface_Top_inst = Module(new HyperRAM_Memory_Interface_Top)
  HyperRAM_Memory_Interface_Top_inst.io.clk := I_clk
  HyperRAM_Memory_Interface_Top_inst.io.memory_clk := memory_clk
  HyperRAM_Memory_Interface_Top_inst.io.pll_lock := mem_pll_lock
  HyperRAM_Memory_Interface_Top_inst.io.rst_n := I_rst_n //rst_n
  O_hpram_ck := HyperRAM_Memory_Interface_Top_inst.io.O_hpram_ck
  O_hpram_ck_n := HyperRAM_Memory_Interface_Top_inst.io.O_hpram_ck_n
  HyperRAM_Memory_Interface_Top_inst.io.IO_hpram_rwds := IO_hpram_rwds
  HyperRAM_Memory_Interface_Top_inst.io.IO_hpram_dq := IO_hpram_dq
  O_hpram_reset_n := HyperRAM_Memory_Interface_Top_inst.io.O_hpram_reset_n
  O_hpram_cs_n := HyperRAM_Memory_Interface_Top_inst.io.O_hpram_cs_n
  HyperRAM_Memory_Interface_Top_inst.io.wr_data := wr_data
  rd_data := HyperRAM_Memory_Interface_Top_inst.io.rd_data
  rd_data_valid := HyperRAM_Memory_Interface_Top_inst.io.rd_data_valid
  HyperRAM_Memory_Interface_Top_inst.io.addr := addr
  HyperRAM_Memory_Interface_Top_inst.io.cmd := cmd
  HyperRAM_Memory_Interface_Top_inst.io.cmd_en := cmd_en
  dma_clk := HyperRAM_Memory_Interface_Top_inst.io.clk_out
  HyperRAM_Memory_Interface_Top_inst.io.data_mask := data_mask
  init_calib := HyperRAM_Memory_Interface_Top_inst.io.init_calib

  //============================================================================
  withClockAndReset(pix_clk, ~hdmi_rst_n) {
    val hv_sync = Module(new HVSync(vp))
    val out_de = Wire(Bool())
    val Rden_w = Wire(Bool())
    val Rden_dn = RegInit(false.B)
    val rd_hofs = Mux(rd_halign.U === 2.U, (vp.H_DISPLAY-rd_hres).U(12.W), Mux(rd_halign.U === 1.U, ((vp.H_DISPLAY-rd_hres)/2).U(12.W), 0.U))
    val rd_vofs = Mux(rd_valign.U === 2.U, (vp.V_DISPLAY-rd_vres).U(12.W), Mux(rd_valign.U === 1.U, ((vp.V_DISPLAY-rd_vres)/2).U(12.W), 0.U))
    Rden_w := (hv_sync.io.hpos >= rd_hofs) && (hv_sync.io.hpos < (rd_hofs+rd_hres.U(12.W))) &&
              (hv_sync.io.vpos >= rd_vofs) && (hv_sync.io.vpos < (rd_vofs+rd_vres.U(12.W)))
    Rden_dn := Rden_w
    syn_off0_re := Rden_dn
    out_de := hv_sync.io.display_on
    syn_off0_hs := Mux(syn_hs_pol.B,  ~hv_sync.io.hsync, hv_sync.io.hsync)
    syn_off0_vs := Mux(syn_vs_pol.B,  ~hv_sync.io.vsync, hv_sync.io.vsync)

    val N = 5 //delay N clocks

    val Pout_hs_dn = RegInit(1.U(N.W))
    val Pout_vs_dn = RegInit(1.U(N.W))
    val Pout_de_dn = RegInit(0.U(N.W))
    Pout_hs_dn := Cat(Pout_hs_dn(N-2,0), syn_off0_hs)
    Pout_vs_dn := Cat(Pout_vs_dn(N-2,0), syn_off0_vs)
    Pout_de_dn := Cat(Pout_de_dn(N-2,0), out_de)

    //========================================================================
    //TMDS TX
    rgb_data := Mux(off0_syn_de, Cat(off0_syn_data(15,11), 0.U(3.W), off0_syn_data(10,5), 0.U(2.W), off0_syn_data(4,0), 0.U(3.W)), "h0000ff".U(24.W)) //{r,g,b}
    rgb_vs := Pout_vs_dn(4) //syn_off0_vs;
    rgb_hs := Pout_hs_dn(4) //syn_off0_hs;
    rgb_de := Pout_de_dn(4) //off0_syn_de;

    /* HDMI interface */
    if(gowinDviTx){
      val DVI_TX_Top_inst = Module(new DVI_TX_Top())

      /* Clocks and reset */
      DVI_TX_Top_inst.io.I_rst_n := hdmi_rst_n //asynchronous reset, low active
      DVI_TX_Top_inst.io.I_serial_clk := serial_clk
      DVI_TX_Top_inst.io.I_rgb_clk := pix_clk //pixel clock

      /* video signals connexions */
      DVI_TX_Top_inst.io.I_rgb_vs := rgb_vs
      DVI_TX_Top_inst.io.I_rgb_hs := rgb_hs
      DVI_TX_Top_inst.io.I_rgb_de := rgb_de
      DVI_TX_Top_inst.io.I_rgb_r := rgb_data(23,16)
      DVI_TX_Top_inst.io.I_rgb_g := rgb_data(15,8)
      DVI_TX_Top_inst.io.I_rgb_b := rgb_data(7,0)

      /* tmds connexions */
      O_tmds.data(0).p := DVI_TX_Top_inst.io.O_tmds_data_p(0)
      O_tmds.data(0).n := DVI_TX_Top_inst.io.O_tmds_data_n(0)
      O_tmds.data(1).p := DVI_TX_Top_inst.io.O_tmds_data_p(1)
      O_tmds.data(1).n := DVI_TX_Top_inst.io.O_tmds_data_n(1)
      O_tmds.data(2).p := DVI_TX_Top_inst.io.O_tmds_data_p(2)
      O_tmds.data(2).n := DVI_TX_Top_inst.io.O_tmds_data_n(2)
      O_tmds.clk.p := DVI_TX_Top_inst.io.O_tmds_clk_p
      O_tmds.clk.n := DVI_TX_Top_inst.io.O_tmds_clk_n
    } else {
      val rgb2tmds = Module(new Rgb2Tmds())
      rgb2tmds.io.videoSig.de := rgb_de
      rgb2tmds.io.videoSig.hsync := rgb_hs
      rgb2tmds.io.videoSig.vsync := rgb_vs
      rgb2tmds.io.videoSig.pixel.red   := rgb_data(23,16)
      rgb2tmds.io.videoSig.pixel.green := rgb_data(15,8)
      rgb2tmds.io.videoSig.pixel.blue  := rgb_data(7,0)

      /* serdes */
      // Blue -> data 0
      val serdesBlue = Module(new Oser10Module())
      serdesBlue.io.data := rgb2tmds.io.tmds_blue
      serdesBlue.io.fclk := serial_clk
      val buffDiffBlue = Module(new TLVDS_OBUF())
      buffDiffBlue.io.I := serdesBlue.io.q

      // Green -> data 1
      val serdesGreen = Module(new Oser10Module())
      serdesGreen.io.data := rgb2tmds.io.tmds_green
      serdesGreen.io.fclk := serial_clk
      val buffDiffGreen = Module(new TLVDS_OBUF())
      buffDiffGreen.io.I := serdesGreen.io.q

      // Red -> data 2
      val serdesRed = Module(new Oser10Module())
      serdesRed.io.data := rgb2tmds.io.tmds_red
      serdesRed.io.fclk := serial_clk
      val buffDiffRed = Module(new TLVDS_OBUF())
      buffDiffRed.io.I := serdesRed.io.q

      // clock
      val serdesClk = Module(new Oser10Module())
      serdesClk.io.data := "b1111100000".U(10.W)
      serdesClk.io.fclk := serial_clk
      val buffDiffClk = Module(new TLVDS_OBUF())
      buffDiffClk.io.I := serdesClk.io.q

      O_tmds.data(0).p := buffDiffBlue.io.O
      O_tmds.data(0).n := buffDiffBlue.io.OB
      O_tmds.data(1).p := buffDiffGreen.io.O
      O_tmds.data(1).n := buffDiffGreen.io.OB
      O_tmds.data(2).p := buffDiffRed.io.O
      O_tmds.data(2).n := buffDiffRed.io.OB
      O_tmds.clk.p := buffDiffClk.io.O
      O_tmds.clk.n := buffDiffClk.io.OB
    }
  } // withClockAndReset(pix_clk, ~hdmi_rst_n)
}

object video_topGen extends App {
  var gowinDviTx = true
  var rd_width = 800
  var rd_height = 600
  var rd_halign = 0
  var rd_valign = 0
  var fullscreen = 0
  var vmode: VideoMode = VideoConsts.m1280x720
  for(arg <- args){
    if(arg == "noGowinDviTx")
      gowinDviTx = false
    else if(arg == "center"){
      rd_halign = 1
      rd_valign = 1
    }
    else if(arg == "left")
      rd_halign = 0
    else if(arg == "right")
      rd_halign = 2
    else if(arg == "top")
      rd_valign = 0
    else if(arg == "bottom")
      rd_valign = 2
    else if((arg == "vga") || (arg == "640x480")){
      rd_width = 640
      rd_height = 480
    }
    else if((arg == "svga") || (arg == "800x600")){
      rd_width = 800
      rd_height = 600
    }
    else if((arg == "480p") || (arg == "720x480")){
      rd_width = 720
      rd_height = 480
    }
    else if((arg == "sd") || (arg == "576p") || (arg == "720x576")){
      rd_width = 720
      rd_height = 576
    }
    else if((arg == "hd") || (arg == "720p") || (arg == "1280x720")){
      rd_width = 1280
      rd_height = 720
    }
    else if(arg == "fullscreen")
      fullscreen = 1
  }
  if(fullscreen == 1){
    /*if((rd_width <= 720) && (rd_height <= 480))
      vmode = VideoConsts.m720x480
    else*/ if((rd_width <= 720) && (rd_height <= 576))
      vmode = VideoConsts.m720x576
    /*else if((rd_width <= 800) && (rd_height <= 600))
      vmode = VideoConsts.m800x600*/
    else if((rd_width <= 1024) && (rd_height <= 600))
      vmode = VideoConsts.m1024x600
    else if((rd_width <= 1024) && (rd_height <= 768))
      vmode = VideoConsts.m1024x768
  }
  if(gowinDviTx)
    println("Generate DkVideo with encrypted Gowin DviTx core")
  else
    println("Generate DkVideo with open source HdmiCore core")
  println(s"rd_hres $rd_width")
  println(s"rd_vres $rd_height")
  (new ChiselStage).execute(args,
    Seq(ChiselGeneratorAnnotation(() =>
        new video_top(gowinDviTx, rd_width, rd_height, rd_halign, rd_valign, vmode))))
}
