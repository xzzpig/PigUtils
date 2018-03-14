package com.xzzpig.pigutils.thread

import com.xzzpig.pigutils.annotation.TestPass
import com.xzzpig.pigutils.core.weakRef
import java.util.*
import java.util.concurrent.ExecutorService
import kotlin.collections.ArrayList

@TestPass
abstract class JobManager {

    class SimpleJobManager : JobManager() {

        private val jobList = LinkedList<Job<*>>()

        override fun cancelJob(job: Job<*>): Boolean {
            job.setCanceled(true)
            return true
        }

        override fun commitJob(job: Job<*>) {
            synchronized(jobList) {
                jobList.add(job)
            }
        }

        fun execute() {
            synchronized(jobList) { jobList.pop() }.execute()
        }

        fun executeAll() {
            synchronized(jobList) {
                ArrayList<Job<*>>(jobList.size).apply { addAll(jobList) }
            }.forEach {
                it.execute()
            }
        }
    }

    class ThreadPoolJobManager(threadPoolCreator: () -> ExecutorService) : JobManager() {
        override fun cancelJob(job: Job<*>): Boolean =
                if (job.executeState == 0) {
                    job.setCanceled(true)
                    true
                } else false

        override fun commitJob(job: Job<*>) {
            threadPool.execute { job.execute() }
        }

        private val threadPool by weakRef(threadPoolCreator)

    }

    inner class Job<T>(internal val block: () -> T) {

        @JvmField
        internal val WAIT = 0
        @JvmField
        internal val RUNNING = 1
        @JvmField
        internal val CANCELED = 2
        @JvmField
        internal val EXECUTED = 3

        var isCanceled: Boolean = false
            internal set(value) {
                field = value
            }

        var result: T? = null
            internal set(value) {
                field = value
            }
        internal var executeState: Int = WAIT

        fun waitResult(timeout: Long? = null): Job<T> {
            if (executeState > 1) return this
            synchronized(this) {
                if (timeout == null)
                    while (executeState <= 1) this.wait()
                else
                    this.wait(timeout)
            }
            return this
        }

        fun cancel(): Boolean {
            return this@JobManager.cancelJob(this)
        }
    }

    inline fun <reified T> async(noinline block: () -> T): Job<T> {
        return Job(block).apply {
            commitJob(this)
        }
    }

    /**
     * @return 是否取消成功
     */
    protected abstract fun cancelJob(job: Job<*>): Boolean

    protected abstract fun commitJob(job: Job<*>)

    protected fun <T> Job<T>.execute() {
        if (isCanceled) {
            executeState = CANCELED
            return
        }
        executeState = RUNNING
        block().apply {
            this@execute.result = this
            this@execute.executeState = EXECUTED
        }
        synchronized(this) {
            this.nodifyAll()
        }
    }

    protected fun <T> Job<T>.setCanceled(b: Boolean) {
        this.isCanceled = b
    }
}