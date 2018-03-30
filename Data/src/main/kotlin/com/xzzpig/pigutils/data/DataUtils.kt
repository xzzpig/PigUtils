@file:JvmName("DataUtils")

package com.xzzpig.pigutils.data

import com.xzzpig.pigutils.annotation.BaseOnClass
import com.xzzpig.pigutils.annotation.NotNull
import com.xzzpig.pigutils.annotation.TestPass
import com.xzzpig.pigutils.core.*
import com.xzzpig.pigutils.reflect.allFields
import com.xzzpig.pigutils.reflect.reflect
import com.xzzpig.pigutils.reflect.utils
import java.lang.reflect.Field
import java.util.*
import java.util.Arrays.asList

/**
 * 数据工具类
 *
 * @author xzzpig
 */

/**
 * @param step 数组元素步长
 */
@BaseOnClass(YieldIterator::class)
fun range(start: Int, end: Int, step: Int): IntArray {
    val yieldIterator = YieldIterator<Int> {
        var i = start
        while (i < end) {

            YieldIterator.yield(i)
            i += step
        }
    }
    val `is` = IntArray(if ((end - start) % step == 0) (end - start) / step else (end - start) / step + 1)
    var i = 0
    while (yieldIterator.hasNext()) {
        `is`[i] = yieldIterator.next()
        i++
    }
    return `is`
}

/**
 * 将数组变为 [Map]
 *
 * @param kclazz K
 * @param vclazz V
 */
fun <K, V> array2KVMap(@NotNull kclazz: Class<K>, @NotNull vclazz: Class<V>,
                       @NotNull vararg objects: Any): Map<K, V> {
    return array2Map(*objects) as Map<K, V>
}

fun array2Map(@NotNull vararg objects: Any): Map<*, *> {
    return list2Map(Arrays.asList(*objects))
}

fun bytes2Bytes(bytes: ByteArray): Array<Byte> = bytes.toTypedArray()

fun bytes2Bytes(bytes: Array<Byte>): ByteArray = bytes.toByteArray()

/**
 * @return should reutrn
 */
fun <E> forEach(list: Array<E>, consumer: EachConsumer<E>): Boolean {
    return forEach(asList(*list), consumer)
}

/**
 * @return should reutrn
 */
fun <E> forEach(list: List<E>, consumer: EachConsumer<E>): Boolean {
    return list
            .map { consumer.consume(it) }
            .takeWhile { it != EachResult.BREAK }
            .contains(EachResult.RETURN)
}

fun <E> forEachWithIndex(list: Array<E>, consumer: WithIndexEachConsumer<E>): Boolean {
    return forEachWithIndex(asList(*list), consumer)
}

/**
 * @return should reutrn
 */
fun <E> forEachWithIndex(list: List<E>, consumer: WithIndexEachConsumer<E>): Boolean {
    return list
            .asSequence()
            .mapIndexed { i, e -> consumer.consume(e, i) }
            .takeWhile { it != EachResult.BREAK }
            .contains(EachResult.RETURN)
}

fun ints2Integers(ints: IntArray): Array<Int> = ints.toTypedArray()

fun <K, V> list2KVMap(@NotNull kclazz: Class<K>, @NotNull vclazz: Class<V>,
                      @NotNull list: List<Any>): Map<K, V> {
    return list2Map(list) as Map<K, V>
}

fun list2Map(list: List<Any>): Map<Any, Any?> {
    val map = HashMap<Any, Any?>()
    var key: Any? = null
    var value: Any
    for (i in list.indices) {
        if (i % 2 == 0) {
            key = list[i]
        } else
            if (key != null) {
                value = list[i]
                map[key] = value
            }
    }
    return map
}

/**
 * @return [0, end)数组
 */
fun range(end: Int): IntArray {
    return range(0, end)
}

/**
 * @return [start, end)数组
 */
fun range(start: Int, end: Int): IntArray {
    return range(start, end, 1)
}

enum class EachResult {
    /**
     * 同for循环中的break关键词
     */
    BREAK,
    /**
     * 同for循环中的continue关键词
     */
    CONTINUE,
    /**
     * 同for循环中的return关键词<br></br>
     * 不保证必定返回<br></br>
     */
    RETURN
}

interface EachConsumer<in E> {
    fun consume(e: E): EachResult
}

interface WithIndexEachConsumer<in E> {
    fun consume(e: E, index: Int): EachResult
}

open class DataObserver<in T, in K, in V> {
    open fun onSet(obj: T, key: K, newValue: V?, oldValue: V? = null) {}

    open fun onGet(obj: T, key: K, value: V?) {}

    open fun onRemove(obj: T, key: K, value: V?) {}

    open fun onClear(obj: T) {}
}

fun IData.observable(): ObservableData = ObservableData(this)

fun <K, V> MutableMap<K, V>.observable(): ObservableMap<K, V> = ObservableMap(this)

fun MutableMap<String, Any?>.toIData(): IData = MapData(this)

@TestPass
fun <T : Any> IData.injectTo(obj: T, transformManager: TransformManager = TransformManager.DefaultManager): T = obj.apply {
    //    val set = mutableSetOf<String>()
    val content = obj.reflect
    content.injector.transformManager = transformManager
    this@injectTo.entries.forEach {
        justTry {
            content[it.key] = it.value
        }
        justTry {
            if (content[it.key] == null && it.value != null && it.value is IData) {
                val clazz = this::class.java
                val field = clazz.utils.getField(it.key)
                if (field != null) {
                    content[it.key] = (it.value as IData).toBean(field.type)
                }
            }
        }
    }
//    for (method in clazz.methods) {
//        val methodName = method.name
//        if (!methodName.startsWith("set")) continue
//        val parameterTypes = method.parameterTypes
//        if (parameterTypes.size != 1) continue
//        val fieldName = String(methodName.substring(3).toCharArray().apply { this[0] = this[0].toLowerCase() })
//        var value: Any? = this@injectTo[fieldName] ?: continue
//        justTry {
//            //            if ((!parameterTypes[0].isInstance(value)))
////                value = transformManager.transform(clazz = parameterTypes[0], from = value)
////            if (value == null || (!parameterTypes[0].isInstance(value))) {
////                value = (this@injectTo[fieldName] as? IData)?.toBean(parameterTypes[0], transformManager)
////            }
////            if (value == null) return@justTry
////            method.isAccessible = true
////            method(this, value)
//            content[fieldName] = value
//            set.add(fieldName)
//        }
//    }
//    for (field in clazz.allFields) {
//        val fieldName = field.name
//        if (set.contains(fieldName)) continue
//        val fieldType = field.type
//        var value: Any? = this@injectTo[fieldName] ?: continue
//        justTry {
//            //            if ((!fieldType.isInstance(value)))
////                value = transformManager.transform(clazz = fieldType, from = value)
////            if (value == null || (!fieldType.isInstance(value))) {
////                value = (this@injectTo[fieldName] as? IData)?.toBean(fieldType, transformManager)
////            }
////            if (value == null) return@justTry
////            field.isAccessible = true
////            field[this] = value
//            content[fieldName] = value
//            set.add(fieldName)
//        }
//}
}

fun <T : Any> IData.toBean(creator: () -> T, transformManager: TransformManager = TransformManager.DefaultManager) = injectTo(creator(), transformManager)

fun <T : Any> IData.toBean(clazz: Class<T>, transformManager: TransformManager = TransformManager.DefaultManager): T = toBean(clazz::newInstance)

inline fun <reified T : Any> IData.toBean(transformManager: TransformManager = TransformManager.DefaultManager): T = toBean(T::class.java)

fun Any.injectTo(data: IData, useGetMethod: Boolean = true, useField: Boolean = true, useAllField: Boolean = false, keyFiller: (String) -> Boolean = { it != "class" }, classFilter: ((Class<*>) -> Class<*>?) = { null }, transformManager: TransformManager = TransformManager.DefaultManager): IData = data.apply {
    val clazz = this@injectTo::class.java
    val set = mutableSetOf<String>()
    for (method in clazz.methods) {
        val methodName = method.name
        if (!methodName.startsWith("get")) continue
        val parameterTypes = method.parameterTypes
        if (parameterTypes.isNotEmpty()) continue
        if (method.returnType == Void.TYPE) continue
        val fieldName = String(methodName.substring(3).toCharArray().apply { this[0] = this[0].toLowerCase() })
        if (!keyFiller(fieldName)) continue
        justTry {
            method.isAccessible = true
            var value = method(this@injectTo) ?: return@justTry
            val targetClass = classFilter(method.returnType)
            if (targetClass != null && targetClass != value.javaClass && !targetClass.isAssignableFrom(value.javaClass)) {
                value = if (targetClass == IData::class.java) {
                    MapData().apply { value.injectTo(this, useGetMethod, useField, useAllField, keyFiller, classFilter, transformManager) }
                } else {
                    transformManager.transform(targetClass, value) ?: return@justTry
                }
            }
            this[fieldName] = value
            set.add(fieldName)
        }
    }
    if (!useField) return@apply
    val fields: Array<Field> = if (useAllField) clazz.allFields.toTypedArray() else clazz.fields
    for (field in fields) {
        val fieldName = field.name
        if (set.contains(fieldName)) continue
        if (!keyFiller(fieldName)) continue
        justTry {
            field.isAccessible = true
            var value = field[this@injectTo] ?: return@justTry
            val targetClass = classFilter(field.type)
            if (targetClass != null && targetClass != value.javaClass && !targetClass.isAssignableFrom(value.javaClass)) {
                value = if (targetClass == IData::class.java) {
                    MapData().apply { value.injectTo(this, useGetMethod, useField, useAllField, keyFiller, classFilter, transformManager) }
                } else {
                    transformManager.transform(targetClass, value) ?: return@justTry
                }
            }
            this[fieldName] = value
            set.add(fieldName)
        }
    }
}