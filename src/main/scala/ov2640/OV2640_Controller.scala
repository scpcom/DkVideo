package dkvideo
package ov2640

import chisel3._

class OV2640_Controller() extends Module {
  val io = IO(new Bundle {
    val clk = Input(Clock()) // 50Mhz clock signal
    val resend = Input(Bool()) // Reset signal
    val config_finished = Output(Bool()) // Flag to indicate that the configuration is finished
    val sioc = Output(Bool()) // SCCB interface - clock signal
    val siod = Output(Bool()) // Inout SCCB interface - data signal
    val reset = Output(Bool()) // RESET signal for OV2640
    val pwdn = Output(Bool()) // PWDN signal for OV2640
  })

  // Internal signals
  val command = Wire(UInt(16.W))
  val finished = Wire(Bool()) 
  val taken = Wire(Bool()) 
  val send = WireDefault(Bool(), false.B) 

  // Signal for testing
  io.config_finished := finished

  // Signals for RESET and PWDN OV2640
  io.reset := true.B
  io.pwdn := false.B

  // Signal to indicate that the configuration is finshied

  send :=  ~finished

  // Create an instance of a LUT table 
  val LUT = Module(new OV2640_Registers) // 50Mhz clock signal
  LUT.io.clk := io.clk
  LUT.io.advance := taken // Flag to advance to next register
  command := LUT.io.command // register value and data for OV2640
  finished := LUT.io.finished // Flag to indicate the configuration is finshed
  LUT.io.resend := io.resend // Re-configure flag for OV2640


  // Create an instance of a SCCB interface
  val I2C = Module(new I2C_Interface(
      SID = "h60".U(8.W)
  ))
  I2C.io.clk := io.clk // 50Mhz clock signal
  taken := I2C.io.taken // Flag to advance to next register
  io.siod := I2C.io.siod // Clock signal for SCCB interface
  io.sioc := I2C.io.sioc // Data signal for SCCB interface
  I2C.io.send := send // Continue to configure OV2640
  I2C.io.rega := command(15,8) // Register address
  I2C.io.value := command(7,0) // Data to write into register


}
