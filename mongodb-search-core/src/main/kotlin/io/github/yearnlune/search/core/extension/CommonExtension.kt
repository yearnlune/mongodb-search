package io.github.yearnlune.search.core.extension

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.yearnlune.search.core.exception.NotSupportedOperatorException
import io.github.yearnlune.search.graphql.CountAggregationInput
import io.github.yearnlune.search.graphql.GroupAggregationInput
import io.github.yearnlune.search.graphql.LimitAggregationInput
import io.github.yearnlune.search.graphql.PropertyType
import io.github.yearnlune.search.graphql.SortAggregationInput
import org.bson.types.ObjectId
import java.util.regex.Pattern
import kotlin.math.floor

fun String.snakeCase(): String {
    return "(?<=[a-zA-Z])[A-Z]".toRegex().replace(this) {
        "_${it.value}"
    }.lowercase()
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

fun Map<*, *>.toAggregationInput(): Any {
    val aggregations = listOf(
        GroupAggregationInput::class.java,
        CountAggregationInput::class.java,
        LimitAggregationInput::class.java,
        SortAggregationInput::class.java
    )
    val objectMapper = jacksonObjectMapper()
    var aggregation: Any? = null

    aggregations.forEach {
        try {
            aggregation = objectMapper.convertValue(this, it)
        } catch (_: Exception) {
        }
    }

    return aggregation ?: throw NotSupportedOperatorException("Not supported operator: $this")
}