package dkvideo

import chisel3._
import chisel3.util.Cat
import chisel3.experimental.Analog

import fpgamacro.gowin.{CLKDIV, LVDS_OBUF, TLVDS_OBUF, ELVDS_OBUF}
import fpgamacro.gowin.{Oser10Module}
import fpgamacro.gowin.{PLLParams, Video_PLL, TMDS_PLLVR, GW_PLLVR, Gowin_rPLL}
import hdmicore.video.{VideoMode, VideoConsts}
import hdmicore.{Rgb2Tmds, TMDSDiff, DiffPair, HdmiTx, VideoHdmi}
import hdl.gowin.DVI_TX_Top
import hdl.gowin.Video_Frame_Buffer_Top
import hdl.gowin.HyperRAM_Memory_Interface_Top
import camcore.{CameraType, ctNone, ctOV2640, ctGC0328}
import video.Video_Output_Sync

class video_noram(vop: VideoOutParams) extends VideoOutModule(vop) {
  val syn_delay = 5

  //-------------------------------------------------
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

  //================================================
  //Clocks
  val tmdsPLL = get_pll()
  tmdsPLL.io.clkin := I_clk //input clk
  serial_clk := tmdsPLL.io.clkout //output clk
  clk_12M := tmdsPLL.io.clkoutd //output clkoutd
  pll_lock := tmdsPLL.io.lock //output lock
  hdmi_rst_n := I_rst_n & pll_lock

  val uClkDiv = Module(new CLKDIV)
  uClkDiv.io.RESETN := hdmi_rst_n
  uClkDiv.io.HCLKIN := serial_clk //clk  x5
  pix_clk := uClkDiv.io.CLKOUT //clk  x1
  uClkDiv.io.CALIB := "b1".U(1.W)

  XCLK := clk_12M

  //================================================
  //Video input

  vidMix.io.I_clk := pix_clk //I_clk
  vidMix.io.I_rst_n := hdmi_rst_n
  //vidMix.io.I_button := I_button
  vidMix.io.clk_12M := clk_12M
  vidMix.io.init_calib := init_calib
  //O_led := vidMix.io.O_led

  //================================================
  init_calib := false.B
  off0_syn_de := ch0_vfb_de_in
  off0_syn_data := ch0_vfb_data_in
  val use_syn_in = ((vop.camtype == ctNone) &&
                    (vop.vmode.params.H_DISPLAY == vop.rd_width) &&
                    (vop.vmode.params.V_DISPLAY == vop.rd_height))

  //============================================================================
  withClockAndReset(pix_clk, ~hdmi_rst_n) {
    val voSync = Module(new Video_Output_Sync(vop.vmode.params, vop.rd_width, vop.rd_height, vop.rd_halign, vop.rd_valign, syn_hs_pol, syn_vs_pol, syn_delay))

    syn_off0_vs := voSync.io.syn_off0_vs
    syn_off0_hs := voSync.io.syn_off0_hs
    syn_off0_re := voSync.io.syn_off0_re

    //========================================================================
    //TMDS TX
    rgb_hs := Mux(use_syn_in.B, vidMix.io.videoSig.hsync, voSync.io.rgb_hs)
    rgb_vs := Mux(use_syn_in.B, vidMix.io.videoSig.vsync, voSync.io.rgb_vs)
    rgb_de := Mux(use_syn_in.B, vidMix.io.videoSig.de, voSync.io.rgb_de)
    rgb_data := Mux(off0_syn_de, Cat(off0_syn_data(15,11), 0.U(3.W), off0_syn_data(10,5), 0.U(2.W), off0_syn_data(4,0), 0.U(3.W)), "h0000ff".U(24.W)) //{r,g,b}

    if (vop.ot == otLCD) {
      LCD_CLK := pix_clk
      LCD_HYNC := rgb_hs
      LCD_SYNC := rgb_vs
      LCD_DEN := rgb_de
      LCD_R := rgb_data(23,19)
      LCD_G := rgb_data(15,10)
      LCD_B := rgb_data(7,3)

    /* HDMI interface */
    } else if (vop.gowinDviTx) {
      val dviTx = Module(new DVI_TX_Top())

      /* Clocks and reset */
      dviTx.io.I_rst_n := hdmi_rst_n //asynchronous reset, low active
      dviTx.io.I_serial_clk := serial_clk
      dviTx.io.I_rgb_clk := pix_clk //pixel clock

      /* video signals connexions */
      dviTx.io.I_rgb_vs := rgb_vs
      dviTx.io.I_rgb_hs := rgb_hs
      dviTx.io.I_rgb_de := rgb_de
      dviTx.io.I_rgb_r := rgb_data(23,16)
      dviTx.io.I_rgb_g := rgb_data(15,8)
      dviTx.io.I_rgb_b := rgb_data(7,0)

      /* tmds connexions */
      O_tmds.data(0).p := dviTx.io.O_tmds_data_p(0)
      O_tmds.data(0).n := dviTx.io.O_tmds_data_n(0)
      O_tmds.data(1).p := dviTx.io.O_tmds_data_p(1)
      O_tmds.data(1).n := dviTx.io.O_tmds_data_n(1)
      O_tmds.data(2).p := dviTx.io.O_tmds_data_p(2)
      O_tmds.data(2).n := dviTx.io.O_tmds_data_n(2)
      O_tmds.clk.p := dviTx.io.O_tmds_clk_p
      O_tmds.clk.n := dviTx.io.O_tmds_clk_n
    } else {
      val hdmiTx = Module(new HdmiTx())
      hdmiTx.io.serClk := serial_clk
      hdmiTx.io.videoSig.de := rgb_de
      hdmiTx.io.videoSig.hsync := rgb_hs
      hdmiTx.io.videoSig.vsync := rgb_vs
      hdmiTx.io.videoSig.pixel.red   := rgb_data(23,16)
      hdmiTx.io.videoSig.pixel.green := rgb_data(15,8)
      hdmiTx.io.videoSig.pixel.blue  := rgb_data(7,0)

      /* LVDS output */
      val buffDiffBlue = get_obuf()
      buffDiffBlue.io.I := hdmiTx.io.tmds.data(0)
      val buffDiffGreen = get_obuf()
      buffDiffGreen.io.I := hdmiTx.io.tmds.data(1)
      val buffDiffRed = get_obuf()
      buffDiffRed.io.I := hdmiTx.io.tmds.data(2)
      val buffDiffClk = get_obuf()
      buffDiffClk.io.I := hdmiTx.io.tmds.clk

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
