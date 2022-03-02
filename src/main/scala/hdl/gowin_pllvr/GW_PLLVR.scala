package hdl
package gowin_pllvr

import chisel3._
import chisel3.util._

class GW_PLLVR extends BlackBox {
    val io = IO(new Bundle {
        val clkin = Input(Clock())
        val clkout = Output(Clock())
        val lock = Output(Bool())
    })
}

