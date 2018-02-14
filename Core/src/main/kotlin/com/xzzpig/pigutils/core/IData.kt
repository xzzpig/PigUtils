package com.xzzpig.pigutils.core

import java.io.InputStream
import java.io.OutputStream
import kotlin.collections.Map.Entry

interface IData {

    val entries: Set<Entry<String, Any?>>

    val keys: Set<String>
        get() = keySet()

    val values: Collection<Any>
        get() = values()

    fun clear()

    operator fun get(key: String): Any? {
        return get(key, Any::class.java)
    }

    operator fun <T> get(key: String, clazz: Class<T>): T?

    fun <T> get(key: String, clazz: Class<T>, defaultValue: T?): T? {
        val t = get(key, clazz)
        return t ?: defaultValue
    }

    fun <T> getOrSet(key: String, clazz: Class<T>, defaultValue: T?): T? {
        var t = get(key, clazz)
        if (t == null) {
            t = defaultValue
            set(key, t)
        }
        return t
    }

    fun getBoolean(key: String): Boolean {
        return get(key, Boolean::class.java)!!
    }

    fun getDouble(key: String): Double {
        return get(key, Double::class.java)!!
    }

    fun getInt(key: String): Int {
        return get(key, Int::class.java)!!
    }

    fun getLong(key: String): Long {
        return get(key, Long::class.java)!!
    }

    fun getString(key: String): String? {
        return get(key, String::class.java)
    }

    fun keySet(): Set<String>

    fun load(`in`: InputStream): IData {
        throw UnsupportedOperationException()
    }

    fun remove(key: String): Any?

    fun save(out: OutputStream): IData {
        throw UnsupportedOperationException()
    }

    operator fun set(key: String, value: Any?): IData

    fun put(key: String, value: Any?): Any? = this[key].apply {
        this@IData[key] = value
    }

    fun size(): Int

    fun values(): Collection<Any>
}
