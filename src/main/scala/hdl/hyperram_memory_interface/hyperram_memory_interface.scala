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
} /* HyperRAM_Memory_Interface_Top */
