package hdl.gowin

import chisel3._

//
//Written by GowinSynthesis
//Product Version "GowinSynthesis V1.9.8.03"
//Fri Apr  1 20:23:55 2022

//Source file index table:
//file0 "\/build/Gowin/Gowin_V1.9.8.03_linux/IDE/ipcore/VFB_PSRAM/data/vfb_psram_top.v"
//file1 "\/build/Gowin/Gowin_V1.9.8.03_linux/IDE/ipcore/VFB_PSRAM/data/vfb_psram_wrapper.vp"
// `timescale100ps/100ps


class VFB_PSRAM_Top() extends BlackBox {
    val io = IO(new Bundle {
        val I_rst_n = Input(Bool())
        val I_dma_clk = Input(Clock())
        val I_wr_halt = Input(Bool())
        val I_rd_halt = Input(Bool())
        val I_vin0_clk = Input(Clock())
        val I_vin0_vs_n = Input(Bool())
        val I_vin0_de = Input(Bool())
        val I_vin0_data = Input(UInt(16.W))
        val O_vin0_fifo_full = Output(Bool())
        val I_vout0_clk = Input(Clock())
        val I_vout0_vs_n = Input(Bool())
        val I_vout0_de = Input(Bool())
        val O_vout0_den = Output(Bool())
        val O_vout0_data = Output(UInt(16.W))
        val O_vout0_fifo_empty = Output(Bool())
        val O_cmd = Output(Bool())
        val O_cmd_en = Output(Bool())
        val O_addr = Output(UInt(21.W))
        val O_wr_data = Output(UInt(64.W))
        val O_data_mask = Output(UInt(8.W))
        val I_rd_data_valid = Input(Bool())
        val I_rd_data = Input(UInt(64.W))
        val I_init_calib = Input(Bool())
    })
} /* VFB_PSRAM_Top */
