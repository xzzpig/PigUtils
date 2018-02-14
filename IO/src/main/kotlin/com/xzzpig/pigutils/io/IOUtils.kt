@file:JvmName("IOUtils")

package com.xzzpig.pigutils.io

import com.xzzpig.pigutils.core.later
import java.io.InputStream
import java.io.OutputStream
import java.io.Reader
import java.io.Writer

operator fun OutputStream.plus(right: OutputStream): GroupOutputStream = GroupOutputStream().add(this, right)

operator fun GroupOutputStream.plusAssign(right: OutputStream) {
    this.add(right)
}

operator fun Writer.plus(right: Writer): Writer = GroupWriter().add(this, right)

operator fun GroupWriter.plusAssign(right: Writer) {
    this.add(right)
}

fun InputStream.copyToWithClose(out: OutputStream, bufferSize: Int = DEFAULT_BUFFER_SIZE, closeIn: Boolean = true, closeOut: Boolean = true) =
        later {
            if (closeIn)
                this.close()
            if (closeOut)
                out.close()
        }.map {
            this.copyTo(out, bufferSize)
        }

fun Reader.copyTo(writer: Writer, bufferSize: Int = DEFAULT_BUFFER_SIZE, closeReader: Boolean = true, closeWriter: Boolean = true): Long =
        later {
            if (closeReader)
                this.close()
            if (closeWriter)
                writer.close()
        }.map {
            var bytesCopied: Long = 0
            val buffer = CharArray(bufferSize)
            var bytes = read(buffer)
            while (bytes >= 0) {
                writer.write(buffer, 0, bytes)
                bytesCopied += bytes
                bytes = read(buffer)
            }
            return bytesCopied
        }