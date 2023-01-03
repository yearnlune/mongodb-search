package io.github.yearnlune.search.core.extension

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.yearnlune.search.core.exception.NotSupportedOperatorException
import io.github.yearnlune.search.graphql.CountAggregationInput
import io.github.yearnlune.search.graphql.GroupAggregationInput
import io.github.yearnlune.search.graphql.GroupByInput
import io.github.yearnlune.search.graphql.PropertyType
import io.github.yearnlune.search.graphql.StatisticInput
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import org.bson.types.ObjectId

class CommonExtensionTest : DescribeSpec({

    describe("snakeCase") {
        context("camelcase를 변환할 때") {
            it("snakecase로 변환한다.") {
                "camelCase".snakeCase() shouldBe "camel_case"
            }
        }

        context("pascalCase를 변환할 때") {
            it("snakecase로 변환한다.") {
                "PascalCase".snakeCase() shouldBe "pascal_case"
            }
        }
    }

    describe("toMongoType") {
        context("PropertyType.INTEGER") {
            context("값이 integer 일 때") {
                it("long 타입을 반환한다.") {
                    "1586500".toMongoType(PropertyType.INTEGER) shouldBe 1586500
                }
            }

            context("올바른 값이 아닐 때") {
                it("NumberFormatException을 반환한다.") {
                    shouldThrow<NumberFormatException> {
                        "IllegalArgument".toMongoType(PropertyType.INTEGER)
                    }
                }
            }
        }

        context("PropertyType.LONG") {
            context("값이 long 일 때") {
                it("long 타입을 반환한다.") {
                    "1586500174000".toMongoType(PropertyType.LONG) shouldBe 1586500174000L
                }
            }

            context("올바른 값이 아닐 때") {
                it("NumberFormatException을 반환한다.") {
                    shouldThrow<NumberFormatException> {
                        "IllegalArgument".toMongoType(PropertyType.LONG)
                    }
                }
            }
        }

        context("PropertyType.BOOLEAN") {
            context("값이 boolean 일 때") {
                it("boolean 타입을 반환한다.") {
                    "true".toMongoType(PropertyType.BOOLEAN) shouldBe true
                }
            }

            context("올바른 값이 아닐 때") {
                it("false를 반환한다.") {
                    "IllegalArgument".toMongoType(PropertyType.BOOLEAN) shouldBe false
                }
            }
        }

        context("PropertyType.OBJECT_ID") {
            context("값이 올바른 hexString 일 때") {
                it("hexString 값을 가진 ObjectId를 반환한다.") {
                    "5e90124f851ff61878689d5e".toMongoType(PropertyType.OBJECT_ID) shouldBe ObjectId("5e90124f851ff61878689d5e")
                }
            }

            context("값이 long 일 때") {
                it("long 값을 통해 만들어진 ObjectId를 반환한다.") {
                    "1586500174000".toMongoType(PropertyType.OBJECT_ID) shouldBe ObjectId("5e90124e0000000000000000")
                }
            }

            context("올바른 값이 아닐 때") {
                it("IllegalArgumentException을 반환한다.") {
                    shouldThrow<IllegalArgumentException> {
                        "IllegalArgument".toMongoType(PropertyType.OBJECT_ID)
                    }
                }
            }
        }
    }

    describe("toAggregationInput") {
        context("Map의 데이터에 따라") {
            it("GroupAggregationInput으로 반환한다.") {
                val map = jacksonObjectMapper().convertValue(
                    GroupAggregationInput.builder().withBy(listOf(GroupByInput.builder().build())).build(),
                    Map::class.java
                )
                map.toAggregationInput().shouldBeTypeOf<GroupAggregationInput>()
            }

            it("CountAggregationInput으로 반환한다.") {
                val map = jacksonObjectMapper().convertValue(
                    CountAggregationInput.builder().build(),
                    Map::class.java
                )
                map.toAggregationInput().shouldBeTypeOf<CountAggregationInput>()
            }

            it("NotSupportedOperatorException 예외를 반환한다.") {
                val map = jacksonObjectMapper().convertValue(
                    StatisticInput.builder().build(),
                    Map::class.java
                )

                shouldThrow<NotSupportedOperatorException> { map.toAggregationInput() }
            }
        }
    }
})