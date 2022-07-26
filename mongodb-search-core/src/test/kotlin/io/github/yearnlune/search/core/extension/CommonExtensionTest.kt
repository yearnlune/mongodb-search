package io.github.yearnlune.search.core.extension

import io.github.yearnlune.search.core.domain.Product
import io.github.yearnlune.search.graphql.PropertyType
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.bson.types.ObjectId

class CommonExtensionTest : DescribeSpec({

    describe("getAllFields") {
        context("객체의 필드 목록을 가져올 때") {
            it("상속받은 객체의 필드 또한 가져온다.") {
                Product::class.java.getAllFields().map { it.name } shouldBe mutableListOf(
                    "name",
                    "category",
                    "price",
                    "stockQuantity",
                    "id",
                    "updatedAt",
                    "deleted"
                )
            }
        }
    }

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
})