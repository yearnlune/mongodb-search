package io.github.yearnlune.search.core

import io.github.yearnlune.search.core.domain.Product
import io.github.yearnlune.search.graphql.AggregationAccumulatorOperatorType
import io.github.yearnlune.search.graphql.AggregationInput
import io.github.yearnlune.search.graphql.CountAggregationInput
import io.github.yearnlune.search.graphql.GroupAggregationInput
import io.github.yearnlune.search.graphql.GroupByInput
import io.github.yearnlune.search.graphql.PropertyType
import io.github.yearnlune.search.graphql.SearchInput
import io.github.yearnlune.search.graphql.SearchOperatorType
import io.github.yearnlune.search.graphql.StatisticInput
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
                        SearchInput(
                            by = "name",
                            type = PropertyType.STRING,
                            value = listOf("사과"),
                            operator = SearchOperatorType.CONTAIN
                        ),
                        SearchInput(
                            by = "updated_at",
                            type = PropertyType.DATE,
                            value = listOf("1657854891000", "1659150891000"),
                            operator = SearchOperatorType.BETWEEN
                        ),
                        SearchInput(
                            by = "price",
                            type = PropertyType.DOUBLE,
                            value = listOf("0.0", "100.0"),
                            operator = SearchOperatorType.BETWEEN
                        )
                    ),
                    Product::class.java
                )

                SerializationUtils.serializeToJsonSafely(Query(criteria).queryObject) shouldBe "{ \"name\" : { \"\$regularExpression\" : { \"pattern\" : \"사과\", \"options\" : \"iu\"}}, \"updated_at\" : { \"\$gte\" : 1657854891000, \"\$lt\" : 1659150891000}, \"price\" : { \"\$gte\" : 0.0, \"\$lt\" : 100.0}}"
            }
        }
    }

    describe("statistic") {
        context("단일 aggregate를 할 경우") {
            it("aggregation query를 반환한다.") {
                val equal = SearchInput(
                    by = "name",
                    type = PropertyType.STRING,
                    value = listOf("사과", "바나나", "세제"),
                    operator = SearchOperatorType.EQUAL
                )
                val between = SearchInput(
                    by = "updated_at",
                    type = PropertyType.DATE,
                    value = listOf("1657854891000", "1659150891000"),
                    operator = SearchOperatorType.BETWEEN
                )
                val countAggregation = CountAggregationInput(alias = "total")

                SerializationUtils.serializeToJsonSafely(
                    MongoSearch.statistic(
                        StatisticInput(searches = listOf(equal, between), aggregates = listOf(countAggregation)),
                        Product::class.java
                    ).toPipeline(Aggregation.DEFAULT_CONTEXT)
                ) shouldBe "[{ \"\$match\" : { \"name\" : { \"\$in\" : [\"사과\", \"바나나\", \"세제\"]}, \"updated_at\" : { \"\$gte\" : 1657854891000, \"\$lt\" : 1659150891000}}}, { \"\$count\" : \"total\"}]"
            }
        }

        context("복수의 aggregate를 할 경우") {
            it("aggregation query를 반환한다.") {
                val equal = SearchInput(
                    by = "name",
                    type = PropertyType.STRING,
                    value = listOf("사과", "바나나", "세제"),
                    operator = SearchOperatorType.EQUAL
                )
                val between = SearchInput(
                    by = "updated_at",
                    type = PropertyType.DATE,
                    value = listOf("1657854891000", "1659150891000"),
                    operator = SearchOperatorType.BETWEEN
                )
                val groupAggregation = GroupAggregationInput(
                    by = listOf(GroupByInput(key = "category")),
                    aggregations = listOf(
                        AggregationInput(operator = AggregationAccumulatorOperatorType.COUNT),
                        AggregationInput(property = "price", operator = AggregationAccumulatorOperatorType.AVERAGE)
                    )
                )

                SerializationUtils.serializeToJsonSafely(
                    MongoSearch.statistic(
                        StatisticInput(searches = listOf(equal, between), aggregates = listOf(groupAggregation)),
                        Product::class.java
                    ).toPipeline(Aggregation.DEFAULT_CONTEXT)
                ) shouldBe "[{ \"\$match\" : { \"name\" : { \"\$in\" : [\"사과\", \"바나나\", \"세제\"]}, \"updated_at\" : { \"\$gte\" : 1657854891000, \"\$lt\" : 1659150891000}}}, { \"\$group\" : { \"_id\" : \"\$category\", \"count\" : { \"\$sum\" : 1}, \"price_avg\" : { \"\$avg\" : \"\$price\"}}}]"
            }
        }
    }
})