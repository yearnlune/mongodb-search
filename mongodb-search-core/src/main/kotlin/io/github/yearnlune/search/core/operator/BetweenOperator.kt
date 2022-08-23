package io.github.yearnlune.search.core.operator

import io.github.yearnlune.search.core.exception.NotSupportedExpressionException
import org.springframework.data.mongodb.core.aggregation.AggregationExpression
import org.springframework.data.mongodb.core.query.Criteria

class BetweenOperator(
    searchBy: String,
    values: List<Any>
) : SearchOperator(searchBy, values) {

    override fun appendExpression(criteria: Criteria): Criteria = criteria.gte(values[0]).lt(values[1])

    override fun buildExpression(operatorType: Any?): AggregationExpression {
        throw NotSupportedExpressionException("$operatorType")
    }
}