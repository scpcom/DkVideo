#!/bin/sh
# remove some wires, rename others to shorter names

f=video_top.v

unwireO(){
a=`echo $1 | tr [] ..`
b=`echo $2 | tr [] ..`
sed -i /'wire  '$b';'/d $f
sed -i /'wire ..:.. '$b';'/d $f
sed -i /'wire ...:.. '$b';'/d $f
sed -i /'wire  '$a' = '$b';'/d $f
sed -i /'wire ..:.. '$a' = '$b';'/d $f
sed -i /'wire ...:.. '$a' = '$b';'/d $f
sed -i /'assign '$b' = '$a';'/d $f
sed -i /'assign '$a' = '$b';'/d $f
sed -i s/$2/$1/g $f
}

unwireOD(){
a=`echo $1 | tr [] ..`
b=`echo $2 | tr [] ..`
echo sed -i /'wire  '$b';'/d $f
echo sed -i /'wire ..:.. '$b';'/d $f
echo sed -i /'wire ...:.. '$b';'/d $f
echo sed -i /'wire  '$a' = '$b';'/d $f
echo sed -i /'wire ..:.. '$a' = '$b';'/d $f
echo sed -i /'wire ...:.. '$a' = '$b';'/d $f
echo sed -i /'assign '$b' = '$a';'/d $f
echo sed -i /'assign '$a' = '$b';'/d $f
echo sed -i s/$2/$1/g $f
}

unwireI(){
a=`echo $1 | tr [] ..`
b=`echo $2 | tr [] ..`
sed -i /'wire  '$a';'/d $f
sed -i /'wire ..:.. '$a';'/d $f
sed -i /'wire ...:.. '$a';'/d $f

sed -i /'wire  '$a' = '$b';'/d $f
sed -i /'wire ..:.. '$a' = '$b';'/d $f
sed -i /'wire ...:.. '$a' = '$b';'/d $f

sed -i /'assign '$b' = '$a';'/d $f
sed -i /'assign '$a' = '$b';'/d $f
sed -i s/$1/$2/g $f
}


unwireO O_hpram_ck_n HyperRAM_Memory_Interface_Top_inst_O_hpram_ck_n
unwireO O_hpram_ck HyperRAM_Memory_Interface_Top_inst_O_hpram_ck
unwireO O_hpram_cs_n HyperRAM_Memory_Interface_Top_inst_O_hpram_cs_n
unwireO O_hpram_reset_n HyperRAM_Memory_Interface_Top_inst_O_hpram_reset_n

unwireI Video_Frame_Buffer_Top_inst_I_rst_n HyperRAM_Memory_Interface_Top_inst_init_calib
unwireI Video_Frame_Buffer_Top_inst_I_init_calib HyperRAM_Memory_Interface_Top_inst_init_calib
unwireO init_calib HyperRAM_Memory_Interface_Top_inst_init_calib

unwireI HyperRAM_Memory_Interface_Top_inst_memory_clk GW_PLLVR_inst_clkout
unwireI HyperRAM_Memory_Interface_Top_inst_pll_lock GW_PLLVR_inst_lock

unwireI Video_Frame_Buffer_Top_inst_I_vout0_clk u_clkdiv_CLKOUT
unwireI hv_sync_clock u_clkdiv_CLKOUT
unwireI rgb2tmds_clock u_clkdiv_CLKOUT
unwireI serdesBlue_clock u_clkdiv_CLKOUT
unwireI serdesGreen_clock u_clkdiv_CLKOUT
unwireI serdesRed_clock u_clkdiv_CLKOUT
unwireI serdesClk_clock u_clkdiv_CLKOUT
unwireI DVI_TX_Top_inst_I_rgb_clk u_clkdiv_CLKOUT
unwireI pix_clk u_clkdiv_CLKOUT

unwireI u_OV2640_Controller_clock TMDS_PLLVR_inst_clkoutd
unwireI u_clkdiv_HCLKIN TMDS_PLLVR_inst_clkout
unwireI serdesBlue_io_fclk TMDS_PLLVR_inst_clkout
unwireI serdesGreen_io_fclk TMDS_PLLVR_inst_clkout
unwireI serdesRed_io_fclk TMDS_PLLVR_inst_clkout
unwireI serdesClk_io_fclk TMDS_PLLVR_inst_clkout
unwireI DVI_TX_Top_inst_I_serial_clk TMDS_PLLVR_inst_clkout

unwireI Video_Frame_Buffer_Top_inst_I_rd_data_valid HyperRAM_Memory_Interface_Top_inst_rd_data_valid
unwireI Video_Frame_Buffer_Top_inst_I_rd_data HyperRAM_Memory_Interface_Top_inst_rd_data
unwireO HyperRAM_Memory_Interface_Top_inst_rd_data Video_Frame_Buffer_Top_inst_O_rd_data
unwireO HyperRAM_Memory_Interface_Top_inst_wr_data Video_Frame_Buffer_Top_inst_O_wr_data

unwireO HyperRAM_Memory_Interface_Top_inst_cmd_en Video_Frame_Buffer_Top_inst_O_cmd_en
unwireO HyperRAM_Memory_Interface_Top_inst_cmd Video_Frame_Buffer_Top_inst_O_cmd
unwireO HyperRAM_Memory_Interface_Top_inst_addr Video_Frame_Buffer_Top_inst_O_addr
unwireO HyperRAM_Memory_Interface_Top_inst_data_mask Video_Frame_Buffer_Top_inst_O_data_mask

unwireI off0_syn_de Video_Frame_Buffer_Top_inst_O_vout0_den
unwireI off0_syn_data Video_Frame_Buffer_Top_inst_O_vout0_data

unwireI Video_Frame_Buffer_Top_inst_I_dma_clk HyperRAM_Memory_Interface_Top_inst_clk_out

unwireI pll_lock TMDS_PLLVR_inst_lock

unwireO SDA u_OV2640_Controller_io_siod
unwireO SCL u_OV2640_Controller_io_sioc

unwireI command LUT_io_command
unwireO finished LUT_io_finished
unwireI LUT_io_advance I2C_io_taken
unwireO io_siod I2C_io_siod
unwireO io_sioc I2C_io_sioc

#grep 'assign io_O_tmds' video_top.v | sed s/'  assign '/'unwireO '/g |sed s/' = '/' '/g |sed s/'; .*'/''/g
unwireO io_O_tmds_clk_n buffDiffClk_OB
unwireO io_O_tmds_clk_p buffDiffClk_O
unwireO io_O_tmds_data_0_n buffDiffBlue_OB
unwireO io_O_tmds_data_0_p buffDiffBlue_O
unwireO io_O_tmds_data_1_n buffDiffGreen_OB
unwireO io_O_tmds_data_1_p buffDiffGreen_O
unwireO io_O_tmds_data_2_n buffDiffRed_OB
unwireO io_O_tmds_data_2_p buffDiffRed_O

unwireO O_tmds_clk_n io_O_tmds_clk_n
unwireO O_tmds_clk_p io_O_tmds_clk_p
unwireO O_tmds_data_0_n io_O_tmds_data_0_n
unwireO O_tmds_data_0_p io_O_tmds_data_0_p
unwireO O_tmds_data_1_n io_O_tmds_data_1_n
unwireO O_tmds_data_1_p io_O_tmds_data_1_p
unwireO O_tmds_data_2_n io_O_tmds_data_2_n
unwireO O_tmds_data_2_p io_O_tmds_data_2_p

#grep 'assign buffDiff' video_top.v | sed s/'  assign '/'unwireI '/g |sed s/' = '/' '/g |sed s/'; .*'/''/g
unwireI buffDiffBlue_I serdesBlue_io_q
unwireI buffDiffGreen_I serdesGreen_io_q
unwireI buffDiffRed_I serdesRed_io_q
unwireI buffDiffClk_I serdesClk_io_q

#grep ' assign serdes.*_io_.* = ' video_top.v | sed s/'  assign '/'unwireI '/g |sed s/' = '/' '/g |sed s/'; .*'/''/g
unwireI serdesBlue_io_data rgb2tmds_io_tmds_blue
unwireI serdesBlue_io_fclk serial_clk
unwireI serdesGreen_io_data rgb2tmds_io_tmds_green
unwireI serdesGreen_io_fclk serial_clk
unwireI serdesRed_io_data rgb2tmds_io_tmds_red
unwireI serdesRed_io_fclk serial_clk
#unwireI serdesClk_io_data 10'h3e0
unwireI serdesClk_io_fclk serial_clk

sed -i s/'LUT_io_command'/'command'/g $f
sed -i s/I2C_io_taken/taken/g $f
sed -i s/I2C_io_send/send/g $f
sed -i s/'LUT_io_command'/'command'/g $f

sed -i s/'TMDS_PLLVR_inst_clkoutd'/'clk_12M'/g video_top.v
sed -i s/'TMDS_PLLVR_inst_clkout'/'serial_clk'/g video_top.v
sed -i s/'u_clkdiv_CLKOUT'/'pix_clk'/g video_top.v

sed -i s/'TMDS_PLLVR_inst_clkoutd'/'clk_12M'/g src/constraints/dk_video.sdc
sed -i s/'TMDS_PLLVR_inst_clkout'/'serial_clk'/g src/constraints/dk_video.sdc
sed -i s/'u_clkdiv_CLKOUT'/'pix_clk'/g src/constraints/dk_video.sdc

#sed -i s/Video_Frame_Buffer_Top_inst_I_vin0_clk/I_clk/g video_top.v
sed -i s/Video_Frame_Buffer_Top_inst_I_vin0_clk/ch0_vfb_clk_in/g video_top.v
sed -i s/testpattern_inst__I_pxl_clk/I_clk/g video_top.v
sed -i s/testpattern_inst__I_rst_n/I_rst_n/g video_top.v
sed -i s/HyperRAM_Memory_Interface_Top_inst_clk_out/dma_clk/g video_top.v
sed -i s/HyperRAM_Memory_Interface_Top_inst_clk/I_clk/g video_top.v
sed -i s/HyperRAM_Memory_Interface_Top_inst_rst_n/I_rst_n/g video_top.v
sed -i s/TMDS_PLLVR_inst_clkin/I_clk/g video_top.v
sed -i s/GW_PLLVR_inst_clkin/I_clk/g video_top.v

sed -i /'assign I_clk = I_clk'/d video_top.v
sed -i /'assign I_rst_n = I_rst_n'/d video_top.v

sed -i /'wire  I_clk;'/d video_top.v
sed -i /'wire  I_rst_n;'/d video_top.v

sed -i s/GW_PLLVR_inst_clkout/memory_clk/g video_top.v

sed -i s/Video_Frame_Buffer_Top_inst_I_init_calib/init_calib/g video_top.v
sed -i s/GW_PLLVR_inst_lock/mem_pll_lock/g video_top.v
sed -i s/HyperRAM_Memory_Interface_Top_inst_init_calib/init_calib/g video_top.v

sed -i s/TMDS_PLLVR_inst_lock/pll_lock/g video_top.v

sed -i s/HyperRAM_Memory_Interface_Top_inst_wr_data/wr_data/g video_top.v
sed -i s/HyperRAM_Memory_Interface_Top_inst_rd_data/rd_data/g video_top.v
sed -i s/HyperRAM_Memory_Interface_Top_inst_cmd/cmd/g video_top.v
sed -i s/HyperRAM_Memory_Interface_Top_inst_addr/addr/g video_top.v
sed -i s/HyperRAM_Memory_Interface_Top_inst_data_mask/data_mask/g video_top.v

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
