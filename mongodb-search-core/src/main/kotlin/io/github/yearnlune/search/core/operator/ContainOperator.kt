package io.github.yearnlune.search.core.operator

import io.github.yearnlune.search.core.extension.escapeSpecialRegexChars
import org.springframework.data.mongodb.core.query.Criteria
import java.util.regex.Pattern

class ContainOperator(
    override val searchBy: String,
    override val values: List<Any>
) : SearchOperator(searchBy, values) {

    override fun buildQuery(): Criteria =
        Criteria.where(searchBy).regex(Pattern.compile((values.first() as String).escapeSpecialRegexChars(), Pattern.CASE_INSENSITIVE or Pattern.UNICODE_CASE))
}
