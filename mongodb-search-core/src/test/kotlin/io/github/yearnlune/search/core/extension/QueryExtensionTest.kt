package io.github.yearnlune.search.core.extension

import io.github.yearnlune.search.core.MongoSearch
import io.github.yearnlune.search.core.domain.Product
import io.github.yearnlune.search.core.exception.ValidationException
import io.github.yearnlune.search.core.operator.SearchOperatorDelegator
import io.github.yearnlune.search.graphql.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.SerializationUtils

class QueryExtensionTest : DescribeSpec({

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

    describe("aggregate") {
        context("group") {
            context("count operator") {
                val searchInput: SearchInput = SearchInput.builder()
                    .withBy("name")
                    .withType(PropertyType.STRING)
                    .withValue(listOf("사과", "바나나", "세제"))
                    .withOperator(SearchOperatorType.EQUAL).build()

                context("그룹 별 개수를 구할 때") {
                    it("\$group에서 \$count를 사용한다.") {
                        val aggregates = SearchOperatorDelegator()
                            .create(searchInput, Product::class.java)
                            .buildAggregation()
                        val groupAggregation = GroupAggregationInput.builder()
                            .withBy(listOf("category"))
                            .withAggregations(
                                listOf(
                                    AggregationInput.builder()
                                        .withOperator(AggregateOperatorType.COUNT)
                                        .build()
                                )
                            )
                            .build()
                        SerializationUtils.serializeToJsonSafely(
                            aggregates.aggregate(listOf(groupAggregation), Product::class.java)
                                .toPipeline(Aggregation.DEFAULT_CONTEXT)
                        ) shouldBe "[{ \"\$match\" : { \"name\" : { \"\$in\" : [\"사과\", \"바나나\", \"세제\"]}}}, " +
                                "{ \"\$group\" : { \"_id\" : \"\$category\", \"count\" : { \"\$sum\" : 1}}}]"
                    }
                }
            }

            context("sum operator") {
                val searchInput: SearchInput = SearchInput.builder()
                    .withBy("price")
                    .withType(PropertyType.DOUBLE)
                    .withValue(listOf("0.0", "100.0"))
                    .withOperator(SearchOperatorType.BETWEEN).build()

                context("특정 필드의 그룹 별 총합을 구할 때") {
                    it("\$group에서 \$sum을 사용한다.") {
                        val aggregates = SearchOperatorDelegator()
                            .create(searchInput, Product::class.java)
                            .buildAggregation()

                        val groupAggregation = GroupAggregationInput.builder()
                            .withBy(listOf("category"))
                            .withAggregations(
                                listOf(
                                    AggregationInput.builder()
                                        .withProperty("stockQuantity")
                                        .withOperator(AggregateOperatorType.SUM)
                                        .build()
                                )
                            )
                            .build()
                        SerializationUtils.serializeToJsonSafely(
                            aggregates.aggregate(
                                listOf(groupAggregation),
                                Product::class.java
                            )
                                .toPipeline(Aggregation.DEFAULT_CONTEXT)
                        ) shouldBe "[{ \"\$match\" : { \"price\" : { \"\$gte\" : 0.0, \"\$lt\" : 100.0}}}, " +
                                "{ \"\$group\" : { \"_id\" : \"\$category\", \"stock_quantity_sum\" : { \"\$sum\" : \"\$stock_quantity\"}}}]"
                    }

                    context("올바른 값이 아닐 때") {
                        context("필드 지정을 하지 않았을 때") {
                            it("IllegalArgumentException을 반환한다.") {
                                val aggregates = SearchOperatorDelegator()
                                    .create(searchInput, Product::class.java)
                                    .buildAggregation()

                                val groupAggregation = GroupAggregationInput.builder()
                                    .withBy(listOf("category"))
                                    .withAggregations(
                                        listOf(
                                            AggregationInput.builder()
                                                .withOperator(AggregateOperatorType.SUM)
                                                .build()
                                        )
                                    )
                                    .build()

                                shouldThrow<IllegalArgumentException> {
                                    aggregates.aggregate(
                                        listOf(groupAggregation),
                                        Product::class.java
                                    )
                                }
                            }
                        }

                        context("필드가 빈 값 일 때") {
                            it("ValidationException을 반환한다.") {
                                val aggregates = SearchOperatorDelegator()
                                    .create(searchInput, Product::class.java)
                                    .buildAggregation()

                                val groupAggregation = GroupAggregationInput.builder()
                                    .withBy(listOf("category"))
                                    .withAggregations(
                                        listOf(
                                            AggregationInput.builder()
                                                .withProperty("")
                                                .withOperator(AggregateOperatorType.SUM)
                                                .build()
                                        )
                                    )
                                    .build()

                                shouldThrow<ValidationException> {
                                    aggregates.aggregate(
                                        listOf(groupAggregation),
                                        Product::class.java
                                    )
                                }
                            }
                        }
                    }
                }
            }

            context("average operator") {
                val searchInput: SearchInput = SearchInput.builder()
                    .withBy("updated_at")
                    .withType(PropertyType.DATE)
                    .withValue(listOf("1657854891000", "1659150891000"))
                    .withOperator(SearchOperatorType.BETWEEN).build()

                context("그룹 별 평균을 구할 때") {
                    it("\$group에서 \$avg를 사용한다.") {
                        val aggregates = SearchOperatorDelegator()
                            .create(searchInput, Product::class.java)
                            .buildAggregation()

                        val groupAggregation = GroupAggregationInput.builder()
                            .withBy(listOf("category"))
                            .withAggregations(
                                listOf(
                                    AggregationInput.builder()
                                        .withProperty("price")
                                        .withOperator(AggregateOperatorType.AVERAGE)
                                        .build()
                                )
                            )
                            .build()
                        SerializationUtils.serializeToJsonSafely(
                            aggregates.aggregate(
                                listOf(groupAggregation),
                                Product::class.java
                            )
                                .toPipeline(Aggregation.DEFAULT_CONTEXT)
                        ) shouldBe "[{ \"\$match\" : { \"updated_at\" : { \"\$gte\" : 1657854891000, \"\$lt\" : 1659150891000}}}, " +
                                "{ \"\$group\" : { \"_id\" : \"\$category\", \"price_avg\" : { \"\$avg\" : \"\$price\"}}}]"
                    }
                }
            }

            context("올바른 값이 아닐 때") {
                it ("ValidationException를 반환한다.") {
                    val searchInput: SearchInput = SearchInput.builder()
                        .withBy("updated_at")
                        .withType(PropertyType.DATE)
                        .withValue(listOf("1657854891000", "1659150891000"))
                        .withOperator(SearchOperatorType.BETWEEN).build()
                    val aggregates = SearchOperatorDelegator()
                        .create(searchInput, Product::class.java)
                        .buildAggregation()
                    val groupAggregation = GroupAggregationInput.builder()
                        .withBy(listOf())
                        .withAggregations(
                            listOf(
                                AggregationInput.builder()
                                    .withProperty("price")
                                    .withOperator(AggregateOperatorType.AVERAGE)
                                    .build()
                            )
                        )
                        .build()


                    shouldThrow<ValidationException> {
                        aggregates.aggregate(
                            listOf(groupAggregation),
                            Product::class.java
                        )
                    }
                }
            }
        }

        context("count") {
            context("전체 개수를 구할 때") {
                it("\$count 를 사용한다.") {
                    val searchInput: SearchInput = SearchInput.builder()
                        .withBy("name")
                        .withType(PropertyType.STRING)
                        .withValue(listOf("사과", "바나나", "세제"))
                        .withOperator(SearchOperatorType.EQUAL).build()
                    val countAggregation = CountAggregationInput.builder()
                        .withAlias("total")
                        .build()

                    SerializationUtils.serializeToJsonSafely(
                        MongoSearch.statistic(
                            StatisticInput.builder()
                                .withSearches(listOf(searchInput))
                                .withAggregates(listOf(countAggregation))
                                .build(),
                            Product::class.java
                        ).toPipeline(Aggregation.DEFAULT_CONTEXT)
                    ) shouldBe "[{ \"\$match\" : { \"name\" : { \"\$in\" : [\"사과\", \"바나나\", \"세제\"]}}}, { \"\$count\" : \"total\"}]"
                }

            }
        }
    }
})