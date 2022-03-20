package hdl.gowin

import chisel3._

//
//Written by GowinSynthesis
//Product Version "GowinSynthesis V1.9.8.03"
//Mon Feb 14 00:27:29 2022

//Source file index table:
//file0 "\D:/Gowin/Gowin_V1.9.8.03/IDE/ipcore/DVI_TX/data/dvi_tx_top.v"
//file1 "\D:/Gowin/Gowin_V1.9.8.03/IDE/ipcore/DVI_TX/data/rgb2dvi.vp"
// `timescale100ps/100ps


class DVI_TX_Top() extends BlackBox {
    val io = IO(new Bundle {
        val I_rst_n = Input(Bool())
        val I_serial_clk = Input(Clock())
        val I_rgb_clk = Input(Clock())
        val I_rgb_vs = Input(Bool())
        val I_rgb_hs = Input(Bool())
        val I_rgb_de = Input(Bool())
        val I_rgb_r = Input(UInt(8.W))
        val I_rgb_g = Input(UInt(8.W))
        val I_rgb_b = Input(UInt(8.W))
        val O_tmds_clk_p = Output(Bool())
        val O_tmds_clk_n = Output(Bool())
        val O_tmds_data_p = Output(UInt(3.W))
        val O_tmds_data_n = Output(UInt(3.W))
    })
} /* DVI_TX_Top */
