package io.github.yearnlune.search.core.operator

import io.github.yearnlune.search.core.exception.NotSupportedOperatorException
import io.github.yearnlune.search.core.extension.getFieldPath
import io.github.yearnlune.search.graphql.AggregateOperatorType
import io.github.yearnlune.search.graphql.StatisticInput
import org.springframework.data.mongodb.core.aggregation.AggregationOperation

object AggregateOperatorDelegator {

    private var aggregateOperator: AggregateOperator? = null

    private var groupOperator: GroupOperator? = null

    fun <T> create(statisticInput: StatisticInput, targetClass: Class<T>): AggregateOperatorDelegator {
        val groupBy = statisticInput.groupBy.map { targetClass.getFieldPath(it, true) }
        groupOperator = if (groupBy.isNotEmpty()) GroupOperator(groupBy) else null

        statisticInput.aggregates.forEach {
            val target = if (it.property != null) targetClass.getFieldPath(it.property, true) else ""
            aggregateOperator = when (it.operator) {
                AggregateOperatorType.COUNT -> CountOperator()
                AggregateOperatorType.SUM -> SumOperator(target)
                else -> throw NotSupportedOperatorException("Not supported operator: ${it.operator.name}")
            }

            if (groupBy.isNotEmpty()) {
                aggregateOperator?.buildOperation(groupOperator!!.groupOperation)
            }
        }

        return this
    }

    fun buildAggregate(): AggregationOperation = aggregateOperator?.buildOperation(groupOperator?.groupOperation)
        ?: throw NotSupportedOperatorException("Not supported operator")
}