@file:JvmName("CoreUtils")

package com.xzzpig.pigutils.core

import java.io.Closeable
import java.io.File
import java.lang.ref.WeakReference
import java.nio.charset.Charset
import java.util.*
import java.util.function.Consumer
import java.util.stream.Stream
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

fun Closeable.with(block: Closeable.() -> Unit) {
    block.invoke(this)
    this.close()
}

class LaterBlock<T>(val later: T.() -> Unit, val it: T) {
    inline fun <R> map(block: T.() -> R): R {
        val result: R
        try {
            result = block(it)
        } catch (e: Exception) {
            throw e
        } finally {
            later(it)
        }
        return result
    }
}

fun <T> T.later(block: T.() -> Unit): LaterBlock<T> = LaterBlock(block, this)


operator fun String.times(right: Int): String {
    if (right == 0) return ""
    val builder = StringBuffer(this)
    for (i in 1 until right) {
        builder.append(this)
    }
    return builder.toString()
}

fun <T> ((T) -> Unit).filter(f: (T) -> Boolean): (T) -> Unit {
    return { t ->
        run {
            if (f(t))
                this(t)
        }
    }
}

fun <T> ((T) -> Unit).toConsumer(): Consumer<T> {
    return Consumer { t -> this@toConsumer(t) }
}

fun <T> Consumer<T>.toMethod(): (T) -> Unit {
    return { t -> this.accept(t) }
}

inline fun <F, reified R> F.to(useFor: String? = null, extras: Map<Any, Any>? = null, transformManager: TransformManager = TransformManager.DefaultManager): R =
        to(R::class.java, useFor, extras, transformManager)

fun <F, R> F.to(clazz: Class<R>, useFor: String? = null, extras: Map<Any, Any>? = null, transformManager: TransformManager = TransformManager.DefaultManager): R {
    return transformManager.transform(clazz, this, useFor, extras)
            ?: throw NullPointerException("$this can not be cast to $clazz")
}

fun <T> Array<T>.stream(startInclusive: Int = 0, endExclusive: Int = this.size): Stream<T> {
    return Arrays.stream(this, startInclusive, endExclusive)
}

inline fun justTry(printStackTrace: Boolean = false, block: () -> Unit) {
    try {
        block()
    } catch (e: Exception) {
        if (printStackTrace) e.printStackTrace()
    }
}

inline operator fun <reified T> IData.get(key: String, defaultValue: T? = null): T? = get(key, T::class.java, defaultValue)

inline fun <reified T> IData.getOrSet(key: String, noinline block: () -> (T?)): T? = getOrSet(key, T::class.java, block)

fun String.recoding(source: Charset = Charsets.ISO_8859_1, target: Charset = Charsets.UTF_8): String = toByteArray(source).toString(target)

fun String.toFile(): File = File(this)

fun IData.toProperties(): Properties = Properties().apply {
    this@toProperties.entries.forEach({ (k, v) -> this.setProperty(k, v.toString()) })
}

fun Properties.toIData(creator: () -> IData = { MapData() }) = creator().apply {
    this@toIData.stringPropertyNames().forEach { this[it] = this@toIData.getProperty(it) }
}

fun <F, R> TransformManager.Transformer<F, R>.wrap(useFor: String? = null, mark: String? = null, errorConsumer: Consumer<Exception>? = null) = TransformManager.wrapTransformer(this, useFor, mark, errorConsumer)

fun Iterable<*>.flatToString(start: String = "[", end: String = "]", split: String = ","): String {
    val sb = StringBuffer(start)
    for ((index, it) in this.withIndex()) {
        if (index != 0)
            sb.append(split)
        sb.append(it)
    }
    sb.append(end)
    return sb.toString()
}

fun <T> weakRef(block: () -> T): WeakRef<Any?, T> = WeakRef(block)

class WeakRef<in R, out T>(private val block: () -> T) : ReadOnlyProperty<R, T> {

    private var ref: WeakReference<T>? = null

    /**
     * Returns the value of the property for the given object.
     * @param thisRef the object for which the value is requested.
     * @param property the metadata for the property.
     * @return the property value.
     */
    override fun getValue(thisRef: R, property: KProperty<*>): T {
        return ref?.get() ?: block().apply {
            ref?.clear()
            ref = WeakReference(this)
        }
    }


}