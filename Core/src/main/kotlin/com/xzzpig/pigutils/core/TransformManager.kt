package com.xzzpig.pigutils.core

import java.math.BigDecimal
import java.math.BigInteger
import java.nio.charset.Charset
import java.util.*
import java.util.function.Consumer

class TransformManager {

    companion object {
        @JvmStatic
        val DefaultManager: TransformManager by lazy {
            TransformManager().apply {
                addSubManager(StringManager)
                addSubManager(NumberManager)
            }.apply {
                        addTransformer(Array<*>::toMutableList)
                        addTransformer(Array<*>::toHashSet)
                        addTransformer(List<*>::toHashSet)
                        addTransformer(List<*>::toTypedArray)
                        addTransformer(Set<*>::toTypedArray)
                        addTransformer(Set<*>::toMutableList)

                        addTransformer(IData::toProperties)
                        addTransformer<Properties, IData> { it.toIData() }
                    }
        }

        @JvmStatic
        val StringManager: TransformManager by lazy {
            TransformManager().apply {
                addTransformer(Any::toString)

                addTransformer(String::toInt)
                addTransformer(String::toBigDecimal)
                addTransformer(String::toBigInteger)
                addTransformer(String::toBoolean)
                addTransformer(String::toByte)
                addTransformer(StringByteArrayTransformer)
                addTransformer(String::toCharArray)
                addTransformer(String::toDouble)
                addTransformer(String::toFloat)
                addTransformer(String::toLong)
                addTransformer(String::toShort)
                addTransformer(String::toRegex)
                addTransformer(String::toFile)
            }
        }

        @JvmStatic
        val NumberManager: TransformManager by lazy {
            TransformManager().apply {
                addTransformer(Any::toString)

                addTransformer(Number::toInt)
                addTransformer(Number::toChar)
                addTransformer(Number::toLong)
                addTransformer(Number::toShort)
                addTransformer(Number::toByte)
                addTransformer(Number::toDouble)
                addTransformer(Number::toFloat)

                addTransformer(Int::toBigDecimal)
                addTransformer(Int::toBigInteger)

                addTransformer(Long::toBigDecimal)
                addTransformer(Long::toBigInteger)

                addTransformer(Double::toBigDecimal)

                addTransformer(Float::toBigDecimal)

                addTransformer(BigInteger::toBigDecimal)

                addTransformer(BigDecimal::toBigInteger)
            }
        }

        @JvmStatic
        fun <F, R> wrapTransformer(transformer: Transformer<F, R>, useFor: String? = null, mark: String? = null,
                                   errorConsumer: Consumer<Exception>? = null): Transformer<F, R> {
            return object : Transformer<F, R> {
                override fun transform(f: F, extras: Map<Any, Any>?, targetClass: Class<*>): R {
                    return transformer.transform(f, extras, targetClass)
                }

                override fun mark(): String? {
                    return mark
                }

                override fun onError(error: Exception) {
                    errorConsumer?.accept(error)
                }

                override fun useFor(): String {
                    return useFor ?: "Default"
                }
            }
        }

        object StringByteArrayTransformer : Transformer<String, ByteArray> {
            override fun transform(f: String, extras: Map<Any, Any>?, targetClass: Class<*>): ByteArray {
                val charset: Charset = extras?.get("charset") as? Charset ?: Charsets.UTF_8
                return f.toByteArray(charset)
            }
        }
    }

    val transformers: MutableList<Transformer<*, *>> = LinkedList()

    private val preSubManager: MutableList<TransformManager> = LinkedList()
    private val afterSubManager: MutableList<TransformManager> = LinkedList()

    fun <F, R> addTransformer(transformer: Transformer<F, R>) {
        transformers.add(transformer)
    }

    fun addSubManager(manager: TransformManager, pre: Boolean = true) {
        if (pre) preSubManager.add(manager)
        else afterSubManager.add(manager)
    }

    fun removeSubManager(manager: TransformManager) {
        preSubManager.remove(manager)
        afterSubManager.remove(manager)
    }

    fun removeSubManager(block: (TransformManager) -> Boolean) {
        preSubManager.removeIf(block)
        afterSubManager.removeIf(block)
    }

    fun <F, R> addTransformer(simpleTransformer: (F) -> R) {
        this.addTransformer(object : TransformManager.SimpleTransformer<F, R> {
            override fun transform(f: F): R = simpleTransformer(f)
        })
    }

    fun <F, R> addTransformer(transformer: Transformer<F, R>, useFor: String? = null, mark: String? = null,
                              errorConsumer: Consumer<Exception>? = null) {
        transformers.add(wrapTransformer(transformer, useFor, mark, errorConsumer))
    }

    fun <F, R> transform(usedManager: MutableSet<TransformManager>, clazz: Class<R>, from: F, useFor: String? = null, extras: Map<Any, Any>? = null): R? {
        var r: R? = null
        for (manager in preSubManager) {
            if (usedManager.contains(manager)) continue
            r = manager.transform(usedManager, clazz, from, useFor, extras)
            usedManager.add(manager)
            if (r != null) return r
        }
        if (!usedManager.contains(this))
            for (transformer in transformers) {
                if (!transformer.accept(from!!))
                    continue
                if (useFor != null && !useFor.equals(transformer.useFor(), ignoreCase = true))
                    continue
                try {
                    @Suppress("UNCHECKED_CAST")
                    r = (transformer as? Transformer<F, R>)?.transform(from, extras, clazz)
                } catch (e: Exception) {
                    transformer.onError(e)
                }

                if (!clazz.isInstance(r))
                    r = null
                if (r != null)
                    return r
            }
        for (manager in afterSubManager) {
            if (usedManager.contains(manager)) continue
            r = manager.transform(usedManager, clazz, from, useFor, extras)
            usedManager.add(this)
            if (r != null) return r
        }
        return null
    }

    fun <F, R> transform(clazz: Class<R>, from: F, useFor: String? = null, extras: Map<Any, Any>? = null): R? = transform(mutableSetOf(), clazz, from, useFor, extras)

    inline fun <F, reified R> transform(from: F, useFor: String? = null, extras: Map<Any, Any>? = null): R? =
            this.transform(R::class.java, from, useFor, extras)

    @FunctionalInterface
    interface SimpleTransformer<in F, out R> : Transformer<F, R> {
        fun transform(f: F): R

        override fun transform(f: F, extras: Map<Any, Any>?, targetClass: Class<*>): R {
            return this.transform(f)
        }

    }

    @FunctionalInterface
    interface Transformer<in F, out R> {
        fun accept(o: Any): Boolean {
            return try {
                @Suppress("UNCHECKED_CAST")
                o as F
                true
            } catch (e: Exception) {
                false
            }

        }

        fun mark(): String? = null

        fun onError(error: Exception) {}

        fun transform(f: F, extras: Map<Any, Any>? = null, targetClass: Class<*>): R

        fun useFor(): String = "Default"
    }
}
