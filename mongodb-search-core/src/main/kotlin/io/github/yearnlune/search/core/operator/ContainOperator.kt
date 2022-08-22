package io.github.yearnlune.search.core.operator

import io.github.yearnlune.search.core.extension.escapeSpecialRegexChars
import org.springframework.data.mongodb.core.query.Criteria
import java.util.regex.Pattern

class ContainOperator(
    searchBy: String,
    values: List<Any>
) : SearchOperator(searchBy, values) {

    override fun appendExpression(criteria: Criteria): Criteria = criteria.regex(
        Pattern.compile(
            values.joinToString("|") { (it as String).escapeSpecialRegexChars() },
            Pattern.CASE_INSENSITIVE or Pattern.UNICODE_CASE
        )
    )
}