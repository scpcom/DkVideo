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

```sh
sbt "runMain hdl.video_topGen"
```

#### Generate the binary fs

Create new project in GOWIN FPGA Designer, select GW1NSR-LV4CQN48PC7/I6 as target device.
Add video_top.v and all files from src/constraints and src/verilog to the project.
Push the "Run All" button

#### Upload to the device

```sh
openFPGALoader -b tangnano4k impl/pnr/DkVideo.fs
```

