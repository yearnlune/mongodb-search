package io.github.yearnlune.search.core.extension

import io.github.yearnlune.search.core.MongoSearch
import io.github.yearnlune.search.core.domain.Product
import io.github.yearnlune.search.core.exception.ValidationException
import io.github.yearnlune.search.core.operator.SearchOperatorDelegator
import io.github.yearnlune.search.graphql.AggregationAccumulatorOperatorType
import io.github.yearnlune.search.graphql.AggregationInput
import io.github.yearnlune.search.graphql.ConditionInput
import io.github.yearnlune.search.graphql.CountAggregationInput
import io.github.yearnlune.search.graphql.DataInput
import io.github.yearnlune.search.graphql.GroupAggregationInput
import io.github.yearnlune.search.graphql.GroupByInput
import io.github.yearnlune.search.graphql.GroupByOptionInput
import io.github.yearnlune.search.graphql.GroupByOptionType
import io.github.yearnlune.search.graphql.LimitAggregationInput
import io.github.yearnlune.search.graphql.PropertyType
import io.github.yearnlune.search.graphql.SearchInput
import io.github.yearnlune.search.graphql.SearchOperatorType
import io.github.yearnlune.search.graphql.SortAggregationInput
import io.github.yearnlune.search.graphql.SortInput
import io.github.yearnlune.search.graphql.StatisticInput
import io.github.yearnlune.search.graphql.UnwindAggregationInput
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.query.SerializationUtils

class QueryExtensionAggregateTest : DescribeSpec({

    describe("aggregate") {
        context("group") {
            context("count operator") {
                val searchInput: SearchInput = SearchInput(
                    by = "name",
                    type = PropertyType.STRING,
                    value = listOf("사과", "바나나", "세제"),
                    operator = SearchOperatorType.EQUAL
                )

                context("그룹 별 개수를 구할 때") {
                    it("\$group에서 \$count를 사용한다.") {
                        val aggregates = SearchOperatorDelegator()
                            .create(searchInput, Product::class.java)
                            .buildAggregation()
                        val groupAggregation = GroupAggregationInput(
                            by = listOf(GroupByInput(key = "category")),
                            aggregations = listOf(AggregationInput(operator = AggregationAccumulatorOperatorType.COUNT))
                        )

                        SerializationUtils.serializeToJsonSafely(
                            aggregates.aggregate(listOf(groupAggregation), Product::class.java)
                                .toPipeline(Aggregation.DEFAULT_CONTEXT)
                        ) shouldBe "[{ \"\$match\" : { \"name\" : { \"\$in\" : [\"사과\", \"바나나\", \"세제\"]}}}, " +
                            "{ \"\$group\" : { \"_id\" : \"\$category\", \"count\" : { \"\$sum\" : 1}}}]"
                    }
                }
            }

            context("sum operator") {
                val searchInput = SearchInput(
                    by = "price",
                    type = PropertyType.DOUBLE,
                    value = listOf("0.0", "100.0"),
                    operator = SearchOperatorType.BETWEEN
                )

                context("특정 필드의 그룹 별 총합을 구할 때") {
                    it("\$group에서 \$sum을 사용한다.") {
                        val aggregates = SearchOperatorDelegator()
                            .create(searchInput, Product::class.java)
                            .buildAggregation()
                        val groupAggregation = GroupAggregationInput(
                            by = listOf(GroupByInput(key = "category")),
                            aggregations = listOf(
                                AggregationInput(
                                    property = "stockQuantity",
                                    operator = AggregationAccumulatorOperatorType.SUM
                                )
                            )
                        )

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
                            it("ValidationException을 반환한다.") {
                                val aggregates = SearchOperatorDelegator()
                                    .create(searchInput, Product::class.java)
                                    .buildAggregation()
                                val groupAggregation = GroupAggregationInput(
                                    by = listOf(
                                        GroupByInput(key = "category")
                                    ),
                                    aggregations = listOf(
                                        AggregationInput(operator = AggregationAccumulatorOperatorType.SUM)
                                    )
                                )

                                shouldThrow<ValidationException> {
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

                                val groupAggregation = GroupAggregationInput(
                                    by = listOf(
                                        GroupByInput(key = "category")
                                    ),
                                    aggregations = listOf(
                                        AggregationInput(operator = AggregationAccumulatorOperatorType.SUM)
                                    )
                                )

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

                context("특정 필드의 조건 그룹 별 총합을 구할 때") {
                    it("\$cond를 활용하여 조건을 적용하여 \$sum을 통해 총합을 구한다.") {
                        val expectedQuery = "[{ \"\$match\" : { \"price\" : { \"\$gte\" : 0.0, \"\$lt\" : 100.0}}}, " +
                            "{ \"\$addFields\" : { \"updated_at_2\" : { \"\$dateToString\" : " +
                            "{ \"format\" : \"%Y%m\", \"date\" : { \"\$convert\" : { \"input\" : { \"\$add\" : " +
                            "[{ \"\$convert\" : { \"input\" : { \"\$convert\" : " +
                            "{ \"input\" : \"\$updated_at\", \"to\" : \"date\"}}, " +
                            "\"to\" : \"long\"}}, 0]}, \"to\" : \"date\"}}}}}}, " +
                            "{ \"\$group\" : { \"_id\" : \"\$updated_at_2\", \"과일종류\" : " +
                            "{ \"\$sum\" : { \"\$cond\" : { \"if\" : { \"\$eq\" : " +
                            "[\"\$category\", \"fruit\"]}, \"then\" : 1, \"else\" : \"\$nullField\"}}}}}]"
                        val aggregates = SearchOperatorDelegator()
                            .create(searchInput, Product::class.java)
                            .buildAggregation()

                        val groupAggregation = GroupAggregationInput(
                            by = listOf(
                                GroupByInput(
                                    key = "updatedAt",
                                    option = GroupByOptionType.MONTHLY
                                )
                            ),
                            aggregations = listOf(
                                AggregationInput(
                                    propertyExpression = ConditionInput(
                                        `if` = SearchInput(
                                            by = "category",
                                            operator = SearchOperatorType.EQUAL,
                                            type = PropertyType.STRING,
                                            value = listOf("fruit")
                                        ),
                                        then = DataInput(type = PropertyType.LONG, value = "1")
                                    ),
                                    operator = AggregationAccumulatorOperatorType.SUM, alias = "과일종류"
                                )
                            )
                        )
                        SerializationUtils.serializeToJsonSafely(
                            aggregates.aggregate(listOf(groupAggregation), Product::class.java)
                                .toPipeline(Aggregation.DEFAULT_CONTEXT)
                        ) shouldBe expectedQuery
                    }
                }
            }

            context("average operator") {
                val searchInput: SearchInput = SearchInput(
                    by = "updated_at",
                    type = PropertyType.DATE,
                    value = listOf("1657854891000", "1659150891000"),
                    operator = SearchOperatorType.BETWEEN
                )

                context("그룹 별 평균을 구할 때") {
                    it("\$group에서 \$avg를 사용한다.") {
                        val aggregates = SearchOperatorDelegator()
                            .create(searchInput, Product::class.java)
                            .buildAggregation()
                        val groupAggregation = GroupAggregationInput(
                            by = listOf(
                                GroupByInput(key = "category")
                            ),
                            aggregations = listOf(
                                AggregationInput(
                                    property = "price",
                                    operator = AggregationAccumulatorOperatorType.AVERAGE
                                )
                            )
                        )

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

            context("max operator") {
                it("\$max") {
                    val searchInput: SearchInput = SearchInput(
                        by = "price",
                        type = PropertyType.DOUBLE,
                        value = listOf("0.0", "100.0"),
                        operator = SearchOperatorType.BETWEEN
                    )
                    val aggregates = SearchOperatorDelegator()
                        .create(searchInput, Product::class.java)
                        .buildAggregation()
                    val groupAggregation = GroupAggregationInput(
                        by = listOf(
                            GroupByInput(key = "category")
                        ),
                        aggregations = listOf(
                            AggregationInput(
                                property = "price",
                                operator = AggregationAccumulatorOperatorType.MAX
                            )
                        )
                    )

                    SerializationUtils.serializeToJsonSafely(
                        aggregates.aggregate(
                            listOf(groupAggregation),
                            Product::class.java
                        )
                            .toPipeline(Aggregation.DEFAULT_CONTEXT)
                    ) shouldBe "[{ \"\$match\" : { \"price\" : { \"\$gte\" : 0.0, \"\$lt\" : 100.0}}}, " +
                        "{ \"\$group\" : { \"_id\" : \"\$category\", \"price_max\" : { \"\$max\" : \"\$price\"}}}]"
                }
            }

            context("min operator") {
                it("\$min") {
                    val searchInput: SearchInput = SearchInput(
                        by = "price",
                        type = PropertyType.DOUBLE,
                        value = listOf("0.0", "100.0"),
                        operator = SearchOperatorType.BETWEEN
                    )
                    val aggregates = SearchOperatorDelegator()
                        .create(searchInput, Product::class.java)
                        .buildAggregation()
                    val groupAggregation = GroupAggregationInput(
                        by = listOf(
                            GroupByInput(key = "category")
                        ),
                        aggregations = listOf(
                            AggregationInput(
                                property = "price",
                                operator = AggregationAccumulatorOperatorType.MIN
                            )
                        )
                    )

                    SerializationUtils.serializeToJsonSafely(
                        aggregates.aggregate(
                            listOf(groupAggregation),
                            Product::class.java
                        )
                            .toPipeline(Aggregation.DEFAULT_CONTEXT)
                    ) shouldBe "[{ \"\$match\" : { \"price\" : { \"\$gte\" : 0.0, \"\$lt\" : 100.0}}}, " +
                        "{ \"\$group\" : { \"_id\" : \"\$category\", \"price_min\" : { \"\$min\" : \"\$price\"}}}]"
                }
            }

            context("기간별 그룹을 낼 경우") {
                val start = "1595731371000"
                val end = "1658803371000"
                val searchInput = SearchInput(
                    by = "updated_at",
                    type = PropertyType.DATE,
                    value = listOf(start, end),
                    operator = SearchOperatorType.BETWEEN
                )

                context("일별") {
                    it("일별 기간을 추가하고 이를 활용하여 그룹화 하는 쿼리를 반환한다.") {
                        val expectedQuery = "{ \"\$addFields\" : { \"updated_at_0\" : { \"\$dateToString\" : " +
                            "{ \"format\" : \"%Y%m%d\", \"date\" : { \"\$convert\" : { \"input\" : { \"\$add\" : " +
                            "[{ \"\$convert\" : { \"input\" : { \"\$convert\" : { \"input\" : \"\$updated_at\", " +
                            "\"to\" : \"date\"}}, \"to\" : \"long\"}}, 0]}, \"to\" : \"date\"}}}}}}"
                        val aggregates = SearchOperatorDelegator()
                            .create(searchInput, Product::class.java)
                            .buildAggregation()
                        val groupAggregation = GroupAggregationInput(
                            by = listOf(
                                GroupByInput(key = "updatedAt", option = GroupByOptionType.DAILY)
                            ),
                            aggregations = listOf(
                                AggregationInput(
                                    property = "stockQuantity",
                                    operator = AggregationAccumulatorOperatorType.SUM
                                )
                            )
                        )

                        SerializationUtils.serializeToJsonSafely(
                            aggregates.aggregate(listOf(groupAggregation), Product::class.java)
                                .toPipeline(Aggregation.DEFAULT_CONTEXT)
                        ) shouldContain expectedQuery
                    }
                }

                context("주별") {
                    it("주별 기간을 추가하고 이를 활용하여 그룹화 하는 쿼리를 반환한다.") {
                        val expectedQuery = "{ \"\$addFields\" : { \"updated_at_1\" : { \"\$dateToString\" : " +
                            "{ \"format\" : \"%Y%V\", \"date\" : { \"\$convert\" : { \"input\" : { \"\$add\" : " +
                            "[{ \"\$convert\" : { \"input\" : { \"\$convert\" : { \"input\" : \"\$updated_at\", " +
                            "\"to\" : \"date\"}}, \"to\" : \"long\"}}, 0]}, \"to\" : \"date\"}}}}}}"
                        val aggregates = SearchOperatorDelegator()
                            .create(searchInput, Product::class.java)
                            .buildAggregation()
                        val groupAggregation = GroupAggregationInput(
                            by = listOf(
                                GroupByInput(key = "updatedAt", option = GroupByOptionType.WEEKLY)
                            ),
                            aggregations = listOf(
                                AggregationInput(
                                    property = "stockQuantity",
                                    operator = AggregationAccumulatorOperatorType.SUM
                                )
                            )
                        )

                        SerializationUtils.serializeToJsonSafely(
                            aggregates.aggregate(listOf(groupAggregation), Product::class.java)
                                .toPipeline(Aggregation.DEFAULT_CONTEXT)
                        ) shouldContain expectedQuery
                    }
                }

                context("월별") {
                    it("월별 기간을 추가하고 이를 활용하여 그룹화 하는 쿼리를 반환한다.") {
                        val expectedQuery = "{ \"\$addFields\" : { \"updated_at_2\" : { \"\$dateToString\" : " +
                            "{ \"format\" : \"%Y%m\", \"date\" : { \"\$convert\" : { \"input\" : { \"\$add\" : " +
                            "[{ \"\$convert\" : { \"input\" : { \"\$convert\" : { \"input\" : \"\$updated_at\", " +
                            "\"to\" : \"date\"}}, \"to\" : \"long\"}}, 0]}, \"to\" : \"date\"}}}}}}"
                        val aggregates = SearchOperatorDelegator()
                            .create(searchInput, Product::class.java)
                            .buildAggregation()
                        val groupAggregation = GroupAggregationInput(
                            by = listOf(
                                GroupByInput(key = "updatedAt", option = GroupByOptionType.MONTHLY)
                            ),
                            aggregations = listOf(
                                AggregationInput(
                                    property = "stockQuantity",
                                    operator = AggregationAccumulatorOperatorType.SUM
                                )
                            )
                        )

                        SerializationUtils.serializeToJsonSafely(
                            aggregates.aggregate(listOf(groupAggregation), Product::class.java)
                                .toPipeline(Aggregation.DEFAULT_CONTEXT)
                        ) shouldContain expectedQuery
                    }
                }

                context("연도별") {
                    it("연도별 기간을 추가하고 이를 활용하여 그룹화 하는 쿼리를 반환한다.") {
                        val expectedQuery = "{ \"\$addFields\" : { \"updated_at_3\" : { \"\$dateToString\" : " +
                            "{ \"format\" : \"%Y\", \"date\" : { \"\$convert\" : { \"input\" : { \"\$add\" : " +
                            "[{ \"\$convert\" : { \"input\" : { \"\$convert\" : { \"input\" : \"\$updated_at\", " +
                            "\"to\" : \"date\"}}, \"to\" : \"long\"}}, 0]}, \"to\" : \"date\"}}}}}}"
                        val aggregates = SearchOperatorDelegator()
                            .create(searchInput, Product::class.java)
                            .buildAggregation()
                        val groupAggregation = GroupAggregationInput(
                            by = listOf(
                                GroupByInput(key = "updatedAt", option = GroupByOptionType.YEARLY)
                            ),
                            aggregations = listOf(
                                AggregationInput(
                                    property = "stockQuantity",
                                    operator = AggregationAccumulatorOperatorType.SUM
                                )
                            )
                        )

                        SerializationUtils.serializeToJsonSafely(
                            aggregates.aggregate(listOf(groupAggregation), Product::class.java)
                                .toPipeline(Aggregation.DEFAULT_CONTEXT)
                        ) shouldContain expectedQuery
                    }
                }

                it("timezone을 적용할 경우") {
                    val expectedQuery = "{ \"\$addFields\" : { \"updated_at_0\" : { \"\$dateToString\" : " +
                        "{ \"format\" : \"%Y%m%d\", \"date\" : { \"\$convert\" : { \"input\" : { \"\$add\" : " +
                        "[{ \"\$convert\" : { \"input\" : { \"\$convert\" : { \"input\" : \"\$updated_at\", " +
                        "\"to\" : \"date\"}}, \"to\" : \"long\"}}, 32400]}, \"to\" : \"date\"}}}}}}"
                    val aggregates = SearchOperatorDelegator()
                        .create(searchInput, Product::class.java)
                        .buildAggregation()
                    val groupAggregation = GroupAggregationInput(
                        by = listOf(
                            GroupByInput(
                                key = "updatedAt",
                                options = GroupByOptionInput(type = GroupByOptionType.DAILY, timezone = 9)
                            )
                        ),
                        aggregations = listOf(
                            AggregationInput(
                                property = "stockQuantity",
                                operator = AggregationAccumulatorOperatorType.SUM
                            )
                        )
                    )

                    SerializationUtils.serializeToJsonSafely(
                        aggregates.aggregate(listOf(groupAggregation), Product::class.java)
                            .toPipeline(Aggregation.DEFAULT_CONTEXT)
                    ) shouldContain expectedQuery
                }
            }

            context("올바른 값이 아닐 때") {
                it("ValidationException를 반환한다.") {
                    val searchInput = SearchInput(
                        by = "updated_at",
                        type = PropertyType.DATE,
                        value = listOf("1657854891000", "1659150891000"),
                        operator = SearchOperatorType.BETWEEN
                    )
                    val aggregates = SearchOperatorDelegator()
                        .create(searchInput, Product::class.java)
                        .buildAggregation()
                    val groupAggregation = GroupAggregationInput(
                        by = listOf(),
                        aggregations = listOf(
                            AggregationInput(property = "price", operator = AggregationAccumulatorOperatorType.AVERAGE)
                        )
                    )

                    shouldThrow<ValidationException> {
                        aggregates.aggregate(
                            listOf(groupAggregation),
                            Product::class.java
                        )
                    }
                }
            }

            context("필드의 존재여부로 나눌 경우") {
                it("\$not을 활용하여 필드를 추가하여 쿼리 제공한다.") {
                    val aggregates = SearchOperatorDelegator().create(
                        SearchInput(
                            by = "name",
                            type = PropertyType.STRING,
                            value = listOf("사과", "바나나", "세제"),
                            operator = SearchOperatorType.EQUAL
                        ),
                        Product::class.java
                    ).buildAggregation()
                    val groupAggregation = GroupAggregationInput(
                        by = listOf(GroupByInput(key = "deleted", option = GroupByOptionType.EXISTS)),
                        aggregations = listOf(AggregationInput(operator = AggregationAccumulatorOperatorType.COUNT))
                    )

                    SerializationUtils.serializeToJsonSafely(
                        aggregates.aggregate(listOf(groupAggregation), Product::class.java)
                            .toPipeline(Aggregation.DEFAULT_CONTEXT)
                    ) shouldContain "{ \"\$addFields\" : { \"deleted_4\" : { \"\$not\" : [{ \"\$not\" : [\"\$deleted\"]}]}}}, { \"\$group\" : { \"_id\" : \"\$deleted_4\", \"count\" : { \"\$sum\" : 1}}}]"
                }
            }
        }

        context("count") {
            context("전체 개수를 구할 때") {
                it("\$count 를 사용한다.") {
                    val searchInput =
                        SearchInput(
                            by = "name",
                            type = PropertyType.STRING,
                            value = listOf("사과", "바나나", "세제"),
                            operator = SearchOperatorType.EQUAL
                        )
                    val countAggregation = CountAggregationInput(alias = "total")

                    SerializationUtils.serializeToJsonSafely(
                        MongoSearch.statistic(
                            StatisticInput(searches = listOf(searchInput), aggregates = listOf(countAggregation)),
                            Product::class.java
                        ).toPipeline(Aggregation.DEFAULT_CONTEXT)
                    ) shouldBe "[{ \"\$match\" : { \"name\" : { \"\$in\" : [\"사과\", \"바나나\", \"세제\"]}}}, { \"\$count\" : \"total\"}]"
                }
            }
        }

        context("limit") {
            context("상위 데이터를 가져올 때") {
                it("\$limit 를 사용한다.") {
                    val searchInput = SearchInput(
                        by = "name",
                        type = PropertyType.STRING,
                        value = listOf("사과", "바나나", "세제"),
                        operator = SearchOperatorType.EQUAL
                    )
                    val limitAggregation = LimitAggregationInput(maxElements = 5L)

                    SerializationUtils.serializeToJsonSafely(
                        MongoSearch.statistic(
                            StatisticInput(searches = listOf(searchInput), aggregates = listOf(limitAggregation)),
                            Product::class.java
                        ).toPipeline(Aggregation.DEFAULT_CONTEXT)
                    ) shouldBe "[{ \"\$match\" : { \"name\" : { \"\$in\" : [\"사과\", \"바나나\", \"세제\"]}}}, { \"\$limit\" : 5}]"
                }
            }
        }

        context("sort") {
            it("하나를 정렬할 때 \$sort 를 사용한다") {
                val searchInput: SearchInput = SearchInput(
                    by = "name",
                    type = PropertyType.STRING,
                    value = listOf("사과", "바나나", "세제"),
                    operator = SearchOperatorType.EQUAL
                )
                val sortAggregation = SortAggregationInput(
                    sorts = listOf(
                        SortInput(property = "name", isDescending = false)
                    )
                )

                SerializationUtils.serializeToJsonSafely(
                    MongoSearch.statistic(
                        StatisticInput(searches = listOf(searchInput), aggregates = listOf(sortAggregation)),
                        Product::class.java
                    ).toPipeline(Aggregation.DEFAULT_CONTEXT)
                ) shouldBe "[{ \"\$match\" : { \"name\" : { \"\$in\" : [\"사과\", \"바나나\", \"세제\"]}}}, { \"\$sort\" : { \"name\" : 1}}]"
            }

            it("두개 이상을 정렬할 때 \$sort 를 사용한다") {
                val searchInput: SearchInput = SearchInput(
                    by = "name",
                    type = PropertyType.STRING,
                    value = listOf("사과", "바나나", "세제"),
                    operator = SearchOperatorType.EQUAL
                )
                val sortAggregation = SortAggregationInput(
                    sorts = listOf(
                        SortInput(property = "name"),
                        SortInput(property = "price", isDescending = true)
                    )
                )

                SerializationUtils.serializeToJsonSafely(
                    MongoSearch.statistic(
                        StatisticInput(searches = listOf(searchInput), aggregates = listOf(sortAggregation)),
                        Product::class.java
                    ).toPipeline(Aggregation.DEFAULT_CONTEXT)
                ) shouldBe "[{ \"\$match\" : { \"name\" : { \"\$in\" : [\"사과\", \"바나나\", \"세제\"]}}}, { \"\$sort\" : { \"name\" : 1, \"price\" : -1}}]"
            }
        }

        context("unwind") {
            it("d ") {
                val searchInput: SearchInput = SearchInput(
                    by = "name",
                    type = PropertyType.STRING,
                    value = listOf("사과", "바나나", "세제"),
                    operator = SearchOperatorType.EQUAL
                )
                val unwindAggregation = UnwindAggregationInput(by = "tags")

                SerializationUtils.serializeToJsonSafely(
                    MongoSearch.statistic(
                        StatisticInput(searches = listOf(searchInput), aggregates = listOf(unwindAggregation)),
                        Product::class.java
                    ).toPipeline(Aggregation.DEFAULT_CONTEXT)
                ) shouldBe "[{ \"\$match\" : { \"name\" : { \"\$in\" : [\"사과\", \"바나나\", \"세제\"]}}}, { \"\$unwind\" : \"\$tags\"}]"
            }
        }
    }
})