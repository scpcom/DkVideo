DkVideo
=======

Chisel translation of [TangNano-4K-example/dk_video](https://github.com/sipeed/TangNano-4K-example/tree/main/dk_video/project)

## Build this Chisel3 project

### Dependencies

#### JDK 8 or newer

We recommend LTS releases Java 8 and Java 11. You can install the JDK as recommended by your operating system, or use the prebuilt binaries from [AdoptOpenJDK](https://adoptopenjdk.net/).

#### SBT or mill

SBT is the most common built tool in the Scala community. You can download it [here](https://www.scala-sbt.org/download.html).  
mill is another Scala/Java build tool without obscure DSL like SBT. You can download it [here](https://github.com/com-lihaoyi/mill/releases)

### How to get started

#### Clone this repository

```sh
git clone git@github.com:scpcom/DkVideo.git
cd DkVideo
```

#### Build the project

To generate core with gowin DviTx encrypted ip do:
```sh
sbt "runMain dkvideo.video_topGen"
sh src/scripts/fix-verilog.sh
```

To use opensource HdmiCore:
```sh
sbt "runMain dkvideo.video_topGen noGowinDviTx"
sh src/scripts/fix-verilog.sh
```

Currently the fix-verilog.sh is used as workaround for inout until a real solution is found.

#### Generate the binary fs

Since the project still uses HyperRAM, VFB and PLL IP you can not use open source tools to build the binary.  
Only DVI_TX can be replaced with open source HdmiCore at this moment.  
Open the project in GOWIN FPGA Designer.  
If you use use opensource HdmiCore disable the file src/verilog/dvi_tx.v.  
Push the "Run All" button

#### Upload to the device

```sh
openFPGALoader -b tangnano4k impl/pnr/DkVideo.fs
```

