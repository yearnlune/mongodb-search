package io.github.yearnlune.search.core

import io.github.yearnlune.search.core.domain.Product
import io.github.yearnlune.search.graphql.*
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.SerializationUtils

class MongoSearchTest : DescribeSpec({
    describe("search") {
        context("search를 진행할 경우") {
            it("criteria query를 반환한다.") {
                val criteria = MongoSearch.search(
                    listOf(
                        SearchInput.builder()
                            .withBy("name")
                            .withType(PropertyType.STRING)
                            .withValue(listOf("사과"))
                            .withOperator(SearchOperatorType.CONTAIN)
                            .build(),
                        SearchInput.builder()
                            .withBy("updated_at")
                            .withType(PropertyType.DATE)
                            .withValue(listOf("1657854891000", "1659150891000"))
                            .withOperator(SearchOperatorType.BETWEEN)
                            .build(),
                        SearchInput.builder()
                            .withBy("price")
                            .withType(PropertyType.DOUBLE)
                            .withValue(listOf("0.0", "100.0"))
                            .withOperator(SearchOperatorType.BETWEEN)
                            .build()
                    ), Product::class.java
                )

                SerializationUtils.serializeToJsonSafely(Query(criteria).queryObject) shouldBe "{ \"name\" : { \"\$regularExpression\" : { \"pattern\" : \"사과\", \"options\" : \"iu\"}}, \"updated_at\" : { \"\$gte\" : 1657854891000, \"\$lt\" : 1659150891000}, \"price\" : { \"\$gte\" : 0.0, \"\$lt\" : 100.0}}"
            }
        }
    }

    describe("statistic") {
        context("단일 aggregate를 할 경우") {
            it("aggregation query를 반환한다.") {
                val equal: SearchInput = SearchInput.builder()
                    .withBy("name")
                    .withType(PropertyType.STRING)
                    .withValue(listOf("사과", "바나나", "세제"))
                    .withOperator(SearchOperatorType.EQUAL)
                    .build()
                val between = SearchInput.builder()
                    .withBy("updated_at")
                    .withType(PropertyType.DATE)
                    .withValue(listOf("1657854891000", "1659150891000"))
                    .withOperator(SearchOperatorType.BETWEEN)
                    .build()
                val countAggregation = CountAggregationInput.builder()
                    .withAlias("total")
                    .build()

                SerializationUtils.serializeToJsonSafely(
                    MongoSearch.statistic(
                        StatisticInput.builder()
                            .withSearches(listOf(equal, between))
                            .withAggregates(listOf(countAggregation))
                            .build(),
                        Product::class.java
                    ).toPipeline(Aggregation.DEFAULT_CONTEXT)
                ) shouldBe "[{ \"\$match\" : { \"name\" : { \"\$in\" : [\"사과\", \"바나나\", \"세제\"]}, \"updated_at\" : { \"\$gte\" : 1657854891000, \"\$lt\" : 1659150891000}}}, { \"\$count\" : \"total\"}]"
            }
        }

        context("복수의 aggregate를 할 경우") {
            it("aggregation query를 반환한다.") {
                val equal: SearchInput = SearchInput.builder()
                    .withBy("name")
                    .withType(PropertyType.STRING)
                    .withValue(listOf("사과", "바나나", "세제"))
                    .withOperator(SearchOperatorType.EQUAL)
                    .build()
                val between = SearchInput.builder()
                    .withBy("updated_at")
                    .withType(PropertyType.DATE)
                    .withValue(listOf("1657854891000", "1659150891000"))
                    .withOperator(SearchOperatorType.BETWEEN)
                    .build()
                val groupAggregation = GroupAggregationInput.builder()
                    .withBy(
                        listOf(
                            GroupByInput.builder()
                                .withKey("category")
                                .build()
                        )
                    )
                    .withAggregations(
                        listOf(
                            AggregationInput.builder()
                                .withOperator(AggregateOperatorType.COUNT)
                                .build(),
                            AggregationInput.builder()
                                .withProperty("price")
                                .withOperator(AggregateOperatorType.AVERAGE)
                                .build()
                        )
                    )
                    .build()

                SerializationUtils.serializeToJsonSafely(
                    MongoSearch.statistic(
                        StatisticInput.builder()
                            .withSearches(listOf(equal, between))
                            .withAggregates(listOf(groupAggregation))
                            .build(),
                        Product::class.java
                    ).toPipeline(Aggregation.DEFAULT_CONTEXT)
                ) shouldBe "[{ \"\$match\" : { \"name\" : { \"\$in\" : [\"사과\", \"바나나\", \"세제\"]}, \"updated_at\" : { \"\$gte\" : 1657854891000, \"\$lt\" : 1659150891000}}}, { \"\$group\" : { \"_id\" : \"\$category\", \"count\" : { \"\$sum\" : 1}, \"price_avg\" : { \"\$avg\" : \"\$price\"}}}]"
            }
        }
    }
})