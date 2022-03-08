#!/bin/sh

# inout is unknown in chisel, one bit array also missing
sed -i s/'input  .7.0. IO_hpram_dq'/'inout  [7:0] IO_hpram_dq'/g video_top.v
#sed -i s/'input        IO_hpram_rwds'/'inout        IO_hpram_rwds'/g video_top.v
sed -i s/'input        IO_hpram_rwds'/'inout  [0:0] IO_hpram_rwds'/g video_top.v
sed -i s/'output       O_hpram'/'output [0:0] O_hpram'/g video_top.v

sed -i /'wire .7.0. HyperRAM_Memory_Interface_Top_inst_IO_hpram_dq'/d video_top.v
sed -i /'assign HyperRAM_Memory_Interface_Top_inst_IO_hpram_dq = IO_hpram_dq;'/d video_top.v
sed -i s/'HyperRAM_Memory_Interface_Top_inst_IO_hpram_dq'/'IO_hpram_dq'/g video_top.v

sed -i /'wire  HyperRAM_Memory_Interface_Top_inst_IO_hpram_rwds;'/d video_top.v
sed -i /'wire .0.0. HyperRAM_Memory_Interface_Top_inst_IO_hpram_rwds;'/d video_top.v
sed -i /'assign HyperRAM_Memory_Interface_Top_inst_IO_hpram_rwds = IO_hpram_rwds;'/d video_top.v
sed -i s/'HyperRAM_Memory_Interface_Top_inst_IO_hpram_rwds'/'IO_hpram_rwds'/g video_top.v

# how to do clock mux in chisel?
sed -i "s/Video_Frame_Buffer_Top_inst_I_vin0_clk = I_clk/Video_Frame_Buffer_Top_inst_I_vin0_clk = (cnt_vs <= 10'h1ff) ? I_clk : PIXCLK/g" video_top.v
