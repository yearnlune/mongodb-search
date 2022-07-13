package io.github.yearnlune.search.core.operator

import io.github.yearnlune.search.core.extension.toObjectId
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.SerializationUtils
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class BetweenOperatorTest : DescribeSpec({

    describe("buildQuery") {
        val searchBy = "updated_at"
        val start = 1657000000000L
        val end = 1657600000000L

        context("ObjectId 타입 필드를 범위 검색하려고 할 때") {
            it("ObjectId 타입 필드에 대한 \$gte, \$lt query를 반환한다.") {
                val betweenOperator = BetweenOperator(searchBy, listOf(start.toObjectId(), end.toObjectId())).buildQuery()
                val expectedQuery =
                    "{ \"$searchBy\" : { \"\$gte\" : { \"\$oid\" : \"${start.toObjectId()}\"}, \"\$lt\" : { \"\$oid\" : \"${end.toObjectId()}\"}}}"

                SerializationUtils.serializeToJsonSafely(Query(betweenOperator).queryObject) shouldBe expectedQuery
            }
        }

        context("long 타입 필드를 범위 검색하려고 할 때") {
            it("long 타입 필드에 대한 \$gte, \$lt query를 반환한다.") {
                val betweenOperator = BetweenOperator(searchBy, listOf(start, end)).buildQuery()
                val expectedQuery = "{ \"$searchBy\" : { \"\$gte\" : $start, \"\$lt\" : $end}}"

                SerializationUtils.serializeToJsonSafely(Query(betweenOperator).queryObject) shouldBe expectedQuery
            }
        }

        context("date 타입의 업데이트 날짜를 검색하려고 할 때") {
            it("date 타입에 대한 \$gte, \$lt query를 반환한다.") {
                val startDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(start), TimeZone.getDefault().toZoneId()).format(DateTimeFormatter.ISO_DATE_TIME)
                val endDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(end), TimeZone.getDefault().toZoneId()).format(DateTimeFormatter.ISO_DATE_TIME)

                val betweenOperator = BetweenOperator(searchBy, listOf(startDate, endDate)).buildQuery()
                val expectedQuery = "{ \"$searchBy\" : { \"\$gte\" : \"$startDate\", \"\$lt\" : \"$endDate\"}}"

                SerializationUtils.serializeToJsonSafely(Query(betweenOperator).queryObject) shouldBe expectedQuery
            }
        }
    }
})