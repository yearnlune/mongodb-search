package io.github.yearnlune.search.core.extension

import io.github.yearnlune.search.core.domain.Product
import io.github.yearnlune.search.core.type.ReservedWordType
import io.github.yearnlune.search.graphql.PropertyType
import io.github.yearnlune.search.graphql.SearchInput
import io.github.yearnlune.search.graphql.SearchOperatorType
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.bson.Document
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.SerializationUtils

class QueryExtensionSearchTest() : DescribeSpec({

    describe("search") {
        context("equal operator") {
            it("검색어가 하나 일 때") {
                val searchInput = SearchInput(
                    by = "name",
                    type = PropertyType.STRING,
                    value = listOf("사과"),
                    operator = SearchOperatorType.EQUAL
                )
                val criteria = Criteria().search(listOf(searchInput), Product::class.java)

                SerializationUtils.serializeToJsonSafely(Query(criteria).queryObject) shouldBe
                    "{ \"name\" : { \"\$in\" : [\"사과\"]}}"
            }

            it("검색어가 여러 개 일 때") {
                val searchInput = SearchInput(
                    by = "name",
                    type = PropertyType.STRING,
                    value = listOf("사과", "바나나"),
                    operator = SearchOperatorType.EQUAL
                )
                val criteria = Criteria().search(listOf(searchInput), Product::class.java)

                SerializationUtils.serializeToJsonSafely(Query(criteria).queryObject) shouldBe "{ \"name\" : { \"\$in\" : [\"사과\", \"바나나\"]}}"
            }
        }

        context("contain operator") {
            it("검색어가 하나 일 때") {
                val searchInput = SearchInput(
                    by = "name",
                    type = PropertyType.STRING,
                    value = listOf("사과"),
                    operator = SearchOperatorType.CONTAIN
                )
                val criteria = Criteria().search(listOf(searchInput), Product::class.java)

                SerializationUtils.serializeToJsonSafely(Query(criteria).queryObject) shouldBe "{ \"name\" : { \"\$regularExpression\" : { \"pattern\" : \"사과\", \"options\" : \"iu\"}}}"
            }
        }

        context("between operator") {
            it("long 타입의 timestamp일 때") {
                val start = 1657767559757L
                val searchInput = SearchInput(
                    by = "updated_at",
                    type = PropertyType.DATE,
                    value = listOf("$start", "${start + 1000L}"),
                    operator = SearchOperatorType.BETWEEN
                )
                val criteria = Criteria().search(listOf(searchInput), Product::class.java)

                SerializationUtils.serializeToJsonSafely(Query(criteria).queryObject) shouldBe
                    "{ \"updated_at\" : { \"\$gte\" : 1657767559757, \"\$lt\" : 1657767560757}}"
            }

            it("objectId 타입일 때") {
                val start = 1657767559757L
                val searchInput = SearchInput(
                    by = "_id",
                    type = PropertyType.OBJECT_ID,
                    value = listOf("$start", "${start + 1000L}"),
                    operator = SearchOperatorType.BETWEEN
                )

                val criteria = Criteria().search(listOf(searchInput), Product::class.java)
                SerializationUtils.serializeToJsonSafely(Query(criteria).queryObject) shouldBe
                    "{ \"_id\" : { \"\$gte\" : { \"\$oid\" : \"62cf86870000000000000000\"}, \"\$lt\" : { \"\$oid\" : \"62cf86880000000000000000\"}}}"
            }

            it("현재시각으로 검색 할 때") {
                val start = ReservedWordType.CURRENT_DATE.name
                val searchInput = SearchInput(
                    by = "updated_at",
                    type = PropertyType.DATE,
                    value = listOf(start, "1738291115000"),
                    operator = SearchOperatorType.BETWEEN
                )
                val criteria = Criteria().search(listOf(searchInput), Product::class.java)

                SerializationUtils.serializeToJsonSafely(Query(criteria).queryObject) shouldContain
                    """\{ "updated_at" : \{ "\$\w{3}" : \d{13}""".toRegex()
            }
        }

        context("startWith operator") {
            it("String 타입일 때") {
                val property = "ip"
                val startWith = "192.168.12"
                val searchInput = SearchInput(
                    by = property,
                    type = PropertyType.STRING,
                    value = listOf(startWith),
                    operator = SearchOperatorType.START_WITH
                )

                val criteria = Criteria().search(listOf(searchInput), Any::class.java)
                SerializationUtils.serializeToJsonSafely(Query(criteria).queryObject) shouldBe
                    "{ \"$property\" : { \"\$regularExpression\" : { \"pattern\" : \"^192\\\\.168\\\\.12\", \"options\" : \"iu\"}}}"
            }
        }

        context("dateRange operator") {
            it("현재 시각부터 이전 3일 동안") {
                val property = "updated_at"
                val searchInput = SearchInput(
                    by = property,
                    type = PropertyType.ANY,
                    value = listOf("LAST", "DAYS", "3"),
                    operator = SearchOperatorType.DATE_RANGE
                )
                val criteria = Criteria().search(listOf(searchInput), Any::class.java)
                val updatedAtDoc = Query(criteria).queryObject["updated_at"] as Document
                val lte = updatedAtDoc.getLong("\$lt")
                val gte = updatedAtDoc.getLong("\$gte")
                val threeDaysMilliSecond = 86400 * 3 * 1000L

                lte - gte shouldBe threeDaysMilliSecond
            }
        }

        context("aggregate pipeline에서 활용할 때") {
            it("\$match를 query를 추가하여 반환한다.") {
                val start = 1657767559757L
                val searches: List<SearchInput> = listOf(
                    SearchInput(
                        by = "updated_at",
                        type = PropertyType.DATE,
                        value = listOf("$start", "${start + 1000L}"),
                        operator = SearchOperatorType.BETWEEN
                    )
                )

                SerializationUtils.serializeToJsonSafely(
                    Aggregation.newAggregation(
                        Aggregation.match(Criteria.where("deleted").exists(false))
                    ).search(searches, Product::class.java).toPipeline(Aggregation.DEFAULT_CONTEXT)
                ) shouldBe "[{ \"\$match\" : { \"deleted\" : { \"\$exists\" : false}}}, " +
                    "{ \"\$match\" : { \"updated_at\" : { \"\$gte\" : 1657767559757, \"\$lt\" : 1657767560757}}}]"
            }
        }
    }
})