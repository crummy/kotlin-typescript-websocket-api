package com.malcolmcrum.typescriptwebsocketapi.typescriptgenerator

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.WildcardType

object TypeScriptConverter {
    private val overrides: MutableMap<Class<*>?, String> = HashMap()

    init {
        overrides[String::class.java] = "string"
        overrides[Int::class.javaPrimitiveType] = "number"
        overrides[Int::class.java] = "number"
        overrides[Float::class.javaPrimitiveType] = "number"
        overrides[Float::class.java] = "number"
        overrides[Double::class.javaPrimitiveType] = "number"
        overrides[Double::class.java] = "number"
        overrides[Long::class.javaPrimitiveType] = "number"
        overrides[Long::class.java] = "number"
        overrides[Any::class.java] = "any"
    }

    fun getTsType(type: Type): String {
        if (type is Class<*>) {
            return getTsTypeFrom(type)
        }
        if (type is ParameterizedType) {
            val typeName = StringBuilder(getTsType(type.rawType))
            val specificTypes = type.actualTypeArguments
            if (null != specificTypes && specificTypes.isNotEmpty()) {
                typeName.append("<")
                for (j in specificTypes.indices) {
                    if (j > 0) {
                        typeName.append(",")
                    }
                    typeName.append(getTsType(specificTypes[j]))
                }
                typeName.append(">")
            }
            return typeName.toString()
        }

        // or it could be a Something<?>
        if (type is WildcardType) {
            return "any"
        }
        throw RuntimeException("Couldn't figure out type for $type")
    }

    private fun getTsTypeFrom(clazz: Class<*>?): String {
        if (clazz == null) {
            return ""
        }
        val override = overrides[clazz]
        if (override != null) {
            return override
        }
        if (clazz.isPrimitive) {
            return clazz.simpleName
        }
        if (clazz.name == "java.lang.Void") {
            return "void"
        }
        if (clazz.isArray) {
            return "Array<" + getTsTypeFrom(clazz.componentType) + ">"
        }
        if (MutableCollection::class.java.isAssignableFrom(clazz)) {
            return "Array" + getTsTypeFrom(clazz.componentType)
        } else if (MutableMap::class.java.isAssignableFrom(clazz)) {
            return "Record" + getTsTypeFrom(clazz.componentType)
        }
        return clazz.simpleName
    }
}