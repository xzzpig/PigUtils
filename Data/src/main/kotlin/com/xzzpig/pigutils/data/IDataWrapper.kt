package com.xzzpig.pigutils.data

import com.xzzpig.pigutils.core.IData

open class IDataWrapper(protected val data: IData) : IData {
    override val entries: Set<Map.Entry<String, Any?>>
        get() = data.entries

    override fun clear() {
        data.clear()
    }

    override fun <T> get(key: String, clazz: Class<T>): T? = data[key, clazz]

    override fun keySet(): Set<String> = data.keySet()

    override fun remove(key: String): Any? = data.remove(key)

    override fun set(key: String, value: Any?): IDataWrapper = this.apply { data[key] = value }

    override fun size(): Int = data.size()

    override fun values(): Collection<Any> = data.values()
}