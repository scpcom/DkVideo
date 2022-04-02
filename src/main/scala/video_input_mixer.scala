package dkvideo

import chisel3._
import hdmicore.video.{VideoParams, VideoMode, VideoConsts}
import hdmicore.{VideoHdmi, PatternExample}
import camcore.{Camera_Receiver, CameraType, ctNone, ctOV2640, ctGC0328}

class Video_Input_Mixer(vp: VideoParams = VideoConsts.m1280x720.params,
                rd_width: Int = 800, rd_height: Int = 600, rd_halign: Int = 0, rd_valign: Int = 0,
                syn_hs_pol: Int = 1, syn_vs_pol: Int = 1,
                camtype: CameraType = ctOV2640, camzoom: Boolean = false) extends RawModule {
  val io = IO(new Bundle {
    val I_clk = Input(Clock()) //27Mhz
    val I_rst_n = Input(Bool())
    val I_button = Input(Bool())
    val clk_12M = Input(Clock())
    val init_calib = Input(Bool())
    val O_led = Output(UInt(2.W))
    val videoClk = Output(Clock())
    val videoSig = Output(new VideoHdmi())
    val SDA = Output(Bool()) // Inout
    val SCL = Output(Bool()) // Inout
    val VSYNC = Input(Bool())
    val HREF = Input(Bool())
    val PIXDATA = Input(UInt(10.W))
    val PIXCLK = Input(Clock())
    //val XCLK = Output(Clock())
  })

  //==================================================
  /* set val rd_vp = vp for full screen */
  val rd_vp = VideoParams(
      H_DISPLAY = rd_width, H_FRONT = vp.H_FRONT,
      H_SYNC = vp.H_SYNC, H_BACK = vp.H_BACK,
      V_SYNC = vp.V_SYNC,  V_BACK = vp.V_BACK,
      V_TOP = vp.V_TOP, V_DISPLAY = rd_height,
      V_BOTTOM = vp.V_BOTTOM)
  val rd_hres = rd_vp.H_DISPLAY // 800
  val rd_vres = rd_vp.V_DISPLAY // 600

  val running = Wire(Bool())

  //--------------------------
  val tp0_vs_in = Wire(Bool())
  val tp0_hs_in = Wire(Bool())
  val tp0_de_in = Wire(Bool())
  val tp0_data_r = Wire(UInt(8.W))  /*synthesis syn_keep=1*/
  val tp0_data_g = Wire(UInt(8.W))  /*synthesis syn_keep=1*/
  val tp0_data_b = Wire(UInt(8.W))  /*synthesis syn_keep=1*/

  //--------------------------
  val cam_vs_in = Wire(Bool())
  val cam_hs_in = Wire(Bool())
  val cam_de_in = Wire(Bool())
  val cam_data_r = Wire(UInt(8.W))  /*synthesis syn_keep=1*/
  val cam_data_g = Wire(UInt(8.W))  /*synthesis syn_keep=1*/
  val cam_data_b = Wire(UInt(8.W))  /*synthesis syn_keep=1*/

  //io.XCLK := io.clk_12M

  //============================================================================
  //I_clk

  val vmx_pxl_clk = if (camtype == ctNone) io.I_clk else io.PIXCLK
  val ptEnabled = ((camtype == ctNone) &&
                   (vp.H_DISPLAY == rd_vp.H_DISPLAY) &&
                   (vp.H_DISPLAY == rd_vp.H_DISPLAY))

  if (ptEnabled)
    println("with PatternExample")

  withClockAndReset(vmx_pxl_clk, ~io.I_rst_n) {
    val cnt_vs = RegInit(0.U(10.W))
    val run_cnt = RegInit(0.U(32.W))
    val vs_r = Reg(Bool())

    //========================================================================
    //LED test
    when (run_cnt >= "d27_000_000".U(32.W)) {
      run_cnt := 0.U(32.W)
    } .otherwise {
      run_cnt := run_cnt+"b1".U(1.W)
    }
    running := (Mux((run_cnt < "d13_500_000".U(32.W)), "b1".U(1.W), "b0".U(1.W)) =/= 0.U)
    io.O_led := ~io.init_calib ## running

    //========================================================================
    //testpattern
    val testpattern_inst = Module(new testpattern(vp))
    testpattern_inst.io.I_pxl_clk := vmx_pxl_clk //pixel clock
    testpattern_inst.io.I_rst_n := io.I_rst_n //low active
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
      if ((camtype == ctNone) && !ptEnabled) {
        cnt_vs := 0.U
      } else {
        cnt_vs := cnt_vs
      }
    } .elsewhen (vs_r && ( !tp0_vs_in)) { //vs24 falling edge
      cnt_vs := cnt_vs+"b1".U(1.W)
    } .otherwise {
      cnt_vs := cnt_vs
    }

    //============================================================================
    if (ptEnabled) {
      io.SCL := DontCare
      io.SDA := DontCare

      val patternExample = Module(new PatternExample(rd_vp))
      patternExample.io.I_button := io.I_button

      cam_de_in := patternExample.io.videoSig.de
      cam_hs_in := patternExample.io.videoSig.hsync
      cam_vs_in := patternExample.io.videoSig.vsync
      cam_data_r := patternExample.io.videoSig.pixel.red
      cam_data_g := patternExample.io.videoSig.pixel.green
      cam_data_b := patternExample.io.videoSig.pixel.blue
    } else if (camtype == ctNone) {
      io.SCL := DontCare
      io.SDA := DontCare

      cam_de_in := tp0_de_in
      cam_hs_in := ~tp0_hs_in
      cam_vs_in := ~tp0_vs_in
      cam_data_r := tp0_data_r
      cam_data_g := tp0_data_g
      cam_data_b := tp0_data_b
    } else {
      val cam_mode = "h08".U(8.W) // 08:RGB565  04:RAW10

      val u_Camera_Receiver = Module(new Camera_Receiver(rd_vp, camtype, camzoom))
      u_Camera_Receiver.io.clk := io.clk_12M // 24Mhz clock signal
      u_Camera_Receiver.io.resend := "b0".U(1.W) // Reset signal
      u_Camera_Receiver.io.mode := cam_mode // 08:RGB565  04:RAW10
      u_Camera_Receiver.io.href := io.HREF
      u_Camera_Receiver.io.vsync := io.VSYNC
      u_Camera_Receiver.io.data := io.PIXDATA
      //u_Camera_Receiver.io.config_finished := () // Flag to indicate that the configuration is finished
      io.SCL := u_Camera_Receiver.io.sioc // SCCB interface - clock signal
      io.SDA := u_Camera_Receiver.io.siod // SCCB interface - data signal
      //u_Camera_Receiver.io.reset := () // RESET signal for Camera
      //u_Camera_Receiver.io.pwdn := () // PWDN signal for Camera

      cam_de_in := u_Camera_Receiver.io.videoSig.de
      cam_hs_in := u_Camera_Receiver.io.videoSig.hsync
      cam_vs_in := u_Camera_Receiver.io.videoSig.vsync
      cam_data_r := u_Camera_Receiver.io.videoSig.pixel.red
      cam_data_g := u_Camera_Receiver.io.videoSig.pixel.green
      cam_data_b := u_Camera_Receiver.io.videoSig.pixel.blue
    }

    //================================================
    //data width 24bit
    io.videoClk := vmx_pxl_clk // Mux((cnt_vs <= "h1ff".U(10.W)), I_clk, PIXCLK)
    io.videoSig.de := Mux((cnt_vs <= "h1ff".U(10.W)), tp0_de_in, cam_de_in) //HREF or hcnt
    io.videoSig.hsync := Mux((cnt_vs <= "h1ff".U(10.W)),  ~tp0_hs_in, cam_hs_in) //negative
    io.videoSig.vsync := Mux((cnt_vs <= "h1ff".U(10.W)),  ~tp0_vs_in, cam_vs_in) //negative
    io.videoSig.pixel.red   := Mux((cnt_vs <= "h1ff".U(10.W)), tp0_data_r, cam_data_r)
    io.videoSig.pixel.green := Mux((cnt_vs <= "h1ff".U(10.W)), tp0_data_g, cam_data_g)
    io.videoSig.pixel.blue  := Mux((cnt_vs <= "h1ff".U(10.W)), tp0_data_b, cam_data_b)
  } // withClockAndReset(vmx_pxl_clk, ~io.I_rst_n)
}

