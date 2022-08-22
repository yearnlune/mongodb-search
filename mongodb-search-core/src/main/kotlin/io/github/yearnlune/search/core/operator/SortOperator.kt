package io.github.yearnlune.search.core.operator

import io.github.yearnlune.search.graphql.SortInput
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.AggregationOperation
import org.springframework.data.mongodb.core.aggregation.SortOperation

class SortOperator(
    private val sorts: List<SortInput>
) : AggregateOperator() {

    override fun buildOperation(aggregationOperation: AggregationOperation?): AggregationOperation {
        var sortOperation: SortOperation? = null

        sorts.forEachIndexed { index, sortInput ->
            sortOperation =
                if (index == 0) Aggregation.sort(getSortDirection(sortInput.isDescending), sortInput.property)
                else sortOperation!!.and(getSortDirection(sortInput.isDescending), sortInput.property)
        }

        return sortOperation ?: throw IllegalArgumentException()
    }

    private fun getSortDirection(isDescending: Boolean): Sort.Direction {
        return if (isDescending) {
            Sort.Direction.DESC
        } else {
            Sort.Direction.ASC
        }
    }
}