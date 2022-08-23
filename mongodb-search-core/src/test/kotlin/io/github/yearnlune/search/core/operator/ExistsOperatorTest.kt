package io.github.yearnlune.search.core.operator

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.SerializationUtils

class ExistsOperatorTest : DescribeSpec({

    describe("buildQuery") {
        val searchBy = "relatedId"
        val values = listOf(true)

        context("필드의 존재 여부로 검색할 때") {
            it("필드에 대한 \$exists query를 반환한다.") {
                val equalOperator = ExistsOperator(searchBy, values).buildQuery()
                val expectedQuery =
                    "{ \"$searchBy\" : { \"\$exists\" : ${values.first()}}}"

                SerializationUtils.serializeToJsonSafely(Query(equalOperator).queryObject) shouldBe expectedQuery
            }
        }
    }
})