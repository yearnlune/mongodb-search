package io.github.yearnlune.search.core.operator

import org.springframework.data.mongodb.core.aggregation.AggregationExpression
import org.springframework.data.mongodb.core.query.Criteria

abstract class SearchOperator(
    open val searchBy: String,
    open val values: List<Any>
) : Operator {

    override fun validate(): Boolean = true

    abstract fun appendExpression(criteria: Criteria): Criteria

    abstract fun buildExpression(): AggregationExpression

    fun buildQuery(criteria: Criteria? = null): Criteria {
        val searchCriteria = initializeCriteriaWithTarget(criteria)
        return appendExpression(searchCriteria)
    }

    private fun initializeCriteriaWithTarget(criteria: Criteria? = null): Criteria {
        return criteria?.and(searchBy) ?: Criteria.where(searchBy)
    }
}