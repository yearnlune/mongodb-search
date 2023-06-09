package io.github.yearnlune.search.core.operator

import io.github.yearnlune.search.core.exception.SyntaxException
import io.github.yearnlune.search.core.extension.toTemporalType
import io.github.yearnlune.search.graphql.DateRangeType
import io.github.yearnlune.search.graphql.DateUnitType
import org.springframework.data.mongodb.core.query.Criteria
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

class DateRangeOperator(
    searchBy: String,
    values: List<String>,
    private var rangeType: DateRangeType = DateRangeType.LAST,
    private var unitType: DateUnitType = DateUnitType.MONTHS,
    private var range: Int = 0,
    private var source: LocalDateTime = LocalDateTime.now()
) : SearchOperator(searchBy, values) {

    init {
        runCatching {
            rangeType = DateRangeType.fromValue(values[0])
            unitType = DateUnitType.fromValue(values[1])
            range = values[2].toInt()
            if (values.size > 3) {
                source = Instant.ofEpochMilli(values[3].toLong()).atOffset(ZoneOffset.UTC).toLocalDateTime()
            }
        }.getOrElse {
            val msg = when (it) {
                is IndexOutOfBoundsException -> "values syntax: [DateRangeType, UnitType, Range, initTimestamp?]"
                else -> it.localizedMessage
            }
            throw SyntaxException(msg)
        }
    }

    override fun appendExpression(criteria: Criteria) = BetweenOperator(this.searchBy, getDates()).buildQuery()

    override fun buildExpression() = BetweenOperator(this.searchBy, getDates()).buildExpression()

    private fun getDates() = listOf(source.toEpochMilli(), processTargetDate().toEpochMilli()).sorted()

    private fun processTargetDate(): LocalDateTime {
        return when (rangeType) {
            DateRangeType.LAST -> {
                source.minus(range.toLong(), unitType.toTemporalType())
            }

            DateRangeType.NEXT -> {
                source.plus(range.toLong(), unitType.toTemporalType())
            }
        }
    }

    private fun LocalDateTime.toEpochMilli(offset: ZoneOffset = ZoneOffset.UTC) = this.toInstant(offset).toEpochMilli()
}