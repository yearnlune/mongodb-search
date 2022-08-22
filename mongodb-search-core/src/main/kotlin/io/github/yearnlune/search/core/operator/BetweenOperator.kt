package io.github.yearnlune.search.core.operator

import org.springframework.data.mongodb.core.query.Criteria

class BetweenOperator(
    searchBy: String,
    values: List<Any>
) : SearchOperator(searchBy, values) {

    override fun appendExpression(criteria: Criteria): Criteria = criteria.gte(values[0]).lt(values[1])
}