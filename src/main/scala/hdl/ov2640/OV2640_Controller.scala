package hdl
package ov2640

import chisel3._
import sv2chisel.helpers.vecconvert._

class OV2640_Controller() extends Module { // 50Mhz clock signal
  val resend = IO(Input(Bool())) // Reset signal
  val config_finished = IO(Output(Bool())) // Flag to indicate that the configuration is finished
  val sioc = IO(Output(Bool())) // SCCB interface - clock signal
  val siod = IO(Inout(Bool())) // SCCB interface - data signal
  val ureset = IO(Output(Bool())) // RESET signal for OV2640
  val pwdn = IO(Output(Bool())) // PWDN signal for OV2640


  // Internal signals
  val command = Wire(Vec(16, Bool())) 
  val finished = Wire(Bool()) 
  val taken = Wire(Bool()) 
  val send = WireDefault(Bool(), false.B) 

  // Signal for testing
  config_finished := finished

  // Signals for RESET and PWDN OV2640
  ureset := true.B
  pwdn := false.B

  // Signal to indicate that the configuration is finshied    

  send :=  ~finished

  // Create an instance of a LUT table 
  val LUT = Module(new OV2640_Registers) // 50Mhz clock signal
  LUT.advance := taken // Flag to advance to next register
  command := LUT.command.asTypeOf(command) // register value and data for OV2640
  finished := LUT.finished // Flag to indicate the configuration is finshed
  LUT.resend := resend // Re-configure flag for OV2640


  // Create an instance of a SCCB interface
  val I2C = Module(new I2C_Interface(
      SID = "h60".U(8.W)
  )) // 50Mhz clock signal
  taken := I2C.taken // Flag to advance to next register
  I2C.siod := siod // Clock signal for SCCB interface
  sioc := I2C.sioc // Data signal for SCCB interface 
  I2C.send := send // Continue to configure OV2640
  I2C.rega := command(15,8).asTypeOf(I2C.rega) // Register address
  I2C.value := command(7,0).asTypeOf(I2C.value) // Data to write into register


}