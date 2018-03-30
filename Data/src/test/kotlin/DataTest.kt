import com.xzzpig.pigutils.core.IData
import com.xzzpig.pigutils.core.MapData
import com.xzzpig.pigutils.data.*
import org.junit.Assert.assertEquals
import org.junit.Test

class DataTest {
    @Test
    fun testDataObserver() {
        val data = MapData().observable()
        data.addObserver(TestObserver)

        data["aaa"] = "bbb"
        println()
        data["aaa"] = ObservableObj
        println()
        data["aaa"]
        println()
        data.remove("aaa")
        println()
        data["aaa"] = ObservableObj
        println()
        data.clear()
    }

    @Test
    fun testMapObserver() {
        val data = mutableMapOf<String, Any?>().observable()
        data.addObserver(TestObserver)

        data["aaa"] = "bbb"
        println()
        data["aaa"] = ObservableObj
        println()
        data["aaa"]
        println()
        data.remove("aaa")
        println()
        data["aaa"] = ObservableObj
        println()
        data.clear()
    }


    @Test
    fun testToBean() {
        val data = MapData(mutableMapOf("aaa" to "bbb", "ccc" to 123, "ddd" to "456", "testBean" to MapData(mutableMapOf("aaa" to "ccc", "ccc" to 233, "ddd" to "444"))))
        println(data.toBean(TestBean::class.java))
    }

    @Test
    fun testInject() {
        val data = MapData(mutableMapOf("aaa" to "bbb", "ccc" to 123, "ddd" to 456, "testBean" to MapData(mutableMapOf("aaa" to "ccc", "ccc" to 233, "ddd" to "444"))))
        println(data)
        val bean = TestBean().apply { data.injectTo(this) }
        println(bean)
        data.clear()
        println(data)
        println(bean.injectTo(data, classFilter = { if (it == TestBean::class.java) IData::class.java else null }))
    }

    data class TestBean(val aaa: String? = null, var ccc: String? = null, var ddd: Int = 0, var testBean: TestBean? = null)

    @Test
    fun test(): Unit {
        println(String::class.java.isAssignableFrom(String::class.java))
    }

    @Test
    fun testMultiData() {
        val data = MultiData { MapData() }
        data["aaa.bbb.ccc"] = "ddd"
        println(data["aaa.bbb.ccc"])
        assertEquals(data["aaa.bbb.ccc"], "ddd")
        println(data.getData("aaa.bbb")["ccc"])
        assertEquals(data["aaa.bbb.ccc"], data.getData("aaa.bbb")["ccc"])
        data.getData("aaa.bbb")["ccc"] = "eee"
        println(data.getData("aaa").getData("bbb")["ccc"])
        assertEquals(data.getData("aaa").getData("bbb")["ccc"], "eee")
        data.setData("aaa.bbb.ccc.ddd", data)
        println(data["aaa.bbb.ccc.ddd.aaa.bbb.ccc.ddd.aaa.bbb.ccc"])
        assertEquals(data["aaa.bbb.ccc.ddd.aaa.bbb.ccc.ddd.aaa.bbb.ccc"], "eee")
    }
}

object ObservableObj : Observable {
    override fun onBind(container: Any, key: Any) {
        println("[BIND]$this is binded as $container[$key]")
    }

    override fun toString(): String = this::class.toString()

    override fun onUnbind(container: Any, key: Any) {
        println("[UNBIND]$this is unbinded as $container[$key]")
    }
}

object TestObserver : DataObserver<Any, String, Any?>() {
    override fun onGet(obj: Any, key: String, value: Any?) {
        println("[GET]$obj[$key]=$value")
    }

    override fun onSet(obj: Any, key: String, newValue: Any?, oldValue: Any?) {
        println("[SET]$obj[$key]:$oldValue->$newValue")
    }

    override fun onClear(obj: Any) {
        println("[CLEAR]$obj.clear()")
    }

    override fun onRemove(obj: Any, key: String, value: Any?) {
        println("[REMOVE]$obj[$key]:$value->[null]")
    }
}