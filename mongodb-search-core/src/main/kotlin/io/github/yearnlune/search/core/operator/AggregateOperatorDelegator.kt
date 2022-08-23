package io.github.yearnlune.search.core.operator

import io.github.yearnlune.search.core.exception.NotSupportedExpressionException
import io.github.yearnlune.search.core.exception.NotSupportedOperatorException
import io.github.yearnlune.search.core.extension.snakeCase
import io.github.yearnlune.search.graphql.AggregationAccumulatorOperatorType
import io.github.yearnlune.search.graphql.AggregationInput
import io.github.yearnlune.search.graphql.ConditionInput
import io.github.yearnlune.search.graphql.CountAggregationInput
import io.github.yearnlune.search.graphql.GroupAggregationInput
import io.github.yearnlune.search.graphql.LimitAggregationInput
import io.github.yearnlune.search.graphql.SortAggregationInput
import org.springframework.data.mongodb.core.aggregation.AggregationExpression
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
            is LimitAggregationInput -> {
                aggregateOperator.add(LimitOperator(aggregationInput.maxElements))
            }
            is SortAggregationInput -> {
                aggregateOperator.add(SortOperator(aggregationInput.sorts))
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
                val property = it.property?.snakeCase()
                var propertyExpression: AggregationExpression? = null
                if (it.propertyExpression != null) {
                    propertyExpression = fieldExpression(it.propertyExpression)
                }

                val expression = when (it.operator) {
                    AggregationAccumulatorOperatorType.COUNT -> CountOperator(it.alias)
                    AggregationAccumulatorOperatorType.SUM -> SumOperator(property, propertyExpression, it.alias)
                    AggregationAccumulatorOperatorType.AVERAGE ->
                        AverageOperator(property, propertyExpression, it.alias)
                    else -> throw NotSupportedExpressionException("Not supported expression: ${it.operator.name}")
                }

                aggregateOperator.operation = expression.buildQuery(aggregateOperator.operation)
            } catch (e: NullPointerException) {
                throw IllegalArgumentException()
            }
        }

        return aggregateOperator
    }

    private fun fieldExpression(expression: Any): AggregationExpression {
        val operator = when (expression) {
            is ConditionInput -> ConditionOperator(`if` = expression.`if`, then = expression.then)
            else ->
                throw NotSupportedExpressionException("Not supported expression: ${expression.javaClass.simpleName}")
        }

        return operator.buildExpression()
    }
}