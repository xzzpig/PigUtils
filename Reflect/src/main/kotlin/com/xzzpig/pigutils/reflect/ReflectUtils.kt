@file:JvmName("ReflectUtils")

package com.xzzpig.pigutils.reflect

import com.xzzpig.pigutils.annotation.TestPass
import com.xzzpig.pigutils.core.TransformManager
import com.xzzpig.pigutils.core.flatToString
import com.xzzpig.pigutils.core.justTry
import com.xzzpig.pigutils.core.testTry
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

class ObjectContentInjector(val obj: Any) {
    var useSetMethod: Boolean = true
    var useField: Boolean = true
    var transformManager: TransformManager = TransformManager.DefaultManager

    @TestPass
    operator fun set(key: String, value: Any?) {
        val classUtils = obj::class.java.utils
        if (useSetMethod) {
            val method = classUtils.getMethod("get" + String(key.toCharArray().apply { this[0] = this[0].toUpperCase() }))
            if (method != null && method.parameterCount == 1) {
                val targetType: Class<*> = method.parameters[0].type
                if (targetType.isRawType) {
                    justTry {
                        if (value != null) {
                            if (value::class.java.isRawType) {
                                method.isAccessible = true
                                method(obj, value)
                                return
                            } else {
                                method.isAccessible = true
                                method(obj, value.reflect["value"])
                                return
                            }
                        }
                    }
                }
                var v = value
                if ((!targetType.isInstance(value)))
                    v = transformManager.transform(clazz = targetType, from = value)
                if (v != null) {
                    method.isAccessible = true
                    method(obj, v)
                    return
                }
            }
        }
        if (useField) {
            val field = classUtils.getField(key)
            if (field != null) {
                val targetType = field.type
                if (targetType.isRawType) {
                    justTry {
                        if (value != null) {
                            if (value::class.java.isRawType) {
                                field.isAccessible = true
                                field[obj] = value
                                return
                            } else {
                                field.isAccessible = true
                                field[obj] = value.reflect["value"]
                                return
                            }
                        }
                    }
                }
                var v = value
                if ((!targetType.isInstance(value)) && v != null)
                    v = transformManager.transform(clazz = targetType, from = value)
                field.isAccessible = true
                field[obj] = v
                return
            }
        }
        throw NoSuchFieldException("${obj::class.java.name}.$key")
    }
}

val Class<*>.isWarpType: Boolean
    get() = this.utils.isWarpClass


val Class<*>.isRawType: Boolean
    get() = this.utils.isRawClass

data class ObjectContent(val obj: Any) {

    var injector = ObjectContentInjector(obj)

    operator fun invoke(name: String, vararg args: Any?): Any? {
        return (obj::class.java.utils.getMethod(name, *args)
                ?: throw NoSuchMethodException("${obj::class.java.name}.$name(${getClassesString(*args)})")
                ).invoke(obj, *args)
    }

    @TestPass
    operator fun set(key: String, value: Any?) {
        injector[key] = value
//        val field = obj::class.java.utils.getField(key)
//        if (field != null) {
//            field.isAccessible = true
//            field[obj] = value
//            return
//        }
//
//        val methodName = "set" + String(key.toCharArray().apply { this[0] = this[0].toUpperCase() })
//
//        obj::class.java.methods
//                .filterNot { it.name != methodName && it.parameterCount == 1 }
//                .forEach {
//                    justTry {
//                        it.isAccessible = true
//                        it(obj, value)
//                        return
//                    }
//                }
//        throw NoSuchFieldException("${obj::class.java.name}.$key")
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
                    testTry {
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