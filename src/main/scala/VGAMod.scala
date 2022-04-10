package dkvideo

import chisel3._
import chisel3.util.Cat
import hdmicore.{VideoHdmi}
import hdmicore.video.{VideoParams,HVSync}

class VGAMod(vp: VideoParams) extends RawModule {
  val io = IO(new Bundle {
    val I_clk = Input(Clock())
    val I_rst_n = Input(Bool())
    val I_pxl_clk = Input(Clock())
    val I_rd_hres = Input(UInt(12.W)) //hor resolution
    val I_rd_vres = Input(UInt(12.W)) //ver resolution
    val videoSig = Output(new VideoHdmi())
  })

  withClockAndReset(io.I_pxl_clk, ~io.I_rst_n) {
  val VGA_DE = RegInit(false.B)
  val VGA_HSYNC = RegInit(false.B)
  val VGA_VSYNC = RegInit(false.B)

  val VGA_B = RegInit(0.U(5.W))
  val VGA_G = RegInit(0.U(6.W))
  val VGA_R = RegInit(0.U(5.W))

  val PixelCount = RegInit("b0".U(16.W))
  val LineCount = RegInit("b0".U(16.W))

  //pluse include in back pluse; t=pluse, sync act; t=bp, data act; t=bp+height, data end
  /*val V_BackPorch = 12.U(16.W)
  val V_Pluse = 11.U(16.W)
  val HightPixel = 272.U(16.W)
  val V_FrontPorch = 8.U(16.W)

  val H_BackPorch = 50.U(16.W)
  val H_Pluse = 10.U(16.W)
  val WidthPixel = 480.U(16.W)
  val H_FrontPorch = 8.U(16.W)*/


  /*val V_BackPorch = 0.U(16.W) //6
  val V_Pluse = 5.U(16.W)
  val HightPixel = 480.U(16.W)
  val V_FrontPorch = 45.U(16.W) //62

  val H_BackPorch = 182.U(16.W) //NOTE: 高像素时钟时，增加这里的延迟，方便K210加入中断
  val H_Pluse = 1.U(16.W)
  val WidthPixel = 800.U(16.W)
  val H_FrontPorch = 210.U(16.W)*/

  val V_BackPorch = vp.V_BACK.U(16.W)
  val V_Pluse = vp.V_SYNC.U(16.W)
  val HightPixel = vp.V_DISPLAY.U(16.W)
  val V_FrontPorch = vp.V_TOP.U(16.W) //62

  val H_BackPorch = vp.H_BACK.U(16.W) //NOTE: 高像素时钟时，增加这里的延迟，方便K210加入中断
  val H_Pluse = vp.H_SYNC.U(16.W)
  val WidthPixel = vp.H_DISPLAY.U(16.W)
  val H_FrontPorch = vp.H_FRONT.U(16.W)

  val BarCount = RegInit(5.U(9.W))
  val Width_bar = (io.I_rd_hres+H_BackPorch) / (BarCount+17.U) //45.U


  val vga_sync = Module(new HVSync(vp))
  /*VGA_DE := (vga_sync.io.hpos < io.I_rd_hres)&(vga_sync.io.vpos < io.I_rd_vres)
  VGA_HSYNC := vga_sync.io.hsync
  VGA_VSYNC := vga_sync.io.vsync*/
  PixelCount := H_BackPorch+vga_sync.io.hpos
  LineCount := V_BackPorch+vga_sync.io.vpos

  val PixelForHS = (WidthPixel+H_BackPorch)+H_FrontPorch
  val LineForVS = (HightPixel+V_BackPorch)+V_FrontPorch
  /*
  when (PixelCount === PixelForHS) {
    PixelCount := "b0".U(16.W)
    LineCount := LineCount+"b1".U(1.W)
  } .elsewhen (LineCount === LineForVS) {
    LineCount := "b0".U(16.W)
    PixelCount := "b0".U(16.W)
  } .otherwise {
    PixelCount := PixelCount+"b1".U(1.W)
  }
  */

  val Data_R = RegInit("b0".U(10.W))
  val Data_G = RegInit("b0".U(10.W))
  val Data_B = RegInit("b0".U(10.W))

    //注意这里HSYNC和VSYNC负极性
    VGA_HSYNC := (Mux(((PixelCount >= H_Pluse) && (PixelCount <= (WidthPixel+H_BackPorch))), "b0".U(1.W), "b1".U(1.W)) =/= 0.U)
    //VGA_VSYNC := (Mux((((LineCount >= 0.U) && (LineCount <= (V_Pluse-1.U)))), "b1".U(1.W), "b0".U(1.W)) =/= 0.U) //这里不减一的话，图片底部会往下拖尾？
    VGA_VSYNC := (Mux((((LineCount >= V_Pluse) && (LineCount <= (HightPixel+V_BackPorch+V_FrontPorch)))), "b0".U(1.W), "b1".U(1.W)) =/= 0.U)
    //FIFO_RST := Mux(((PixelCount === 0.U)), "b1".U(1.W), "b0".U(1.W)) //留给主机H_BackPorch的时间进入中断，发送数据

    VGA_DE := (Mux(((((PixelCount >= H_BackPorch) &&
                      (PixelCount <= (io.I_rd_hres+H_BackPorch))) &&
                      (LineCount >= V_BackPorch)) &&
                      (LineCount <= ((io.I_rd_vres+V_BackPorch)-1.U))), "b1".U(1.W), "b0".U(1.W)) =/= 0.U)
                                               //这里不减一，会抖动

    /*VGA_R := Mux(PixelCount < 200.U, "b00000".U(5.W),
            (Mux(PixelCount < 240.U, "b00001".U(5.W),
            (Mux(PixelCount < 280.U, "b00010".U(5.W),
            (Mux(PixelCount < 320.U, "b00100".U(5.W),
            (Mux(PixelCount < 360.U, "b01000".U(5.W),
            (Mux(PixelCount < 400.U, "b10000".U(5.W), "b00000".U(5.W))))))))))))

    VGA_G := Mux(PixelCount < 400.U, "b000000".U(6.W),
            (Mux(PixelCount < 440.U, "b000001".U(6.W),
            (Mux(PixelCount < 480.U, "b000010".U(6.W),
            (Mux(PixelCount < 520.U, "b000100".U(6.W),
            (Mux(PixelCount < 560.U, "b001000".U(6.W),
            (Mux(PixelCount < 600.U, "b010000".U(6.W),
            (Mux(PixelCount < 640.U, "b100000".U(6.W), "b000000".U(6.W))))))))))))))

    VGA_B := Mux(PixelCount < 640.U, "b00000".U(5.W),
            (Mux(PixelCount < 680.U, "b00001".U(5.W),
            (Mux(PixelCount < 720.U, "b00010".U(5.W),
            (Mux(PixelCount < 760.U, "b00100".U(5.W),
            (Mux(PixelCount < 800.U, "b01000".U(5.W),
            (Mux(PixelCount < 840.U, "b10000".U(5.W), "b00000".U(5.W))))))))))))*/

    VGA_R := Mux(PixelCount < (Width_bar*(BarCount+0.U)), "b00000".U(5.W),
            (Mux(PixelCount < (Width_bar*(BarCount+1.U)), "b00001".U(5.W),
            (Mux(PixelCount < (Width_bar*(BarCount+2.U)), "b00010".U(5.W),
            (Mux(PixelCount < (Width_bar*(BarCount+3.U)), "b00100".U(5.W),
            (Mux(PixelCount < (Width_bar*(BarCount+4.U)), "b01000".U(5.W),
            (Mux(PixelCount < (Width_bar*(BarCount+5.U)), "b10000".U(5.W), "b00000".U(5.W))))))))))))

    VGA_G := Mux(PixelCount < (Width_bar*(BarCount+5.U)), "b000000".U(6.W),
            (Mux(PixelCount < (Width_bar*(BarCount+6.U)), "b000001".U(6.W),
            (Mux(PixelCount < (Width_bar*(BarCount+7.U)), "b000010".U(6.W),
            (Mux(PixelCount < (Width_bar*(BarCount+8.U)), "b000100".U(6.W),
            (Mux(PixelCount < (Width_bar*(BarCount+9.U)), "b001000".U(6.W),
            (Mux(PixelCount < (Width_bar*(BarCount+10.U)), "b010000".U(6.W),
            (Mux(PixelCount < (Width_bar*(BarCount+11.U)), "b100000".U(6.W), "b000000".U(6.W))))))))))))))

    VGA_B := Mux(PixelCount < (Width_bar*(BarCount+11.U)), "b00000".U(5.W),
            (Mux(PixelCount < (Width_bar*(BarCount+12.U)), "b00001".U(5.W),
            (Mux(PixelCount < (Width_bar*(BarCount+13.U)), "b00010".U(5.W),
            (Mux(PixelCount < (Width_bar*(BarCount+14.U)), "b00100".U(5.W),
            (Mux(PixelCount < (Width_bar*(BarCount+15.U)), "b01000".U(5.W),
            (Mux(PixelCount < (Width_bar*(BarCount+16.U)), "b10000".U(5.W), "b00000".U(5.W))))))))))))

    io.videoSig.de := VGA_DE
    io.videoSig.hsync := VGA_HSYNC
    io.videoSig.vsync := VGA_VSYNC
    io.videoSig.pixel.red   := Mux(VGA_DE, Cat(VGA_R, 0.U(3.W)), "h00".U(8.W))
    io.videoSig.pixel.green := Mux(VGA_DE, Cat(VGA_G, 0.U(2.W)), "h00".U(8.W))
    io.videoSig.pixel.blue  := Mux(VGA_DE, Cat(VGA_B, 0.U(3.W)), "hff".U(8.W))
  } // withClockAndReset(io.I_pxl_clk, ~io.I_rst_n)
}
