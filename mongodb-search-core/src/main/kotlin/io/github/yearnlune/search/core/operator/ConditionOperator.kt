package io.github.yearnlune.search.core.operator

import io.github.yearnlune.search.core.exception.NotSupportedExpressionException
import io.github.yearnlune.search.core.extension.toMongoType
import io.github.yearnlune.search.graphql.ComparisonOperatorType
import io.github.yearnlune.search.graphql.DataInput
import io.github.yearnlune.search.graphql.SearchInput
import io.github.yearnlune.search.graphql.SearchOperatorType
import org.springframework.data.mongodb.core.aggregation.AggregationExpression
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators

class ConditionOperator(
    val `if`: SearchInput,
    val then: DataInput,
    val `else`: String? = null
) {

    fun buildExpression(): AggregationExpression {
        return ConditionalOperators.Cond.`when`(
            SearchOperatorDelegator().create(`if`, Any::class.java).buildExpression(getOperatorType())
        ).run {
            then.value?.let { this.then(it.toMongoType(then.type)) } ?: then.fieldReference?.let { this.thenValueOf(it) }
        }?.otherwiseValueOf("nullField")
            ?: throw NotSupportedExpressionException("COULD NOT BUILD \$COND")
    }

    private fun getOperatorType(): ComparisonOperatorType? {
        return when (`if`.operator) {
            SearchOperatorType.CONTAIN -> ComparisonOperatorType.REGEX_MATCH
            SearchOperatorType.EQUAL -> ComparisonOperatorType.EQUAL
            else -> null
        }
    }
}