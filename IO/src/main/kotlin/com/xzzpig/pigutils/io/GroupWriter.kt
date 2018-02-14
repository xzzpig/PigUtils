package com.xzzpig.pigutils.io

import java.io.IOException
import java.io.Writer
import java.util.*

class GroupWriter : Writer() {

    private val subWriters: MutableList<Writer> by lazy { LinkedList<Writer>() }

    fun add(vararg writers: Writer): GroupWriter = this.apply {
        subWriters.addAll(writers)
    }

    @Throws(IOException::class)
    override fun write(cbuf: CharArray?, off: Int, len: Int) {
        subWriters.forEach { it.write(cbuf, off, len) }
    }

    @Throws(IOException::class)
    override fun flush() {
        subWriters.forEach { it.flush() }
    }

    @Throws(IOException::class)
    override fun close() {
        subWriters.forEach { it.close() }
    }
}