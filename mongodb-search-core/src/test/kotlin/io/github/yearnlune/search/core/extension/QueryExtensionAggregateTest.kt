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
                            .withBy(
                                listOf(
                                    GroupByInput.Builder()
                                        .withKey("category")
                                        .build()
                                )
                            )
                            .withAggregations(
                                listOf(
                                    AggregationInput.builder()
                                        .withOperator(AggregationAccumulatorOperatorType.COUNT)
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
                            .withBy(
                                listOf(
                                    GroupByInput.Builder()
                                        .withKey("category")
                                        .build()
                                )
                            )
                            .withAggregations(
                                listOf(
                                    AggregationInput.builder()
                                        .withProperty("stockQuantity")
                                        .withOperator(AggregationAccumulatorOperatorType.SUM)
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
                            it("ValidationException을 반환한다.") {
                                val aggregates = SearchOperatorDelegator()
                                    .create(searchInput, Product::class.java)
                                    .buildAggregation()

                                val groupAggregation = GroupAggregationInput.builder()
                                    .withBy(
                                        listOf(
                                            GroupByInput.Builder()
                                                .withKey("category")
                                                .build()
                                        )
                                    )
                                    .withAggregations(
                                        listOf(
                                            AggregationInput.builder()
                                                .withOperator(AggregationAccumulatorOperatorType.SUM)
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

                        context("필드가 빈 값 일 때") {
                            it("ValidationException을 반환한다.") {
                                val aggregates = SearchOperatorDelegator()
                                    .create(searchInput, Product::class.java)
                                    .buildAggregation()

                                val groupAggregation = GroupAggregationInput.builder()
                                    .withBy(
                                        listOf(
                                            GroupByInput.Builder()
                                                .withKey("category")
                                                .build()
                                        )
                                    )
                                    .withAggregations(
                                        listOf(
                                            AggregationInput.builder()
                                                .withProperty("")
                                                .withOperator(AggregationAccumulatorOperatorType.SUM)
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

                        val groupAggregation = GroupAggregationInput.builder()
                            .withBy(
                                listOf(
                                    GroupByInput.Builder()
                                        .withKey("updatedAt")
                                        .withOption(GroupByOptionType.MONTHLY)
                                        .build()
                                )
                            )
                            .withAggregations(
                                listOf(
                                    AggregationInput.builder()
                                        .withPropertyExpression(
                                            ConditionInput.builder()
                                                .withIf(
                                                    SearchInput.builder()
                                                        .withBy("category")
                                                        .withOperator(SearchOperatorType.EQUAL)
                                                        .withType(PropertyType.STRING)
                                                        .withValue(listOf("fruit"))
                                                        .build()
                                                )
                                                .withThen(
                                                    DataInput.builder()
                                                        .withType(PropertyType.LONG)
                                                        .withValue("1")
                                                        .build()
                                                )
                                                .build()
                                        )
                                        .withOperator(AggregationAccumulatorOperatorType.SUM)
                                        .withAlias("과일종류")
                                        .build()
                                )
                            )
                            .build()
                        SerializationUtils.serializeToJsonSafely(
                            aggregates.aggregate(listOf(groupAggregation), Product::class.java)
                                .toPipeline(Aggregation.DEFAULT_CONTEXT)
                        ) shouldBe expectedQuery
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
                            .withBy(
                                listOf(
                                    GroupByInput.Builder()
                                        .withKey("category")
                                        .build()
                                )
                            )
                            .withAggregations(
                                listOf(
                                    AggregationInput.builder()
                                        .withProperty("price")
                                        .withOperator(AggregationAccumulatorOperatorType.AVERAGE)
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

            context("기간별 그룹을 낼 경우") {
                val start = "1595731371000"
                val end = "1658803371000"
                val searchInput: SearchInput = SearchInput.builder()
                    .withBy("updated_at")
                    .withType(PropertyType.DATE)
                    .withValue(listOf(start, end))
                    .withOperator(SearchOperatorType.BETWEEN).build()

                context("일별") {
                    it("일별 기간을 추가하고 이를 활용하여 그룹화 하는 쿼리를 반환한다.") {
                        val expectedQuery = "{ \"\$addFields\" : { \"updated_at_0\" : { \"\$dateToString\" : " +
                                "{ \"format\" : \"%Y%m%d\", \"date\" : { \"\$convert\" : { \"input\" : { \"\$add\" : " +
                                "[{ \"\$convert\" : { \"input\" : { \"\$convert\" : { \"input\" : \"\$updated_at\", " +
                                "\"to\" : \"date\"}}, \"to\" : \"long\"}}, 0]}, \"to\" : \"date\"}}}}}}"
                        val aggregates = SearchOperatorDelegator()
                            .create(searchInput, Product::class.java)
                            .buildAggregation()
                        val groupAggregation = GroupAggregationInput.builder()
                            .withBy(
                                listOf(
                                    GroupByInput.Builder()
                                        .withKey("updatedAt")
                                        .withOption(GroupByOptionType.DAILY)
                                        .build()
                                )
                            )
                            .withAggregations(
                                listOf(
                                    AggregationInput.builder()
                                        .withProperty("stockQuantity")
                                        .withOperator(AggregationAccumulatorOperatorType.SUM)
                                        .build()
                                )
                            )
                            .build()
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
                        val groupAggregation = GroupAggregationInput.builder()
                            .withBy(
                                listOf(
                                    GroupByInput.Builder()
                                        .withKey("updatedAt")
                                        .withOption(GroupByOptionType.WEEKLY)
                                        .build()
                                )
                            )
                            .withAggregations(
                                listOf(
                                    AggregationInput.builder()
                                        .withProperty("stockQuantity")
                                        .withOperator(AggregationAccumulatorOperatorType.SUM)
                                        .build()
                                )
                            )
                            .build()
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
                        val groupAggregation = GroupAggregationInput.builder()
                            .withBy(
                                listOf(
                                    GroupByInput.Builder()
                                        .withKey("updatedAt")
                                        .withOption(GroupByOptionType.MONTHLY)
                                        .build()
                                )
                            )
                            .withAggregations(
                                listOf(
                                    AggregationInput.builder()
                                        .withProperty("stockQuantity")
                                        .withOperator(AggregationAccumulatorOperatorType.SUM)
                                        .build()
                                )
                            )
                            .build()
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
                        val groupAggregation = GroupAggregationInput.builder()
                            .withBy(
                                listOf(
                                    GroupByInput.Builder()
                                        .withKey("updatedAt")
                                        .withOption(GroupByOptionType.YEARLY)
                                        .build()
                                )
                            )
                            .withAggregations(
                                listOf(AggregationInput.builder()
                                        .withProperty("stockQuantity")
                                        .withOperator(AggregationAccumulatorOperatorType.SUM)
                                        .build()
                                )
                            )
                            .build()
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
                    val groupAggregation = GroupAggregationInput.builder()
                        .withBy(
                            listOf(
                                GroupByInput.Builder()
                                    .withKey("updatedAt")
                                    .withOptions(
                                        GroupByOptionInput.Builder()
                                            .withType(GroupByOptionType.DAILY)
                                            .withTimezone(9)
                                            .build()
                                    )
                                    .build()
                            )
                        )
                        .withAggregations(
                            listOf(
                                AggregationInput.builder()
                                    .withProperty("stockQuantity")
                                    .withOperator(AggregationAccumulatorOperatorType.SUM)
                                    .build()
                            )
                        )
                        .build()
                    SerializationUtils.serializeToJsonSafely(
                        aggregates.aggregate(listOf(groupAggregation), Product::class.java)
                            .toPipeline(Aggregation.DEFAULT_CONTEXT)
                    ) shouldContain expectedQuery
                }
            }

            context("올바른 값이 아닐 때") {
                it("ValidationException를 반환한다.") {
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
                                    .withOperator(AggregationAccumulatorOperatorType.AVERAGE)
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

            context("필드의 존재여부로 나눌 경우") {
                it("\$not을 활용하여 필드를 추가하여 쿼리 제공한다.") {
                    val aggregates = SearchOperatorDelegator().create(
                        SearchInput.builder()
                            .withBy("name")
                            .withType(PropertyType.STRING)
                            .withValue(listOf("사과", "바나나", "세제"))
                            .withOperator(SearchOperatorType.EQUAL)
                            .build(),
                        Product::class.java
                    ).buildAggregation()
                    val groupAggregation = GroupAggregationInput.builder()
                        .withBy(
                            listOf(
                                GroupByInput.Builder()
                                    .withKey("deleted")
                                    .withOption(GroupByOptionType.EXISTS)
                                    .build()
                            )
                        )
                        .withAggregations(
                            listOf(
                                AggregationInput.builder()
                                    .withOperator(AggregationAccumulatorOperatorType.COUNT)
                                    .build()
                            )
                        )
                        .build()
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

        context("limit") {
            context("상위 데이터를 가져올 때") {
                it("\$limit 를 사용한다.") {
                    val searchInput: SearchInput = SearchInput.builder()
                        .withBy("name")
                        .withType(PropertyType.STRING)
                        .withValue(listOf("사과", "바나나", "세제"))
                        .withOperator(SearchOperatorType.EQUAL).build()
                    val limitAggregation = LimitAggregationInput.builder()
                        .withMaxElements(5L)
                        .build()

                    SerializationUtils.serializeToJsonSafely(
                        MongoSearch.statistic(
                            StatisticInput.builder()
                                .withSearches(listOf(searchInput))
                                .withAggregates(listOf(limitAggregation))
                                .build(),
                            Product::class.java
                        ).toPipeline(Aggregation.DEFAULT_CONTEXT)
                    ) shouldBe "[{ \"\$match\" : { \"name\" : { \"\$in\" : [\"사과\", \"바나나\", \"세제\"]}}}, { \"\$limit\" : 5}]"
                }
            }
        }

        context("sort") {
            it("하나를 정렬할 때 \$sort 를 사용한다") {
                val searchInput: SearchInput = SearchInput.builder()
                    .withBy("name")
                    .withType(PropertyType.STRING)
                    .withValue(listOf("사과", "바나나", "세제"))
                    .withOperator(SearchOperatorType.EQUAL).build()
                val sortAggregation = SortAggregationInput.builder()
                    .withSorts(
                        listOf(
                            SortInput.builder()
                                .withProperty("name")
                                .withIsDescending(false)
                                .build()
                        )
                    )
                    .build()

                SerializationUtils.serializeToJsonSafely(
                    MongoSearch.statistic(
                        StatisticInput.builder()
                            .withSearches(listOf(searchInput))
                            .withAggregates(listOf(sortAggregation))
                            .build(),
                        Product::class.java
                    ).toPipeline(Aggregation.DEFAULT_CONTEXT)
                ) shouldBe "[{ \"\$match\" : { \"name\" : { \"\$in\" : [\"사과\", \"바나나\", \"세제\"]}}}, { \"\$sort\" : { \"name\" : 1}}]"
            }

            it("두개 이상을 정렬할 때 \$sort 를 사용한다") {
                val searchInput: SearchInput = SearchInput.builder()
                    .withBy("name")
                    .withType(PropertyType.STRING)
                    .withValue(listOf("사과", "바나나", "세제"))
                    .withOperator(SearchOperatorType.EQUAL).build()
                val sortAggregation = SortAggregationInput.builder()
                    .withSorts(
                        listOf(
                            SortInput.builder()
                                .withProperty("name")
                                .withIsDescending(false)
                                .build(),
                            SortInput.builder()
                                .withProperty("price")
                                .withIsDescending(true)
                                .build()
                        )
                    )
                    .build()

                SerializationUtils.serializeToJsonSafely(
                    MongoSearch.statistic(
                        StatisticInput.builder()
                            .withSearches(listOf(searchInput))
                            .withAggregates(listOf(sortAggregation))
                            .build(),
                        Product::class.java
                    ).toPipeline(Aggregation.DEFAULT_CONTEXT)
                ) shouldBe "[{ \"\$match\" : { \"name\" : { \"\$in\" : [\"사과\", \"바나나\", \"세제\"]}}}, { \"\$sort\" : { \"name\" : 1, \"price\" : -1}}]"
            }
        }

        context("unwind") {
            it("d ") {
                val searchInput: SearchInput = SearchInput.builder()
                    .withBy("name")
                    .withType(PropertyType.STRING)
                    .withValue(listOf("사과", "바나나", "세제"))
                    .withOperator(SearchOperatorType.EQUAL).build()
                val unwindAggregation = UnwindAggregationInput.builder()
                    .withBy("tags")
                    .build()

                SerializationUtils.serializeToJsonSafely(
                    MongoSearch.statistic(
                        StatisticInput.builder()
                            .withSearches(listOf(searchInput))
                            .withAggregates(listOf(unwindAggregation))
                            .build(),
                        Product::class.java
                    ).toPipeline(Aggregation.DEFAULT_CONTEXT)
                ) shouldBe "[{ \"\$match\" : { \"name\" : { \"\$in\" : [\"사과\", \"바나나\", \"세제\"]}}}, { \"\$unwind\" : \"\$tags\"}]"
            }
        }
    }
})