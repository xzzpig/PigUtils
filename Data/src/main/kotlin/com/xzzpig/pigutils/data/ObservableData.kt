package com.xzzpig.pigutils.data

import com.xzzpig.pigutils.core.IData


class ObservableData(private val data: IData) : IData {
    override val entries: Set<Map.Entry<String, Any?>>
        get() = data.entries

    private val observers = mutableListOf<DataObserver<IData, String, Any?>>()

    fun addObserver(observer: DataObserver<IData, String, Any?>) = observers.add(observer)


    fun removeObserver(observer: DataObserver<IData, String, Any?>): Boolean = observers.remove(observer)

    override fun clear() {
        observers.forEach { it.onClear(this) }
        entries.filter { (_, v) -> v is Observable }.forEach { (k, v) -> (v as? Observable)?.onUnbind(this, k!!) }
        data.clear()
    }

    override fun <T : Any?> get(key: String, clazz: Class<T>): T? =
            data.get(key, clazz).apply {
                observers.forEach { it.onGet(this@ObservableData, key, this) }
            }

    override fun keySet(): Set<String> = data.keySet()

    override fun remove(key: String): Any? =
            data.remove(key).apply {
                (this as? Observable)?.onUnbind(this@ObservableData, key!!)
                observers.forEach { it.onRemove(this@ObservableData, key, this) }

            }

    override fun set(key: String, value: Any?): IData = this.apply {
        val oldValue = data[key]
        data[key] = value
        observers.forEach { it.onSet(this, key, value, oldValue) }
        (oldValue as? Observable)?.onUnbind(this@ObservableData, key)
        (value as? Observable)?.onBind(this@ObservableData, key)
    }

    override fun size(): Int = data.size()

    override fun values(): Collection<Any> = data.values()

}