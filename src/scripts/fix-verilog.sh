sed -i s/'output       SCL'/'inout        SCL'/g video_top.v
sed -i s/'input        SDA'/'inout        SDA'/g video_top.v
sed -i s/'input  .7.0. IO_hpram_dq'/'inout  [7:0] IO_hpram_dq'/g video_top.v
sed -i s/'input        IO_hpram_rwds'/'inout        IO_hpram_rwds'/g video_top.v
