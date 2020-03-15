package maces.yosys.tests

import maces.tests._
import utest._
import maces.executor._
import maces.yosys._

object YosysCliSpce extends MacesTestSuite {
  val tests = Tests {
    test("yosys should give a opt verilog") {
      (new chisel3.stage.ChiselStage).run(Seq(
        new chisel3.stage.ChiselGeneratorAnnotation(() => new GCD),
        new firrtl.TargetDirAnnotation(testPath.toString)
      ))

      val sc = ScratchPad()
      sc.write("yosys_bin", "/usr/bin/yosys")
      sc.write("verilogs", Seq((testPath / "GCD.v").toString))
      sc.write("top_name", "GCD")
      sc.write("out_opt_verilog", (testPath / "GCD.opt.v").toString)
      sc.write("liberty_cell_paths", Seq((resourcesDir / "asap7sc7p5t_24_SIMPLE_RVT_TT.lib").toString))
      val cli = new YosysCli(sc)
      cli.run
    }
  }
}