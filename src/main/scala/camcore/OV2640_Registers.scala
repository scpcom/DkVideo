package dkvideo.camcore

import chisel3._
import chisel3.util.Cat
import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}
import hdmicore.video.VideoParams

class OV2640_Registers(vp: VideoParams) extends Camera_Registers {
  // Internal signals
  val sreg = Reg(UInt(16.W))
  val finished_temp = Wire(Bool())
  val address = RegInit(UInt(9.W), (VecInit.tabulate(9)(_ => false.B)).asUInt)

  val rd_hres = vp.H_DISPLAY.U(12.W) // 800.U(12.W)
  val rd_vres = vp.V_DISPLAY.U(12.W) // 600.U(12.W)
  val uxga = Mux((rd_hres <= 800.U) && (rd_vres <= 600.U), false.B, true.B)
  val clkrc = Mux(uxga, "h81".U(8.W), "h80".U(8.W))

  // Assign values to outputs
  io.command := sreg
  io.finished := finished_temp

  // When register and value is FFFF
  // a flag is asserted indicating the configuration is finished
  when(sreg === "hFFFF".U(16.W)) {
    finished_temp := true.B
  } .otherwise {
    finished_temp := false.B
  }


  // Get value out of the LUT

  when(io.resend === true.B) { // reset the configuration
    address := (VecInit.tabulate(8)(_ => false.B)).asTypeOf(UInt(9.W))
  } .elsewhen (io.advance === true.B) { // Get the next value
    address := address+"b1".U(1.W)
  }

  when(address === 0.U) {
    sreg := "hFF_01".U(16.W)
  } .elsewhen (address === 1.U) {
    sreg := "h12_80".U(16.W)
  } .elsewhen (address === 2.U) {
    sreg := "hFF_00".U(16.W)
  } .elsewhen (address === 3.U) {
    sreg := "h2c_ff".U(16.W)
  } .elsewhen (address === 4.U) {
    sreg := "h2e_df".U(16.W)
  } .elsewhen (address === 5.U) {
    sreg := "hFF_01".U(16.W)
  } .elsewhen (address === 6.U) {
    sreg := "h3c_32".U(16.W)
  } .elsewhen (address === 7.U) {
    sreg := Cat("h11".U(8.W), clkrc) /* Set PCLK divider */
  } .elsewhen (address === 8.U) {
    sreg := "h09_02".U(16.W) /* Output drive x2 */
  } .elsewhen (address === 9.U) {
    sreg := "h04_28".U(16.W)
  } .elsewhen (address === 10.U) {
    sreg := "h13_E5".U(16.W)
  } .elsewhen (address === 11.U) {
    sreg := "h14_48".U(16.W)
  } .elsewhen (address === 12.U) {
    sreg := "h15_00".U(16.W) //Invert VSYNC
  } .elsewhen (address === 13.U) {
    sreg := "h2c_0c".U(16.W)
  } .elsewhen (address === 14.U) {
    sreg := "h33_78".U(16.W)
  } .elsewhen (address === 15.U) {
    sreg := "h3a_33".U(16.W)
  } .elsewhen (address === 16.U) {
    sreg := "h3b_fb".U(16.W)
  } .elsewhen (address === 17.U) {
    sreg := "h3e_00".U(16.W)
  } .elsewhen (address === 18.U) {
    sreg := "h43_11".U(16.W)
  } .elsewhen (address === 19.U) {
    sreg := "h16_10".U(16.W)
  } .elsewhen (address === 20.U) {
    sreg := "h39_02".U(16.W)
  } .elsewhen (address === 21.U) {
    sreg := "h35_88".U(16.W)
  } .elsewhen (address === 22.U) {
    sreg := "h22_0a".U(16.W)
  } .elsewhen (address === 23.U) {
    sreg := "h37_40".U(16.W)
  } .elsewhen (address === 24.U) {
    sreg := "h23_00".U(16.W)
  } .elsewhen (address === 25.U) {
    sreg := "h34_a0".U(16.W)
  } .elsewhen (address === 26.U) {
    sreg := "h06_02".U(16.W)
  } .elsewhen (address === 27.U) {
    sreg := "h06_88".U(16.W)
  } .elsewhen (address === 28.U) {
    sreg := "h07_c0".U(16.W)
  } .elsewhen (address === 29.U) {
    sreg := "h0d_b7".U(16.W)
  } .elsewhen (address === 30.U) {
    sreg := "h0e_01".U(16.W)
  } .elsewhen (address === 31.U) {
    sreg := "h4c_00".U(16.W)
  } .elsewhen (address === 32.U) {
    sreg := "h4a_81".U(16.W)
  } .elsewhen (address === 33.U) {
    sreg := "h21_99".U(16.W)
  } .elsewhen (address === 34.U) {
    sreg := "h24_40".U(16.W)
  } .elsewhen (address === 35.U) {
    sreg := "h25_38".U(16.W)
  } .elsewhen (address === 36.U) {
    sreg := "h26_82".U(16.W) /* AGC/AEC fast mode operating region */
  } .elsewhen (address === 37.U) {
    sreg := "h48_00".U(16.W) /* Zoom control 2 MSBs */
  } .elsewhen (address === 38.U) {
    sreg := "h49_00".U(16.W) /* Zoom control 8 MSBs */
  } .elsewhen (address === 39.U) {
    sreg := "h5c_00".U(16.W)
  } .elsewhen (address === 40.U) {
    sreg := "h63_00".U(16.W)
  } .elsewhen (address === 41.U) {
    sreg := "h46_00".U(16.W)
  } .elsewhen (address === 42.U) {
    sreg := "h47_00".U(16.W)
  } .elsewhen (address === 43.U) {
    sreg := "h0C_3A".U(16.W) /* Set banding filter */
  } .elsewhen (address === 44.U) {
    sreg := "h5D_55".U(16.W)
  } .elsewhen (address === 45.U) {
    sreg := "h5E_7d".U(16.W)
  } .elsewhen (address === 46.U) {
    sreg := "h5F_7d".U(16.W)
  } .elsewhen (address === 47.U) {
    sreg := "h60_55".U(16.W)
  } .elsewhen (address === 48.U) {
    sreg := "h61_70".U(16.W)
  } .elsewhen (address === 49.U) {
    sreg := "h62_80".U(16.W)
  } .elsewhen (address === 50.U) {
    sreg := "h7c_05".U(16.W)
  } .elsewhen (address === 51.U) {
    sreg := "h20_80".U(16.W)
  } .elsewhen (address === 52.U) {
    sreg := "h28_30".U(16.W)
  } .elsewhen (address === 53.U) {
    sreg := "h6c_00".U(16.W)
  } .elsewhen (address === 54.U) {
    sreg := "h6d_80".U(16.W)
  } .elsewhen (address === 55.U) {
    sreg := "h6e_00".U(16.W)
  } .elsewhen (address === 56.U) {
    sreg := "h70_02".U(16.W)
  } .elsewhen (address === 57.U) {
    sreg := "h71_94".U(16.W)
  } .elsewhen (address === 58.U) {
    sreg := "h73_c1".U(16.W)
  } .elsewhen (address === 59.U) {
    sreg := "h3d_34".U(16.W)
  } .elsewhen (address === 60.U) {
    sreg := "h5a_57".U(16.W)
  } .elsewhen (address === 61.U) {
    sreg := "h4F_bb".U(16.W)
  } .elsewhen (address === 62.U) {
    sreg := "h50_9c".U(16.W)
  } .elsewhen (address === 63.U) {
    sreg := "hFF_00".U(16.W)
  } .elsewhen (address === 64.U) {
    sreg := "he5_7f".U(16.W)
  } .elsewhen (address === 65.U) {
    sreg := "hF9_C0".U(16.W)
  } .elsewhen (address === 66.U) {
    sreg := "h41_24".U(16.W)
  } .elsewhen (address === 67.U) {
    sreg := "hE0_14".U(16.W)
  } .elsewhen (address === 68.U) {
    sreg := "h76_ff".U(16.W)
  } .elsewhen (address === 69.U) {
    sreg := "h33_a0".U(16.W)
  } .elsewhen (address === 70.U) {
    sreg := "h42_20".U(16.W)
  } .elsewhen (address === 71.U) {
    sreg := "h43_18".U(16.W)
  } .elsewhen (address === 72.U) {
    sreg := "h4c_00".U(16.W)
  } .elsewhen (address === 73.U) {
    sreg := "h87_D0".U(16.W)
  } .elsewhen (address === 74.U) {
    sreg := "h88_3f".U(16.W)
  } .elsewhen (address === 75.U) {
    sreg := "hd7_03".U(16.W)
  } .elsewhen (address === 76.U) {
    sreg := "hd9_10".U(16.W)
  } .elsewhen (address === 77.U) {
    sreg := "hD3_82".U(16.W)
  } .elsewhen (address === 78.U) {
    sreg := "hc8_08".U(16.W)
  } .elsewhen (address === 79.U) {
    sreg := "hc9_80".U(16.W)
  } .elsewhen (address === 80.U) {
    sreg := "h7C_00".U(16.W)
  } .elsewhen (address === 81.U) {
    sreg := "h7D_00".U(16.W)
  } .elsewhen (address === 82.U) {
    sreg := "h7C_03".U(16.W)
  } .elsewhen (address === 83.U) {
    sreg := "h7D_48".U(16.W)
  } .elsewhen (address === 84.U) {
    sreg := "h7D_48".U(16.W)
  } .elsewhen (address === 85.U) {
    sreg := "h7C_08".U(16.W)
  } .elsewhen (address === 86.U) {
    sreg := "h7D_20".U(16.W)
  } .elsewhen (address === 87.U) {
    sreg := "h7D_10".U(16.W)
  } .elsewhen (address === 88.U) {
    sreg := "h7D_0e".U(16.W)
  } .elsewhen (address === 89.U) {
    sreg := "h90_00".U(16.W)
  } .elsewhen (address === 90.U) {
    sreg := "h91_0e".U(16.W)
  } .elsewhen (address === 91.U) {
    sreg := "h91_1a".U(16.W)
  } .elsewhen (address === 92.U) {
    sreg := "h91_31".U(16.W)
  } .elsewhen (address === 93.U) {
    sreg := "h91_5a".U(16.W)
  } .elsewhen (address === 94.U) {
    sreg := "h91_69".U(16.W)
  } .elsewhen (address === 95.U) {
    sreg := "h91_75".U(16.W)
  } .elsewhen (address === 96.U) {
    sreg := "h91_7e".U(16.W)
  } .elsewhen (address === 97.U) {
    sreg := "h91_88".U(16.W)
  } .elsewhen (address === 98.U) {
    sreg := "h91_8f".U(16.W)
  } .elsewhen (address === 99.U) {
    sreg := "h91_96".U(16.W)
  } .elsewhen (address === 100.U) {
    sreg := "h91_a3".U(16.W)
  } .elsewhen (address === 101.U) {
    sreg := "h91_af".U(16.W)
  } .elsewhen (address === 102.U) {
    sreg := "h91_c4".U(16.W)
  } .elsewhen (address === 103.U) {
    sreg := "h91_d7".U(16.W)
  } .elsewhen (address === 104.U) {
    sreg := "h91_e8".U(16.W)
  } .elsewhen (address === 105.U) {
    sreg := "h91_20".U(16.W)
  } .elsewhen (address === 106.U) {
    sreg := "h92_00".U(16.W)
  } .elsewhen (address === 107.U) {
    sreg := "h93_06".U(16.W)
  } .elsewhen (address === 108.U) {
    sreg := "h93_e3".U(16.W)
  } .elsewhen (address === 109.U) {
    sreg := "h93_03".U(16.W)
  } .elsewhen (address === 110.U) {
    sreg := "h93_03".U(16.W)
  } .elsewhen (address === 111.U) {
    sreg := "h93_00".U(16.W)
  } .elsewhen (address === 112.U) {
    sreg := "h93_02".U(16.W)
  } .elsewhen (address === 113.U) {
    sreg := "h93_00".U(16.W)
  } .elsewhen (address === 114.U) {
    sreg := "h93_00".U(16.W)
  } .elsewhen (address === 115.U) {
    sreg := "h93_00".U(16.W)
  } .elsewhen (address === 116.U) {
    sreg := "h93_00".U(16.W)
  } .elsewhen (address === 117.U) {
    sreg := "h93_00".U(16.W)
  } .elsewhen (address === 118.U) {
    sreg := "h93_00".U(16.W)
  } .elsewhen (address === 119.U) {
    sreg := "h93_00".U(16.W)
  } .elsewhen (address === 120.U) {
    sreg := "h96_00".U(16.W)
  } .elsewhen (address === 121.U) {
    sreg := "h97_08".U(16.W)
  } .elsewhen (address === 122.U) {
    sreg := "h97_19".U(16.W)
  } .elsewhen (address === 123.U) {
    sreg := "h97_02".U(16.W)
  } .elsewhen (address === 124.U) {
    sreg := "h97_0c".U(16.W)
  } .elsewhen (address === 125.U) {
    sreg := "h97_24".U(16.W)
  } .elsewhen (address === 126.U) {
    sreg := "h97_30".U(16.W)
  } .elsewhen (address === 127.U) {
    sreg := "h97_28".U(16.W)
  } .elsewhen (address === 128.U) {
    sreg := "h97_26".U(16.W)
  } .elsewhen (address === 129.U) {
    sreg := "h97_02".U(16.W)
  } .elsewhen (address === 130.U) {
    sreg := "h97_98".U(16.W)
  } .elsewhen (address === 131.U) {
    sreg := "h97_80".U(16.W)
  } .elsewhen (address === 132.U) {
    sreg := "h97_00".U(16.W)
  } .elsewhen (address === 133.U) {
    sreg := "h97_00".U(16.W)
  } .elsewhen (address === 134.U) {
    sreg := "ha4_00".U(16.W)
  } .elsewhen (address === 135.U) {
    sreg := "ha8_00".U(16.W)
  } .elsewhen (address === 136.U) {
    sreg := "hc5_11".U(16.W)
  } .elsewhen (address === 137.U) {
    sreg := "hc6_51".U(16.W)
  } .elsewhen (address === 138.U) {
    sreg := "hbf_80".U(16.W)
  } .elsewhen (address === 139.U) {
    sreg := "hc7_10".U(16.W)
  } .elsewhen (address === 140.U) {
    sreg := "hb6_66".U(16.W)
  } .elsewhen (address === 141.U) {
    sreg := "hb8_A5".U(16.W)
  } .elsewhen (address === 142.U) {
    sreg := "hb7_64".U(16.W)
  } .elsewhen (address === 143.U) {
    sreg := "hb9_7C".U(16.W)
  } .elsewhen (address === 144.U) {
    sreg := "hb3_af".U(16.W)
  } .elsewhen (address === 145.U) {
    sreg := "hb4_97".U(16.W)
  } .elsewhen (address === 146.U) {
    sreg := "hb5_FF".U(16.W)
  } .elsewhen (address === 147.U) {
    sreg := "hb0_C5".U(16.W)
  } .elsewhen (address === 148.U) {
    sreg := "hb1_94".U(16.W)
  } .elsewhen (address === 149.U) {
    sreg := "hb2_0f".U(16.W)
  } .elsewhen (address === 150.U) {
    sreg := "hc4_5c".U(16.W)
  } .elsewhen (address === 151.U) {
    sreg := "ha6_00".U(16.W)
  } .elsewhen (address === 152.U) {
    sreg := "ha7_20".U(16.W)
  } .elsewhen (address === 153.U) {
    sreg := "ha7_d8".U(16.W)
  } .elsewhen (address === 154.U) {
    sreg := "ha7_1b".U(16.W)
  } .elsewhen (address === 155.U) {
    sreg := "ha7_31".U(16.W)
  } .elsewhen (address === 156.U) {
    sreg := "ha7_00".U(16.W)
  } .elsewhen (address === 157.U) {
    sreg := "ha7_18".U(16.W)
  } .elsewhen (address === 158.U) {
    sreg := "ha7_20".U(16.W)
  } .elsewhen (address === 159.U) {
    sreg := "ha7_d8".U(16.W)
  } .elsewhen (address === 160.U) {
    sreg := "ha7_19".U(16.W)
  } .elsewhen (address === 161.U) {
    sreg := "ha7_31".U(16.W)
  } .elsewhen (address === 162.U) {
    sreg := "ha7_00".U(16.W)
  } .elsewhen (address === 163.U) {
    sreg := "ha7_18".U(16.W)
  } .elsewhen (address === 164.U) {
    sreg := "ha7_20".U(16.W)
  } .elsewhen (address === 165.U) {
    sreg := "ha7_d8".U(16.W)
  } .elsewhen (address === 166.U) {
    sreg := "ha7_19".U(16.W)
  } .elsewhen (address === 167.U) {
    sreg := "ha7_31".U(16.W)
  } .elsewhen (address === 168.U) {
    sreg := "ha7_00".U(16.W)
  } .elsewhen (address === 169.U) {
    sreg := "ha7_18".U(16.W)
  } .elsewhen (address === 170.U) {
    sreg := "h7f_00".U(16.W)
  } .elsewhen (address === 171.U) {
    sreg := "he5_1f".U(16.W)
  } .elsewhen (address === 172.U) {
    sreg := "he1_77".U(16.W)
  } .elsewhen (address === 173.U) {
    sreg := "hdd_7f".U(16.W)
  } .elsewhen (address === 174.U) {
    sreg := "hC2_0E".U(16.W)
  } .elsewhen (address === 175.U) {
    sreg := "hFF_01".U(16.W)
  } .elsewhen (address === 176.U) {
    sreg := "hFF_00".U(16.W)
  } .elsewhen (address === 177.U) {
    sreg := "hE0_04".U(16.W)
  } .elsewhen (address === 178.U) {
    sreg := Cat("hDA".U(8.W), io.mode) //08:RGB565  04:RAW10
  } .elsewhen (address === 179.U) {
    sreg := "hD7_03".U(16.W)
  } .elsewhen (address === 180.U) {
    sreg := "hE1_77".U(16.W)
  } .elsewhen (address === 181.U) {
    sreg := "hE0_00".U(16.W)
  } .elsewhen (address === 182.U) {
    sreg := "hFF_00".U(16.W)
  } .elsewhen (address === 183.U) {
    sreg := "h05_01".U(16.W)
  } .elsewhen (address === 184.U) {
    sreg := Cat("h5A".U(8.W), rd_hres(9,2)) //(w>>2)&0xFF	//28:w=160 //A0:w=640 //C8:w=800
  } .elsewhen (address === 185.U) {
    sreg := Cat("h5B".U(8.W), rd_vres(9,2)) //(h>>2)&0xFF	//1E:h=120 //78:h=480 //96:h=600
  } .elsewhen (address === 186.U) {
    sreg := Cat("h5C".U(8.W), 0.U(5.W) ## rd_vres(10) ## rd_hres(11,10)) //((h>>8)&0x04)|((w>>10)&0x03)
  } .elsewhen (address === 187.U) {
    sreg := "hFF_01".U(16.W)
  } .elsewhen (address === 188.U) {
    sreg := Cat("h11".U(8.W), clkrc) //clkrc=0x83 for resolution <= SVGA
  } .elsewhen (address === 189.U) {
    sreg := "hFF_01".U(16.W)
  } .elsewhen (address === 190.U) {
    sreg := Cat("h12".U(8.W), Mux(uxga, "h00".U(8.W), "h40".U(8.W))) /* DSP input image resoultion and window size control */
  } .elsewhen (address === 191.U) {
    sreg := Cat("h03".U(8.W), Mux(uxga, "h0F".U(8.W), "h0A".U(8.W))) /* UXGA=0x0F, SVGA=0x0A, CIF=0x06 */
  } .elsewhen (address === 192.U) {
    sreg := Cat("h32".U(8.W), Mux(uxga, "h36".U(8.W), "h09".U(8.W))) /* UXGA=0x36, SVGA/CIF=0x09 */
  } .elsewhen (address === 193.U) {
    sreg := Cat("h17".U(8.W), Mux(uxga, "h11".U(8.W), "h11".U(8.W))) /* UXGA=0x11, SVGA/CIF=0x11 */
  } .elsewhen (address === 194.U) {
    sreg := Cat("h18".U(8.W), Mux(uxga, "h75".U(8.W), "h43".U(8.W))) /* UXGA=0x75, SVGA/CIF=0x43 */
  } .elsewhen (address === 195.U) {
    sreg := Cat("h19".U(8.W), Mux(uxga, "h01".U(8.W), "h00".U(8.W))) /* UXGA=0x01, SVGA/CIF=0x00 */
  } .elsewhen (address === 196.U) {
    sreg := Cat("h1A".U(8.W), Mux(uxga, "h97".U(8.W), "h4b".U(8.W))) /* UXGA=0x97, SVGA/CIF=0x4b */
  } .elsewhen (address === 197.U) {
    sreg := Cat("h3d".U(8.W), Mux(uxga, "h34".U(8.W), "h38".U(8.W))) /* UXGA=0x34, SVGA/CIF=0x38 */
  } .elsewhen (address === 198.U) {
    sreg := Cat("h35".U(8.W), Mux(uxga, "h88".U(8.W), "hda".U(8.W)))
  } .elsewhen (address === 199.U) {
    sreg := Cat("h22".U(8.W), Mux(uxga, "h0a".U(8.W), "h1a".U(8.W)))
  } .elsewhen (address === 200.U) {
    sreg := Cat("h37".U(8.W), Mux(uxga, "h40".U(8.W), "hc3".U(8.W)))
  } .elsewhen (address === 201.U) {
    sreg := Cat("h34".U(8.W), Mux(uxga, "ha0".U(8.W), "hc0".U(8.W)))
  } .elsewhen (address === 202.U) {
    sreg := Cat("h06".U(8.W), Mux(uxga, "h02".U(8.W), "h88".U(8.W)))
  } .elsewhen (address === 203.U) {
    sreg := Cat("h0d".U(8.W), Mux(uxga, "hb7".U(8.W), "h87".U(8.W)))
  } .elsewhen (address === 204.U) {
    sreg := Cat("h0e".U(8.W), Mux(uxga, "h01".U(8.W), "h41".U(8.W)))
  } .elsewhen (address === 205.U) {
    sreg := Cat("h42".U(8.W), Mux(uxga, "h83".U(8.W), "h03".U(8.W)))
  } .elsewhen (address === 206.U) {
    sreg := "hFF_00".U(16.W) /* Set DSP input image size and offset. The sensor output image can be scaled with OUTW/OUTH */
  } .elsewhen (address === 207.U) {
    sreg := "h05_01".U(16.W)
  } .elsewhen (address === 208.U) {
    sreg := "hE0_04".U(16.W)
  } .elsewhen (address === 209.U) {
    sreg := Cat("hC0".U(8.W), rd_hres(10,3)) /* Image Horizontal Size 0x51[10:3] */ //11_0010_0000 = 800
  } .elsewhen (address === 210.U) {
    sreg := Cat("hC1".U(8.W), rd_vres(10,3)) /* Image Vertiacl Size 0x52[10:3] */ //10_0101_1000 = 600
  } .elsewhen (address === 211.U) {
    sreg := Cat("h8C".U(8.W), 0.U(1.W) ## rd_hres(11) ## rd_hres(2,0) ## rd_vres(2,0)) /* {0x51[11], 0x51[2:0], 0x52[2:0]} */
  } .elsewhen (address === 212.U) {
    sreg := "h53_00".U(16.W) /* OFFSET_X[7:0] */
  } .elsewhen (address === 213.U) {
    sreg := "h54_00".U(16.W) /* OFFSET_Y[7:0] */
  } .elsewhen (address === 214.U) {
    sreg := Cat("h51".U(8.W), rd_hres(9,2)) /* H_SIZE[7:0]= 0x51/4 */ //200
  } .elsewhen (address === 215.U) {
    sreg := Cat("h52".U(8.W), rd_vres(9,2)) /* V_SIZE[7:0]= 0x52/4 */ //150
  } .elsewhen (address === 216.U) {
    sreg := Cat("h55".U(8.W), Mux(uxga, (rd_vres(10,3) & "h80".U(8.W)) | (rd_hres(10,7) & "h8".U(4.W)), "h00".U(8.W))) /* V_SIZE[8]/OFFSET_Y[10:8]/H_SIZE[8]/OFFSET_X[10:8] */
  } .elsewhen (address === 217.U) {
    sreg := Cat("h57".U(8.W), Mux(uxga, rd_hres(11,4) & "h80".U(8.W), "h00".U(8.W))) /* H_SIZE[9] */
  } .elsewhen (address === 218.U) {
    sreg := "h86_3D".U(16.W)
  } .elsewhen (address === 219.U) {
    sreg := "h50_80".U(16.W) /* H_DIVIDER/V_DIVIDER */
  } .elsewhen (address === 220.U) {
    sreg := "hD3_80".U(16.W) /* DVP prescalar */
  } .elsewhen (address === 221.U) {
    sreg := "h05_00".U(16.W)
  } .elsewhen (address === 222.U) {
    sreg := "hE0_00".U(16.W)
  } .elsewhen (address === 223.U) {
    sreg := "hFF_00".U(16.W)
  } .elsewhen (address === 224.U) {
    sreg := "h05_00".U(16.W)
  } .elsewhen (address === 225.U) {
    sreg := "hFF_00".U(16.W)
  } .elsewhen (address === 226.U) {
    sreg := "hE0_04".U(16.W)
  } .elsewhen (address === 227.U) {
    sreg := Cat("hDA".U(8.W), io.mode) //08:RGB565  04:RAW10
  } .elsewhen (address === 228.U) {
    sreg := "hD7_03".U(16.W)
  } .elsewhen (address === 229.U) {
    sreg := "hE1_77".U(16.W)
  } .elsewhen (address === 230.U) {
    sreg := "hE0_00".U(16.W)
  } .otherwise {
    sreg := "hFF_FF".U(16.W) // End configuration
  }
}
