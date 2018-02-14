package com.xzzpig.pigutils.data

import com.xzzpig.pigutils.core.IData

class MultiData(private val creator: () -> IData) : IDataWrapper(creator()) {

    private val subDataMap: MutableMap<String, MultiData> by lazy { mutableMapOf<String, MultiData>() }

    fun getData(key: String): MultiData {
        val keys = key.split("\\.".toRegex(), 2)
        return if (keys.size == 1) subDataMap.getOrPut(key) { MultiData(creator) }
        else subDataMap.getOrPut(keys[0]) { MultiData(creator) }.getData(keys[1])
    }

    fun setData(key: String, value: MultiData): MultiData = this.apply {
        val keys = key.split("\\.".toRegex(), 2)
        if (keys.size == 1) subDataMap[key] = value
        else subDataMap.getOrPut(keys[0]) { MultiData(creator) }.setData(keys[1], value)
    }

    override fun <T> get(key: String, clazz: Class<T>): T? {
        val keys = key.split("\\.".toRegex(), 2)
        return if (keys.size == 1)
            super.get(key, clazz)
        else
            subDataMap.getOrPut(keys[0]) { MultiData(creator) }[keys[1], clazz]

    }

    override fun set(key: String, value: Any?): MultiData = this.apply {
        val keys = key.split("\\.".toRegex(), 2)
        if (keys.size == 1)
            super.set(key, value)
        else
            subDataMap.getOrPut(keys[0]) { MultiData(creator) }[keys[1]] = value
    }

}