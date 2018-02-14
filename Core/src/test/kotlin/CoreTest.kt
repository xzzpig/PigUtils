package com.xzzpig.pigutils.core

import org.junit.Test
import java.math.BigDecimal

class CoreTest {
    @Test
    fun testLaterMap() {
        println("aaaaa".later {
            println(this)
        }.map { this * 3 * 2 })
    }

    @Test
    fun testFilter() {
        val list = 1..10
        list.forEach({ i: Int -> println(i) }.filter { it % 2 == 0 })
    }

    @Test
    fun testConsumer() {
        val method: (String) -> Unit = ::println
        val consumer = method.toConsumer()
        consumer.accept("aaa")
        consumer.toMethod()("bbb")
    }

    @Test
    fun testSimpleTransformer() {
        TransformManager.DefaultManager.addTransformer<String, Int> { it.toInt() }
        println("123".to<String, Int>())
    }

    @Test
    fun testDefaultTransformer() {
        val int: Int = "123".to()
        println(int::class)
    }

    @Test
    fun testNumberTransformer() {
        val i: Int = 1
        println(i.to<Int, BigDecimal>())
    }

    @Test
    fun testFlatToString() {
        println(listOf("aaa", "bbb", "ccc").flatToString(split = "|"))
    }
}