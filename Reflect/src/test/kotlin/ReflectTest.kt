import com.xzzpig.pigutils.reflect.invoke
import com.xzzpig.pigutils.reflect.reflect
import org.junit.Test

class ReflectTest {
    @Test
    fun testClassNewInstance() {
        val testData: Any = TestData::class.java(1, "2")!!
        println(testData)
        val reflect = testData.reflect
        println(reflect["s"])
        reflect["s"] = null
        println(reflect["s"])
        reflect("aaaa", 123)
        reflect["i"] = 22
        println(reflect["i"])
    }

    data class TestData(private val i: Int, val s: String?) {
        fun aaaa() {
            println(i)
        }

        fun aaaa(num: Int) {
            println(num)
        }
    }
}