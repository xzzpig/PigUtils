@file:JvmName("FileUtils")

package com.xzzpig.pigutils.file

import com.xzzpig.pigutils.core.later
import java.io.File
import java.nio.charset.Charset
import java.nio.file.Files

fun File.text(charset: Charset = Charsets.UTF_8): String = this.writer(charset).later { close() }.map { text(charset) }

val File.extension: String
    get() = name.substringAfterLast(".")

val File.mime: String?
    get() = Files.probeContentType(this.toPath())
