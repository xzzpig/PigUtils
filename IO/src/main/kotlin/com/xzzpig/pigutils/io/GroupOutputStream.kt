package com.xzzpig.pigutils.io

import java.io.IOException
import java.io.OutputStream
import java.util.*

class GroupOutputStream : OutputStream() {

    private val outputStreams: MutableList<OutputStream> by lazy { LinkedList<OutputStream>() }

    fun add(vararg outs: OutputStream): GroupOutputStream {
        outputStreams.addAll(outs)
        return this
    }

    @Throws(IOException::class)
    override fun close() {
        for (out in outputStreams) {
            out.close()
        }
    }

    @Throws(IOException::class)
    override fun flush() {
        for (out in outputStreams) {
            out.flush()
        }
    }

    @Throws(IOException::class)
    override fun write(b: Int) {
        for (out in outputStreams) {
            out.write(b)
        }
    }

}
