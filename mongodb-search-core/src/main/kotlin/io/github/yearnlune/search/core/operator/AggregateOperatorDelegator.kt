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

    private val aggregateOperator: MutableList<AggregateOperator> = mutableListOf()

    fun create(aggregationInput: Any, targetClass: Class<*>): AggregateOperatorDelegator {
        when (aggregationInput) {
            is GroupAggregationInput -> {
                val addFields = mutableListOf<AddFieldsOperator.Field>()
                val groupByList = aggregationInput.by
                    .map { groupBy ->
                        groupBy.key = groupBy.key.snakeCase()
                        groupBy.option
                            ?.let {
                                val newFieldKey = "${groupBy.key}_${groupBy.option.ordinal}"
                                addFields.add(AddFieldsOperator.Field(groupBy.key, groupBy.option, newFieldKey))
                                groupBy.key = newFieldKey
                            }
                        groupBy.key
                    }

                if (addFields.isNotEmpty()) {
                    aggregateOperator.add(AddFieldsOperator(addFields))
                }

                val groupOperator =
                    buildExpression(GroupOperator(groupByList), aggregationInput.aggregations, targetClass)
                aggregateOperator.add(groupOperator)
            }
            is CountAggregationInput -> {
                aggregateOperator.add(CountOperator(aggregationInput.alias))
            }
            else -> {
                throw NotSupportedOperatorException("Not supported operator: ${aggregationInput::class.simpleName}")
            }
        }

        return this
    }

    fun buildAggregate(aggregationOperation: AggregationOperation? = null): List<AggregationOperation> {
        return aggregateOperator.map {
            it.buildQuery(aggregationOperation)
        }
//        return aggregateOperator.buildQuery(aggregationOperation)
    }

    private fun buildExpression(
        aggregateOperator: AggregateOperator,
        aggregations: List<AggregationInput>,
        targetClass: Class<*>
    ): AggregateOperator {
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

        return aggregateOperator
    }
}