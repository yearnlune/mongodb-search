package io.github.yearnlune.search.core.extension

import io.github.yearnlune.search.core.exception.NotFoundFieldException
import io.github.yearnlune.search.graphql.PropertyType
import org.bson.types.ObjectId
import java.lang.reflect.Field
import java.util.regex.Pattern
import kotlin.math.floor

fun String.snakeCase(): String {
    return "(?<=[a-zA-Z])[A-Z]".toRegex().replace(this) {
        "_${it.value}"
    }.lowercase()
}

fun <T> Class<T>.getFieldPath(fieldName: String): String {
    val fields = this.getAllFields()
        .filter { !it.isSynthetic }
        .associateBy { it.name.snakeCase() }

    return if (fields.containsKey(fieldName)) fieldName else throw NotFoundFieldException("Not found field: '$fieldName' at [${this.javaClass.simpleName}]")
}

fun <T> Class<T>.getAllFields(): MutableList<Field> {
    val fields: MutableList<Field> = this.declaredFields.toMutableList()

    if (this.superclass != null) {
        val list = this.superclass.getAllFields()
        fields.addAll(list)
    }

    return fields
}

@kotlin.jvm.Throws(NumberFormatException::class, IllegalArgumentException::class)
fun String.toMongoType(type: PropertyType): Any {
    return when (type) {
        PropertyType.INTEGER -> this.toLong()
        PropertyType.LONG -> this.toLong()
        PropertyType.FLOAT -> this.toFloat()
        PropertyType.DOUBLE -> this.toDouble()
        PropertyType.BOOLEAN -> this.toBoolean()
        PropertyType.DATE -> this.toLong()
        PropertyType.OBJECT_ID -> try {
            this.toLong().toObjectId()
        } catch (_: NumberFormatException) {
            ObjectId(this)
        }
        else -> this
    }
}

fun Long.toObjectId(): ObjectId = ObjectId(floor((this / 1000).toDouble()).toLong().toString(16) + "0000000000000000")

fun String.escapeSpecialRegexChars(): String {
    return Pattern.compile("[{}()\\[\\].,+*?^$#\\\\|-]").matcher(this).replaceAll("\\\\$0")
}