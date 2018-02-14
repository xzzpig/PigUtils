package com.xzzpig.pigutils.data


class ObservableMap<K, V>(private val data: MutableMap<K, V>) : MutableMap<K, V> {
    private val observers = mutableListOf<DataObserver<ObservableMap<K, V>, K, V>>()

    fun addObserver(observer: DataObserver<ObservableMap<K, V>, K, V>) = observers.add(observer)


    fun removeObserver(observer: DataObserver<ObservableMap<K, V>, K, V>): Boolean = observers.remove(observer)


    override val size: Int
        get() = data.size

    /**
     * Returns `true` if the map contains the specified [key].
     */
    override fun containsKey(key: K): Boolean = data.containsKey(key)

    /**
     * Returns `true` if the map maps one or more keys to the specified [value].
     */
    override fun containsValue(value: V): Boolean = data.containsValue(value)

    /**
     * Returns the value corresponding to the given [key], or `null` if such a key is not present in the map.
     */
    override fun get(key: K): V? = data[key].apply {
        observers.forEach { it.onGet(this@ObservableMap, key, this) }
    }

    /**
     * Returns `true` if the map is empty (contains no elements), `false` otherwise.
     */
    override fun isEmpty(): Boolean = data.isEmpty()

    /**
     * Returns a [MutableSet] of all key/value pairs in this map.
     */
    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() = data.entries
    /**
     * Returns a [MutableSet] of all keys in this map.
     */
    override val keys: MutableSet<K>
        get() = data.keys
    /**
     * Returns a [MutableCollection] of all values in this map. Note that this collection may contain duplicate values.
     */
    override val values: MutableCollection<V>
        get() = data.values

    /**
     * Removes all elements from this map.
     */
    override fun clear() {
        observers.forEach { it.onClear(this) }
        entries.filter { (_, v) -> v is Observable }.forEach { (k, v) -> (v as? Observable)?.onUnbind(this, k!!) }
        data.clear()
    }

    /**
     * Associates the specified [value] with the specified [key] in the map.
     *
     * @return the previous value associated with the key, or `null` if the key was not present in the map.
     */
    override fun put(key: K, value: V): V? =
            data.put(key, value).apply {
                observers.forEach { it.onSet(this@ObservableMap, key, value, this) }
                (this as? Observable)?.onUnbind(this@ObservableMap, key!!)
                (value as? Observable)?.onBind(this@ObservableMap, key!!)
            }

    /**
     * Updates this map with key/value pairs from the specified map [from].
     */
    override fun putAll(from: Map<out K, V>) = data.putAll(from)

    /**
     * Removes the specified key and its corresponding value from this map.
     *
     * @return the previous value associated with the key, or `null` if the key was not present in the map.
     */
    override fun remove(key: K): V? =
            data.remove(key).apply {
                (this as? Observable)?.onUnbind(this@ObservableMap, key!!)
                observers.forEach { it.onRemove(this@ObservableMap, key, this) }
            }
}