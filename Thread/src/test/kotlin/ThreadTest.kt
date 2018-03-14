import com.xzzpig.pigutils.thread.*
import org.junit.Ignore
import org.junit.Test
import java.util.concurrent.Executors
import kotlin.concurrent.thread


class ThreadTest {
    val executor = AsyncExecutor<String>(Executors::newCachedThreadPool)
//    val executor = AsyncExecutor<String> { Executors.newFixedThreadPool(10) }

    val threadPoolJobManager = JobManager.ThreadPoolJobManager(Executors::newCachedThreadPool)
    val simpleJobManager = JobManager.SimpleJobManager()

    @Test
    fun testAsyncExecutor() {
        executor.observer = { false.apply { System.err.println(it) } }
        for (i in 0..100) {
            executor.async(i) {
                Thread.sleep((i * 50).toLong())
                return@async i.toString()
            }
        }
        for (i in 0..100) {
            println(executor.waitResult(i).getResult(i))
        }
    }

    @Ignore
    @Test
    fun testDefaultJobManager() {
        val jobList = (0..100).map {
            asyncJob {
                Thread.sleep((it * 50).toLong())
                return@asyncJob it.toString()
            }
        }
        for (job in jobList) {
            println(job.waitResult().result)
        }
    }

    @Ignore
    @Test
    fun testThreadPoolJobManager() {
        val jobList = (0..100).map {
            threadPoolJobManager.async {
                Thread.sleep((it * 50).toLong())
                return@async it.toString()
            }
        }
        for (job in jobList) {
            println(job.waitResult().result)
        }
    }

    @Ignore
    @Test
    fun testSimpleJobManager() {
        val jobList = (0..100).map {
            simpleJobManager.async {
                Thread.sleep((it * 50).toLong())
                return@async it.toString()
            }
        }
        thread {
            for (i in 0..100) {
//                System.err.println(i)
                simpleJobManager.execute()
            }
        }
        for (job in jobList) {
            println(job.waitResult(1000).result)
        }
    }

    @Test
    fun testTimeout() {
        val obj = "1234"
        thread {
            Thread.sleep(5000)
            synchronized(obj) {
                obj.nodifyAll()
            }
        }
        synchronized(obj) {
            obj.wait(1000)
        }
        println("finish")
    }
}