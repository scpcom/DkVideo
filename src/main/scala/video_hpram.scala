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

class video_hpram(dt: DeviceType = dtGW1NSR4C, gowinDviTx: Boolean = true,
                rd_width: Int = 800, rd_height: Int = 600, rd_halign: Int = 0, rd_valign: Int = 0,
                vmode: VideoMode = VideoConsts.m1280x720, camtype: CameraType = ctOV2640,
                camzoom: Boolean = false) extends RawModule {
  val DQ_WIDTH = if (dt == dtGW1NSR4C) 8 else 16
  val ADDR_WIDTH = if (dt == dtGW1NSR4C) 22 else 21
  val DATA_WIDTH = if (dt == dtGW1NSR4C) 32 else 64
  val CS_WIDTH = if (dt == dtGW1NSR4C) 1 else 2
  val MASK_WIDTH = if (dt == dtGW1NSR4C) 4 else 8

  val I_clk = IO(Input(Clock())) //27Mhz
  val I_rst_n = IO(Input(Bool()))
  val O_led = IO(Output(UInt(2.W)))
  val I_button = IO(Input(Bool()))
  val SDA = IO(Output(Bool())) // Inout
  val SCL = IO(Output(Bool())) // Inout
  val VSYNC = IO(Input(Bool()))
  val HREF = IO(Input(Bool()))
  val PIXDATA = IO(Input(UInt(10.W)))
  val PIXCLK = IO(Input(Clock()))
  val XCLK = IO(Output(Clock()))
  val O_hpram_ck = IO(Output(UInt(CS_WIDTH.W)))
  val O_hpram_ck_n = IO(Output(UInt(CS_WIDTH.W)))
  val O_hpram_cs_n = IO(Output(UInt(CS_WIDTH.W)))
  val O_hpram_reset_n = IO(Output(UInt(CS_WIDTH.W)))
  val IO_hpram_dq = IO(Analog(DQ_WIDTH.W)) // Inout
  val IO_hpram_rwds = IO(Analog(CS_WIDTH.W)) // Inout
  val O_tmds = IO(Output(new TMDSDiff()))

  val syn_hs_pol = 1   //HS polarity , 0:负极性，1：正极性
  val syn_vs_pol = 1   //VS polarity , 0:负极性，1：正极性
  val syn_delay = 7

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
  val addr = Wire(UInt(ADDR_WIDTH.W))     //[ADDR_WIDTH-1:0]
  val wr_data = Wire(UInt(DATA_WIDTH.W))  //[DATA_WIDTH-1:0]
  val data_mask = Wire(UInt(MASK_WIDTH.W))
  val rd_data_valid = Wire(Bool())
  val rd_data = Wire(UInt(DATA_WIDTH.W))  //[DATA_WIDTH-1:0]
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
  def get_pll(): Video_PLL = {
    if (dt == dtGW1NSR4C)
      Module(new TMDS_PLLVR(vmode.pll))
    else
      Module(new Gowin_rPLL(vmode.pll))
  }
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
  val vidMix = Module(new Video_Input_Mixer(vmode.params,
                rd_width, rd_height, rd_halign, rd_valign,
                syn_hs_pol, syn_vs_pol,
                camtype, camzoom))

  vidMix.io.I_clk := I_clk
  vidMix.io.I_rst_n := hdmi_rst_n
  vidMix.io.I_button := I_button
  vidMix.io.clk_12M := clk_12M
  vidMix.io.init_calib := init_calib
  O_led := vidMix.io.O_led

  SDA := vidMix.io.SDA
  SCL := vidMix.io.SCL
  vidMix.io.VSYNC := VSYNC
  vidMix.io.HREF := HREF
  vidMix.io.PIXDATA := PIXDATA
  vidMix.io.PIXCLK := PIXCLK
  //XCLK := vidMix.io.XCLK

  //data width 16bit
  ch0_vfb_clk_in := vidMix.io.videoClk
  //ch0_vfb_hs_in := vidMix.io.videoSig.hsync
  ch0_vfb_vs_in := vidMix.io.videoSig.vsync
  ch0_vfb_de_in := vidMix.io.videoSig.de
  ch0_vfb_data_in := Cat(vidMix.io.videoSig.pixel.red(7,3),
                         vidMix.io.videoSig.pixel.green(7,2),
                         vidMix.io.videoSig.pixel.blue(7,3)) // RGB565

  //================================================
  //SRAM 控制模块
  val videoFrameBuffer = Module(new Video_Frame_Buffer_Top)
  videoFrameBuffer.io.I_rst_n := init_calib //rst_n            ),
  videoFrameBuffer.io.I_dma_clk := dma_clk //sram_clk         ),
  videoFrameBuffer.io.I_wr_halt := 0.U(1.W) //1:halt,  0:no halt
  videoFrameBuffer.io.I_rd_halt := 0.U(1.W) //1:halt,  0:no halt
  // video data input
  videoFrameBuffer.io.I_vin0_clk := ch0_vfb_clk_in
  videoFrameBuffer.io.I_vin0_vs_n := ch0_vfb_vs_in
  videoFrameBuffer.io.I_vin0_de := ch0_vfb_de_in
  videoFrameBuffer.io.I_vin0_data := ch0_vfb_data_in
  // video data output
  videoFrameBuffer.io.I_vout0_clk := pix_clk
  videoFrameBuffer.io.I_vout0_vs_n :=  ~syn_off0_vs
  videoFrameBuffer.io.I_vout0_de := syn_off0_re
  off0_syn_de := videoFrameBuffer.io.O_vout0_den
  off0_syn_data := videoFrameBuffer.io.O_vout0_data
  // ddr write request
  cmd := videoFrameBuffer.io.O_cmd
  cmd_en := videoFrameBuffer.io.O_cmd_en
  addr := videoFrameBuffer.io.O_addr //[ADDR_WIDTH-1:0]
  wr_data := videoFrameBuffer.io.O_wr_data //[DATA_WIDTH-1:0]
  data_mask := videoFrameBuffer.io.O_data_mask
  videoFrameBuffer.io.I_rd_data_valid := rd_data_valid
  videoFrameBuffer.io.I_rd_data := rd_data //[DATA_WIDTH-1:0]
  videoFrameBuffer.io.I_init_calib := init_calib

  //================================================
  //HyperRAM ip
  if (dt == dtGW1NSR4C) {
  val memPLL = Module(new GW_PLLVR)
  memory_clk := memPLL.io.clkout //output clkout
  mem_pll_lock := memPLL.io.lock //output lock
  memPLL.io.clkin := I_clk //input clkin
  } else {
  val memPLL = Module(new Gowin_rPLL(PLLParams(IDIV_SEL = 8, FBDIV_SEL = 52, ODIV_SEL = 4, DYN_SDIV_SEL = 2)))
  memory_clk := memPLL.io.clkout //output clkout
  mem_pll_lock := memPLL.io.lock //output lock
  memPLL.io.clkin := I_clk //input clkin
  }

  val memoryInterface = Module(new HyperRAM_Memory_Interface_Top)
  memoryInterface.io.clk := I_clk
  memoryInterface.io.memory_clk := memory_clk
  memoryInterface.io.pll_lock := mem_pll_lock
  memoryInterface.io.rst_n := I_rst_n //rst_n
  O_hpram_ck := memoryInterface.io.O_hpram_ck
  O_hpram_ck_n := memoryInterface.io.O_hpram_ck_n
  memoryInterface.io.IO_hpram_rwds <> IO_hpram_rwds
  memoryInterface.io.IO_hpram_dq <> IO_hpram_dq
  O_hpram_reset_n := memoryInterface.io.O_hpram_reset_n
  O_hpram_cs_n := memoryInterface.io.O_hpram_cs_n
  memoryInterface.io.wr_data := wr_data
  rd_data := memoryInterface.io.rd_data
  rd_data_valid := memoryInterface.io.rd_data_valid
  memoryInterface.io.addr := addr
  memoryInterface.io.cmd := cmd
  memoryInterface.io.cmd_en := cmd_en
  dma_clk := memoryInterface.io.clk_out
  memoryInterface.io.data_mask := data_mask
  init_calib := memoryInterface.io.init_calib

  //============================================================================
  withClockAndReset(pix_clk, ~hdmi_rst_n) {
    val voSync = Module(new Video_Output_Sync(vmode.params, rd_width, rd_height, rd_halign, rd_valign, syn_hs_pol, syn_vs_pol, syn_delay))

    syn_off0_vs := voSync.io.syn_off0_vs
    syn_off0_hs := voSync.io.syn_off0_hs
    syn_off0_re := voSync.io.syn_off0_re

    //========================================================================
    //TMDS TX
    rgb_hs := voSync.io.rgb_hs
    rgb_vs := voSync.io.rgb_vs
    rgb_de := voSync.io.rgb_de
    rgb_data := Mux(off0_syn_de, Cat(off0_syn_data(15,11), 0.U(3.W), off0_syn_data(10,5), 0.U(2.W), off0_syn_data(4,0), 0.U(3.W)), "h0000ff".U(24.W)) //{r,g,b}

    /* HDMI interface */
    if(gowinDviTx){
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
      def get_obuf(): LVDS_OBUF = {
        if ((dt == dtGW1NSR4C) || (dt == dtGW2AR18C))
          Module(new TLVDS_OBUF())
        else
          Module(new ELVDS_OBUF())
      }
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
