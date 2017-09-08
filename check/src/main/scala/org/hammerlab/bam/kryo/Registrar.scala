package org.hammerlab.bam.kryo

import java.util

import com.esotericsoftware.kryo.Kryo
import htsjdk.samtools.{ BAMRecord, SAMFileHeader, SAMProgramRecord, SAMReadGroupRecord, SAMSequenceDictionary, SAMSequenceRecord, ValidationStringency }
import org.apache.spark.serializer.KryoRegistrator
import org.hammerlab.args.{ ByteRanges, Endpoints, OffsetLength, Point }
import org.hammerlab.bam.check.full.error.{ Counts, Flags }
import org.hammerlab.bam.check.{ Blocks, NextRecord, PosMetadata }
import org.hammerlab.bam.header.ContigLengths.ContigLengthsSerializer
import org.hammerlab.bam.header.{ ContigLengths, Header }
import org.hammerlab.bam.index.Index.Chunk
import org.hammerlab.bam.spark.Split
import org.hammerlab.bgzf.Pos
import org.hammerlab.bgzf.block.Metadata
import org.hammerlab.genomics.reference
import org.hammerlab.kryo.serializeAs
import org.hammerlab.paths.Path

import scala.collection.mutable

class Registrar extends KryoRegistrator {
  override def registerClasses(kryo: Kryo): Unit = {
    implicit val k = kryo
    kryo.register(Class.forName("scala.reflect.ClassTag$$anon$1"))
    kryo.register(classOf[java.lang.Class[_]])

    kryo.register(classOf[Flags])
    kryo.register(classOf[Counts])

    /** [[org.hammerlab.bam.check.full.Main]] has a .collect */
    kryo.register(classOf[mutable.WrappedArray.ofRef[_]])

    /**
     * [[org.hammerlab.bam.spark.LoadBamContext.loadBamIntervals()]] parallelizes a [[Vector]] of [[Vector]]s of
     * [[Chunk]]s
     */
//    kryo.register(classOf[Chunk])
//    kryo.register(classOf[Pos])
//    kryo.register(classOf[Array[Vector[_]]])

    /**
     * [[org.hammerlab.bam.spark.LoadBamContext.loadBam]] parallelizes an [[Array[Long]]] of file-split start-positions.
     */
//    kryo.register(classOf[mutable.WrappedArray.ofLong])

    /** It also collects an [[Array[Split]]] in [[org.hammerlab.bam.spark.Spark]] mode */
    kryo.register(classOf[Array[Split]])
    kryo.register(classOf[Split])

    /** [[SAMFileHeader]] */
    kryo.register(classOf[SAMFileHeader])
    kryo.register(classOf[util.LinkedHashMap[_, _]])
    kryo.register(classOf[util.ArrayList[_]])
    kryo.register(classOf[util.HashMap[_, _]])
    kryo.register(classOf[SAMReadGroupRecord])
    kryo.register(classOf[SAMSequenceDictionary])
    kryo.register(Class.forName("scala.collection.convert.Wrappers$"))
    kryo.register(classOf[SAMSequenceRecord])
    kryo.register(classOf[SAMProgramRecord])

    /** Backs [[org.hammerlab.bam.header.ContigLengths]] */
    kryo.register(classOf[ContigLengths], ContigLengthsSerializer)

    new reference.Registrar().registerClasses(kryo)

    /**
     * [[org.hammerlab.bam.spark.load.CanLoadBam.loadBamIntervals]] broadcasts a
     * [[org.hammerlab.genomics.loci.set.LociSet]]
     */
//    new loci.set.Registrar().registerClasses(kryo)

    kryo.register(classOf[Metadata])
    kryo.register(classOf[Array[Metadata]])

    kryo.register(classOf[Pos])
    kryo.register(classOf[Array[Pos]])

    kryo.register(classOf[Header])

    /** [[org.hammerlab.bam.check.AnalyzeCalls]] broadcasts a [[Path]] */
    kryo.register(
      classOf[Path],
      serializeAs[Path, String](_.toString, Path(_))
    )

    Blocks.register

    kryo.register(classOf[Array[PosMetadata]])
    kryo.register(classOf[PosMetadata])
    kryo.register(classOf[NextRecord])
    kryo.register(classOf[BAMRecord])
    kryo.register(classOf[ValidationStringency])

    kryo.register(classOf[ByteRanges])
    kryo.register(classOf[mutable.ArraySeq[_]])
    kryo.register(classOf[Array[Object]])
    kryo.register(classOf[OffsetLength[_]])
    kryo.register(classOf[Point[_]])
    kryo.register(classOf[Endpoints[_]])
    kryo.register(classOf[spire.math.Integral[_]])
    kryo.register(Class.forName("spire.math.LongIsIntegral"))
    kryo.register(classOf[cats.kernel.instances.LongGroup])

    kryo.register(classOf[mutable.WrappedArray.ofInt])

    // compare-splits performs a reduce on some Result-filtered HLists
    import shapeless._
    kryo.register(classOf[_ :: _])
    kryo.register(HNil.getClass)

    import org.hammerlab.bytes._
    kryo.register(classOf[ B])
    kryo.register(classOf[KB])
    kryo.register(classOf[MB])
    kryo.register(classOf[GB])
    kryo.register(classOf[TB])
    kryo.register(classOf[PB])
    kryo.register(classOf[EB])

    kryo.register(classOf[Endpoints[_]])
    kryo.register(classOf[OffsetLength[_]])
    kryo.register(classOf[Point[_]])

    kryo.register(Class.forName("org.hammerlab.args.Integral$$anon$1"))
  }
}

object Registrar extends Registrar
