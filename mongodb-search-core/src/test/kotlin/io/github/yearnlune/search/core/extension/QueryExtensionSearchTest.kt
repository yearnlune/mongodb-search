package io.github.yearnlune.search.core.extension

import io.github.yearnlune.search.core.domain.Product
import io.github.yearnlune.search.core.type.ReservedWordType
import io.github.yearnlune.search.graphql.PropertyType
import io.github.yearnlune.search.graphql.SearchInput
import io.github.yearnlune.search.graphql.SearchOperatorType
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.SerializationUtils

class QueryExtensionSearchTest : DescribeSpec({

    describe("search") {
        context("equal operator") {
            it("검색어가 하나 일 때") {
                val searchInput: SearchInput =
                    SearchInput.builder().withBy("name").withType(PropertyType.STRING).withValue(listOf("사과"))
                        .withOperator(SearchOperatorType.EQUAL).build()

                val criteria = Criteria().search(listOf(searchInput), Product::class.java)
                SerializationUtils.serializeToJsonSafely(Query(criteria).queryObject) shouldBe
                        "{ \"name\" : { \"\$in\" : [\"사과\"]}}"
            }

            it("검색어가 여러 개 일 때") {
                val searchInput: SearchInput =
                    SearchInput.builder().withBy("name").withType(PropertyType.STRING).withValue(listOf("사과", "바나나"))
                        .withOperator(SearchOperatorType.EQUAL).build()
                val criteria = Criteria().search(listOf(searchInput), Product::class.java)
                SerializationUtils.serializeToJsonSafely(Query(criteria).queryObject) shouldBe "{ \"name\" : { \"\$in\" : [\"사과\", \"바나나\"]}}"
            }
        }

        context("contain operator") {
            it("검색어가 하나 일 때") {
                val searchInput: SearchInput =
                    SearchInput.builder().withBy("name").withType(PropertyType.STRING).withValue(listOf("사과"))
                        .withOperator(SearchOperatorType.CONTAIN).build()

                val criteria = Criteria().search(listOf(searchInput), Product::class.java)
                SerializationUtils.serializeToJsonSafely(Query(criteria).queryObject) shouldBe "{ \"name\" : { \"\$regularExpression\" : { \"pattern\" : \"사과\", \"options\" : \"iu\"}}}"
            }
        }

        context("between operator") {
            it("long 타입의 timestamp일 때") {
                val start = 1657767559757L
                val searchInput: SearchInput = SearchInput.builder().withBy("updated_at").withType(PropertyType.DATE)
                    .withValue(listOf("$start", "${start + 1000L}")).withOperator(SearchOperatorType.BETWEEN).build()

                val criteria = Criteria().search(listOf(searchInput), Product::class.java)
                SerializationUtils.serializeToJsonSafely(Query(criteria).queryObject) shouldBe
                        "{ \"updated_at\" : { \"\$gte\" : 1657767559757, \"\$lt\" : 1657767560757}}"
            }

            it("objectId 타입일 때") {
                val start = 1657767559757L
                val searchInput: SearchInput = SearchInput.builder().withBy("id").withType(PropertyType.OBJECT_ID)
                    .withValue(listOf("$start", "${start + 1000L}")).withOperator(SearchOperatorType.BETWEEN).build()

                val criteria = Criteria().search(listOf(searchInput), Product::class.java)
                SerializationUtils.serializeToJsonSafely(Query(criteria).queryObject) shouldBe
                        "{ \"id\" : { \"\$gte\" : { \"\$oid\" : \"62cf86870000000000000000\"}, \"\$lt\" : { \"\$oid\" : \"62cf86880000000000000000\"}}}"
            }

            it("현재시각으로 검색 할 때") {
                val start = ReservedWordType.CURRENT_DATE.name
                val searchInput: SearchInput = SearchInput.builder().withBy("updated_at").withType(PropertyType.DATE)
                    .withValue(listOf(start, "1738291115000")).withOperator(SearchOperatorType.BETWEEN).build()

                val criteria = Criteria().search(listOf(searchInput), Product::class.java)

                SerializationUtils.serializeToJsonSafely(Query(criteria).queryObject) shouldContain
                        """\{ "updated_at" : \{ "\$\w{3}" : \d{13}""".toRegex()
            }
        }

        context("startWith operator") {
            it("String 타입일 때") {
                val property = "ip"
                val startWith = "192.168.12"
                val searchInput: SearchInput = SearchInput.builder()
                    .withBy(property)
                    .withType(PropertyType.STRING)
                    .withValue(listOf(startWith))
                    .withOperator(SearchOperatorType.START_WITH)
                    .build()

                val criteria = Criteria().search(listOf(searchInput), Any::class.java)
                SerializationUtils.serializeToJsonSafely(Query(criteria).queryObject) shouldBe
                        "{ \"$property\" : { \"\$regularExpression\" : { \"pattern\" : \"^192\\\\.168\\\\.12\", \"options\" : \"iu\"}}}"
            }
        }

        context("aggregate pipeline에서 활용할 때") {
            it("\$match를 query를 추가하여 반환한다.") {
                val start = 1657767559757L
                val searches: List<SearchInput> = listOf(
                    SearchInput.builder()
                        .withBy("updated_at")
                        .withType(PropertyType.DATE)
                        .withValue(listOf("$start", "${start + 1000L}"))
                        .withOperator(SearchOperatorType.BETWEEN).build()
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