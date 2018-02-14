package com.xzzpig.pigutils.file

import com.xzzpig.pigutils.core.toFile
import org.junit.Before
import org.junit.Test
import java.io.File

class FileTest {

    lateinit var file: File

    @Before
    fun init() {
        file = this::class.java.getResource("FileTest.class").file.toFile()
    }

    @Test
    fun testExtension() {
        println(file.extension)
    }

    @Test
    fun testMime() {
        println("1.jpg".toFile().mime)
    }
}