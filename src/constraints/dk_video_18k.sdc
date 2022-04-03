//Copyright (C)2014-2020 GOWIN Semiconductor Corporation.
//All rights reserved.
//File Title: Timing Constraints file
//GOWIN Version: 1.9.7 Beta
//Created Time: 2020-09-27 09:37:56
create_clock -name I_clk -period 37.037 -waveform {0 18.518} [get_ports {I_clk}] -add
create_clock -name memPLL_io_clkout -period 6.173 -waveform {0 3.087} [get_nets {memPLL_io_clkout}] -add
create_clock -name memoryInterface_clk_out -period 12.346 -waveform {0 6.173} [get_nets {memoryInterface_clk_out}] -add
set_clock_groups -exclusive -group [get_clocks {I_clk}] -group [get_clocks {memPLL_io_clkout}] -group [get_clocks {memoryInterface_clk_out}]

