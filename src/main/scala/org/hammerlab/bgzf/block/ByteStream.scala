package org.hammerlab.bgzf.block

import org.hammerlab.bgzf.Pos
import org.hammerlab.io.{ ByteChannel, SeekableByteChannel }
import org.hammerlab.iterator.FlatteningIterator._
import org.hammerlab.iterator.SimpleBufferedIterator

trait ByteStreamI[S <: StreamI]
  extends SimpleBufferedIterator[Byte] {
  def stream: S
  val it = stream.smush
  def curBlock: Option[Block] = it.cur
  def curPos: Option[Pos] = curBlock.map(_.pos)

  override protected def _advance: Option[Byte] = it.nextOption
}

case class ByteStream(stream: Stream)
  extends ByteStreamI[Stream]

object ByteStream {
  def apply(ch: ByteChannel): ByteStream = ByteStream(Stream(ch))
}

case class SeekableByteStream(stream: SeekableStream)
  extends ByteStreamI[SeekableStream] {
  def seek(pos: Pos): Unit = {
    stream.seek(pos.blockPos)
    it.reset()
    clear()
    curBlock
      .foreach {
        block ⇒
          block.idx = pos.offset
      }

  }
}

object SeekableByteStream {
  def apply(ch: SeekableByteChannel): SeekableByteStream =
    SeekableByteStream(
      SeekableStream(
        ch
      )
    )
}
