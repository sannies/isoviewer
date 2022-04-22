package org.mp4parser.isoviewer

import org.mp4parser.*
import java.io.File
import java.nio.channels.FileChannel
import java.nio.channels.ReadableByteChannel
import java.nio.channels.WritableByteChannel


class MyIsoFile(val file: File) : BasicContainer(), Box {
    override fun getBox(writableByteChannel: WritableByteChannel?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getType(): String {
        return file.name
    }

    override fun getSize(): Long {
        return file.length()
    }
}

class Dummy() : BasicContainer(), Box {
    override fun getBox(writableByteChannel: WritableByteChannel?) {
        TODO("not implemented")
    }

    override fun getType(): String {
        return "No file loaded"
    }

    override fun getSize(): Long {
        return 0;
    }

}

class BoxParser(private val offsets: MutableMap<Box, Long>): PropertyBoxParserImpl() {
    override fun parseBox(byteChannel: ReadableByteChannel?, parentType: String?): ParsableBox {
        val p = super.parseBox(byteChannel, parentType)
        if ( byteChannel is FileChannel) {
            offsets.set(p, byteChannel.position() - p.size )
        }
        return p
    }

}