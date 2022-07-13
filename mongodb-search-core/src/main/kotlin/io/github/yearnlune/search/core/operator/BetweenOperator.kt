package io.github.yearnlune.search.core.operator

import org.springframework.data.mongodb.core.query.Criteria

class BetweenOperator(
    override val searchBy: String,
    override val values: List<Any>
) : SearchOperator(searchBy, values) {

    override fun buildQuery(): Criteria = Criteria.where(searchBy).gte(values[0]).lt(values[1])
}
