package com.xzzpig.pigutils.thread

import com.xzzpig.pigutils.core.weakRef
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

class AsyncExecutor<R>(threadPoolCreator: () -> ExecutorService) : ExecutorService {

    private val threadPool by weakRef(threadPoolCreator)
    /**
     * 执行结果的观察者
     * param:执行结果
     * return:是否被消耗，如果被消耗(true)则结果不会被[getResult]获取到
     */
    var observer: (R) -> Boolean = { false }

    override fun shutdown() {
        threadPool.shutdown()
    }

    override fun <T : Any?> submit(task: Callable<T>?): Future<T> = threadPool.submit(task)

    override fun <T : Any?> submit(task: Runnable?, result: T): Future<T> = threadPool.submit(task, result)

    override fun submit(task: Runnable?): Future<*> = threadPool.submit(task)

    override fun shutdownNow(): MutableList<Runnable> = threadPool.shutdownNow()

    override fun isShutdown(): Boolean = threadPool.isShutdown

    override fun awaitTermination(timeout: Long, unit: TimeUnit?): Boolean = threadPool.awaitTermination(timeout, unit)

    override fun <T : Any?> invokeAny(tasks: MutableCollection<out Callable<T>>?): T = threadPool.invokeAny(tasks)

    override fun <T : Any?> invokeAny(tasks: MutableCollection<out Callable<T>>?, timeout: Long, unit: TimeUnit?): T = threadPool.invokeAny(tasks, timeout, unit)

    override fun isTerminated(): Boolean = threadPool.isTerminated

    override fun <T : Any?> invokeAll(tasks: MutableCollection<out Callable<T>>?): MutableList<Future<T>> = threadPool.invokeAll(tasks)

    override fun <T : Any?> invokeAll(tasks: MutableCollection<out Callable<T>>?, timeout: Long, unit: TimeUnit?): MutableList<Future<T>> = threadPool.invokeAll(tasks, timeout, unit)

    override fun execute(command: Runnable?) {
        threadPool.execute(command)
    }

    private val resultMap = mutableMapOf<Int, R>()

    fun async(taskId: Int, block: () -> R) {
        execute {
            val result = block()
            if (observer(result)) return@execute
            synchronized(resultMap) {
                resultMap[taskId] = result
                (resultMap as java.lang.Object).notifyAll()
            }
        }
    }

    fun waitResult(taskId: Int, timeout: Long? = null): AsyncExecutor<R> {
        synchronized(resultMap) {
            if (timeout == null)
                while (!resultMap.containsKey(taskId)) resultMap.wait()
            else
                resultMap.wait(timeout)
        }
        return this
    }

    fun getResult(taskId: Int) = synchronized(resultMap) { resultMap.getValue(taskId) }
}