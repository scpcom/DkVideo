package dkvideo
package video

import chisel3._
import hdmicore.video.VideoParams
import hdl.gowin_pllvr.PLLParams

case class VideoMode(
  val params: VideoParams,
  val pll: PLLParams
)

package object VideoConsts {
  // D: 40.00 MHz, H: 37.879 kHz, V: 60.32 Hz
  // timings 25000 88 40 23 1 128 4
  //               hb hf vb vt hs vs
  val m800x600 = VideoMode(
    params = VideoParams(
      H_DISPLAY = 800, H_FRONT = 40,
      H_SYNC = 128, H_BACK = 88,
      V_SYNC = 4,  V_BACK = 23,
      V_TOP = 1, V_DISPLAY = 600,
      V_BOTTOM = 23
    ),
    pll = PLLParams(IDIV_SEL = 4, FBDIV_SEL = 36, ODIV_SEL = 4, DYN_SDIV_SEL = 16)
  )

  // # D: 65.00 MHz, H: 48.363 kHz, V: 60.00 Hz
  // timings 15385 160 24 29 3 136 6
  //               hb  hf vb vt hs vs
  val m1024x768 = VideoMode(
    params = VideoParams(
      H_DISPLAY = 1024, H_FRONT = 24,
      H_SYNC = 136, H_BACK = 160,
      V_SYNC = 6,  V_BACK = 29,
      V_TOP = 3, V_DISPLAY = 768,
      V_BOTTOM = 29
    ),
    pll = PLLParams(IDIV_SEL = 0, FBDIV_SEL = 11, ODIV_SEL = 2, DYN_SDIV_SEL = 26)
  )

  val m1280x720 = VideoMode(
    params = VideoParams(
      H_DISPLAY = 1280, H_FRONT = 110,
      H_SYNC = 40, H_BACK = 220,
      V_SYNC = 5,  V_BACK = 20,
      V_TOP = 5, V_DISPLAY = 720,
      V_BOTTOM = 20
    ),
    pll = PLLParams(IDIV_SEL = 3, FBDIV_SEL = 54, ODIV_SEL = 2, DYN_SDIV_SEL = 30)
  )
}
