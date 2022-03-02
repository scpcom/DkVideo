package hdl
package video_frame_buffer

import chisel3._
import sv2chisel.helpers.vecconvert._
//
//Written by GowinSynthesis
//Product Version "GowinSynthesis V1.9.8.03"
//Mon Feb 14 23:30:58 2022

//Source file index table:
//file0 "\D:/Gowin/Gowin_V1.9.8.03/IDE/ipcore/VFB/data/vfb_top.v"
//file1 "\D:/Gowin/Gowin_V1.9.8.03/IDE/ipcore/VFB/data/vfb_wrapper.vp"
// `timescale100ps/100ps


class Video_Frame_Buffer_Top() extends BlackBox {
  val I_rst_n = IO(Input(Bool()))
  val I_dma_clk = IO(Input(Bool()))
  val I_wr_halt = IO(Input(Vec(1, Bool())))
  val I_rd_halt = IO(Input(Vec(1, Bool())))
  val I_vin0_clk = IO(Input(Bool()))
  val I_vin0_vs_n = IO(Input(Bool()))
  val I_vin0_de = IO(Input(Bool()))
  val I_vin0_data = IO(Input(Vec(16, Bool())))
  val O_vin0_fifo_full = IO(Output(Bool()))
  val I_vout0_clk = IO(Input(Bool()))
  val I_vout0_vs_n = IO(Input(Bool()))
  val I_vout0_de = IO(Input(Bool()))
  val O_vout0_den = IO(Output(Bool()))
  val O_vout0_data = IO(Output(Vec(16, Bool())))
  val O_vout0_fifo_empty = IO(Output(Bool()))
  val O_cmd = IO(Output(Bool()))
  val O_cmd_en = IO(Output(Bool()))
  val O_addr = IO(Output(Vec(22, Bool())))
  val O_wr_data = IO(Output(Vec(32, Bool())))
  val O_data_mask = IO(Output(Vec(4, Bool())))
  val I_rd_data_valid = IO(Input(Bool()))
  val I_rd_data = IO(Input(Vec(32, Bool())))
  val I_init_calib = IO(Input(Bool()))
  val VCC = Wire(Bool()) 
  val GND = Wire(Bool()) 
  /*val vfb_hyperram_wrapper_inst = Module(new \~vfb_hyperram_wrapper.Video_Frame_Buffer_Top  )
  vfb_hyperram_wrapper_inst.I_dma_clk := I_dma_clk
  vfb_hyperram_wrapper_inst.I_rst_n := I_rst_n
  vfb_hyperram_wrapper_inst.I_init_calib := I_init_calib
  vfb_hyperram_wrapper_inst.I_rd_data_valid := I_rd_data_valid
  vfb_hyperram_wrapper_inst.I_vin0_clk := I_vin0_clk
  vfb_hyperram_wrapper_inst.I_vin0_vs_n := I_vin0_vs_n
  vfb_hyperram_wrapper_inst.I_vin0_de := I_vin0_de
  vfb_hyperram_wrapper_inst.I_vout0_clk := I_vout0_clk
  vfb_hyperram_wrapper_inst.I_vout0_vs_n := I_vout0_vs_n
  vfb_hyperram_wrapper_inst.I_vout0_de := I_vout0_de
  vfb_hyperram_wrapper_inst.I_rd_data := I_rd_data(31,0)
  vfb_hyperram_wrapper_inst.I_vin0_data := I_vin0_data(15,0)
  vfb_hyperram_wrapper_inst.I_wr_halt := I_wr_halt(0)
  vfb_hyperram_wrapper_inst.I_rd_halt := I_rd_halt(0)
  O_cmd_en := vfb_hyperram_wrapper_inst.O_cmd_en.asTypeOf(O_cmd_en)
  O_cmd := vfb_hyperram_wrapper_inst.O_cmd.asTypeOf(O_cmd)
  O_vin0_fifo_full := vfb_hyperram_wrapper_inst.O_vin0_fifo_full.asTypeOf(O_vin0_fifo_full)
  O_vout0_den := vfb_hyperram_wrapper_inst.O_vout0_den.asTypeOf(O_vout0_den)
  O_vout0_fifo_empty := vfb_hyperram_wrapper_inst.O_vout0_fifo_empty.asTypeOf(O_vout0_fifo_empty)
  O_wr_data(31,0) := vfb_hyperram_wrapper_inst.O_wr_data.asTypeOf(O_wr_data(31,0))
  O_addr(21,6) := vfb_hyperram_wrapper_inst.O_addr.asTypeOf(O_addr(21,6))
  O_vout0_data(15,0) := vfb_hyperram_wrapper_inst.O_vout0_data.asTypeOf(O_vout0_data(15,0))
  val VCC_cZ = Module(new VCC)
  VCC_cZ.V <> VCC
  val GND_cZ = Module(new GND)
  GND := GND_cZ.G.asTypeOf(GND)
  val GSR = Module(new GSR)
  GSR.GSRI <> VCC
  O_addr(0) := GND
  O_addr(1) := GND
  O_addr(2) := GND
  O_addr(3) := GND
  O_addr(4) := GND
  O_addr(5) := GND
  O_data_mask(0) := GND
  O_data_mask(1) := GND
  O_data_mask(2) := GND
  O_data_mask(3) := GND*/
} /* Video_Frame_Buffer_Top */
