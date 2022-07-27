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

fun <T> Class<T>.getFieldPath(fieldName: String, snakeCase: Boolean = false): String {
    val name = if (snakeCase) fieldName.snakeCase() else fieldName
    val fields = this.getAllFields()
        .associateBy { if (snakeCase) it.name.snakeCase() else it.name }

    return if (fields.containsKey(name)) name else throw NotFoundFieldException("Not found field: '$name' at [${this.simpleName}]")
}

fun <T> Class<T>.getAllFields(): MutableList<Field> {
    val fields: MutableList<Field> = this.declaredFields.toMutableList().filter { !it.isSynthetic }.toMutableList()

    if (this.superclass != null) {
        val superClassFields = this.superclass.getAllFields()
        fields.addAll(superClassFields)
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
        PropertyType.NUMBER -> this.toDouble()
        PropertyType.BOOLEAN -> this.toBoolean()
        PropertyType.DATE -> this.toLong()
        PropertyType.OBJECT_ID -> try {
            this.toLong().toObjectId()
        } catch (_: NumberFormatException) {
            ObjectId(this)
        }
        PropertyType.CURRENCY -> this.toDouble()
        else -> this
    }
}

fun Long.toObjectId(): ObjectId = ObjectId(floor((this / 1000).toDouble()).toLong().toString(16) + "0000000000000000")

fun String.escapeSpecialRegexChars(): String =
    Pattern.compile("[{}()\\[\\].,+*?^$#\\\\|-]").matcher(this).replaceAll("\\\\$0")
