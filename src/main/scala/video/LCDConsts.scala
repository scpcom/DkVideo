package dkvideo.video

import chisel3._
import fpgamacro.gowin.PLLParams
import hdmicore.video.{VideoMode,VideoParams}

package object LCDConsts {
  /* 27 MHz * (19+1) / (8+1) = 60.0 MHz / 6 = 10.00 MHz ; 60.0 MHz / 5 = 12.0 MHz */
  val p12000khz  = PLLParams(IDIV_SEL = 8, FBDIV_SEL = 19, ODIV_SEL = 8, DYN_SDIV_SEL = 6)
  /* 27 MHz * (13+1) / (2+1) = 126.0 MHz / 10 = 12.60 MHZ ; 126.0 MHz / 5 = 25,2 MHz */
  val p25200khz  = PLLParams(IDIV_SEL = 2, FBDIV_SEL = 13, ODIV_SEL = 4, DYN_SDIV_SEL = 10)
  /* 27 MHz * (36+1) / (5+1) = 166.5 MHz / 14 = 11.89 MHz ; 166.5 MHz / 5 = 33.3 MHz */
  val p33300khz  = PLLParams(IDIV_SEL = 5, FBDIV_SEL = 36, ODIV_SEL = 4, DYN_SDIV_SEL = 14)

  //pluse include in back pluse; t=pluse, sync act; t=bp, data act; t=bp+height, data end
  val m480x272 = VideoMode(
    params = VideoParams(
        V_BOTTOM = 12,
        V_BACK = 12,
        V_SYNC = 11,
        V_DISPLAY = 272,
        V_TOP = 8,

        H_BACK = 50,
        H_SYNC = 10,
        H_DISPLAY = 480,
        H_FRONT = 8,
    ),
    pll = p12000khz
  )

  val m800x480 = VideoMode(
    params = VideoParams(
        V_BOTTOM = 0,
        V_BACK = 0, //6
        V_SYNC = 5,
        V_DISPLAY = 480,
        V_TOP = 45, //62

        H_BACK = 182, //NOTE: 高像素时钟时，增加这里的延迟，方便K210加入中断
        H_SYNC = 1,
        H_DISPLAY = 800,
        H_FRONT = 210,
    ),
    pll = p33300khz
  )
}
