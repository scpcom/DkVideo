package dkvideo.camcore

import chisel3._
import chisel3.util.Cat
import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}
import hdmicore.video.VideoParams

class Camera_Registers() extends Module {
  val io = IO(new Bundle {
    val clk = Input(Clock())
    val resend = Input(Bool())
    val advance = Input(Bool())
    val mode = Input(UInt(8.W))
    val command = Output(UInt(16.W))
    val finished = Output(Bool())
  })
}
