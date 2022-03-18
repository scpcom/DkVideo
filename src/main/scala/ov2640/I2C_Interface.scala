package dkvideo
package ov2640

import chisel3._
import chisel3.util.Cat

////////////////////////////////////////////////////////////////////////////////////////////////////////
// I2C_Interface.v
//
// Author:			Thanh Tien Truong
//
// Description:
// ------------
// SCCB interface to communicate with Camera
//  - Implementing two-wire data transmission of SCCB protocol
//	- Using 3-phase write transmission cycle of SCCB protocol
//
//////////////////////////////////////////////////////////////////////////////////////////////////////

class I2C_Interface(
    val SID: UInt = "h42".U(8.W)
  ) extends Module {
  val io = IO(new Bundle {
    val clk = Input(Clock()) // 50Mhz clock signal
    val siod = Output(Bool()) // Inout Data signal for SCCB
    val sioc = Output(Bool()) // Clock signal for SCCB
    val taken = Output(Bool()) // Flag to go to next address of LUT
    val send = Input(Bool()) // Flag to indicate if configuration is finshed
    val rega = Input(UInt(8.W)) // Resgister address
    val value = Input(UInt(8.W)) // Data to write into a regsiter address
  })


  // Internal signals
  val divider = RegInit(UInt(8.W), "b00000001".U(8.W))
  val busy_sr = RegInit(UInt(32.W), (VecInit.tabulate(32)(_ => false.B)).asUInt)
  val data_sr = RegInit(UInt(32.W), (VecInit.tabulate(32)(_ => true.B)).asUInt)
  val sioc_temp = RegInit(false.B)
  val taken_temp = Reg(Bool())
  val siod_temp = Wire(Bool())

  // ID of an Camera for SCCB protocol
  val id: UInt = SID //8'h42;

  // Assign value for outputs
  io.siod := siod_temp
  io.sioc := sioc_temp
  io.taken := taken_temp

  // when the bus is idle SIOD must be tri-state
  when(((busy_sr(11,10) === "b10".U(2.W)) || (busy_sr(20,19) === "b10".U(2.W))) || (busy_sr(29,28) === "b10".U(2.W))) {
    siod_temp := false.B
  // else SIOD will be driven my master (FPGA board)
  } .otherwise {
    siod_temp := data_sr(31)
  }
  taken_temp := false.B

  // If all 31 bits are transmitted
  when(busy_sr(31) === false.B) {
    // Assert SIOC high for starting new transmission
    sioc_temp := true.B

    // If New data is arrived from LUT
    when(io.send === true.B) {
      when(divider === "b00000000".U(8.W)) {
        // Create an data to send through the data signal of the SCCB
        // The data is created using 3-phase write transmission cycle of SCCB protocol
        //
        // Data:
        // 3'b100 --> SIOD will go from 1 to 0 to indicate a start transmission
        //            the last bit is an don't care bit
        // id     --> the ID of a slave (8'h42). The last bit of the slave is 0 inidicate a write transaction
        // 1'b0   --> don't care bit to seperate phases
        // rega   --> register address that want to write into
        // 1'b0   --> don't care bit to seperate phases
        // value  --> data to write into the register address
        // 1'b0   --> don't care bit to seperate phases
        // 2'b01  --> SIOD will go from 0 to 1 to indicate a stop tranmission

        data_sr := Cat("b100".U(3.W), id, "b0".U(1.W), io.rega, "b0".U(1.W), io.value, "b0".U(1.W), "b01".U(2.W))
        busy_sr := Cat("b111".U(3.W), "b111111111".U(9.W), "b111111111".U(9.W), "b111111111".U(9.W), "b11".U(2.W))
        taken_temp := true.B
      } .otherwise {
        divider := divider+"b1".U(1.W)
      }
    }

  // Implement two-write data transmission of SCCB protocol
  } .otherwise {
    // Checking for the start and stop condition
    // For START condition
    when(Cat(busy_sr(31,29), busy_sr(2,0)) === "b111_111".U(6.W)) { // bit 31th of data_sr is transmitted, SIOC must be high
      when(divider(7,6) === "b00".U(2.W)) { // --> SIOD goes from tri-state to high
        sioc_temp := true.B
      } .elsewhen (divider(7,6) === "b01".U(2.W)) {
        sioc_temp := true.B
      } .elsewhen (divider(7,6) === "b10".U(2.W)) {
        sioc_temp := true.B
      } .otherwise {
        sioc_temp := true.B
      }

    } .elsewhen (Cat(busy_sr(31,29), busy_sr(2,0)) === "b111_110".U(6.W)) { // bit 30th of data_sr is transmitted
      when(divider(7,6) === "b00".U(2.W)) {  // --> SIOD goes from high to low, SIOC must be high
        sioc_temp := true.B // --> complete START condition
      } .elsewhen (divider(7,6) === "b01".U(2.W)) {
        sioc_temp := true.B
      } .elsewhen (divider(7,6) === "b10".U(2.W)) {
        sioc_temp := true.B
      } .otherwise {
        sioc_temp := true.B
      }

    } .elsewhen (Cat(busy_sr(31,29), busy_sr(2,0)) === "b111_100".U(6.W)) { // bit 29th of data_sr is transmitted (don't care bit)
      when(divider(7,6) === "b00".U(2.W)) { // --> SIOD goes from tri-state to high, then high to low
        sioc_temp := false.B //     after SIOC goes from high to low
      } .elsewhen (divider(7,6) === "b01".U(2.W)) {
        sioc_temp := false.B // --> Ready for first transmission from Master to Slave
      } .elsewhen (divider(7,6) === "b10".U(2.W)) {
        sioc_temp := false.B
      } .otherwise {
        sioc_temp := false.B
      }

    // For STOP condition
    } .elsewhen (Cat(busy_sr(31,29), busy_sr(2,0)) === "b110_000".U(6.W)) { // bit 2nd of data_sr is transmitted (don't care bit)
      when(divider(7,6) === "b00".U(2.W)) { // SIOC waits for 1 clock cyle of 200Khz then go high
        sioc_temp := false.B
      } .elsewhen (divider(7,6) === "b01".U(2.W)) {
        sioc_temp := true.B
      } .elsewhen (divider(7,6) === "b10".U(2.W)) {
        sioc_temp := true.B
      } .otherwise {
        sioc_temp := true.B
      }

    } .elsewhen (Cat(busy_sr(31,29), busy_sr(2,0)) === "b100_000".U(6.W)) { // bit 1st of data_sr is transmitted
      when(divider(7,6) === "b00".U(2.W)) { // SIOD is low
        sioc_temp := true.B // SIOC must be high
      } .elsewhen (divider(7,6) === "b01".U(2.W)) {
        sioc_temp := true.B
      } .elsewhen (divider(7,6) === "b10".U(2.W)) {
        sioc_temp := true.B
      } .otherwise {
        sioc_temp := true.B
      }

    } .elsewhen (Cat(busy_sr(31,29), busy_sr(2,0)) === "b000_000".U(6.W)) { // bit 0th of data_sr is transmitted
      when(divider(7,6) === "b00".U(2.W)) { // SIOD is high
        sioc_temp := true.B // --> SIOD goes from low to high while SIOC is high
      } .elsewhen (divider(7,6) === "b01".U(2.W)) {
        sioc_temp := true.B // --> complete STOP condition for SCCB protocol
      } .elsewhen (divider(7,6) === "b10".U(2.W)) {
        sioc_temp := true.B
      } .otherwise {
        sioc_temp := true.B
      }

    // Between START and STOP condition
    // SIOC is high on 2 cycles of 400Khz and low on 2 cycle of 400Khz
    // --> SIOC is 200Khz
    } .otherwise {
      when(divider(7,6) === "b00".U(2.W)) {
        sioc_temp := false.B
      } .elsewhen (divider(7,6) === "b01".U(2.W)) {
        sioc_temp := true.B
      } .elsewhen (divider(7,6) === "b10".U(2.W)) {
        sioc_temp := true.B
      } .otherwise {
        sioc_temp := false.B
      }
    }

    // Create a frequency for SCCB with is 200KHz
    when(divider === "b11111111".U(8.W)) {
      busy_sr := Cat(busy_sr(30,0), "b0".U(1.W)) // Update number of bit that get transmitted
      data_sr := Cat(data_sr(30,0), "b1".U(1.W)) // Update new bit 31th by bit 30th
      divider := VecInit.tabulate(8)(_ => false.B).asUInt // Reset counter for clock divider

    } .otherwise {
      divider := divider+"b1".U(1.W)
    }

  }
}
