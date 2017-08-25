package org.mp4parser.isoviewer.parser

import org.mp4parser.BoxParser
import org.mp4parser.ParsableBox
import org.mp4parser.support.DoNotParseDetail
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.channels.ReadableByteChannel
import java.nio.channels.WritableByteChannel


class MediaDataBoxNoCopy : ParsableBox {

    private var header: ByteBuffer? = null
    private var fc: FileChannel? = null
    private var position: Long = 0
    private var contentSize: Long = 0

    override fun getType(): String {
        return "mdat"
    }


    @Throws(IOException::class)
    override fun getBox(writableByteChannel: WritableByteChannel) {
        fc!!.transferTo(position, contentSize, writableByteChannel)
    }

    override fun getSize(): Long {
        return header!!.limit() + contentSize
    }

    @DoNotParseDetail
    @Throws(IOException::class)
    override fun parse(dataSource: ReadableByteChannel, header: ByteBuffer, contentSize: Long, boxParser: BoxParser) {
        this.header = header
        this.fc = dataSource as FileChannel
        this.contentSize = contentSize
        this.position = fc!!.position()
        fc!!.position(this.position + contentSize)
    }


}
