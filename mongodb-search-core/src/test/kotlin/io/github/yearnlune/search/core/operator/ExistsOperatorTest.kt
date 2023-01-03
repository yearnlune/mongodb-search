package io.github.yearnlune.search.core.operator

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.SerializationUtils

class ExistsOperatorTest : DescribeSpec({
    val searchBy = "relatedId"
    val values = listOf(true)

    describe("buildQuery") {
        context("필드의 존재 여부로 검색할 때") {
            it("필드에 대한 \$exists query를 반환한다.") {
                val existsQuery = ExistsOperator(searchBy, values).buildQuery()
                val expectedQuery = "{ \"$searchBy\" : { \"\$exists\" : ${values.first()}}}"

                SerializationUtils.serializeToJsonSafely(Query(existsQuery).queryObject) shouldBe expectedQuery
            }
        }
    }

    describe("buildExpression") {
        context("필드의 존재하는 경우로 검색할 때") {
            it("필드에 대한 \$not의 \$not query를 반환한다.") {
                val existsExpression = ExistsOperator(searchBy, values).buildExpression()
                val expectedQuery = "{ \"\$not\" : [{ \"\$not\" : [\"\$$searchBy\"]}]}"

                existsExpression.buildTestAggregation().toString() shouldContain expectedQuery
            }
        }

        context("필드가 존재하지 않는 경우로 검색할 때") {
            it("필드에 대한 \$not query를 반환한다.") {
                val notExistsExpression = ExistsOperator(searchBy, listOf(false)).buildExpression()
                val expectedQuery = "{ \"\$not\" : [\"\$$searchBy\"]}"

                notExistsExpression.buildTestAggregation().toString() shouldContain expectedQuery
            }
        }
    }
})