package dkvideo.camcore

import chisel3._
import hdmicore.video.VideoParams

sealed trait CameraType
case object ctOV2640 extends CameraType
case object ctGC0328 extends CameraType

class Camera_Controller(vp: VideoParams, ct: CameraType = ctOV2640) extends Module {
  val io = IO(new Bundle {
    val clk = Input(Clock()) // 50Mhz clock signal
    val resend = Input(Bool()) // Reset signal
    val mode = Input(UInt(8.W)) // 08:RGB565  04:RAW10
    val config_finished = Output(Bool()) // Flag to indicate that the configuration is finished
    val sioc = Output(Bool()) // SCCB interface - clock signal
    val siod = Output(Bool()) // Inout SCCB interface - data signal
    val reset = Output(Bool()) // RESET signal for Camera
    val pwdn = Output(Bool()) // PWDN signal for Camera
  })

  // Internal signals
  val command = Wire(UInt(16.W))
  val finished = Wire(Bool())
  val taken = Wire(Bool())
  val send = WireDefault(Bool(), false.B)

  // Signal for testing
  io.config_finished := finished

  // Signals for RESET and PWDN Camera
  io.reset := true.B
  io.pwdn := false.B

  // Signal to indicate that the configuration is finshied
  send :=  ~finished

  // Create an instance of a LUT table
  def get_lut(): Camera_Registers = if (ct == ctGC0328) Module(new GC0328_Registers(vp)) else Module(new OV2640_Registers(vp))
  val LUT = get_lut()
  LUT.io.clk := io.clk // 50Mhz clock signal
  LUT.io.advance := taken // Flag to advance to next register
  LUT.io.mode := io.mode // 08:RGB565  04:RAW10
  command := LUT.io.command // register value and data for Camera
  finished := LUT.io.finished // Flag to indicate the configuration is finshed
  LUT.io.resend := io.resend // Re-configure flag for Camera


  // Create an instance of a SCCB interface
  def get_sid(): UInt = if (ct == ctGC0328) "h42".U(8.W) else "h60".U(8.W)
  val I2C = Module(new I2C_Interface(
      SID = get_sid() // GC0328: "h42".U(8.W), OV2640: "h60".U(8.W)
  ))
  I2C.io.clk := io.clk // 50Mhz clock signal
  taken := I2C.io.taken // Flag to advance to next register
  io.siod := I2C.io.siod // Clock signal for SCCB interface
  io.sioc := I2C.io.sioc // Data signal for SCCB interface
  I2C.io.send := send // Continue to configure Camera
  I2C.io.rega := command(15,8) // Register address
  I2C.io.value := command(7,0) // Data to write into register
}
