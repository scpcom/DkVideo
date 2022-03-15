package hdl
package gowin_pllvr

import chisel3._
import chisel3.util._
import chisel3.experimental.Param

case class PLLParams(
  val IDIV_SEL: Byte,
  val FBDIV_SEL: Byte,
  val ODIV_SEL: Byte,
  val DYN_SDIV_SEL: Byte
)

/* PLLVR */
class PLLVR(val pm: Map[String, Param]) extends BlackBox(pm){
    val io = IO(new Bundle{
        val CLKOUT = Output(Clock())
        val LOCK = Output(UInt(1.W))
        val CLKOUTP = Output(Clock())
        val CLKOUTD = Output(Clock())
        val CLKOUTD3 = Output(Clock())
        val RESET = Input(UInt(1.W))
        val RESET_P = Input(UInt(1.W))
        val CLKIN = Input(Clock())
        val CLKFB = Input(Bool())
        val FBDSEL = Input(UInt(6.W))
        val IDSEL = Input(UInt(6.W))
        val ODSEL = Input(UInt(6.W))
        val PSDA = Input(UInt(4.W))
        val DUTYDA = Input(UInt(4.W))
        val FDLY = Input(UInt(4.W))
        val VREN = Input(UInt(1.W))
    })
}

// PLLParams(IDIV_SEL = 3, FBDIV_SEL = 54, ODIV_SEL = 2, DYN_SDIV_SEL = 30) # CLKOUT_FREQ=371.25 / 5 = 74.25 MHz
// PLLParams(IDIV_SEL = 0, FBDIV_SEL = 11, ODIV_SEL = 2, DYN_SDIV_SEL = 26) # CLKOUT_FREQ=325    / 5 = 65.00 MHz
// PLLParams(IDIV_SEL = 4, FBDIV_SEL = 36, ODIV_SEL = 4, DYN_SDIV_SEL = 16) # CLKOUT_FREQ=200    / 5 = 40.00 MHz

/* TMDS PLLVR */
class TMDS_PLLVR(pp: PLLParams = PLLParams(IDIV_SEL = 3, FBDIV_SEL = 54, ODIV_SEL = 2, DYN_SDIV_SEL = 30)) extends RawModule {
    val io = IO(new Bundle {
        val clkin = Input(Clock())
        val clkout = Output(Clock())
        val clkoutd = Output(Clock())
        val lock = Output(Bool())
    })

  val pm: Map[String, Param] = Map(
  "FCLKIN" -> "27",
  "DYN_IDIV_SEL" -> "false",
  "IDIV_SEL" -> pp.IDIV_SEL,
  "DYN_FBDIV_SEL" -> "false",
  "FBDIV_SEL" -> pp.FBDIV_SEL,
  "DYN_ODIV_SEL" -> "false",
  "ODIV_SEL" -> pp.ODIV_SEL,
  "PSDA_SEL" -> "0000",
  "DYN_DA_EN" -> "false",
  "DUTYDA_SEL" -> "1000",
  "CLKOUT_FT_DIR" -> "1'b1",
  "CLKOUTP_FT_DIR" -> "1'b1",
  "CLKOUT_DLY_STEP" -> 0,
  "CLKOUTP_DLY_STEP" -> 0,
  "CLKFB_SEL" -> "internal",
  "CLKOUT_BYPASS" -> "false",
  "CLKOUTP_BYPASS" -> "false",
  "CLKOUTD_BYPASS" -> "false",
  "DYN_SDIV_SEL" -> pp.DYN_SDIV_SEL,
  "CLKOUTD_SRC" -> "CLKOUT",
  "CLKOUTD3_SRC" -> "CLKOUT",
  "DEVICE" -> "GW1NSR-4C")

  val clkoutp_o = Wire(Clock())
  var clkoutd3_o = Wire(Clock())
  val gw_vcc = Wire(UInt(1.W))
  val gw_gnd = Wire(UInt(1.W))

  gw_vcc := 1.U(1.W)
  gw_gnd := 0.U(1.W)

  val pllvr_inst = Module(new PLLVR(pm))

  io.clkout := pllvr_inst.io.CLKOUT
  io.lock := pllvr_inst.io.LOCK
  clkoutp_o := pllvr_inst.io.CLKOUTP
  io.clkoutd := pllvr_inst.io.CLKOUTD
  clkoutd3_o := pllvr_inst.io.CLKOUTD3

  pllvr_inst.io.RESET := gw_gnd
  pllvr_inst.io.RESET_P := gw_gnd
  pllvr_inst.io.CLKIN := io.clkin
  pllvr_inst.io.CLKFB := gw_gnd
  pllvr_inst.io.FBDSEL := Cat(gw_gnd,gw_gnd,gw_gnd,gw_gnd,gw_gnd,gw_gnd)
  pllvr_inst.io.IDSEL := Cat(gw_gnd,gw_gnd,gw_gnd,gw_gnd,gw_gnd,gw_gnd)
  pllvr_inst.io.ODSEL := Cat(gw_gnd,gw_gnd,gw_gnd,gw_gnd,gw_gnd,gw_gnd)
  pllvr_inst.io.PSDA := Cat(gw_gnd,gw_gnd,gw_gnd,gw_gnd)
  pllvr_inst.io.DUTYDA := Cat(gw_gnd,gw_gnd,gw_gnd,gw_gnd)
  pllvr_inst.io.FDLY := Cat(gw_gnd,gw_gnd,gw_gnd,gw_gnd)
  pllvr_inst.io.VREN := gw_vcc
}

/* HyperRAM PLLVR (GW1NSR-4C) */
class GW_PLLVR extends RawModule {
    val io = IO(new Bundle {
        val clkin = Input(Clock())
        val clkout = Output(Clock())
        val lock = Output(Bool())
    })

  val pm: Map[String, Param] = Map(
  "FCLKIN" -> "27",
  "DYN_IDIV_SEL" -> "false",
  "IDIV_SEL" -> 8,
  "DYN_FBDIV_SEL" -> "false",
  "FBDIV_SEL" -> 52,
  "DYN_ODIV_SEL" -> "false",
  "ODIV_SEL" -> 4,
  "PSDA_SEL" -> "0000",
  "DYN_DA_EN" -> "true",
  "DUTYDA_SEL" -> "1000",
  "CLKOUT_FT_DIR" -> "1'b1",
  "CLKOUTP_FT_DIR" -> "1'b1",
  "CLKOUT_DLY_STEP" -> 0,
  "CLKOUTP_DLY_STEP" -> 0,
  "CLKFB_SEL" -> "internal",
  "CLKOUT_BYPASS" -> "false",
  "CLKOUTP_BYPASS" -> "false",
  "CLKOUTD_BYPASS" -> "false",
  "DYN_SDIV_SEL" -> 2,
  "CLKOUTD_SRC" -> "CLKOUT",
  "CLKOUTD3_SRC" -> "CLKOUT",
  "DEVICE" -> "GW1NSR-4C")

  val clkoutp_o = Wire(Clock())
  val clkoutd_o = Wire(Clock())
  var clkoutd3_o = Wire(Clock())
  val gw_vcc = Wire(UInt(1.W))
  val gw_gnd = Wire(UInt(1.W))

  gw_vcc := 1.U(1.W)
  gw_gnd := 0.U(1.W)

  val pllvr_inst = Module(new PLLVR(pm))

  io.clkout := pllvr_inst.io.CLKOUT
  io.lock := pllvr_inst.io.LOCK
  clkoutp_o := pllvr_inst.io.CLKOUTP
  clkoutd_o := pllvr_inst.io.CLKOUTD
  clkoutd3_o := pllvr_inst.io.CLKOUTD3

  pllvr_inst.io.RESET := gw_gnd
  pllvr_inst.io.RESET_P := gw_gnd
  pllvr_inst.io.CLKIN := io.clkin
  pllvr_inst.io.CLKFB := gw_gnd
  pllvr_inst.io.FBDSEL := Cat(gw_gnd,gw_gnd,gw_gnd,gw_gnd,gw_gnd,gw_gnd)
  pllvr_inst.io.IDSEL := Cat(gw_gnd,gw_gnd,gw_gnd,gw_gnd,gw_gnd,gw_gnd)
  pllvr_inst.io.ODSEL := Cat(gw_gnd,gw_gnd,gw_gnd,gw_gnd,gw_gnd,gw_gnd)
  pllvr_inst.io.PSDA := Cat(gw_gnd,gw_gnd,gw_gnd,gw_gnd)
  pllvr_inst.io.DUTYDA := Cat(gw_gnd,gw_gnd,gw_gnd,gw_gnd)
  pllvr_inst.io.FDLY := Cat(gw_gnd,gw_gnd,gw_gnd,gw_gnd)
  pllvr_inst.io.VREN := gw_vcc
}
