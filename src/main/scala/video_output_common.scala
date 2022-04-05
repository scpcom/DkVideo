package dkvideo

import chisel3._
import chisel3.util.Cat

import fpgamacro.gowin.{LVDS_OBUF, TLVDS_OBUF, ELVDS_OBUF}
import fpgamacro.gowin.{Video_PLL, TMDS_PLLVR, GW_PLLVR, Gowin_rPLL}
import hdmicore.video.{VideoMode, VideoConsts}
import camcore.{CameraType, ctNone, ctOV2640, ctGC0328}

sealed trait DeviceType
case object dtGW1N1 extends DeviceType
case object dtGW1NZ1 extends DeviceType
case object dtGW1NSR4C extends DeviceType
case object dtGW1NR9 extends DeviceType
case object dtGW2AR18C extends DeviceType

sealed trait MemoryType
case object mtNone extends MemoryType
case object mtHyperRAM extends MemoryType
case object mtPSRAM extends MemoryType

case class VideoOutParams(
                dt: DeviceType = dtGW1NSR4C, gowinDviTx: Boolean = true,
                rd_width: Int = 800, rd_height: Int = 600, rd_halign: Int = 0, rd_valign: Int = 0,
                vmode: VideoMode = VideoConsts.m1280x720, camtype: CameraType = ctOV2640,
                camzoom: Boolean = false
)

class VideoOutModule(vop: VideoOutParams) extends RawModule {
  val DQ_WIDTH = if (vop.dt == dtGW1NSR4C) 8 else 16
  val ADDR_WIDTH = if (vop.dt == dtGW1NSR4C) 22 else 21
  val DATA_WIDTH = if (vop.dt == dtGW1NSR4C) 32 else 64
  val CS_WIDTH = if (vop.dt == dtGW1NSR4C) 1 else 2
  val MASK_WIDTH = if (vop.dt == dtGW1NSR4C) 4 else 8

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

  val syn_hs_pol = 1   //HS polarity , 0:负极性，1：正极性
  val syn_vs_pol = 1   //VS polarity , 0:负极性，1：正极性

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

  //================================================
  //Helpers
  def get_pll(): Video_PLL = {
    if (vop.dt == dtGW1NSR4C)
      Module(new TMDS_PLLVR(vop.vmode.pll))
    else
      Module(new Gowin_rPLL(vop.vmode.pll))
  }

  def get_obuf(): LVDS_OBUF = {
    if ((vop.dt == dtGW1NSR4C) || (vop.dt == dtGW2AR18C))
      Module(new TLVDS_OBUF())
    else
      Module(new ELVDS_OBUF())
  }

  //================================================
  //Video input
  val vidMix = Module(new Video_Input_Mixer(vop.vmode.params,
                vop.rd_width, vop.rd_height, vop.rd_halign, vop.rd_valign,
                syn_hs_pol, syn_vs_pol,
                vop.camtype, vop.camzoom))

  //vidMix.io.I_clk := pix_clk //I_clk
  //vidMix.io.I_rst_n := hdmi_rst_n
  vidMix.io.I_button := I_button
  //vidMix.io.clk_12M := clk_12M
  //vidMix.io.init_calib := init_calib
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
}

