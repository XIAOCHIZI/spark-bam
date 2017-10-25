package org.hammerlab.bam.check.eager

import caseapp.{ AppName, ProgName, Recurse ⇒ R, ExtraName ⇒ O, HelpMessage ⇒ M }
import org.hammerlab.args.{ FindReadArgs, LogArgs, PostPartitionArgs }
import org.hammerlab.bam.check.indexed.IndexedRecordPositions
import org.hammerlab.bam.check.{ CallPartition, Blocks, CheckerApp, eager, seqdoop }
import org.hammerlab.cli.app
import org.hammerlab.cli.app.{ Args, Cmd }
import org.hammerlab.cli.args.PrintLimitArgs
import org.hammerlab.kryo._

object CheckBam
  extends Cmd {

  /**
   * Check every (bgzf-decompressed) byte-position in a BAM file for a record-start with and compare the results to the
   * true read-start positions.
   *
   * - Takes one argument: the path to a BAM file.
   * - Requires that BAM to have been indexed prior to running by [[org.hammerlab.bgzf.index.IndexBlocks]] and
   *   [[org.hammerlab.bam.index.IndexRecords]].
   *
   * @param sparkBam  if set, run the [[org.hammerlab.bam.check.eager.Checker]] on the input BAM file. If both [[sparkBam]] and
   *                  [[hadoopBam]] are set, they are compared to each other; if only one is set, then an
   *                  [[IndexedRecordPositions.Args.recordsPath indexed-records]] file is assumed to exist for the BAM, and is
   *                  used as the source of truth against which to compare.
   * @param hadoopBam if set, run the [[org.hammerlab.bam.check.seqdoop.Checker]] on the input BAM file. If both [[sparkBam]]
   *                  and [[hadoopBam]] are set, they are compared to each other; if only one is set, then an
   *                  [[IndexedRecordPositions.Args.recordsPath indexed-records]] file is assumed to exist for the BAM, and is
   *                  used as the source of truth against which to compare.
   */
  @AppName("Check all uncompressed positions in a BAM file for record-boundary-identification errors")
  @ProgName("… org.hammerlab.bam.check.Main")
  case class Opts(
      @R blocks: Blocks.Args,
      @R records: IndexedRecordPositions.Args,
      @R logging: LogArgs,
      @R printLimit: PrintLimitArgs,
      @R partitioning: PostPartitionArgs,
      @R findReadArgs: FindReadArgs,

      @O("s")
      @M("Run the spark-bam checker; if both or neither of -s and -u are set, then they are both run, and the results compared. If only one is set, its results are compared against a \"ground truth\" file generated by the index-records command")
      sparkBam: Boolean = false,

      @O("upstream") @O("u")
      @M("Run the hadoop-bam checker; if both or neither of -s and -u are set, then they are both run, and the results compared. If only one is set, its results are compared against a \"ground truth\" file generated by the index-records command")
      hadoopBam: Boolean = false
  )

  val main = Main(makeApp)
  def makeApp(args: Args[Opts]): app.App[Opts] =
    new CheckerApp(args, Registrar)
      with CallPartition {

      println("CheckBam makeApp")
      val calls =
        (args.sparkBam, args.hadoopBam) match {
          case (true, false) ⇒
            vsIndexed[Boolean, eager.Checker]
          case (false, true) ⇒
            vsIndexed[Boolean, seqdoop.Checker]
          case _ ⇒
            Blocks()
              .mapPartitions {
                callPartition[
                  eager.Checker,
                  Boolean,
                  seqdoop.Checker
                ]
              }
        }

      apply(
        calls,
        args.partitioning.resultsPerPartition
      )
    }

  case class Registrar() extends spark.Registrar(
    CallPartition,
    CheckerApp
  )
}
