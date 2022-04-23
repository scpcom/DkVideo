#!/bin/sh
devtype=$1
outtype=$2
sdcfile=src/constraints/${devtype}.sdc

if [ -e ${sdcfile} ]; then
  if [ $outtype = lcd ]; then
    sed -i s/'get_nets {uClkDiv_CLKOUT}'/'get_ports {LCD_CLK}'/g ${sdcfile}
  else
    sed -i s/'get_ports {LCD_CLK}'/'get_nets {uClkDiv_CLKOUT}'/g ${sdcfile}
  fi
fi
