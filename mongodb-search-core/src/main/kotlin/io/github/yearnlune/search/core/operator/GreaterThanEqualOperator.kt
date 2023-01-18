package io.github.yearnlune.search.core.operator

import org.springframework.data.mongodb.core.aggregation.AggregationExpression
import org.springframework.data.mongodb.core.aggregation.ComparisonOperators
import org.springframework.data.mongodb.core.query.Criteria

class GreaterThanEqualOperator(
    searchBy: String,
    values: List<Any>
) : SearchOperator(searchBy, values) {

    override fun appendExpression(criteria: Criteria): Criteria = criteria.gte(values[0])

    override fun buildExpression(): AggregationExpression =
        ComparisonOperators.Gte.valueOf(searchBy).greaterThanEqualToValue(values[0])
}