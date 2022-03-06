package hdl

import chisel3._
import chisel3.util.Cat
import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}
import sv2chisel.helpers.tools.VerilogPortWrapper
import sv2chisel.helpers.vecconvert._

import fpgamacro.gowin.{CLKDIV, TMDS_PLLVR, TLVDS_OBUF}
import hdl.dvi_tx.DVI_TX_Top
import hdl.ov2640.OV2640_Controller
import hdl.video_frame_buffer.Video_Frame_Buffer_Top
import hdl.gowin_pllvr.GW_PLLVR
import hdl.hyperram_memory_interface.HyperRAM_Memory_Interface_Top
import hdl.syn_code.syn_gen

// ==============0ooo===================================================0ooo===========
// =  Copyright (C) 2014-2020 Gowin Semiconductor Technology Co.,Ltd.
// =                     All rights reserved.
// ====================================================================================
// 
//  __      __      __
//  \ \    /  \    / /   [File name   ] video_top.v
//   \ \  / /\ \  / /    [Description ] Video demo
//    \ \/ /  \ \/ /     [Timestamp   ] Friday May 26 14:00:30 2019
//     \  /    \  /      [version     ] 1.0.0
//      \/      \/
//
// ==============0ooo===================================================0ooo===========
// Code Revision History :
// ----------------------------------------------------------------------------------
// Ver:    |  Author    | Mod. Date    | Changes Made:
// ----------------------------------------------------------------------------------
// V1.0    | Caojie     | 11/22/19     | Initial version 
// ----------------------------------------------------------------------------------
// ==============0ooo===================================================0ooo===========

class video_top() extends RawModule {
  val I_clk = IO(Input(Clock())) //27Mhz
  val I_rst_n = IO(Input(Bool()))
  val O_led = IO(Output(UInt(2.W)))
  val SDA = IO(Output(Bool())) // Inout
  val SCL = IO(Output(Bool())) // Inout
  val VSYNC = IO(Input(Bool()))
  val HREF = IO(Input(Bool()))
  val PIXDATA = IO(Input(UInt(10.W)))
  val PIXCLK = IO(Input(Clock()))
  val XCLK = IO(Output(Clock()))
  val O_hpram_ck = IO(Output(UInt(1.W)))
  val O_hpram_ck_n = IO(Output(UInt(1.W)))
  val O_hpram_cs_n = IO(Output(UInt(1.W)))
  val O_hpram_reset_n = IO(Output(UInt(1.W)))
  val IO_hpram_dq = IO(Input(UInt(8.W))) // Inout
  val IO_hpram_rwds = IO(Input(UInt(1.W))) // Inout
  val O_tmds_clk_p = IO(Output(Bool()))
  val O_tmds_clk_n = IO(Output(Bool()))
  val O_tmds_data_p = IO(Output(UInt(3.W))) //{r,g,b}
  val O_tmds_data_n = IO(Output(UInt(3.W)))

  //==================================================
  val running = Wire(Bool()) 

  //--------------------------
  val tp0_vs_in = Wire(Bool()) 
  val tp0_hs_in = Wire(Bool()) 
  val tp0_de_in = Wire(Bool()) 
  val tp0_data_r = Wire(UInt(8.W))  /*synthesis syn_keep=1*/
  val tp0_data_g = Wire(UInt(8.W))  /*synthesis syn_keep=1*/
  val tp0_data_b = Wire(UInt(8.W))  /*synthesis syn_keep=1*/

  //--------------------------
  val cam_data = Wire(UInt(16.W)) 

  //-------------------------
  //frame buffer in
  val ch0_vfb_clk_in = Wire(Clock()) 
  val ch0_vfb_vs_in = Wire(Bool()) 
  val ch0_vfb_de_in = Wire(Bool()) 
  val ch0_vfb_data_in = Wire(UInt(16.W)) 

  //-------------------
  //syn_code
  val syn_off0_re = Wire(Bool())  // ofifo read enable signal
  val syn_off0_vs = Wire(Bool()) 
  val syn_off0_hs = Wire(Bool()) 

  val off0_syn_de = Wire(Bool()) 
  val off0_syn_data = Wire(UInt(16.W))

  //-------------------------------------
  //Hyperram
  val dma_clk = Wire(Clock())

  val memory_clk = Wire(Clock())
  val mem_pll_lock = Wire(Bool()) 

  //-------------------------------------------------
  //memory interface
  val cmd = Wire(Bool()) 
  val cmd_en = Wire(Bool()) 
  val addr = Wire(UInt(22.W))  //[ADDR_WIDTH-1:0]
  val wr_data = Wire(UInt(32.W))  //[DATA_WIDTH-1:0]
  val data_mask = Wire(UInt(4.W)) 
  val rd_data_valid = Wire(Bool()) 
  val rd_data = Wire(UInt(32.W))  //[DATA_WIDTH-1:0]
  val init_calib = Wire(Bool()) 

  //------------------------------------------
  //rgb data
  val rgb_vs = Wire(Bool()) 
  val rgb_hs = Wire(Bool()) 
  val rgb_de = Wire(Bool()) 
  val rgb_data = Wire(UInt(24.W))

  //------------------------------------
  //HDMI TX
  val serial_clk = Wire(Clock())
  val pll_lock = Wire(Bool()) 

  val hdmi_rst_n = Wire(Bool()) 

  val pix_clk = Wire(Clock())

  val clk_12M = Wire(Clock()) 

  //===================================================
  //LED test
  //I_clk

  val g_cnt_vs = Wire(UInt(10.W))

  withClockAndReset(I_clk, ~I_rst_n) {
  val cnt_vs = RegInit(0.U(10.W))
  val run_cnt = RegInit(0.U(32.W))
  val vs_r = Reg(Bool())

  g_cnt_vs := cnt_vs

  when (run_cnt >= "d27_000_000".U(32.W)) {
    run_cnt := 0.U(32.W)
  } .otherwise {
    run_cnt := run_cnt+"b1".U(1.W)
  }
  running := (Mux((run_cnt < "d13_500_000".U(32.W)), "b1".U(1.W), "b0".U(1.W)) =/= 0.U)
  O_led := running ## ~init_calib
  XCLK := clk_12M
  // NOTE: The following statements are auto generated due to the use of concatenation in port-map of instance testpattern_inst
  //       This default translation is very verbose, you may hence want to refactor it by:
  //          > (TO DO AUTOMATICALLY?) Remove existing wire declaration used in concat {<w1>, <w2>, ...} and rename those wire as <bundleName>.<w1> wherever used.
  //          > Reuse same autogenerated bundle for in and out of extra (use chiselTypeOf())
  /*val testpattern_inst_I_mode = Wire(new Bundle { 
    val 0 = Bool()
    val cnt_vs_7_6 = UInt(((7-6)+1).W)
  }) 
  testpattern_inst_I_mode.0 := false.B
  testpattern_inst_I_mode.cnt_vs_7_6 := cnt_vs(7,6)*/
  val testpattern_inst_I_mode = 0.U(1.W) ## cnt_vs(7,6)

  //===========================================================================
  //testpattern
  val testpattern_inst = Module(new testpattern)
  testpattern_inst.I_pxl_clk := I_clk //pixel clock
  testpattern_inst.I_rst_n := I_rst_n //low active 
  testpattern_inst.I_mode := testpattern_inst_I_mode //data select
  testpattern_inst.I_single_r := 0.U(8.W)
  testpattern_inst.I_single_g := 255.U(8.W)
  testpattern_inst.I_single_b := 0.U(8.W) //800x600    //1024x768   //1280x720    
  testpattern_inst.I_h_total := 1650.U(12.W) //hor total time  // 16'd1056  // 16'd1344  // 16'd1650  
  testpattern_inst.I_h_sync := 40.U(12.W) //hor sync time   // 16'd128   // 16'd136   // 16'd40    
  testpattern_inst.I_h_bporch := 220.U(12.W) //hor back porch  // 16'd88    // 16'd160   // 16'd220   
  testpattern_inst.I_h_res := 800.U(12.W) //hor resolution  // 16'd800   // 16'd1024  // 16'd1280  
  testpattern_inst.I_v_total := 750.U(12.W) //ver total time  // 16'd628   // 16'd806   // 16'd750    
  testpattern_inst.I_v_sync := 5.U(12.W) //ver sync time   // 16'd4     // 16'd6     // 16'd5     
  testpattern_inst.I_v_bporch := 20.U(12.W) //ver back porch  // 16'd23    // 16'd29    // 16'd20    
  testpattern_inst.I_v_res := 600.U(12.W) //ver resolution  // 16'd600   // 16'd768   // 16'd720    
  testpattern_inst.I_hs_pol := "b1".U(1.W) //HS polarity , 0:negetive ploarity，1：positive polarity
  testpattern_inst.I_vs_pol := "b1".U(1.W) //VS polarity , 0:negetive ploarity，1：positive polarity
  tp0_de_in := testpattern_inst.O_de
  tp0_hs_in := testpattern_inst.O_hs
  tp0_vs_in := testpattern_inst.O_vs
  tp0_data_r := testpattern_inst.O_data_r
  tp0_data_g := testpattern_inst.O_data_g
  tp0_data_b := testpattern_inst.O_data_b
  vs_r := tp0_vs_in
  when (cnt_vs === "h3ff".U(10.W)) {
    cnt_vs := cnt_vs
  } .elsewhen (vs_r && ( !tp0_vs_in)) { //vs24 falling edge
    cnt_vs := cnt_vs+"b1".U(1.W)
  } .otherwise {
    cnt_vs := cnt_vs
  }
  } // withClockAndReset(I_clk, ~I_rst_n)

 //==============================================================================
  withClockAndReset(PIXCLK, ~I_rst_n) {
  val pixdata_d1 = RegInit(0.U(10.W))
  val hcnt = RegInit(false.B)

  val u_OV2640_Controller = Module(new OV2640_Controller)
  u_OV2640_Controller.clock := clk_12M
  u_OV2640_Controller.io.clk := clk_12M // 24Mhz clock signal
  u_OV2640_Controller.io.resend := "b0".U(1.W) // Reset signal
  // Flag to indicate that the configuration is finished
  SCL := u_OV2640_Controller.io.sioc // SCCB interface - clock signal
  SDA := u_OV2640_Controller.io.siod // SCCB interface - data signal
  // RESET signal for OV7670
  // PWDN signal for OV7670
  //I_clk
  pixdata_d1 := PIXDATA

  when (HREF) {
    hcnt :=  ~hcnt
  } .otherwise {
    hcnt := false.B
  }

  // assign cam_data = {pixdata_d1[9:5],pixdata_d1[4:2],PIXDATA[9:7],PIXDATA[6:2]}; //RGB565
  // assign cam_data = {PIXDATA[9:5],PIXDATA[4:2],pixdata_d1[9:7],pixdata_d1[6:2]}; //RGB565
  cam_data := Cat(PIXDATA(9,5), PIXDATA(9,4), PIXDATA(9,5)) //RAW10
  } //withClockAndReset(PIXCLK, ~I_rst_n)

  //==============================================
  //data width 16bit   
  ch0_vfb_clk_in := Mux((g_cnt_vs <= "h1ff".U(10.W)), I_clk, PIXCLK)
  ch0_vfb_vs_in := Mux((g_cnt_vs <= "h1ff".U(10.W)),  ~tp0_vs_in, VSYNC) //negative
  ch0_vfb_de_in := Mux((g_cnt_vs <= "h1ff".U(10.W)), tp0_de_in, HREF) //hcnt;
  ch0_vfb_data_in := Mux((g_cnt_vs <= "h1ff".U(10.W)), Cat(tp0_data_r(7,3), tp0_data_g(7,2), tp0_data_b(7,3)), cam_data) // RGB565

  // assign ch0_vfb_clk_in  = PIXCLK;         // assign ch0_vfb_vs_in   = VSYNC;  //negative
  // assign ch0_vfb_de_in   = HREF;//hcnt;  
  // assign ch0_vfb_data_in = cam_data; // RGB565


  //=====================================================
  //SRAM 控制模块 
  val Video_Frame_Buffer_Top_inst = Module(new Video_Frame_Buffer_Top)
  Video_Frame_Buffer_Top_inst.io.I_rst_n := init_calib //rst_n            ),
  Video_Frame_Buffer_Top_inst.io.I_dma_clk := dma_clk //sram_clk         ),
  Video_Frame_Buffer_Top_inst.io.I_wr_halt := 0.U(1.W) //1:halt,  0:no halt
  Video_Frame_Buffer_Top_inst.io.I_rd_halt := 0.U(1.W) //1:halt,  0:no halt
  // video data input           
  Video_Frame_Buffer_Top_inst.io.I_vin0_clk := ch0_vfb_clk_in
  Video_Frame_Buffer_Top_inst.io.I_vin0_vs_n := ch0_vfb_vs_in
  Video_Frame_Buffer_Top_inst.io.I_vin0_de := ch0_vfb_de_in
  Video_Frame_Buffer_Top_inst.io.I_vin0_data := ch0_vfb_data_in
  // video data output          
  Video_Frame_Buffer_Top_inst.io.I_vout0_clk := pix_clk
  Video_Frame_Buffer_Top_inst.io.I_vout0_vs_n :=  ~syn_off0_vs
  Video_Frame_Buffer_Top_inst.io.I_vout0_de := syn_off0_re
  off0_syn_de := Video_Frame_Buffer_Top_inst.io.O_vout0_den
  off0_syn_data := Video_Frame_Buffer_Top_inst.io.O_vout0_data
  // ddr write request
  cmd := Video_Frame_Buffer_Top_inst.io.O_cmd
  cmd_en := Video_Frame_Buffer_Top_inst.io.O_cmd_en
  addr := Video_Frame_Buffer_Top_inst.io.O_addr //[ADDR_WIDTH-1:0]
  wr_data := Video_Frame_Buffer_Top_inst.io.O_wr_data //[DATA_WIDTH-1:0]
  data_mask := Video_Frame_Buffer_Top_inst.io.O_data_mask
  Video_Frame_Buffer_Top_inst.io.I_rd_data_valid := rd_data_valid
  Video_Frame_Buffer_Top_inst.io.I_rd_data := rd_data //[DATA_WIDTH-1:0]
  Video_Frame_Buffer_Top_inst.io.I_init_calib := init_calib

 //================================================
  //HyperRAM ip
  val GW_PLLVR_inst = Module(new GW_PLLVR)
  memory_clk := GW_PLLVR_inst.io.clkout //output clkout
  mem_pll_lock := GW_PLLVR_inst.io.lock //output lock
  GW_PLLVR_inst.io.clkin := I_clk //input clkin


  val HyperRAM_Memory_Interface_Top_inst = Module(new HyperRAM_Memory_Interface_Top)
  HyperRAM_Memory_Interface_Top_inst.io.clk := I_clk
  HyperRAM_Memory_Interface_Top_inst.io.memory_clk := memory_clk
  HyperRAM_Memory_Interface_Top_inst.io.pll_lock := mem_pll_lock
  HyperRAM_Memory_Interface_Top_inst.io.rst_n := I_rst_n //rst_n
  O_hpram_ck := HyperRAM_Memory_Interface_Top_inst.io.O_hpram_ck
  O_hpram_ck_n := HyperRAM_Memory_Interface_Top_inst.io.O_hpram_ck_n
  HyperRAM_Memory_Interface_Top_inst.io.IO_hpram_rwds := IO_hpram_rwds
  HyperRAM_Memory_Interface_Top_inst.io.IO_hpram_dq := IO_hpram_dq
  O_hpram_reset_n := HyperRAM_Memory_Interface_Top_inst.io.O_hpram_reset_n
  O_hpram_cs_n := HyperRAM_Memory_Interface_Top_inst.io.O_hpram_cs_n
  HyperRAM_Memory_Interface_Top_inst.io.wr_data := wr_data
  rd_data := HyperRAM_Memory_Interface_Top_inst.io.rd_data
  rd_data_valid := HyperRAM_Memory_Interface_Top_inst.io.rd_data_valid
  HyperRAM_Memory_Interface_Top_inst.io.addr := addr
  HyperRAM_Memory_Interface_Top_inst.io.cmd := cmd
  HyperRAM_Memory_Interface_Top_inst.io.cmd_en := cmd_en
  dma_clk := HyperRAM_Memory_Interface_Top_inst.io.clk_out
  HyperRAM_Memory_Interface_Top_inst.io.data_mask := data_mask
  init_calib := HyperRAM_Memory_Interface_Top_inst.io.init_calib

 //================================================
  withClockAndReset(pix_clk, ~hdmi_rst_n) {
  val out_de = Wire(Bool()) 
  val syn_gen_inst = Module(new syn_gen)
  syn_gen_inst.I_pxl_clk := pix_clk //40MHz      //65MHz      //74.25MHz    
  syn_gen_inst.I_rst_n := hdmi_rst_n //800x600    //1024x768   //1280x720       
  syn_gen_inst.I_h_total := 1650.U(16.W) // 16'd1056  // 16'd1344  // 16'd1650    
  syn_gen_inst.I_h_sync := 40.U(16.W) // 16'd128   // 16'd136   // 16'd40     
  syn_gen_inst.I_h_bporch := 220.U(16.W) // 16'd88    // 16'd160   // 16'd220     
  syn_gen_inst.I_h_res := 1280.U(16.W) // 16'd800   // 16'd1024  // 16'd1280    
  syn_gen_inst.I_v_total := 750.U(16.W) // 16'd628   // 16'd806   // 16'd750      
  syn_gen_inst.I_v_sync := 5.U(16.W) // 16'd4     // 16'd6     // 16'd5        
  syn_gen_inst.I_v_bporch := 20.U(16.W) // 16'd23    // 16'd29    // 16'd20        
  syn_gen_inst.I_v_res := 720.U(16.W) // 16'd600   // 16'd768   // 16'd720      
  syn_gen_inst.I_rd_hres := 800.U(16.W)
  syn_gen_inst.I_rd_vres := 600.U(16.W)
  syn_gen_inst.I_hs_pol := "b1".U(1.W) //HS polarity , 0:负极性，1：正极性
  syn_gen_inst.I_vs_pol := "b1".U(1.W) //VS polarity , 0:负极性，1：正极性
  syn_off0_re := syn_gen_inst.O_rden
  out_de := syn_gen_inst.O_de
  syn_off0_hs := syn_gen_inst.O_hs
  syn_off0_vs := syn_gen_inst.O_vs

  val N = 5 //delay N clocks

  val Pout_hs_dn = RegInit(1.U(N.W))
  val Pout_vs_dn = RegInit(1.U(N.W))
  val Pout_de_dn = RegInit(0.U(N.W))
  Pout_hs_dn := Cat(Pout_hs_dn(N-2,0), syn_off0_hs)
  Pout_vs_dn := Cat(Pout_vs_dn(N-2,0), syn_off0_vs)
  Pout_de_dn := Cat(Pout_de_dn(N-2,0), out_de)

  //==============================================================================
  //TMDS TX
  rgb_data := Mux(off0_syn_de, Cat(off0_syn_data(15,11), 0.U(3.W), off0_syn_data(10,5), 0.U(2.W), off0_syn_data(4,0), 0.U(3.W)), "h0000ff".U(24.W)) //{r,g,b}
  rgb_vs := Pout_vs_dn(4) //syn_off0_vs;
  rgb_hs := Pout_hs_dn(4) //syn_off0_hs;
  rgb_de := Pout_de_dn(4) //off0_syn_de;
  } // withClockAndReset(pix_clk, ~hdmi_rst_n)


  val TMDS_PLLVR_inst = Module(new TMDS_PLLVR)
  TMDS_PLLVR_inst.io.clkin := I_clk //input clk 
  serial_clk := TMDS_PLLVR_inst.io.clkout //output clk
  clk_12M := TMDS_PLLVR_inst.io.clkoutd //output clkoutd
  pll_lock := TMDS_PLLVR_inst.io.lock //output lock
  hdmi_rst_n := I_rst_n & pll_lock

  val u_clkdiv = Module(new CLKDIV)
  u_clkdiv.io.RESETN := hdmi_rst_n
  u_clkdiv.io.HCLKIN := serial_clk //clk  x5
  pix_clk := u_clkdiv.io.CLKOUT //clk  x1
  u_clkdiv.io.CALIB := "b1".U(1.W)

  val DVI_TX_Top_inst = Module(new DVI_TX_Top)
  DVI_TX_Top_inst.io.I_rst_n := hdmi_rst_n //asynchronous reset, low active
  DVI_TX_Top_inst.io.I_serial_clk := serial_clk
  DVI_TX_Top_inst.io.I_rgb_clk := pix_clk //pixel clock
  DVI_TX_Top_inst.io.I_rgb_vs := rgb_vs
  DVI_TX_Top_inst.io.I_rgb_hs := rgb_hs
  DVI_TX_Top_inst.io.I_rgb_de := rgb_de
  DVI_TX_Top_inst.io.I_rgb_r := rgb_data(23,16)
  DVI_TX_Top_inst.io.I_rgb_g := rgb_data(15,8)
  DVI_TX_Top_inst.io.I_rgb_b := rgb_data(7,0)
  O_tmds_clk_p := DVI_TX_Top_inst.io.O_tmds_clk_p
  O_tmds_clk_n := DVI_TX_Top_inst.io.O_tmds_clk_n
  O_tmds_data_p := DVI_TX_Top_inst.io.O_tmds_data_p //{r,g,b}
  O_tmds_data_n := DVI_TX_Top_inst.io.O_tmds_data_n



}

object video_topGen extends App {
  /*VerilogPortWrapper.emit(
    () => new video_top(),
    forcePreset = true,
    args = args
  )*/
  (new ChiselStage).execute(args,
    Seq(ChiselGeneratorAnnotation(() => new video_top())))
}
