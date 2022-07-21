package io.github.yearnlune.search.core.operator

import io.github.yearnlune.search.core.exception.NotSupportedExpressionException
import io.github.yearnlune.search.core.exception.NotSupportedOperatorException
import io.github.yearnlune.search.core.extension.snakeCase
import io.github.yearnlune.search.graphql.AggregateOperatorType
import io.github.yearnlune.search.graphql.AggregationInput
import io.github.yearnlune.search.graphql.CountAggregationInput
import io.github.yearnlune.search.graphql.GroupAggregationInput
import org.springframework.data.mongodb.core.aggregation.AggregationOperation

class AggregateOperatorDelegator {

    private lateinit var aggregateOperator: AggregateOperator

    fun create(aggregationInput: Any, targetClass: Class<*>): AggregateOperatorDelegator {
        when (aggregationInput) {
            is GroupAggregationInput -> {
                val groupBy = aggregationInput.by.map { it.snakeCase() }
                aggregateOperator = GroupOperator(groupBy)
                buildExpression(aggregationInput.aggregations, targetClass)
            }
            is CountAggregationInput -> {
                aggregateOperator = CountOperator(aggregationInput.alias)
            }
            else -> {
                throw NotSupportedOperatorException("Not supported operator: ${aggregationInput::class.simpleName}")
            }
        }

        return this
    }

    fun buildAggregate(aggregationOperation: AggregationOperation? = null): AggregationOperation {
        return aggregateOperator.buildQuery(aggregationOperation)
    }

    private fun buildExpression(aggregations: List<AggregationInput>, targetClass: Class<*>) {
        aggregations.forEach {
            try {
                val expression = when (it.operator) {
                    AggregateOperatorType.COUNT -> CountOperator(it.alias)
                    AggregateOperatorType.SUM -> SumOperator(it.property.snakeCase())
                    AggregateOperatorType.AVERAGE -> AverageOperator(it.property.snakeCase())
                    else -> throw NotSupportedExpressionException("Not supported expression: ${it.operator.name}")
                }

                aggregateOperator.operation = expression.buildQuery(aggregateOperator.operation)
            } catch (e: NullPointerException) {
                throw IllegalArgumentException()
            }
        }
    }
}