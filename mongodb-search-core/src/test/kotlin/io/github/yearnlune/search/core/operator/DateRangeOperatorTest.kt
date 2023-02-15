package io.github.yearnlune.search.core.operator

import io.github.yearnlune.search.core.exception.SyntaxException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.SerializationUtils
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

class DateRangeOperatorTest : DescribeSpec({
    val searchBy = "updated_at"
    val now = LocalDateTime.now()
    val nowInstance = now.toInstant(ZoneOffset.UTC)

    describe("buildQuery") {
        it("Illegal syntax: 값 누락") {
            shouldThrow<SyntaxException> {
                val values = listOf("LAST", "MONTHS")
                DateRangeOperator(searchBy, values).buildQuery()
            }
        }

        it("Illegal syntax: 잘못된 값 제공") {
            shouldThrow<SyntaxException> {
                val values = listOf("LAST", "MONTHS", "5", "2022-03-03")
                DateRangeOperator(searchBy, values).buildQuery()
            }
        }

        it("DateRangeType.LAST") {
            val values = listOf("LAST", "MONTHS", "3", "${nowInstance.toEpochMilli()}")
            val dateRangeQuery = DateRangeOperator(searchBy, values).buildQuery()
            val expectedQuery = "{ \"updated_at\" : { \"\$gte\" : ${
            now.minus(3L, ChronoUnit.MONTHS).toInstant(ZoneOffset.UTC).toEpochMilli()
            }, \"\$lt\" : ${
            nowInstance.toEpochMilli()
            }}}"

            SerializationUtils.serializeToJsonSafely(Query(dateRangeQuery).queryObject) shouldBe expectedQuery
        }

        it("DateRangeType.NEXT") {
            val values = listOf("NEXT", "MONTHS", "3", "${nowInstance.toEpochMilli()}")
            val dateRangeQuery = DateRangeOperator(searchBy, values).buildQuery()
            val expectedQuery = "{ \"updated_at\" : { \"\$gte\" : ${
            nowInstance.toEpochMilli()
            }, \"\$lt\" : ${
            now.plus(3L, ChronoUnit.MONTHS).toInstant(ZoneOffset.UTC).toEpochMilli()
            }}}"

            SerializationUtils.serializeToJsonSafely(Query(dateRangeQuery).queryObject) shouldBe expectedQuery
        }
    }

    describe("buildExpression") {
        it("\$gte, \$lt query를 반환한다.") {
            val values = listOf("NEXT", "MONTHS", "3", "${nowInstance.toEpochMilli()}")
            val dateRangeExpression = DateRangeOperator(searchBy, values).buildExpression()
            val expectedQuery =
                " { \"\$and\" : [{ \"\$gte\" : [\"\$$searchBy\", ${nowInstance.toEpochMilli()}]}, " +
                    "{ \"\$lt\" : [\"\$$searchBy\", ${
                    now.plus(3L, ChronoUnit.MONTHS).toInstant(ZoneOffset.UTC).toEpochMilli()
                    }]}]}"

            dateRangeExpression.buildTestAggregation().toString() shouldContain expectedQuery
        }
    }
})