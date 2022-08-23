package io.github.yearnlune.search.core.operator

import io.github.yearnlune.search.core.exception.NotSupportedExpressionException
import org.springframework.data.mongodb.core.aggregation.AggregationExpression
import org.springframework.data.mongodb.core.query.Criteria

class ExistsOperator(
    searchBy: String,
    override val values: List<Any>
) : SearchOperator(searchBy, values) {

    override fun appendExpression(criteria: Criteria): Criteria = criteria.exists(values.first() as Boolean)

    override fun buildExpression(operatorType: Any?): AggregationExpression {
        throw NotSupportedExpressionException("$operatorType")
    }
}