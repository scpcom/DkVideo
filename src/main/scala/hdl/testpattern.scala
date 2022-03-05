package hdl

import chisel3._
import chisel3.util.Cat
import sv2chisel.helpers.vecconvert._
// ---------------------------------------------------------------------
// File name         : testpattern.v
// Module name       : testpattern
// Created by        : Caojie
// Module Description: 
//						I_mode[2:0] = "000" : color bar     
//						I_mode[2:0] = "001" : net grid     
//						I_mode[2:0] = "010" : gray         
//						I_mode[2:0] = "011" : single color
// ---------------------------------------------------------------------
// Release history
// VERSION |   Date      | AUTHOR  |    DESCRIPTION
// --------------------------------------------------------------------
//   1.0   | 24-Sep-2009 | Caojie  |    initial
// --------------------------------------------------------------------

class testpattern() extends Module {
  val I_pxl_clk = IO(Input(Clock())) //pixel clock
  val I_rst_n = IO(Input(Bool())) //low active 
  val I_mode = IO(Input(UInt(3.W))) //data select
  val I_single_r = IO(Input(UInt(8.W)))
  val I_single_g = IO(Input(UInt(8.W)))
  val I_single_b = IO(Input(UInt(8.W)))
  val I_h_total = IO(Input(UInt(12.W))) //hor total time 
  val I_h_sync = IO(Input(UInt(12.W))) //hor sync time
  val I_h_bporch = IO(Input(UInt(12.W))) //hor back porch
  val I_h_res = IO(Input(UInt(12.W))) //hor resolution
  val I_v_total = IO(Input(UInt(12.W))) //ver total time 
  val I_v_sync = IO(Input(UInt(12.W))) //ver sync time  
  val I_v_bporch = IO(Input(UInt(12.W))) //ver back porch  
  val I_v_res = IO(Input(UInt(12.W))) //ver resolution 
  val I_hs_pol = IO(Input(Bool())) //HS polarity , 0:�����ԣ�1��������
  val I_vs_pol = IO(Input(Bool())) //VS polarity , 0:�����ԣ�1��������
  val O_de = IO(Output(Bool()))
  val O_hs = IO(Output(Bool())) //������
  val O_vs = IO(Output(Bool())) //������
  val O_data_r = IO(Output(UInt(8.W)))
  val O_data_g = IO(Output(UInt(8.W)))
  val O_data_b = IO(Output(UInt(8.W)))

 //====================================================
  val N = 5 //delay N clocks

  val WHITE   = Cat(255.U(8.W), 255.U(8.W), 255.U(8.W)) //{B,G,R}
  val YELLOW  = Cat(0.U(8.W),   255.U(8.W), 255.U(8.W))
  val CYAN    = Cat(255.U(8.W), 255.U(8.W), 0.U(8.W)  )
  val GREEN   = Cat(0.U(8.W),   255.U(8.W), 0.U(8.W)  )
  val MAGENTA = Cat(255.U(8.W), 0.U(8.W),   255.U(8.W))
  val RED     = Cat(0.U(8.W),   0.U(8.W),   255.U(8.W))
  val BLUE    = Cat(255.U(8.W), 0.U(8.W),   0.U(8.W)  )
  val BLACK   = Cat(0.U(8.W),   0.U(8.W),   0.U(8.W)  )

  //====================================================
  val V_cnt = RegInit(0.U(12.W))
  val H_cnt = RegInit(0.U(12.W))

  val Pout_de_w = Wire(Bool()) 
  val Pout_hs_w = Wire(Bool()) 
  val Pout_vs_w = Wire(Bool()) 

  val Pout_de_dn = RegInit(0.U(N.W))
  val Pout_hs_dn = RegInit(1.U(N.W))
  val Pout_vs_dn = RegInit(1.U(N.W))

  //----------------------------
  val De_pos = Wire(Bool()) 
  val De_neg = Wire(Bool()) 
  val Vs_pos = Wire(Bool()) 

  val De_vcnt = RegInit(0.U(12.W))
  val De_hcnt = RegInit(0.U(12.W))
  /*val De_hcnt_d1 = Wire(UInt(12.W))
  val De_hcnt_d2 = Wire(UInt(12.W))*/

  //-------------------------
  //Color bar //8ɫ����
  val Color_trig_num = RegInit(0.U(12.W))
  val Color_trig = RegInit(false.B)
  val Color_cnt = RegInit(0.U(4.W))
  val Color_bar = RegInit(0.U(24.W))

  //----------------------------
  //Net grid //32����
  val Net_h_trig = RegInit(false.B)
  val Net_v_trig = RegInit(false.B)
  val Net_pos = Wire(UInt(2.W))
  val Net_grid = RegInit(0.U(24.W))

  //----------------------------
  //Gray  //�ڰ׻ҽ�
  val Gray = RegInit(0.U(24.W))
  val Gray_d1 = RegInit(0.U(24.W))

  //-----------------------------
  val Single_color = Wire(UInt(24.W)) 

  //-------------------------------
  val Data_sel = Wire(UInt(24.W)) 

  //-------------------------------
  val Data_tmp = RegInit(0.U(24.W))  /*synthesis syn_keep=1*/

  //==============================================================================
  //Generate HS, VS, DE signals

  when((V_cnt >= (I_v_total-"b1".U(1.W))) && (H_cnt >= (I_h_total-"b1".U(1.W)))) {
    V_cnt := 0.U(12.W)
  } .elsewhen (H_cnt >= (I_h_total-"b1".U(1.W))) {
    V_cnt := V_cnt+"b1".U(1.W)
  } .otherwise {
    V_cnt := V_cnt
  }

  //-------------------------------------------------------------    

  when (H_cnt >= (I_h_total-"b1".U(1.W))) {
    H_cnt := 0.U(12.W)
  } .otherwise {
    H_cnt := H_cnt+"b1".U(1.W)
  }

  //-------------------------------------------------------------
  Pout_de_w := ((H_cnt >= (I_h_sync+I_h_bporch))&(H_cnt <= (((I_h_sync+I_h_bporch)+I_h_res)-"b1".U(1.W))))&((V_cnt >= (I_v_sync+I_v_bporch))&(V_cnt <= (((I_v_sync+I_v_bporch)+I_v_res)-"b1".U(1.W))))
  Pout_hs_w :=  ~((H_cnt >= 0.U(12.W))&(H_cnt <= (I_h_sync-"b1".U(1.W))))
  Pout_vs_w :=  ~((V_cnt >= 0.U(12.W))&(V_cnt <= (I_v_sync-"b1".U(1.W))))

  //-------------------------------------------------------------

  Pout_de_dn := Pout_de_dn(N-2,0) ## Pout_de_w
  Pout_hs_dn := Pout_hs_dn(N-2,0) ## Pout_hs_w
  Pout_vs_dn := Pout_vs_dn(N-2,0) ## Pout_vs_w
  O_de := Pout_de_dn(4) //ע�������ݶ���

  when( !I_rst_n) {
    O_hs := true.B
    O_vs := true.B
  } .otherwise {
    O_hs := Mux(I_hs_pol,  ~Pout_hs_dn(3), Pout_hs_dn(3))
    O_vs := Mux(I_vs_pol,  ~Pout_vs_dn(3), Pout_vs_dn(3))
  }

  //=================================================================================
  //Test Pattern
  De_pos := ( !Pout_de_dn(1))&Pout_de_dn(0) //de rising edge
  De_neg := Pout_de_dn(1) && ( !Pout_de_dn(0)) //de falling edge
  Vs_pos := ( !Pout_vs_dn(1)) && Pout_vs_dn(0) //vs rising edge

  when (De_pos === true.B) {
    De_hcnt := 0.U(12.W)
  } .elsewhen (Pout_de_dn(1) === true.B) {
    De_hcnt := De_hcnt+"b1".U(1.W)
  } .otherwise {
    De_hcnt := De_hcnt
  }
  when (Vs_pos === true.B) {
    De_vcnt := 0.U(12.W)
  } .elsewhen (De_neg === true.B) {
    De_vcnt := De_vcnt+"b1".U(1.W)
  } .otherwise {
    De_vcnt := De_vcnt
  }

  //---------------------------------------------------
  //Color bar
  //---------------------------------------------------

  when (Pout_de_dn(1) === false.B) {
    Color_trig_num := I_h_res(11,3) //8ɫ�������
  } .elsewhen ((Color_trig === true.B) && (Pout_de_dn(1) === true.B)) {
    Color_trig_num := Color_trig_num+I_h_res(11,3)
  } .otherwise {
    Color_trig_num := Color_trig_num
  }
  when (De_hcnt === (Color_trig_num-"b1".U(1.W))) {
    Color_trig := true.B
  } .otherwise {
    Color_trig := false.B
  }
  when (Pout_de_dn(1) === false.B) {
    Color_cnt := 0.U(3.W)
  } .elsewhen ((Color_trig === true.B) && (Pout_de_dn(1) === true.B)) {
    Color_cnt := Color_cnt+"b1".U(1.W)
  } .otherwise {
    Color_cnt := Color_cnt
  }
  when (Pout_de_dn(2) === true.B) {
    when(Color_cnt === 0.U(3.W)) {
      Color_bar := WHITE/*.U(24.W)*/
    } .elsewhen (Color_cnt === 1.U(3.W)) {
      Color_bar := YELLOW/*.U(24.W)*/
    } .elsewhen (Color_cnt === 2.U(3.W)) {
      Color_bar := CYAN/*.U(24.W)*/
    } .elsewhen (Color_cnt === 3.U(3.W)) {
      Color_bar := GREEN/*.U(24.W)*/
    } .elsewhen (Color_cnt === 4.U(3.W)) {
      Color_bar := MAGENTA/*.U(24.W)*/
    } .elsewhen (Color_cnt === 5.U(3.W)) {
      Color_bar := RED/*.U(24.W)*/
    } .elsewhen (Color_cnt === 6.U(3.W)) {
      Color_bar := BLUE/*.U(24.W)*/
    } .elsewhen (Color_cnt === 7.U(3.W)) {
      Color_bar := BLACK/*.U(24.W)*/
    } .otherwise {
      Color_bar := BLACK/*.U(24.W)*/
    }
  } .otherwise {
    Color_bar := BLACK/*.U(24.W)*/
  }

  //---------------------------------------------------
  //Net grid
  //---------------------------------------------------

  when (((De_hcnt(4,0) === 0.U(5.W)) || (De_hcnt === (I_h_res-"b1".U(1.W)))) && (Pout_de_dn(1) === true.B)) {
    Net_h_trig := true.B
  } .otherwise {
    Net_h_trig := false.B
  }
  when (((De_vcnt(4,0) === 0.U(5.W)) || (De_vcnt === (I_v_res-"b1".U(1.W)))) && (Pout_de_dn(1) === true.B)) {
    Net_v_trig := true.B
  } .otherwise {
    Net_v_trig := false.B
  }
  Net_pos := Cat(Net_v_trig, Net_h_trig)
  when (Pout_de_dn(2) === true.B) {
    when(Net_pos === "b00".U(2.W)) {
      Net_grid := BLACK/*.U(24.W)*/
    } .elsewhen (Net_pos === "b01".U(2.W)) {
      Net_grid := RED/*.U(24.W)*/
    } .elsewhen (Net_pos === "b10".U(2.W)) {
      Net_grid := RED/*.U(24.W)*/
    } .elsewhen (Net_pos === "b11".U(2.W)) {
      Net_grid := RED/*.U(24.W)*/
    } .otherwise {
      Net_grid := BLACK/*.U(24.W)*/
    }
  } .otherwise {
    Net_grid := BLACK/*.U(24.W)*/
  }

  //---------------------------------------------------
  //Gray
  //---------------------------------------------------

  Gray := Cat(De_hcnt(7,0), De_hcnt(7,0), De_hcnt(7,0))
  Gray_d1 := Gray

  //---------------------------------------------------
  //Single color
  //---------------------------------------------------
  Single_color := Cat(I_single_b, I_single_g, I_single_r)

  //============================================================
  Data_sel := Mux((I_mode(2,0) === "b000".U(3.W)), Color_bar, Mux((I_mode(2,0) === "b001".U(3.W)), Net_grid, Mux((I_mode(2,0) === "b010".U(3.W)), Gray_d1, Mux((I_mode(2,0) === "b011".U(3.W)), Single_color, GREEN))))

  //---------------------------------------------------

  Data_tmp := Data_sel
  O_data_r := Data_tmp(7,0)
  O_data_g := Data_tmp(15,8)
  O_data_b := Data_tmp(23,16)

}
