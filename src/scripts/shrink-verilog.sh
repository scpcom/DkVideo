#!/bin/sh
# remove some wires, rename others to shorter names

sed -i s/'TMDS_PLLVR_inst_clkoutd'/'clk_12M'/g video_top.v
sed -i s/'TMDS_PLLVR_inst_clkout'/'serial_clk'/g video_top.v
sed -i s/'u_clkdiv_CLKOUT'/'pix_clk'/g video_top.v
sed -i /'wire  pix_clk = pix_clk'/d video_top.v

sed -i s/'TMDS_PLLVR_inst_clkoutd'/'clk_12M'/g src/constraints/dk_video.sdc
sed -i s/'TMDS_PLLVR_inst_clkout'/'serial_clk'/g src/constraints/dk_video.sdc
sed -i s/'u_clkdiv_CLKOUT'/'pix_clk'/g src/constraints/dk_video.sdc

sed -i /'wire  HyperRAM_Memory_Interface_Top_inst_O_hpram_c.*;'/d video_top.v
sed -i /'assign HyperRAM_Memory_Interface_Top_inst_O_hpram_c.* = O_hpram_c.*;'/d video_top.v
sed -i /'assign O_hpram_c.* = HyperRAM_Memory_Interface_Top_inst_O_hpram_c.*;'/d video_top.v
sed -i s/'HyperRAM_Memory_Interface_Top_inst_O_hpram_c'/'O_hpram_c'/g video_top.v

sed -i /'wire  HyperRAM_Memory_Interface_Top_inst_O_hpram_reset_n;'/d video_top.v
sed -i /'assign HyperRAM_Memory_Interface_Top_inst_O_hpram_reset_n = O_hpram_reset_n;'/d video_top.v
sed -i /'assign O_hpram_reset_n = HyperRAM_Memory_Interface_Top_inst_O_hpram_reset_n;'/d video_top.v
sed -i s/'HyperRAM_Memory_Interface_Top_inst_O_hpram_reset_n'/'O_hpram_reset_n'/g video_top.v

sed -i /'wire  Video_Frame_Buffer_Top_inst_I_rst_n;'/d video_top.v
sed -i /'wire  Video_Frame_Buffer_Top_inst_I_init_calib;'/d video_top.v
sed -i /'wire  GW_PLLVR_inst_clkout;'/d video_top.v
sed -i /'wire  GW_PLLVR_inst_lock;'/d video_top.v
sed -i /'wire  HyperRAM_Memory_Interface_Top_inst_init_calib;'/d video_top.v

#sed -i s/Video_Frame_Buffer_Top_inst_I_vin0_clk/I_clk/g video_top.v
sed -i s/Video_Frame_Buffer_Top_inst_I_vin0_clk/ch0_vfb_clk_in/g video_top.v
sed -i s/testpattern_inst__I_pxl_clk/I_clk/g video_top.v
sed -i s/testpattern_inst__I_rst_n/I_rst_n/g video_top.v
sed -i s/Video_Frame_Buffer_Top_inst_I_vout0_clk/pix_clk/g video_top.v
sed -i s/HyperRAM_Memory_Interface_Top_inst_clk/I_clk/g video_top.v
sed -i s/HyperRAM_Memory_Interface_Top_inst_rst_n/I_rst_n/g video_top.v
sed -i s/syn_gen_inst_I_pxl_clk/pix_clk/g video_top.v
sed -i s/TMDS_PLLVR_inst_clkin/I_clk/g video_top.v
sed -i s/u_clkdiv_HCLKIN/serial_clk/g video_top.v
sed -i s/DVI_TX_Top_inst_I_serial_clk/serial_clk/g video_top.v
sed -i s/DVI_TX_Top_inst_I_rgb_clk/pix_clk/g video_top.v
sed -i s/GW_PLLVR_inst_clkin/I_clk/g video_top.v

sed -i /'assign I_clk = I_clk'/d video_top.v
sed -i /'assign I_rst_n = I_rst_n'/d video_top.v
sed -i /'assign pix_clk = pix_clk'/d video_top.v
sed -i /'assign serial_clk = serial_clk'/d video_top.v

sed -i /'wire  I_clk;'/d video_top.v
sed -i /'wire  I_rst_n;'/d video_top.v
sed -i /'wire  pix_clk;'/d video_top.v
sed -i /'wire  serial_clk;'/d video_top.v

sed -i s/HyperRAM_Memory_Interface_Top_inst_memory_clk/memory_clk/g video_top.v
sed -i s/GW_PLLVR_inst_clkout/memory_clk/g video_top.v
sed -i /'assign memory_clk = memory_clk;'/d video_top.v

sed -i s/Video_Frame_Buffer_Top_inst_I_init_calib/init_calib/g video_top.v
sed -i s/GW_PLLVR_inst_lock/mem_pll_lock/g video_top.v
sed -i s/HyperRAM_Memory_Interface_Top_inst_init_calib/init_calib/g video_top.v
sed -i s/HyperRAM_Memory_Interface_Top_inst_pll_lock/mem_pll_lock/g video_top.v

sed -i /'wire  init_calib = init_calib;'/d video_top.v
sed -i /'assign init_calib = init_calib;'/d video_top.v
sed -i /'assign mem_pll_lock = mem_pll_lock;'/d video_top.v

sed -i s/Video_Frame_Buffer_Top_inst_I_rst_n/init_calib/g video_top.v
sed -i /'assign init_calib = init_calib;'/d video_top.v
sed -i s/TMDS_PLLVR_inst_lock/pll_lock/g video_top.v
sed -i /'wire  pll_lock = pll_lock;'/d video_top.v

sed -i s/Video_Frame_Buffer_Top_inst_I_dma_clk/dma_clk/g video_top.v
sed -i /'wire  I_clk_out;'/d video_top.v
sed -i s/I_clk_out/dma_clk/g video_top.v
sed -i /'assign dma_clk = dma_clk;'/d video_top.v

sed -i /'wire .31.0. Video_Frame_Buffer_Top_inst_O_wr_data'/d video_top.v
sed -i s/HyperRAM_Memory_Interface_Top_inst_wr_data/wr_data/g video_top.v
sed -i s/Video_Frame_Buffer_Top_inst_O_wr_data/wr_data/g video_top.v
sed -i /'assign wr_data = wr_data;'/d video_top.v

sed -i /'wire  Video_Frame_Buffer_Top_inst_I_rd_data_valid;'/d video_top.v
sed -i /'wire .31:0. Video_Frame_Buffer_Top_inst_I_rd_data;'/d video_top.v

sed -i s/HyperRAM_Memory_Interface_Top_inst_rd_data/rd_data/g video_top.v
sed -i s/Video_Frame_Buffer_Top_inst_O_rd_data/rd_data/g video_top.v
sed -i s/Video_Frame_Buffer_Top_inst_I_rd_data/rd_data/g video_top.v
sed -i /'assign rd_data = rd_data;'/d video_top.v
sed -i /'assign rd_data_valid = rd_data_valid;'/d video_top.v

sed -i /'wire  Video_Frame_Buffer_Top_inst_._cmd;'/d video_top.v
sed -i /'wire  Video_Frame_Buffer_Top_inst_._cmd_en;'/d video_top.v

sed -i s/HyperRAM_Memory_Interface_Top_inst_cmd/cmd/g video_top.v
sed -i s/Video_Frame_Buffer_Top_inst_O_cmd/cmd/g video_top.v
sed -i s/Video_Frame_Buffer_Top_inst_I_cmd/cmd/g video_top.v
sed -i /'assign cmd = cmd;'/d video_top.v
sed -i /'assign cmd_en = cmd_en;'/d video_top.v

sed -i /'wire .21:0. Video_Frame_Buffer_Top_inst_._addr;'/d video_top.v

sed -i s/HyperRAM_Memory_Interface_Top_inst_addr/addr/g video_top.v
sed -i s/Video_Frame_Buffer_Top_inst_O_addr/addr/g video_top.v
sed -i s/Video_Frame_Buffer_Top_inst_I_addr/addr/g video_top.v
sed -i /'assign addr = addr;'/d video_top.v

sed -i /'wire .3:0. Video_Frame_Buffer_Top_inst_._data_mask;'/d video_top.v

sed -i s/HyperRAM_Memory_Interface_Top_inst_data_mask/data_mask/g video_top.v
sed -i s/Video_Frame_Buffer_Top_inst_O_data_mask/data_mask/g video_top.v
sed -i s/Video_Frame_Buffer_Top_inst_I_data_mask/data_mask/g video_top.v
sed -i /'assign data_mask = data_mask;'/d video_top.v

sed -i /'wire  Video_Frame_Buffer_Top_inst_I_vout0_de;'/d video_top.v

sed -i s/Video_Frame_Buffer_Top_inst_I_vout0_de/syn_off0_re/g video_top.v
sed -i s/syn_gen_inst_O_rden/syn_off0_re/g video_top.v
sed -i /'assign syn_off0_re = syn_off0_re;'/d video_top.v

sed -i /'wire  out_de = syn_gen_inst_O_de;'/d video_top.v
sed -i /'wire  syn_off0_hs = syn_gen_inst_O_hs;'/d video_top.v
sed -i /'wire  syn_off0_vs = syn_gen_inst_O_vs;'/d video_top.v

sed -i s/syn_gen_inst_O_de/out_de/g video_top.v
sed -i s/syn_gen_inst_O_hs/syn_off0_hs/g video_top.v
sed -i s/syn_gen_inst_O_vs/syn_off0_vs/g video_top.v

sed -i /'wire  syn_gen_inst_I_rst_n;'/d video_top.v
sed -i /'assign syn_gen_inst_I_rst_n = I_rst_n'/d video_top.v
sed -i s/syn_gen_inst_I_rst_n/hdmi_rst_n/g video_top.v

sed -i /'wire  u_clkdiv_RESETN;'/d video_top.v
sed -i /'assign u_clkdiv_RESETN = I_rst_n'/d video_top.v
sed -i s/u_clkdiv_RESETN/hdmi_rst_n/g video_top.v

sed -i /'wire  DVI_TX_Top_inst_I_rst_n;'/d video_top.v
sed -i /'assign DVI_TX_Top_inst_I_rst_n = I_rst_n'/d video_top.v
sed -i s/DVI_TX_Top_inst_I_rst_n/hdmi_rst_n/g video_top.v

sed -i s/Video_Frame_Buffer_Top_inst_I_vin0_vs_n/ch0_vfb_vs_in/g video_top.v
sed -i s/Video_Frame_Buffer_Top_inst_I_vin0_de/ch0_vfb_de_in/g video_top.v
sed -i s/Video_Frame_Buffer_Top_inst_I_vin0_data/ch0_vfb_data_in/g video_top.v
sed -i s/Video_Frame_Buffer_Top_inst_O_vout0_den/off0_syn_de/g video_top.v
sed -i s/Video_Frame_Buffer_Top_inst_O_vout0_data/off0_syn_data/g video_top.v

sed -i /'wire .15.0. off0_syn_data = off0_syn_data'/d video_top.v
sed -i /'wire  off0_syn_de = off0_syn_de'/d video_top.v

sed -i s/'DVI_TX_Top_inst_I_rgb_vs/rgb_vs'/g video_top.v
sed -i s/'DVI_TX_Top_inst_I_rgb_hs/rgb_hs'/g video_top.v
sed -i s/'DVI_TX_Top_inst_I_rgb_de/rgb_de'/g video_top.v

sed -i /'wire  DVI_TX_Top_inst_O_tmds_clk_p'/d video_top.v
sed -i /'wire  DVI_TX_Top_inst_O_tmds_clk_n'/d video_top.v
sed -i s/DVI_TX_Top_inst_O_tmds_clk_p/O_tmds_clk_p/g video_top.v
sed -i s/DVI_TX_Top_inst_O_tmds_clk_n/O_tmds_clk_n/g video_top.v
sed -i /'assign O_tmds_clk_p = O_tmds_clk_p'/d video_top.v
sed -i /'assign O_tmds_clk_n = O_tmds_clk_n'/d video_top.v

sed -i /'wire .2.0. DVI_TX_Top_inst_O_tmds_data_p'/d video_top.v
sed -i /'wire .2.0. DVI_TX_Top_inst_O_tmds_data_n'/d video_top.v
if grep -q 'O_tmds_data_2_p' video_top.v ; then
  sed -i /'assign O_tmds_data_._. = DVI_TX_Top_inst_O_tmds_data_'/d video_top.v
  sed -i s/DVI_TX_Top_inst_O_tmds_data_p/'{O_tmds_data_2_p, O_tmds_data_1_p, O_tmds_data_0_p}'/g video_top.v
  sed -i s/DVI_TX_Top_inst_O_tmds_data_n/'{O_tmds_data_2_n, O_tmds_data_1_n, O_tmds_data_0_n}'/g video_top.v
else
  sed -i s/DVI_TX_Top_inst_O_tmds_data_p/O_tmds_data_p/g video_top.v
  sed -i s/DVI_TX_Top_inst_O_tmds_data_n/O_tmds_data_n/g video_top.v
  sed -i /'assign O_tmds_data_p = O_tmds_data_p'/d video_top.v
  sed -i /'assign O_tmds_data_n = O_tmds_data_n'/d video_top.v
fi
