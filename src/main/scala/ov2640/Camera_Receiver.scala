package dkvideo
package ov2640

import chisel3._
import chisel3.util.Cat
import hdmicore.{VideoHdmi}
import hdmicore.video.VideoParams

class Camera_Receiver(vp: VideoParams) extends Module {
  val io = IO(new Bundle {
    val clk = Input(Clock()) // 50Mhz clock signal
    val resend = Input(Bool()) // Reset signal
    val mode = Input(UInt(8.W)) // 08:RGB565  04:RAW10
    val href = Input(Bool())
    val vsync = Input(Bool())
    val data = Input(UInt(10.W))
    val config_finished = Output(Bool()) // Flag to indicate that the configuration is finished
    val sioc = Output(Bool()) // SCCB interface - clock signal
    val siod = Output(Bool()) // Inout SCCB interface - data signal
    val reset = Output(Bool()) // RESET signal for OV2640
    val pwdn = Output(Bool()) // PWDN signal for OV2640
    val videoSig = Output(new VideoHdmi())
  })

  val cam_data = Wire(UInt(16.W))
  //val cam_vs_in = Wire(Bool())
  val cam_de_in = Wire(Bool())

  val pixdata_d1 = RegInit(0.U(10.W))
  val pixdata_d2 = RegInit(0.U(10.W))
  val hcnt = RegInit(false.B)

  val u_OV2640_Controller = Module(new OV2640_Controller(vp))
  u_OV2640_Controller.clock := io.clk
  u_OV2640_Controller.io.clk := io.clk // 24Mhz clock signal
  u_OV2640_Controller.io.resend := io.resend // Reset signal
  u_OV2640_Controller.io.mode := io.mode // 08:RGB565  04:RAW10
  io.config_finished := u_OV2640_Controller.io.config_finished // Flag to indicate that the configuration is finished
  io.sioc := u_OV2640_Controller.io.sioc // SCCB interface - clock signal
  io.siod := u_OV2640_Controller.io.siod // SCCB interface - data signal
  io.reset := u_OV2640_Controller.io.reset // RESET signal for OV7670
  io.pwdn := u_OV2640_Controller.io.pwdn // PWDN signal for OV7670

  //I_clk
  when (io.href) {
    when (!hcnt) {
      pixdata_d1 := io.data
    } .otherwise {
      pixdata_d2 := io.data
    }

    hcnt :=  ~hcnt
  } .otherwise {
    hcnt := false.B
  }

  when (io.mode === "h08".U(8.W)) {
    //cam_data := Cat(pixdata_d1(9,5),pixdata_d1(4,2),io.pixdata(9,7),io.pixdata(6,2)) //RGB565
    //cam_data := Cat(io.pixdata(9,5),io.pixdata(4,2),pixdata_d1(9,7),pixdata_d1(6,2)) //RGB565
    cam_data := Cat(pixdata_d1(9,5), pixdata_d1(4,2) ## pixdata_d2(9,7), pixdata_d2(6,2)) //RGB565
    cam_de_in := hcnt
  } .otherwise {
    cam_data := Cat(io.data(9,5), io.data(9,4), io.data(9,5)) //RAW10
    cam_de_in := io.href
  }

  io.videoSig.de := cam_de_in
  io.videoSig.hsync := true.B
  io.videoSig.vsync := io.vsync
  io.videoSig.pixel.red := Cat(cam_data(15,11), 0.U(3.W))
  io.videoSig.pixel.green := Cat(cam_data(10,5), 0.U(2.W))
  io.videoSig.pixel.blue := Cat(cam_data(4,0), 0.U(3.W))
}
