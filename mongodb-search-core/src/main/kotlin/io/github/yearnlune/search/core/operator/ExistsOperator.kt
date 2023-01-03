package io.github.yearnlune.search.core.operator

import org.springframework.data.mongodb.core.aggregation.AggregationExpression
import org.springframework.data.mongodb.core.aggregation.BooleanOperators
import org.springframework.data.mongodb.core.query.Criteria

class ExistsOperator(
    searchBy: String,
    override val values: List<Any>
) : SearchOperator(searchBy, values) {

    override fun appendExpression(criteria: Criteria): Criteria = criteria.exists(values.first() as Boolean)

    override fun buildExpression(): AggregationExpression {
        return when (values.first() as Boolean) {
            false -> BooleanOperators.Not.not(searchBy)
            else -> BooleanOperators.Not.not(BooleanOperators.Not.not(searchBy))
        }
    }
}