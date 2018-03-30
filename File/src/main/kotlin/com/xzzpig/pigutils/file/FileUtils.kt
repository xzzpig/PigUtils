@file:JvmName("FileUtils")

package com.xzzpig.pigutils.file

import com.xzzpig.pigutils.core.justTry
import java.io.File
import java.nio.charset.Charset
import java.nio.file.*
import java.util.*

fun File.text(charset: Charset = Charsets.UTF_8): String = this.reader(charset).use { readText() }

val File.extension: String
    get() = name.substringAfterLast(".")

val File.mime: String?
    get() = Files.probeContentType(this.toPath())

fun File.watch(block: (WatchEvent<Path>) -> Unit) {
    watch(arrayOf(StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY), block)
}

fun File.parents(withSelf: Boolean = false): Iterator<File> = object : Iterator<File> {

    var par = if (!withSelf) this@parents.canonicalFile.parentFile else this@parents.canonicalFile

    override fun hasNext(): Boolean = par?.exists() ?: false

    override fun next(): File = par.apply { par = par.parentFile }

}

fun File.watch(kinds: Array<WatchEvent.Kind<Path>>, block: (WatchEvent<Path>) -> Unit) {
    watcherMap.getOrPut(canonicalPath) { LinkedList() }.push(block)
    toPath().register(watcher, kinds)
    if (watcherThread == null) {
        watcherThread = FileWatcherThread().apply { start() }
    }
}

fun File.watch(kinds: Int, block: (WatchEvent<Path>) -> Unit) {
    val list = LinkedList<WatchEvent.Kind<Path>>()
    if (kinds.and(0b100) != 0) list.push(StandardWatchEventKinds.ENTRY_CREATE)
    if (kinds.and(0b010) != 0) list.push(StandardWatchEventKinds.ENTRY_DELETE)
    if (kinds.and(0b001) != 0) list.push(StandardWatchEventKinds.ENTRY_MODIFY)
    watch(list.toTypedArray(), block)
}

fun File.unWatch() {
    watcherMap[canonicalPath]?.clear()
    watcherMap.remove(canonicalPath)
    if (watcherMap.isEmpty()) {
        watcherThread?.interrupt()
        watcherThread = null
    }
}

private class FileWatcherThread : Thread() {

    init {
        isDaemon = true
    }

    override fun run() {
        while (!this.isInterrupted) {
            justTry {
                val key = watcher.take()
                for (event in key.pollEvents()) {
                    val kind = event.kind()
                    if (kind === StandardWatchEventKinds.OVERFLOW) {//事件可能lost or discarded
                        continue
                    }
                    val e = event as WatchEvent<Path>
                    e.context().toFile().parents(true).forEach {
                        watcherMap[it.canonicalPath]?.forEach { it(e) }
                    }
                }
                if (!key.reset()) {
                    this.interrupt()
                }
            }
        }
    }
}

private var watcherThread: FileWatcherThread? = null

private val watcherMap = hashMapOf<String, LinkedList<(WatchEvent<Path>) -> Unit>>()

private val watcher: WatchService by lazy { FileSystems.getDefault().newWatchService() }