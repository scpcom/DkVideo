package dkvideo.video

import chisel3._
import chisel3.util.Cat
import hdmicore.video.{VideoParams, HVSync}

class Video_Output_Sync(vp: VideoParams,
                        rd_width: Int, rd_height: Int, rd_halign: Int, rd_valign: Int,
                        syn_hs_pol: Int, syn_vs_pol: Int, syn_delay: Int) extends Module {
  val io = IO(new Bundle {
      val syn_off0_vs = Output(Bool())
      val syn_off0_hs = Output(Bool())
      val syn_off0_re = Output(Bool())  // ofifo read enable signal

      val rgb_vs = Output(Bool())
      val rgb_hs = Output(Bool())
      val rgb_de = Output(Bool())
  })

  val rd_hres = rd_width
  val rd_vres = rd_height

  val hv_sync = Module(new HVSync(vp))
  val out_de = Wire(Bool())
  val Rden_w = Wire(Bool())
  val Rden_dn = RegInit(false.B)
  val rd_hofs = Mux(rd_halign.U === 2.U, (vp.H_DISPLAY-rd_hres).U(12.W), Mux(rd_halign.U === 1.U, ((vp.H_DISPLAY-rd_hres)/2).U(12.W), 0.U))
  val rd_vofs = Mux(rd_valign.U === 2.U, (vp.V_DISPLAY-rd_vres).U(12.W), Mux(rd_valign.U === 1.U, ((vp.V_DISPLAY-rd_vres)/2).U(12.W), 0.U))
  Rden_w := (hv_sync.io.hpos >= rd_hofs) && (hv_sync.io.hpos < (rd_hofs+rd_hres.U(12.W))) &&
            (hv_sync.io.vpos >= rd_vofs) && (hv_sync.io.vpos < (rd_vofs+rd_vres.U(12.W)))
  Rden_dn := Rden_w
  io.syn_off0_re := Rden_dn
  out_de := hv_sync.io.display_on
  io.syn_off0_hs := Mux(syn_hs_pol.B,  ~hv_sync.io.hsync, hv_sync.io.hsync)
  io.syn_off0_vs := Mux(syn_vs_pol.B,  ~hv_sync.io.vsync, hv_sync.io.vsync)

  val N = syn_delay //delay N clocks

  val Pout_hs_dn = RegInit(1.U(N.W))
  val Pout_vs_dn = RegInit(1.U(N.W))
  val Pout_de_dn = RegInit(0.U(N.W))
  Pout_hs_dn := Cat(Pout_hs_dn(N-2,0), io.syn_off0_hs)
  Pout_vs_dn := Cat(Pout_vs_dn(N-2,0), io.syn_off0_vs)
  Pout_de_dn := Cat(Pout_de_dn(N-2,0), out_de)

  //========================================================================
  //TMDS TX
  io.rgb_vs := Pout_vs_dn(N-1) //syn_off0_vs;
  io.rgb_hs := Pout_hs_dn(N-1) //syn_off0_hs;
  io.rgb_de := Pout_de_dn(N-1) //off0_syn_de;
}
