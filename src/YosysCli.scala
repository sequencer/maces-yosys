package maces.yosys

import maces._
import executor._
import os._

class YosysCli(sc: ScratchPad) extends MacesCli {
  override val scratchPad: _root_.maces.executor.ScratchPad = sc

  def yosysBin = read[Path]("yosys_bin").toString

  def verilogs = read[Seq[Path]]("verilogs").map(_.toString)

  def libertyCellPaths = read[Seq[String]]("liberty_cell_paths").map(_.toString)

  def topName = read[String]("top_name")

  def outOptVerilog = (workspace / read[String]("out_opt_verilog")).toString

  def entry: CheckPoint = init

  def command: Seq[String] = Seq(yosysBin)

  val init: CheckPoint = CP("") {
    _.contains("yosys>")
  }
  val readVerilog: CheckPoint = CP(s"read_verilog ${verilogs.reduce(_ + " " + _)}\n") {
    _.contains("Successfully")
  }
  val readLibertyLibrary: CheckPoint = CP(s"read_liberty ${libertyCellPaths.reduce(_ + " " + _)}\n") {
    _.contains("Imported")
  }
  val hierarchyCheckAndOpt: CheckPoint = CP(s"hierarchy -check -top $topName; proc; opt; fsm; opt; memory; opt; techmap; opt;\n") {
    str =>
      val targetString = "Finished OPT passes."
      // There are 4 opt passes.
      str.sliding(targetString.length).count(window => window == targetString) == 4
  }
  val mapCell: CheckPoint = CP(s"dfflibmap -liberty ${libertyCellPaths.reduce(_ + " " + _)}\n") {
    _.contains("Mapping DFF cells in module")
  }
  val writeVerilog: CheckPoint = CP(s"write_verilog $outOptVerilog\n") {
    _.contains("Dumping module")
  }
  val exit = CP("exit\n") {
    _ => true
  }
  init := readVerilog
  readVerilog := readLibertyLibrary
  readLibertyLibrary := hierarchyCheckAndOpt
  hierarchyCheckAndOpt := mapCell
  mapCell := writeVerilog
  writeVerilog := exit
}