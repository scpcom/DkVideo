package dkvideo
package video

import chisel3._
import fpgamacro.gowin.PLLParams
import hdmicore.video.VideoParams

case class VideoMode(
  val params: VideoParams,
  val pll: PLLParams
)

package object VideoConsts {
  // to be fixed
  val m720x480 = VideoMode(
    params = VideoParams(
      H_DISPLAY = 720, H_FRONT = 16,
      H_SYNC = 62, H_BACK = 60,
      V_SYNC = 6,  V_BACK = 30,
      V_TOP = 9, V_DISPLAY = 480,
      V_BOTTOM = 30
    ),
    pll = PLLParams(IDIV_SEL = 0, FBDIV_SEL = 4, ODIV_SEL = 8, DYN_SDIV_SEL = 10)
  )

  // D: 27.00 MHz
  val m720x576 = VideoMode(
    params = VideoParams(
      H_DISPLAY = 720, H_FRONT = 12,
      H_SYNC = 64, H_BACK = 68,
      V_SYNC = 5,  V_BACK = 39,
      V_TOP = 5, V_DISPLAY = 576,
      V_BOTTOM = 39
    ),
    pll = PLLParams(IDIV_SEL = 0, FBDIV_SEL = 4, ODIV_SEL = 8, DYN_SDIV_SEL = 10)
  )

  // to be fixed
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

  val m1024x600 = VideoMode(
    params = VideoParams(
      H_DISPLAY = 1024, H_FRONT = 24,
      H_SYNC = 136, H_BACK = 160,
      V_SYNC = 6,  V_BACK = 29,
      V_TOP = 3, V_DISPLAY = 600,
      V_BOTTOM = 29
    ),
    pll = PLLParams(IDIV_SEL = 1, FBDIV_SEL = 18, ODIV_SEL = 4, DYN_SDIV_SEL = 20)
  )

  // D: 65.00 MHz, H: 48.363 kHz, V: 60.00 Hz
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

  // D: 108.00 MHz, H: 63.981 kHz, V: 60.02 Hz
  // timings 9260 248 48 38 1 112 3
  //              hb  hf vb vt hs vs
  val m1280x1024 = VideoMode(
    params = VideoParams(
      H_DISPLAY = 1280, H_FRONT = 48,
      H_SYNC = 112, H_BACK = 248,
      V_SYNC = 3,  V_BACK = 38,
      V_TOP = 1, V_DISPLAY = 1024,
      V_BOTTOM = 38
    ),
    pll = PLLParams(IDIV_SEL = 0, FBDIV_SEL = 19, ODIV_SEL = 2, DYN_SDIV_SEL = 44)
  )
}
