package io.github.yearnlune.search.graphql

enum class SearchOperatorType {
    EQUAL,
    NOT_EQUAL,
    CONTAIN,
    START_WITH,
    GREATER_THAN_EQUAL,
    GREATER_THAN,
    LESS_THAN,
    LESS_THAN_EQUAL,
    BETWEEN,
    EXISTS,
    DATE_RANGE;

    companion object : EnumCompanion<SearchOperatorType, String>(
        SearchOperatorType.values().associateBy(SearchOperatorType::name)
    )
}