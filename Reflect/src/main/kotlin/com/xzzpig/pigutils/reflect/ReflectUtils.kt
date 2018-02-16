@file:JvmName("ReflectUtils")

package com.xzzpig.pigutils.reflect

import com.xzzpig.pigutils.core.flatToString
import com.xzzpig.pigutils.core.justTry
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.*

val Class<*>.allMethods: Set<Method>
    get() = HashSet<Method>().apply { addAll(this@allMethods.declaredMethods) }.apply { addAll(this@allMethods.methods) }

val Class<*>.allFields: Set<Field>
    get() = HashSet<Field>().apply { addAll(this@allFields.declaredFields) }.apply { addAll(this@allFields.fields) }

val Class<*>.allConstructor: Set<Constructor<*>>
    get() = HashSet<Constructor<*>>().apply { addAll(this@allConstructor.declaredConstructors) }.apply { addAll(this@allConstructor.constructors) }

val <T> Class<T>.utils: ClassUtils<T>
    get() = ClassUtils<T>(this)

@Throws(NoSuchMethodException::class)
fun <T> Class<T>.newInstance(vararg params: Any?): T {
    return utils.newInstance(*params) ?: throw NoSuchMethodException("$name.<init>(${getClassesString(*params)})")

}

val Method.utils: MethodUtils
    get() = MethodUtils(this)

val Field.utils: FieldUtils
    get() = FieldUtils(this)

@Throws(NoSuchMethodException::class)
operator fun <T> Class<T>.invoke(vararg params: Any?): T = newInstance(*params)

val Any.reflect: ObjectContent get() = ObjectContent(this)

data class ObjectContent(val obj: Any) {
    operator fun invoke(name: String, vararg args: Any?): Any? {
        return (obj::class.java.utils.getMethod(name, *args)
                ?: throw NoSuchMethodException("${obj::class.java.name}.$name(${getClassesString(*args)})")
                ).invoke(obj, *args)
    }

    operator fun set(key: String, value: Any?) {
        val field = obj::class.java.utils.getField(key)
        if (field != null) {
            field.isAccessible = true
            field[obj] = value
            return
        }

        val methodName = "set" + String(key.toCharArray().apply { this[0] = this[0].toUpperCase() })

        obj::class.java.methods
                .filterNot { it.name != methodName && it.parameterCount == 1 }
                .forEach {
                    justTry {
                        it.isAccessible = true
                        it(obj, value)
                        return
                    }
                }
        throw NoSuchFieldException("${obj::class.java.name}.$key")
    }

    operator fun get(key: String): Any? {
        val field = obj::class.java.utils.getField(key)
        if (field != null) {
            field.isAccessible = true
            return field[obj]
        }

        val methodName = "get" + String(key.toCharArray().apply { this[0] = this[0].toUpperCase() })

        obj::class.java.methods
                .filterNot { it.name != methodName && it.parameterCount == 0 }
                .forEach {
                    justTry {
                        it.isAccessible = true
                        return it(obj)
                    }
                }
        throw NoSuchFieldException("${obj::class.java.name}.$key")
    }
}

private fun getClassesString(vararg args: Any?) = args.map {
    it?.javaClass ?: java.lang.Object::class.java
}.asIterable().flatToString("", "")