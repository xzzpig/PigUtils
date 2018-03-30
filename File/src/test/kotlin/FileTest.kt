package com.xzzpig.pigutils.file

import com.xzzpig.pigutils.core.toFile
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import java.io.File
import java.io.IOException
import java.nio.file.*
import java.nio.file.StandardWatchEventKinds.OVERFLOW

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

    @Ignore
    @Test
    fun testJavaFileWatcher() {
        TestWatcherService(Paths.get("./")).handleEvents()
    }

    @Test
    fun testWatch() {
        val file = "./watch.test".toFile()
        file.createNewFile()
        file.parentFile.watch { println(it.context().toFile().canonicalPath + "|" + it.kind()) }
        Thread.sleep(2000)
        file.writeText("aaaa", Charsets.UTF_8)
        Thread.sleep(1000)
        file.delete()
        file.parentFile.unWatch()
    }

    @Ignore
    @Test
    fun test() {
        val num: Int = 0b111
        println(num.and(0b010))
    }

    @Test
    fun testParents() {
        val file = "./".toFile()
        val ir = file.parents(true)
        while (ir.hasNext()) {
            println(ir.next())
        }
    }
}


class TestWatcherService @Throws(IOException::class)
constructor(path: Path) {

    private val watcher: WatchService = FileSystems.getDefault().newWatchService()

    init {
        path.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY)
    }

    @Throws(InterruptedException::class)
    fun handleEvents() {
        while (true) {
            val key = watcher.take()
            for (event in key.pollEvents()) {
                val kind = event.kind()

                if (kind === OVERFLOW) {//事件可能lost or discarded
                    continue
                }

                val e = event as WatchEvent<Path>
                val fileName = e.context()

                System.out.printf("Event %s has happened,which fileName is %s%n", kind.name(), fileName)
            }
            if (!key.reset()) {
                break
            }
        }
    }
//
//    companion object {
//        @Throws(IOException::class, InterruptedException::class)
//        @JvmStatic
//        fun main(args: Array<String>) {
//            if (args.size != 1) {
//                println("请设置要监听的文件目录作为参数")
//                System.exit(-1)
//            }
//            TestWatcherService(Paths.get(args[0])).handleEvents()
//        }
//    }
}
