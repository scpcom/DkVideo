package hdl.gowin

import chisel3._
import chisel3.experimental.Analog

//
//Written by GowinSynthesis
//Product Version "GowinSynthesis V1.9.8.03"
//Fri Apr  1 17:55:21 2022

//Source file index table:
//file0 "\/build/Gowin/Gowin_V1.9.8.03_linux/IDE/ipcore/PSRAM_HS/data/PSRAM_TOP.v"
//file1 "\/build/Gowin/Gowin_V1.9.8.03_linux/IDE/ipcore/PSRAM_HS/data/psram_code.v"
// `timescale100ps/100ps


class PSRAM_Memory_Interface_HS_Top() extends BlackBox {
    val io = IO(new Bundle {
        val clk = Input(Clock())
        val memory_clk = Input(Clock())
        val pll_lock = Input(Bool())
        val rst_n = Input(Bool())
        val O_psram_ck = Output(UInt(2.W))
        val O_psram_ck_n = Output(UInt(2.W))
        val IO_psram_dq = Analog(16.W)
        val IO_psram_rwds = Analog(2.W)
        val O_psram_cs_n = Output(UInt(2.W))
        val O_psram_reset_n = Output(UInt(2.W))
        val wr_data = Input(UInt(64.W))
        val rd_data = Output(UInt(64.W))
        val rd_data_valid = Output(Bool())
        val addr = Input(UInt(21.W))
        val cmd = Input(Bool())
        val cmd_en = Input(Bool())
        val init_calib = Output(Bool())
        val clk_out = Output(Clock())
        val data_mask = Input(UInt(8.W))
    })
} /* PSRAM_Memory_Interface_HS_Top */
