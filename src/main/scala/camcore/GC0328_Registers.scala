package dkvideo.camcore

import chisel3._
import chisel3.util.Cat
import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}
import hdmicore.video.VideoParams

class GC0328_Registers(vp: VideoParams) extends Camera_Registers {
  // Internal signals
  val sreg = RegInit(0.U(16.W))
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
  when((address > 0.U) && (sreg === "h0000".U(16.W))) {
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
    sreg := "hFE_01".U(16.W)

  } .elsewhen (address === 1.U) {
    sreg := Cat("hfe".U(8.W), "h80".U(8.W))
  } .elsewhen (address === 2.U) {
    sreg := Cat("hfe".U(8.W), "h80".U(8.W))
  } .elsewhen (address === 3.U) {
    sreg := Cat("hfc".U(8.W), "h16".U(8.W))
  } .elsewhen (address === 4.U) {
    sreg := Cat("hfc".U(8.W), "h16".U(8.W))
  } .elsewhen (address === 5.U) {
    sreg := Cat("hfc".U(8.W), "h16".U(8.W))
  } .elsewhen (address === 6.U) {
    sreg := Cat("hfc".U(8.W), "h16".U(8.W))
  } .elsewhen (address === 7.U) {
    sreg := Cat("hf1".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 8.U) {
    sreg := Cat("hf2".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 9.U) {
    sreg := Cat("hfe".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 10.U) {
    sreg := Cat("h4f".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 11.U) {
    sreg := Cat("h42".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 12.U) {
    sreg := Cat("h03".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 13.U) {
    sreg := Cat("h04".U(8.W), "hc0".U(8.W))
  } .elsewhen (address === 14.U) {
    sreg := Cat("h77".U(8.W), "h62".U(8.W))
  } .elsewhen (address === 15.U) {
    sreg := Cat("h78".U(8.W), "h40".U(8.W))
  } .elsewhen (address === 16.U) {
    sreg := Cat("h79".U(8.W), "h4d".U(8.W))

  } .elsewhen (address === 17.U) {
    sreg := Cat("hfe".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 18.U) {
    sreg := Cat("h16".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 19.U) {
    sreg := Cat("h17".U(8.W), "h14".U(8.W))
  } .elsewhen (address === 20.U) {
    sreg := Cat("h18".U(8.W), "h0e".U(8.W))
  } .elsewhen (address === 21.U) {
    sreg := Cat("h19".U(8.W), "h06".U(8.W))

  } .elsewhen (address === 22.U) {
    sreg := Cat("h1b".U(8.W), "h48".U(8.W))
  } .elsewhen (address === 23.U) {
    sreg := Cat("h1f".U(8.W), "hC8".U(8.W))
  } .elsewhen (address === 24.U) {
    sreg := Cat("h20".U(8.W), "h01".U(8.W))
  } .elsewhen (address === 25.U) {
    sreg := Cat("h21".U(8.W), "h78".U(8.W))
  } .elsewhen (address === 26.U) {
    sreg := Cat("h22".U(8.W), "hb0".U(8.W))
  } .elsewhen (address === 27.U) {
    sreg := Cat("h23".U(8.W), "h04".U(8.W)) //0x06  20140519 GC0328C
  } .elsewhen (address === 28.U) {
    sreg := Cat("h24".U(8.W), "h11".U(8.W))
  } .elsewhen (address === 29.U) {
    sreg := Cat("h26".U(8.W), "h00".U(8.W))

  //global gain for range
  } .elsewhen (address === 30.U) {
    sreg := Cat("h70".U(8.W), "h85".U(8.W))
  } .elsewhen (address === 31.U) {

  /////////////banding/////////////
    sreg := Cat("h05".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 32.U) {
    sreg := Cat("h06".U(8.W), "h6a".U(8.W))
  } .elsewhen (address === 33.U) {
    sreg := Cat("h07".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 34.U) {
    sreg := Cat("h08".U(8.W), "h0c".U(8.W))
  } .elsewhen (address === 35.U) {
    sreg := Cat("hfe".U(8.W), "h01".U(8.W))
  } .elsewhen (address === 36.U) {
    sreg := Cat("h29".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 37.U) {
    sreg := Cat("h2a".U(8.W), "h96".U(8.W))
  } .elsewhen (address === 38.U) {
    sreg := Cat("hfe".U(8.W), "h00".U(8.W))

  ///////////////AWB//////////////
  } .elsewhen (address === 39.U) {
    sreg := Cat("hfe".U(8.W), "h01".U(8.W))
  } .elsewhen (address === 40.U) {
    sreg := Cat("h50".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 41.U) {
    sreg := Cat("h4f".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 42.U) {
    sreg := Cat("h4c".U(8.W), "h01".U(8.W))
  } .elsewhen (address === 43.U) {
    sreg := Cat("h4f".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 44.U) {
    sreg := Cat("h4f".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 45.U) {
    sreg := Cat("h4f".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 46.U) {
    sreg := Cat("h4f".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 47.U) {
    sreg := Cat("h4f".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 48.U) {
    sreg := Cat("h4d".U(8.W), "h30".U(8.W))
  } .elsewhen (address === 49.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 50.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 51.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 52.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 53.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 54.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 55.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 56.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 57.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 58.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 59.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 60.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 61.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 62.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 63.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 64.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 65.U) {
    sreg := Cat("h4d".U(8.W), "h40".U(8.W))
  } .elsewhen (address === 66.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 67.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 68.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 69.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 70.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 71.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 72.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 73.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 74.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 75.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 76.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 77.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 78.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 79.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 80.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 81.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 82.U) {
    sreg := Cat("h4d".U(8.W), "h50".U(8.W))
  } .elsewhen (address === 83.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 84.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 85.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 86.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 87.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 88.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 89.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 90.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 91.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 92.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 93.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 94.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 95.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 96.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 97.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 98.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 99.U) {
    sreg := Cat("h4d".U(8.W), "h60".U(8.W))
  } .elsewhen (address === 100.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 101.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 102.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 103.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 104.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 105.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 106.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 107.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 108.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 109.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 110.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 111.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 112.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 113.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 114.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 115.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 116.U) {
    sreg := Cat("h4d".U(8.W), "h70".U(8.W))
  } .elsewhen (address === 117.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 118.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 119.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 120.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 121.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 122.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 123.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 124.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 125.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 126.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 127.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 128.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 129.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 130.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 131.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 132.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 133.U) {
    sreg := Cat("h4f".U(8.W), "h01".U(8.W))
  } .elsewhen (address === 134.U) {
    sreg := Cat("h50".U(8.W), "h88".U(8.W))
  } .elsewhen (address === 135.U) {
    sreg := Cat("hfe".U(8.W), "h00".U(8.W))

  //////////// BLK//////////////////////
  } .elsewhen (address === 136.U) {
    sreg := Cat("hfe".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 137.U) {
    sreg := Cat("h27".U(8.W), "hb7".U(8.W))
  } .elsewhen (address === 138.U) {
    sreg := Cat("h28".U(8.W), "h7F".U(8.W))
  } .elsewhen (address === 139.U) {
    sreg := Cat("h29".U(8.W), "h20".U(8.W))
  } .elsewhen (address === 140.U) {
    sreg := Cat("h33".U(8.W), "h20".U(8.W))
  } .elsewhen (address === 141.U) {
    sreg := Cat("h34".U(8.W), "h20".U(8.W))
  } .elsewhen (address === 142.U) {
    sreg := Cat("h35".U(8.W), "h20".U(8.W))
  } .elsewhen (address === 143.U) {
    sreg := Cat("h36".U(8.W), "h20".U(8.W))
  } .elsewhen (address === 144.U) {
    sreg := Cat("h32".U(8.W), "h08".U(8.W))
  } .elsewhen (address === 145.U) {
    sreg := Cat("h3b".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 146.U) {
    sreg := Cat("h3c".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 147.U) {
    sreg := Cat("h3d".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 148.U) {
    sreg := Cat("h3e".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 149.U) {
    sreg := Cat("h47".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 150.U) {
    sreg := Cat("h48".U(8.W), "h00".U(8.W))

  //////////// block enable/////////////
  } .elsewhen (address === 151.U) {
    sreg := Cat("h40".U(8.W), "h7f".U(8.W))
  } .elsewhen (address === 152.U) {
    sreg := Cat("h41".U(8.W), "h26".U(8.W))
  } .elsewhen (address === 153.U) {
    sreg := Cat("h42".U(8.W), "hfb".U(8.W))
  } .elsewhen (address === 154.U) {
    sreg := Cat("h44".U(8.W), "h06".U(8.W)) // rgb565
  } .elsewhen (address === 155.U) {
    sreg := Cat("h45".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 156.U) {
    sreg := Cat("h46".U(8.W), "h03".U(8.W))
  } .elsewhen (address === 157.U) {
    sreg := Cat("h4f".U(8.W), "h01".U(8.W))
  } .elsewhen (address === 158.U) {
    sreg := Cat("h4b".U(8.W), "h01".U(8.W))
  } .elsewhen (address === 159.U) {
    sreg := Cat("h50".U(8.W), "h01".U(8.W))

  /////DN & EEINTP/////
  } .elsewhen (address === 160.U) {
    sreg := Cat("h7e".U(8.W), "h0a".U(8.W))
  } .elsewhen (address === 161.U) {
    sreg := Cat("h7f".U(8.W), "h03".U(8.W))
  } .elsewhen (address === 162.U) {
    sreg := Cat("h81".U(8.W), "h15".U(8.W))
  } .elsewhen (address === 163.U) {
    sreg := Cat("h82".U(8.W), "h85".U(8.W))
  } .elsewhen (address === 164.U) {
    sreg := Cat("h83".U(8.W), "h03".U(8.W))
  } .elsewhen (address === 165.U) {
    sreg := Cat("h84".U(8.W), "he5".U(8.W))
  } .elsewhen (address === 166.U) {
    sreg := Cat("h90".U(8.W), "hac".U(8.W))
  } .elsewhen (address === 167.U) {
    sreg := Cat("h92".U(8.W), "h02".U(8.W))
  } .elsewhen (address === 168.U) {
    sreg := Cat("h94".U(8.W), "h02".U(8.W))
  } .elsewhen (address === 169.U) {
    sreg := Cat("h95".U(8.W), "h32".U(8.W))

  ////////////YCP///////////
  } .elsewhen (address === 170.U) {
    sreg := Cat("hd1".U(8.W), "h28".U(8.W))
  } .elsewhen (address === 171.U) {
    sreg := Cat("hd2".U(8.W), "h28".U(8.W))
  } .elsewhen (address === 172.U) {
    sreg := Cat("hd3".U(8.W), "h40".U(8.W))
  } .elsewhen (address === 173.U) {
    sreg := Cat("hdd".U(8.W), "h58".U(8.W))
  } .elsewhen (address === 174.U) {
    sreg := Cat("hde".U(8.W), "h36".U(8.W))
  } .elsewhen (address === 175.U) {
    sreg := Cat("he4".U(8.W), "h88".U(8.W))
  } .elsewhen (address === 176.U) {
    sreg := Cat("he5".U(8.W), "h40".U(8.W))
  } .elsewhen (address === 177.U) {
    sreg := Cat("hd7".U(8.W), "h0e".U(8.W))

  ///////////rgb gamma ////////////
  } .elsewhen (address === 178.U) {
    sreg := Cat("hfe".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 179.U) {
    sreg := Cat("hbf".U(8.W), "h0e".U(8.W))
  } .elsewhen (address === 180.U) {
    sreg := Cat("hc0".U(8.W), "h1c".U(8.W))
  } .elsewhen (address === 181.U) {
    sreg := Cat("hc1".U(8.W), "h34".U(8.W))
  } .elsewhen (address === 182.U) {
    sreg := Cat("hc2".U(8.W), "h48".U(8.W))
  } .elsewhen (address === 183.U) {
    sreg := Cat("hc3".U(8.W), "h5a".U(8.W))
  } .elsewhen (address === 184.U) {
    sreg := Cat("hc4".U(8.W), "h6e".U(8.W))
  } .elsewhen (address === 185.U) {
    sreg := Cat("hc5".U(8.W), "h80".U(8.W))
  } .elsewhen (address === 186.U) {
    sreg := Cat("hc6".U(8.W), "h9c".U(8.W))
  } .elsewhen (address === 187.U) {
    sreg := Cat("hc7".U(8.W), "hb4".U(8.W))
  } .elsewhen (address === 188.U) {
    sreg := Cat("hc8".U(8.W), "hc7".U(8.W))
  } .elsewhen (address === 189.U) {
    sreg := Cat("hc9".U(8.W), "hd7".U(8.W))
  } .elsewhen (address === 190.U) {
    sreg := Cat("hca".U(8.W), "he3".U(8.W))
  } .elsewhen (address === 191.U) {
    sreg := Cat("hcb".U(8.W), "hed".U(8.W))
  } .elsewhen (address === 192.U) {
    sreg := Cat("hcc".U(8.W), "hf2".U(8.W))
  } .elsewhen (address === 193.U) {
    sreg := Cat("hcd".U(8.W), "hf8".U(8.W))
  } .elsewhen (address === 194.U) {
    sreg := Cat("hce".U(8.W), "hfd".U(8.W))
  } .elsewhen (address === 195.U) {
    sreg := Cat("hcf".U(8.W), "hff".U(8.W))

  /////////////Y gamma//////////
  } .elsewhen (address === 196.U) {
    sreg := Cat("hfe".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 197.U) {
    sreg := Cat("h63".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 198.U) {
    sreg := Cat("h64".U(8.W), "h05".U(8.W))
  } .elsewhen (address === 199.U) {
    sreg := Cat("h65".U(8.W), "h0b".U(8.W))
  } .elsewhen (address === 200.U) {
    sreg := Cat("h66".U(8.W), "h19".U(8.W))
  } .elsewhen (address === 201.U) {
    sreg := Cat("h67".U(8.W), "h2e".U(8.W))
  } .elsewhen (address === 202.U) {
    sreg := Cat("h68".U(8.W), "h40".U(8.W))
  } .elsewhen (address === 203.U) {
    sreg := Cat("h69".U(8.W), "h54".U(8.W))
  } .elsewhen (address === 204.U) {
    sreg := Cat("h6a".U(8.W), "h66".U(8.W))
  } .elsewhen (address === 205.U) {
    sreg := Cat("h6b".U(8.W), "h86".U(8.W))
  } .elsewhen (address === 206.U) {
    sreg := Cat("h6c".U(8.W), "ha7".U(8.W))
  } .elsewhen (address === 207.U) {
    sreg := Cat("h6d".U(8.W), "hc6".U(8.W))
  } .elsewhen (address === 208.U) {
    sreg := Cat("h6e".U(8.W), "he4".U(8.W))
  } .elsewhen (address === 209.U) {
    sreg := Cat("h6f".U(8.W), "hff".U(8.W))

  //////////////ASDE/////////////
  } .elsewhen (address === 210.U) {
    sreg := Cat("hfe".U(8.W), "h01".U(8.W))
  } .elsewhen (address === 211.U) {
    sreg := Cat("h18".U(8.W), "h02".U(8.W))
  } .elsewhen (address === 212.U) {
    sreg := Cat("hfe".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 213.U) {
    sreg := Cat("h98".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 214.U) {
    sreg := Cat("h9b".U(8.W), "h20".U(8.W))
  } .elsewhen (address === 215.U) {
    sreg := Cat("h9c".U(8.W), "h80".U(8.W))
  } .elsewhen (address === 216.U) {
    sreg := Cat("ha4".U(8.W), "h10".U(8.W))
  } .elsewhen (address === 217.U) {
    sreg := Cat("ha8".U(8.W), "hB0".U(8.W))
  } .elsewhen (address === 218.U) {
    sreg := Cat("haa".U(8.W), "h40".U(8.W))
  } .elsewhen (address === 219.U) {
    sreg := Cat("ha2".U(8.W), "h23".U(8.W))
  } .elsewhen (address === 220.U) {
    sreg := Cat("had".U(8.W), "h01".U(8.W))

  //////////////abs///////////
  } .elsewhen (address === 221.U) {
    sreg := Cat("hfe".U(8.W), "h01".U(8.W))
  } .elsewhen (address === 222.U) {
    sreg := Cat("h9c".U(8.W), "h02".U(8.W))
  } .elsewhen (address === 223.U) {
    sreg := Cat("h9e".U(8.W), "hc0".U(8.W))
  } .elsewhen (address === 224.U) {
    sreg := Cat("h9f".U(8.W), "h40".U(8.W))

  ////////////// AEC////////////
  } .elsewhen (address === 225.U) {
    sreg := Cat("h08".U(8.W), "ha0".U(8.W))
  } .elsewhen (address === 226.U) {
    sreg := Cat("h09".U(8.W), "he8".U(8.W))
  } .elsewhen (address === 227.U) {
    sreg := Cat("h10".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 228.U) {
    sreg := Cat("h11".U(8.W), "h11".U(8.W))
  } .elsewhen (address === 229.U) {
    sreg := Cat("h12".U(8.W), "h10".U(8.W))
  } .elsewhen (address === 230.U) {
    sreg := Cat("h13".U(8.W), "h98".U(8.W))
  } .elsewhen (address === 231.U) {
    sreg := Cat("h15".U(8.W), "hfc".U(8.W))
  } .elsewhen (address === 232.U) {
    sreg := Cat("h18".U(8.W), "h03".U(8.W))
  } .elsewhen (address === 233.U) {
    sreg := Cat("h21".U(8.W), "hc0".U(8.W))
  } .elsewhen (address === 234.U) {
    sreg := Cat("h22".U(8.W), "h60".U(8.W))
  } .elsewhen (address === 235.U) {
    sreg := Cat("h23".U(8.W), "h30".U(8.W))
  } .elsewhen (address === 236.U) {
    sreg := Cat("h25".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 237.U) {
    sreg := Cat("h24".U(8.W), "h14".U(8.W))
  } .elsewhen (address === 238.U) {
    sreg := Cat("h3d".U(8.W), "h80".U(8.W))
  } .elsewhen (address === 239.U) {
    sreg := Cat("h3e".U(8.W), "h40".U(8.W))

  ////////////////AWB///////////
  } .elsewhen (address === 240.U) {
    sreg := Cat("hfe".U(8.W), "h01".U(8.W))
  } .elsewhen (address === 241.U) {
    sreg := Cat("h51".U(8.W), "h88".U(8.W))
  } .elsewhen (address === 242.U) {
    sreg := Cat("h52".U(8.W), "h12".U(8.W))
  } .elsewhen (address === 243.U) {
    sreg := Cat("h53".U(8.W), "h80".U(8.W))
  } .elsewhen (address === 244.U) {
    sreg := Cat("h54".U(8.W), "h60".U(8.W))
  } .elsewhen (address === 245.U) {
    sreg := Cat("h55".U(8.W), "h01".U(8.W))
  } .elsewhen (address === 246.U) {
    sreg := Cat("h56".U(8.W), "h02".U(8.W))
  } .elsewhen (address === 247.U) {
    sreg := Cat("h58".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 248.U) {
    sreg := Cat("h5b".U(8.W), "h02".U(8.W))
  } .elsewhen (address === 249.U) {
    sreg := Cat("h5e".U(8.W), "ha4".U(8.W))
  } .elsewhen (address === 250.U) {
    sreg := Cat("h5f".U(8.W), "h8a".U(8.W))
  } .elsewhen (address === 251.U) {
    sreg := Cat("h61".U(8.W), "hdc".U(8.W))
  } .elsewhen (address === 252.U) {
    sreg := Cat("h62".U(8.W), "hdc".U(8.W))
  } .elsewhen (address === 253.U) {
    sreg := Cat("h70".U(8.W), "hfc".U(8.W))
  } .elsewhen (address === 254.U) {
    sreg := Cat("h71".U(8.W), "h10".U(8.W))
  } .elsewhen (address === 255.U) {
    sreg := Cat("h72".U(8.W), "h30".U(8.W))
  } .elsewhen (address === 256.U) {
    sreg := Cat("h73".U(8.W), "h0b".U(8.W))
  } .elsewhen (address === 257.U) {
    sreg := Cat("h74".U(8.W), "h0b".U(8.W))
  } .elsewhen (address === 258.U) {
    sreg := Cat("h75".U(8.W), "h01".U(8.W))
  } .elsewhen (address === 259.U) {
    sreg := Cat("h76".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 260.U) {
    sreg := Cat("h77".U(8.W), "h40".U(8.W))
  } .elsewhen (address === 261.U) {
    sreg := Cat("h78".U(8.W), "h70".U(8.W))
  } .elsewhen (address === 262.U) {
    sreg := Cat("h79".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 263.U) {
    sreg := Cat("h7b".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 264.U) {
    sreg := Cat("h7c".U(8.W), "h71".U(8.W))
  } .elsewhen (address === 265.U) {
    sreg := Cat("h7d".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 266.U) {
    sreg := Cat("h80".U(8.W), "h70".U(8.W))
  } .elsewhen (address === 267.U) {
    sreg := Cat("h81".U(8.W), "h58".U(8.W))
  } .elsewhen (address === 268.U) {
    sreg := Cat("h82".U(8.W), "h98".U(8.W))
  } .elsewhen (address === 269.U) {
    sreg := Cat("h83".U(8.W), "h60".U(8.W))
  } .elsewhen (address === 270.U) {
    sreg := Cat("h84".U(8.W), "h58".U(8.W))
  } .elsewhen (address === 271.U) {
    sreg := Cat("h85".U(8.W), "h50".U(8.W))
  } .elsewhen (address === 272.U) {
    sreg := Cat("hfe".U(8.W), "h00".U(8.W))

  ////////////////LSC////////////////
  } .elsewhen (address === 273.U) {
    sreg := Cat("hfe".U(8.W), "h01".U(8.W))
  } .elsewhen (address === 274.U) {
    sreg := Cat("hc0".U(8.W), "h10".U(8.W))
  } .elsewhen (address === 275.U) {
    sreg := Cat("hc1".U(8.W), "h0c".U(8.W))
  } .elsewhen (address === 276.U) {
    sreg := Cat("hc2".U(8.W), "h0a".U(8.W))
  } .elsewhen (address === 277.U) {
    sreg := Cat("hc6".U(8.W), "h0e".U(8.W))
  } .elsewhen (address === 278.U) {
    sreg := Cat("hc7".U(8.W), "h0b".U(8.W))
  } .elsewhen (address === 279.U) {
    sreg := Cat("hc8".U(8.W), "h0a".U(8.W))
  } .elsewhen (address === 280.U) {
    sreg := Cat("hba".U(8.W), "h26".U(8.W))
  } .elsewhen (address === 281.U) {
    sreg := Cat("hbb".U(8.W), "h1c".U(8.W))
  } .elsewhen (address === 282.U) {
    sreg := Cat("hbc".U(8.W), "h1d".U(8.W))
  } .elsewhen (address === 283.U) {
    sreg := Cat("hb4".U(8.W), "h23".U(8.W))
  } .elsewhen (address === 284.U) {
    sreg := Cat("hb5".U(8.W), "h1c".U(8.W))
  } .elsewhen (address === 285.U) {
    sreg := Cat("hb6".U(8.W), "h1a".U(8.W))
  } .elsewhen (address === 286.U) {
    sreg := Cat("hc3".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 287.U) {
    sreg := Cat("hc4".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 288.U) {
    sreg := Cat("hc5".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 289.U) {
    sreg := Cat("hc9".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 290.U) {
    sreg := Cat("hca".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 291.U) {
    sreg := Cat("hcb".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 292.U) {
    sreg := Cat("hbd".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 293.U) {
    sreg := Cat("hbe".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 294.U) {
    sreg := Cat("hbf".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 295.U) {
    sreg := Cat("hb7".U(8.W), "h07".U(8.W))
  } .elsewhen (address === 296.U) {
    sreg := Cat("hb8".U(8.W), "h05".U(8.W))
  } .elsewhen (address === 297.U) {
    sreg := Cat("hb9".U(8.W), "h05".U(8.W))
  } .elsewhen (address === 298.U) {
    sreg := Cat("ha8".U(8.W), "h07".U(8.W))
  } .elsewhen (address === 299.U) {
    sreg := Cat("ha9".U(8.W), "h06".U(8.W))
  } .elsewhen (address === 300.U) {
    sreg := Cat("haa".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 301.U) {
    sreg := Cat("hab".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 302.U) {
    sreg := Cat("hac".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 303.U) {
    sreg := Cat("had".U(8.W), "h02".U(8.W))
  } .elsewhen (address === 304.U) {
    sreg := Cat("hae".U(8.W), "h0d".U(8.W))
  } .elsewhen (address === 305.U) {
    sreg := Cat("haf".U(8.W), "h05".U(8.W))
  } .elsewhen (address === 306.U) {
    sreg := Cat("hb0".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 307.U) {
    sreg := Cat("hb1".U(8.W), "h07".U(8.W))
  } .elsewhen (address === 308.U) {
    sreg := Cat("hb2".U(8.W), "h03".U(8.W))
  } .elsewhen (address === 309.U) {
    sreg := Cat("hb3".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 310.U) {
    sreg := Cat("ha4".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 311.U) {
    sreg := Cat("ha5".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 312.U) {
    sreg := Cat("ha6".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 313.U) {
    sreg := Cat("ha7".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 314.U) {
    sreg := Cat("ha1".U(8.W), "h3c".U(8.W))
  } .elsewhen (address === 315.U) {
    sreg := Cat("ha2".U(8.W), "h50".U(8.W))
  } .elsewhen (address === 316.U) {
    sreg := Cat("hfe".U(8.W), "h00".U(8.W))

  ///////////////CCT ///////////
  } .elsewhen (address === 317.U) {
    sreg := Cat("hb1".U(8.W), "h12".U(8.W))
  } .elsewhen (address === 318.U) {
    sreg := Cat("hb2".U(8.W), "hf5".U(8.W))
  } .elsewhen (address === 319.U) {
    sreg := Cat("hb3".U(8.W), "hfe".U(8.W))
  } .elsewhen (address === 320.U) {
    sreg := Cat("hb4".U(8.W), "he0".U(8.W))
  } .elsewhen (address === 321.U) {
    sreg := Cat("hb5".U(8.W), "h15".U(8.W))
  } .elsewhen (address === 322.U) {
    sreg := Cat("hb6".U(8.W), "hc8".U(8.W))

  /////skin CC for front //////
  } .elsewhen (address === 323.U) {
    sreg := Cat("hb1".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 324.U) {
    sreg := Cat("hb2".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 325.U) {
    sreg := Cat("hb3".U(8.W), "h05".U(8.W))
  } .elsewhen (address === 326.U) {
    sreg := Cat("hb4".U(8.W), "hf0".U(8.W))
  } .elsewhen (address === 327.U) {
    sreg := Cat("hb5".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 328.U) {
    sreg := Cat("hb6".U(8.W), "h00".U(8.W))

  ///////////////AWB////////////////
  } .elsewhen (address === 329.U) {
    sreg := Cat("hfe".U(8.W), "h01".U(8.W))
  } .elsewhen (address === 330.U) {
    sreg := Cat("h50".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 331.U) {
    sreg := Cat("hfe".U(8.W), "h01".U(8.W))
  } .elsewhen (address === 332.U) {
    sreg := Cat("h4f".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 333.U) {
    sreg := Cat("h4c".U(8.W), "h01".U(8.W))
  } .elsewhen (address === 334.U) {
    sreg := Cat("h4f".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 335.U) {
    sreg := Cat("h4f".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 336.U) {
    sreg := Cat("h4f".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 337.U) {
    sreg := Cat("h4d".U(8.W), "h34".U(8.W))
  } .elsewhen (address === 338.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 339.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 340.U) {
    sreg := Cat("h4e".U(8.W), "h02".U(8.W))
  } .elsewhen (address === 341.U) {
    sreg := Cat("h4e".U(8.W), "h02".U(8.W))
  } .elsewhen (address === 342.U) {
    sreg := Cat("h4d".U(8.W), "h44".U(8.W))
  } .elsewhen (address === 343.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 344.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 345.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 346.U) {
    sreg := Cat("h4d".U(8.W), "h53".U(8.W))
  } .elsewhen (address === 347.U) {
    sreg := Cat("h4e".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 348.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 349.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 350.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 351.U) {
    sreg := Cat("h4d".U(8.W), "h65".U(8.W))
  } .elsewhen (address === 352.U) {
    sreg := Cat("h4e".U(8.W), "h04".U(8.W))
  } .elsewhen (address === 353.U) {
    sreg := Cat("h4d".U(8.W), "h73".U(8.W))
  } .elsewhen (address === 354.U) {
    sreg := Cat("h4e".U(8.W), "h20".U(8.W))
  } .elsewhen (address === 355.U) {
    sreg := Cat("h4d".U(8.W), "h83".U(8.W))
  } .elsewhen (address === 356.U) {
    sreg := Cat("h4e".U(8.W), "h20".U(8.W))
  } .elsewhen (address === 357.U) {
    sreg := Cat("h4f".U(8.W), "h01".U(8.W))
  } .elsewhen (address === 358.U) {
    sreg := Cat("h50".U(8.W), "h88".U(8.W))

  } .elsewhen (address === 359.U) {
    sreg := Cat("hfe".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 360.U) {
    sreg := Cat("h50".U(8.W), "h00".U(8.W)) // vga

  //Exp level
  } .elsewhen (address === 361.U) {
    sreg := Cat("hfe".U(8.W), "h01".U(8.W))
  } .elsewhen (address === 362.U) {
    sreg := Cat("h2b".U(8.W), "h02".U(8.W))
  } .elsewhen (address === 363.U) {
    sreg := Cat("h2c".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 364.U) {
    sreg := Cat("h2d".U(8.W), "h02".U(8.W))
  } .elsewhen (address === 365.U) {
    sreg := Cat("h2e".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 366.U) {
    sreg := Cat("h2f".U(8.W), "h02".U(8.W))
  } .elsewhen (address === 367.U) {
    sreg := Cat("h30".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 368.U) {
    sreg := Cat("h31".U(8.W), "h02".U(8.W))
  } .elsewhen (address === 369.U) {
    sreg := Cat("h32".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 370.U) {
    sreg := Cat("h33".U(8.W), "h00".U(8.W))

  /////////output//////// 
  } .elsewhen (address === 371.U) {
    sreg := Cat("hfe".U(8.W), "h00".U(8.W))
  } .elsewhen (address === 372.U) {
    sreg := Cat("hf1".U(8.W), "h07".U(8.W))
  } .elsewhen (address === 373.U) {
    sreg := Cat("hf2".U(8.W), "h01".U(8.W))

  }.otherwise{
    sreg := "h00_00".U(16.W) // End configuration
  }
}
