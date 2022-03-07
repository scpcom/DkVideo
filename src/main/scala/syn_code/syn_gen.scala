package dkvideo
package syn_code

import chisel3._
import chisel3.util._
import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}
// ---------------------------------------------------------------------
// File name         : syn_gen.v
// Module name       : syn_gen
// Module Description: 
// Created by        : Caojie
// ---------------------------------------------------------------------
// Release history
// VERSION |   Date      | AUTHOR  |    DESCRIPTION
// --------------------------------------------------------------------
//   1.0   | 16-Jul-2019 | Caojie  |    initial
// --------------------------------------------------------------------

class syn_gen() extends Module {
  val io = IO(new Bundle {
    val I_pxl_clk = Input(Clock()) //pixel clock
    val I_rst_n = Input(Bool()) //low active
    val I_h_total = Input(UInt(16.W)) //hor total time
    val I_h_sync = Input(UInt(16.W)) //hor sync time
    val I_h_bporch = Input(UInt(16.W)) //hor back porch
    val I_h_res = Input(UInt(16.W)) //hor resolution
    val I_v_total = Input(UInt(16.W)) //ver total time
    val I_v_sync = Input(UInt(16.W)) //ver sync time
    val I_v_bporch = Input(UInt(16.W)) //ver back porch
    val I_v_res = Input(UInt(16.W)) //ver resolution
    val I_rd_hres = Input(UInt(16.W))
    val I_rd_vres = Input(UInt(16.W))
    val I_hs_pol = Input(Bool()) //HS polarity , 0:�����ԣ�1��������
    val I_vs_pol = Input(Bool()) //VS polarity , 0:�����ԣ�1��������
    val O_rden = Output(Bool())
    val O_de = Output(Bool())
    val O_hs = Output(Bool())
    val O_vs = Output(Bool())
  })

  //====================================================
  //withClockAndReset(I_pxl_clk, I_rst_n) {
  val V_cnt = RegInit(0.U(16.W))
  val H_cnt = RegInit(0.U(16.W))

//-----------------------------------------              
  val Pout_de_w = Wire(Bool()) 
  val Pout_hs_w = Wire(Bool()) 
  val Pout_vs_w = Wire(Bool()) 

  val Pout_de_dn = RegInit(false.B)
  val Pout_hs_dn = RegInit(true.B)
  val Pout_vs_dn = RegInit(true.B)

//-----------------------------------------
  val Rden_w = Wire(Bool()) 

  val Rden_dn = RegInit(false.B)

//==============================================================================
  //Generate HS, VS, DE signals

  when((V_cnt >= (io.I_v_total-"b1".U(1.W))) && (H_cnt >= (io.I_h_total-"b1".U(1.W)))) {
    V_cnt := 0.U(16.W)
  } .elsewhen (H_cnt >= (io.I_h_total-"b1".U(1.W))) {
    V_cnt := V_cnt+"b1".U(1.W)
  } .otherwise {
    V_cnt := V_cnt
  }

//-------------------------------------------------------------    

  when (H_cnt >= (io.I_h_total-"b1".U(1.W))) {
    H_cnt := 0.U(16.W)
  } .otherwise {
    H_cnt := H_cnt+"b1".U(1.W)
  }

//-------------------------------------------------------------
  Pout_de_w := ((H_cnt >= (io.I_h_sync+io.I_h_bporch))&(H_cnt <= (((io.I_h_sync+io.I_h_bporch)+io.I_h_res)-"b1".U(1.W))))&((V_cnt >= (io.I_v_sync+io.I_v_bporch))&(V_cnt <= (((io.I_v_sync+io.I_v_bporch)+io.I_v_res)-"b1".U(1.W))))
  Pout_hs_w :=  ~((H_cnt >= 0.U(16.W))&(H_cnt <= (io.I_h_sync-"b1".U(1.W))))
  Pout_vs_w :=  ~((V_cnt >= 0.U(16.W))&(V_cnt <= (io.I_v_sync-"b1".U(1.W))))

//==============================================================================
  Rden_w := ((H_cnt >= (io.I_h_sync+io.I_h_bporch))&(H_cnt <= (((io.I_h_sync+io.I_h_bporch)+io.I_rd_hres)-"b1".U(1.W))))&((V_cnt >= (io.I_v_sync+io.I_v_bporch))&(V_cnt <= (((io.I_v_sync+io.I_v_bporch)+io.I_rd_vres)-"b1".U(1.W))))

//-------------------------------------------------------------

  Pout_de_dn := Pout_de_w
  Pout_hs_dn := Pout_hs_w
  Pout_vs_dn := Pout_vs_w
  Rden_dn := Rden_w
  io.O_de := Pout_de_dn
  io.O_hs := Mux(io.I_hs_pol,  ~Pout_hs_dn, Pout_hs_dn)
  io.O_vs := Mux(io.I_vs_pol,  ~Pout_vs_dn, Pout_vs_dn)
  io.O_rden := Rden_dn
  //} // withClockAndReset(I_pxl_clk, I_rst_n)
}
