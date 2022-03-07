package hdl
package hyperram_memory_interface

import chisel3._

//
//Written by GowinSynthesis
//Product Version "GowinSynthesis V1.9.8.03"
//Mon Feb 14 00:31:55 2022

//Source file index table:
//file0 "\D:/Gowin/Gowin_V1.9.8.03/IDE/ipcore/HYPERRAM_EMB/data/HPRAM_TOP.v"
//file1 "\D:/Gowin/Gowin_V1.9.8.03/IDE/ipcore/HYPERRAM_EMB/data/hpram_code_166.v"
// `timescale100ps/100ps


class HyperRAM_Memory_Interface_Top() extends BlackBox {
    val io = IO(new Bundle {
        val clk = Input(Clock())
        val memory_clk = Input(Clock())
        val pll_lock = Input(Bool())
        val rst_n = Input(Bool())
        val O_hpram_ck = Output(Bool())
        val O_hpram_ck_n = Output(Bool())
        val IO_hpram_dq = Input(UInt(8.W)) // Inout
        val IO_hpram_rwds = Input(Bool()) // Inout
        val O_hpram_cs_n = Output(Bool())
        val O_hpram_reset_n = Output(Bool())
        val wr_data = Input(UInt(32.W))
        val rd_data = Output(UInt(32.W))
        val rd_data_valid = Output(Bool())
        val addr = Input(UInt(22.W))
        val cmd = Input(Bool())
        val cmd_en = Input(Bool())
        val init_calib = Output(Bool())
        val clk_out = Output(Clock())
        val data_mask = Input(UInt(4.W))
    })
  /*val VCC = Wire(Bool()) 
  val GND = Wire(Bool()) 
  val u_hpram_top = Module(new \~hpram_top.HyperRAM_Memory_Interface_Top  )
  u_hpram_top.memory_clk := memory_clk
  u_hpram_top.rst_n := rst_n
  u_hpram_top.pll_lock := pll_lock
  u_hpram_top.cmd_en := cmd_en
  u_hpram_top.cmd := cmd
  u_hpram_top.clk := clk
  u_hpram_top.wr_data := wr_data(31,0)
  u_hpram_top.addr := addr(21,0)
  u_hpram_top.data_mask := data_mask(3,0)
  clk_out := u_hpram_top.clk_out.asTypeOf(clk_out)
  rd_data_valid := u_hpram_top.rd_data_valid.asTypeOf(rd_data_valid)
  init_calib := u_hpram_top.init_calib.asTypeOf(init_calib)
  rd_data(31,0) := u_hpram_top.rd_data.asTypeOf(rd_data(31,0))
  O_hpram_ck(0) := u_hpram_top.O_hpram_ck.asTypeOf(O_hpram_ck(0))
  O_hpram_ck_n(0) := u_hpram_top.O_hpram_ck_n.asTypeOf(O_hpram_ck_n(0))
  O_hpram_cs_n(0) := u_hpram_top.O_hpram_cs_n.asTypeOf(O_hpram_cs_n(0))
  O_hpram_reset_n(0) := u_hpram_top.O_hpram_reset_n.asTypeOf(O_hpram_reset_n(0))
  u_hpram_top.IO_hpram_dq := IO_hpram_dq(7,0)
  u_hpram_top.IO_hpram_rwds := IO_hpram_rwds(0)
  val VCC_cZ = Module(new VCC)
  VCC_cZ.V <> VCC
  val GND_cZ = Module(new GND)
  GND_cZ.G <> GND
  val GSR = Module(new GSR)
  GSR.GSRI <> VCC*/
} /* HyperRAM_Memory_Interface_Top */
