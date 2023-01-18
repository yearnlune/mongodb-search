package io.github.yearnlune.search.core.operator

import org.springframework.data.mongodb.core.aggregation.AggregationExpression
import org.springframework.data.mongodb.core.aggregation.ComparisonOperators
import org.springframework.data.mongodb.core.query.Criteria

class GreaterThanOperator(
    searchBy: String,
    values: List<Any>
) : SearchOperator(searchBy, values) {

    override fun appendExpression(criteria: Criteria): Criteria = criteria.gt(values[0])

    override fun buildExpression(): AggregationExpression =
        ComparisonOperators.Gt.valueOf(searchBy).greaterThanValue(values[0])
}