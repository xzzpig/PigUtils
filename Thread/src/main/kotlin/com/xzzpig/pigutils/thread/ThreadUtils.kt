@file:JvmName("ThreadUtils")

package com.xzzpig.pigutils.thread

import com.xzzpig.pigutils.core.weakRef
import java.util.concurrent.ForkJoinPool

fun Any.wait() {
    (this as java.lang.Object).wait()
}

fun Any.wait(timeout: Long) {
    (this as java.lang.Object).wait(timeout)
}

fun Any.wait(timeout: Long, nanos: Int) {
    (this as java.lang.Object).wait(timeout, nanos)
}

fun Any.nodifyAll() {
    (this as java.lang.Object).notifyAll()
}

fun Any.nodify() {
    (this as java.lang.Object).notify()
}

val DefaultJobManager by weakRef { JobManager.ThreadPoolJobManager({ ForkJoinPool.commonPool() }) }

inline fun <reified T> asyncJob(noinline block: () -> T): JobManager.Job<T> {
    return DefaultJobManager.async(block)
}