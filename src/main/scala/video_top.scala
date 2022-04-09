package dkvideo

import sys.process.Process
import chisel3._
import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}

import hdmicore.video.{VideoMode, VideoConsts}
import camcore.{CameraType, ctNone, ctOV2640, ctGC0328}

// Based on:
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

object video_topGen extends App {
  var project_name = "DkVideo"
  var dvitx_name = "dvi_tx"
  var devtype: DeviceType = dtGW1NSR4C
  var memtype: MemoryType = mtHyperRAM
  var outtype: OutputType = otHDMI
  var gowinDviTx = true
  var rd_width = 800
  var rd_height = 600
  var rd_halign = 0
  var rd_valign = 0
  var fullscreen = 0
  var outmode = false
  var vmode: VideoMode = VideoConsts.m1280x720
  var camtype: CameraType = ctOV2640
  var camzoom: Boolean = false

  def set_video_mode(w: Integer, h: Integer, m: VideoMode)
  {
    if (outmode)
      vmode = m
    else {
      rd_width = w
      rd_height = h
    }
  }

  for(arg <- args){
    if ((arg == "GW1N-1") || (arg == "tangnano")) {
      project_name = "DkVideoN"
      devtype = dtGW1N1
      memtype = mtNone
      outtype = otLCD
    } else if ((arg == "GW1NZ-1") || (arg == "tangnano1k")) {
      project_name = "DkVideo1K"
      devtype = dtGW1NZ1
      memtype = mtNone
      outtype = otLCD
    } else if ((arg == "GW1NSR-4C") || (arg == "tangnano4k")) {
      project_name = "DkVideo4K"
      devtype = dtGW1NSR4C
    } else if ((arg == "GW1NR-9") || (arg == "tangnano9k")) {
      project_name = "DkVideo9K"
      dvitx_name = "dvi_tx_elvds"
      devtype = dtGW1NR9
      memtype = mtPSRAM
    } else if ((arg == "GW2AR-18C") || (arg == "gw2ar18c")) {
      project_name = "DkVideo18K"
      devtype = dtGW2AR18C
      memtype = mtPSRAM
    }

    if (arg == "lcd")
      outtype = otLCD

    if ((arg == "hpram") || (arg == "hyperram"))
      memtype = mtHyperRAM
    else if (arg == "psram")
      memtype = mtPSRAM
    else if (arg == "noram")
      memtype = mtNone

    if(arg == "noGowinDviTx")
      gowinDviTx = false
    else if(arg == "center"){
      rd_halign = 1
      rd_valign = 1
    }
    else if(arg == "left")
      rd_halign = 0
    else if(arg == "right")
      rd_halign = 2
    else if(arg == "top")
      rd_valign = 0
    else if(arg == "bottom")
      rd_valign = 2
    else if((arg == "vga") || (arg == "640x480")){
      rd_width = 640
      rd_height = 480
    }
    else if((arg == "vga-15:9") || (arg == "800x480")){
      set_video_mode(800, 480, VideoConsts.m800x480)
    }
    else if((arg == "svga") || (arg == "800x600")){
      set_video_mode(800, 600, VideoConsts.m800x600)
    }
    else if((arg == "480p") || (arg == "720x480")){
      set_video_mode(720, 480, VideoConsts.m720x480)
    }
    else if((arg == "sd") || (arg == "576p") || (arg == "720x576")){
      set_video_mode(720, 576, VideoConsts.m720x576)
    }
    else if((arg == "wsvga") || (arg == "1024x600")){
      set_video_mode(1024, 600, VideoConsts.m1024x600)
    }
    else if((arg == "xga") || (arg == "1024x768")){
      set_video_mode(1024, 768, VideoConsts.m1024x768)
    }
    else if((arg == "hd") || (arg == "720p") || (arg == "1280x720")){
      set_video_mode(1280, 720, VideoConsts.m1280x720)
    }
    else if((arg == "wxga") || (arg == "1280x800")){
      set_video_mode(1280, 800, VideoConsts.m1280x800)
    }
    else if((arg == "sxga") || (arg == "1280x1024")){
      set_video_mode(1280, 1024, VideoConsts.m1280x1024)
    }
    else if(arg == "1360x768"){
      set_video_mode(1360, 768, VideoConsts.m1360x768)
    }
    else if(arg == "1366x768"){
      set_video_mode(1366, 768, VideoConsts.m1366x768)
    }
    else if(arg == "1440x900"){
      set_video_mode(1440, 900, VideoConsts.m1440x900)
    }
    else if((arg == "wsxga") || (arg == "1600x900")){
      set_video_mode(1600, 900, VideoConsts.m1600x900)
    }
    else if(arg == "fullscreen")
      fullscreen = 1
    else if((arg == "out") || (arg == "outmode"))
      outmode = true
    else if(arg == "nocam")
      camtype = ctNone
    else if(arg == "ov2640")
      camtype = ctOV2640
    else if(arg == "gc0328")
      camtype = ctGC0328
    else if(arg == "zoom")
      camzoom = true
  }
  if (camtype == ctGC0328){
    rd_width = 640
    rd_height = 480
  }
  if (camtype == ctOV2640){
    if ((rd_width == 1024) && (rd_height == 600))
      camzoom = true
  }
  if(fullscreen == 1){
    /*if((rd_width <= 720) && (rd_height <= 480))
      vmode = VideoConsts.m720x480
    else*/ if((rd_width <= 720) && (rd_height <= 576))
      vmode = VideoConsts.m720x576
    /*else if((rd_width <= 800) && (rd_height <= 480))
      vmode = VideoConsts.m800x480*/
    /*else if((rd_width <= 800) && (rd_height <= 600))
      vmode = VideoConsts.m800x600*/
    else if((rd_width <= 1024) && (rd_height <= 600))
      vmode = VideoConsts.m1024x600
    else if((rd_width <= 1024) && (rd_height <= 768))
      vmode = VideoConsts.m1024x768
    else if((rd_width <= 1280) && (rd_height <= 720))
      vmode = VideoConsts.m1280x720
    else if((rd_width <= 1366) && (rd_height <= 768))
      vmode = VideoConsts.m1366x768
    else if((rd_width <= 1600) && (rd_height <= 900))
      vmode = VideoConsts.m1600x900
  }

  if (devtype == dtGW1N1)
    println("Building for tangnano")
  else if (devtype == dtGW1NZ1)
    println("Building for tangnano1k")
  else if (devtype == dtGW1NSR4C)
    println("Building for tangnano4k")
  else if (devtype == dtGW1NR9)
    println("Building for tangnano9k")
  else if (devtype == dtGW2AR18C)
    println("Building for gw2ar18c")

  if(outtype == otLCD)
    println("Generate DkVideo with LCD core")
  else if(gowinDviTx)
    println("Generate DkVideo with encrypted Gowin DviTx core")
  else
    println("Generate DkVideo with open source HdmiCore core")
  if (camtype == ctNone)
    println("camtype none")
  else if (camtype == ctGC0328)
    println("camtype GC0328")
  else
    println("camtype OV2640")
  println(s"camzoom $camzoom")
  println(s"rd_hres $rd_width")
  println(s"rd_vres $rd_height")
  val vop = VideoOutParams(
                dt = devtype, gowinDviTx = gowinDviTx,
                rd_width = rd_width, rd_height = rd_height, rd_halign = rd_halign, rd_valign = rd_valign,
                vmode = vmode, camtype = camtype,
                camzoom = camzoom, mt = memtype, ot = outtype)
  val stage_name = "video_top"
  val stage_args = args ++ Array("--output-file", stage_name, "--output-annotation-file", stage_name, "--chisel-output-file", stage_name)
  if (memtype == mtHyperRAM) {
    println("memtype hpram")
    (new ChiselStage).execute(stage_args,
      Seq(ChiselGeneratorAnnotation(() =>
          new video_hpram(vop))))
    val fixres = Process("sh ./src/scripts/fix-verilog.sh")
  } else if (memtype == mtPSRAM) {
    println("memtype psram")
    (new ChiselStage).execute(stage_args,
      Seq(ChiselGeneratorAnnotation(() =>
          new video_psram(vop))))
  } else {
    println("memtype noram")
    (new ChiselStage).execute(stage_args,
      Seq(ChiselGeneratorAnnotation(() =>
          new video_noram(vop))))
  }
  println("To generate the binary fs:")
  println(s"Open the $project_name project in GOWIN FPGA Designer.")
  if(!gowinDviTx)
    println(s"Disable the file src/verilog/$dvitx_name.v.")
  println("Push the \"Run All\" button")
}
