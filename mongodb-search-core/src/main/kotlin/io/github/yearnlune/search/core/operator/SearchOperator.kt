package io.github.yearnlune.search.core.operator

import org.springframework.data.mongodb.core.aggregation.AggregationExpression
import org.springframework.data.mongodb.core.query.Criteria

abstract class SearchOperator(
    open val searchBy: String,
    open val values: List<Any>
) : Operator {

    override fun validate(): Boolean = true

    abstract fun appendExpression(criteria: Criteria): Criteria

    fun buildQuery(criteria: Criteria? = null): Criteria {
        val searchCriteria = initializeCriteriaWithTarget(criteria)
        return appendExpression(searchCriteria)
    }

    abstract fun buildExpression(operatorType: Any?): AggregationExpression

    private fun initializeCriteriaWithTarget(criteria: Criteria? = null): Criteria {
        return criteria?.and(searchBy) ?: Criteria.where(searchBy)
    }
}