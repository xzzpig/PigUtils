package com.xzzpig.pigutils.core

open class MapData(protected val map: MutableMap<String, Any?>) : IData {

    constructor() : this(mutableMapOf())

    override val entries: Set<Map.Entry<String, Any?>>
        get() = map.entries


    override fun clear() {
        map.clear()
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> get(key: String, clazz: Class<T>): T? {
        return map[key] as? T
    }

    override fun keySet(): Set<String> {
        return map.keys
    }

    override fun remove(key: String): Any? {
        return map.remove(key)
    }

    override fun set(key: String, value: Any?): MapData {
        map[key] = value
        return this
    }

    override fun size(): Int {
        return map.size
    }

    override fun values(): Collection<Any> {
        return map.values.filterNotNull()
    }

    override fun put(key: String, value: Any?): Any? {
        return map.put(key, value)
    }

    override fun toString(): String {
        return map.toString()
    }
}
