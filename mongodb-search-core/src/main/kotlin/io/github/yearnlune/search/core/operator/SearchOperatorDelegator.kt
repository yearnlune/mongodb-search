package io.github.yearnlune.search.core.operator

import io.github.yearnlune.search.core.exception.NotSupportedOperatorException
import io.github.yearnlune.search.core.extension.getFieldPath
import io.github.yearnlune.search.core.extension.toMongoType
import io.github.yearnlune.search.graphql.SearchInput
import io.github.yearnlune.search.graphql.SearchOperatorType
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.AggregationOperation
import org.springframework.data.mongodb.core.aggregation.MatchOperation
import org.springframework.data.mongodb.core.query.Criteria

object SearchOperatorDelegator {

    private lateinit var searchOperator: SearchOperator

    fun <T> create(searchInput: SearchInput, targetClass: Class<T>): SearchOperatorDelegator {
        val searchBy = targetClass.getFieldPath(searchInput.by, true)
        val typedValues = searchInput.value.map { value -> value.toMongoType(searchInput.type) }

        searchOperator = when (searchInput.operator) {
            SearchOperatorType.EQUAL -> EqualOperator(searchBy, typedValues)
            SearchOperatorType.CONTAIN -> ContainOperator(searchBy, typedValues)
            SearchOperatorType.BETWEEN -> BetweenOperator(searchBy, typedValues)
            else -> throw NotSupportedOperatorException("Not supported operator: ${searchInput.operator.name}")
        }

        return this
    }

    fun buildQuery(): Criteria = searchOperator.buildQuery()

    fun buildMatchOperation(): AggregationOperation = MatchOperation(searchOperator.buildQuery())

    fun buildAggregation(): Aggregation = Aggregation.newAggregation(buildMatchOperation())
}
