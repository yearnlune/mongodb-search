package io.github.yearnlune.search.core.operator

import io.github.yearnlune.search.core.exception.NotSupportedExpressionException
import io.github.yearnlune.search.core.extension.toMongoType
import io.github.yearnlune.search.graphql.DataInput
import io.github.yearnlune.search.graphql.SearchInput
import org.springframework.data.mongodb.core.aggregation.AggregationExpression
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators

class ConditionOperator(
    private val `if`: SearchInput,
    private val then: DataInput,
    private val `else`: String? = null
) {

    fun buildExpression(): AggregationExpression {
        return ConditionalOperators.Cond.`when`(
            SearchOperatorDelegator().create(`if`, Any::class.java).buildExpression()
        ).run {
            then.value?.let { this.then(it.toMongoType(then.type)) }
                ?: then.fieldReference?.let { this.thenValueOf(it) }
        }?.let {
            `else`?.let { value -> it.otherwise(value) } ?: it.otherwiseValueOf("nullField")
        } ?: throw NotSupportedExpressionException("COULD NOT BUILD \$COND")
    }
}