@file:JvmName("ReflectUtils")

package com.xzzpig.pigutils.reflect

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

fun <T> Class<T>.newInstance(vararg args: Any?): T {
    return utils.newInstance(args)
}

val Method.utils: MethodUtils
    get() = MethodUtils(this)

val Field.utils: FieldUtils
    get() = FieldUtils(this)