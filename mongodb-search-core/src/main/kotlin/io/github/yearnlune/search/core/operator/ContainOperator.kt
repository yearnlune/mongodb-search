package io.github.yearnlune.search.core.operator

import io.github.yearnlune.search.core.exception.NotSupportedExpressionException
import io.github.yearnlune.search.core.extension.escapeSpecialRegexChars
import io.github.yearnlune.search.graphql.ComparisonOperatorType
import org.springframework.data.mongodb.core.aggregation.AggregationExpression
import org.springframework.data.mongodb.core.aggregation.StringOperators
import org.springframework.data.mongodb.core.query.Criteria
import java.util.regex.Pattern

class ContainOperator(
    searchBy: String,
    values: List<Any>
) : SearchOperator(searchBy, values) {

    override fun appendExpression(criteria: Criteria): Criteria = criteria.regex(
        Pattern.compile(
            convertRegex(),
            Pattern.CASE_INSENSITIVE or Pattern.UNICODE_CASE
        )
    )

    override fun buildExpression(operatorType: Any?): AggregationExpression {
        return when (operatorType) {
            ComparisonOperatorType.REGEX_MATCH ->
                StringOperators.RegexMatch
                    .valueOf(searchBy)
                    .regex(convertRegex())
                    .options("ui")
            else -> throw NotSupportedExpressionException("")
        }
    }

    private fun convertRegex(): String {
        return values.joinToString("|") { (it as String).escapeSpecialRegexChars() }
    }
}