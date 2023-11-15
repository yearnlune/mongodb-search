package io.github.yearnlune.search.core.extension

import io.github.yearnlune.search.core.operator.AggregateOperatorDelegator
import io.github.yearnlune.search.core.operator.SearchOperatorDelegator
import io.github.yearnlune.search.graphql.DateUnitType
import io.github.yearnlune.search.graphql.PageInput
import io.github.yearnlune.search.graphql.SearchInput
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.AggregationOperation
import org.springframework.data.mongodb.core.aggregation.Fields
import org.springframework.data.mongodb.core.aggregation.MatchOperation
import org.springframework.data.mongodb.core.aggregation.SortOperation
import org.springframework.data.mongodb.core.query.Criteria
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalUnit

fun Criteria.search(
    searches: List<SearchInput>,
    targetClass: Class<*>,
): Criteria {
    var newCriteria = this

    searches.forEach {
        newCriteria = SearchOperatorDelegator().create(it, targetClass).buildQuery(newCriteria)
    }

    return newCriteria
}

fun Aggregation.search(searches: List<SearchInput>, targetClass: Class<*>): Aggregation {
    this.pipeline.add(MatchOperation(Criteria().search(searches, targetClass)))
    return this
}

fun Aggregation.aggregate(
    aggregates: List<Any>,
    targetClass: Class<*>,
): Aggregation {
    val operations = mutableListOf<AggregationOperation>()
    operations.addAll(this.pipeline.operations)

    aggregates.forEach {
        operations.addAll(
            AggregateOperatorDelegator()
                .create(
                    if (it is Map<*, *>) it.toAggregationInput() else it,
                    targetClass,
                )
                .buildAggregate()
        )
    }

    return Aggregation.newAggregation(operations)
}

fun List<String>.toMongoFields(): Fields {
    var fields = Fields.fields()

    this.forEach {
        fields = fields.and(it.replace(".", "@"), "\$$it")
    }

    return fields
}

fun DateUnitType.toTemporalType(): TemporalUnit {
    return when (this) {
        DateUnitType.DAYS -> ChronoUnit.DAYS
        DateUnitType.MONTHS -> ChronoUnit.MONTHS
        DateUnitType.WEEKS -> ChronoUnit.WEEKS
        DateUnitType.YEARS -> ChronoUnit.YEARS
    }
}

fun PageInput.toPageRequest(): Pageable {
    return PageRequest.of(
        this.pageNumber.toInt() - 1,
        this.pageSize.toInt(),
        Sort.by(
            this.sort.map {
                Sort.Order(if (it.isDescending) Sort.Direction.DESC else Sort.Direction.ASC, it.property)
            }
        )
    )
}

fun PageInput.toSortOperation(): SortOperation? {
    var sorts: Sort? = null
    this.sort.map { sortInput ->
        val direction = if (sortInput.isDescending) Sort.Direction.DESC else Sort.Direction.ASC
        val sort = Sort.by(direction, sortInput.property)
        sorts = if (sorts == null) {
            sort
        } else {
            sort.and(sort)
        }
    }

    return sorts?.let { SortOperation(it) }
}