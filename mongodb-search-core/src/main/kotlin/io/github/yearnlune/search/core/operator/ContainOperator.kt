package io.github.yearnlune.search.core.operator

import io.github.yearnlune.search.core.extension.escapeSpecialRegexChars
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

    override fun buildExpression(): AggregationExpression {
        return StringOperators.RegexMatch
            .valueOf(searchBy)
            .regex(convertRegex())
            .options("i")
    }

    private fun convertRegex(): String {
        return values.joinToString("|") { (it as String).escapeSpecialRegexChars() }
    }
}