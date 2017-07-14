package org.apache.spark.rdd

import org.apache.spark.Partition

/**
 * Hack around [[org.apache.spark.rdd.NewHadoopPartition]] being private[spark]; we want access to it in
 * [[org.hammerlab.bam.spark.Main.hadoopBamLoad]].
 */
object AsHadoopPartition {
  def apply(partition: Partition): NewHadoopPartition =
    partition.asInstanceOf[NewHadoopPartition]
}
