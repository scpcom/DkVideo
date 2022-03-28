#!/bin/sh

# add missing one bit array
sed -i s/'inout        IO_hpram_rwds'/'inout  [0:0] IO_hpram_rwds'/g video_top.v
sed -i s/'output       O_hpram'/'output [0:0] O_hpram'/g video_top.v
