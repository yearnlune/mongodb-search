package io.github.yearnlune.search.graphql

enum class ComparisonOperatorType {
    EQUAL,
    NOT_EQUAL,
    REGEX_MATCH;

    companion object :
        EnumCompanion<ComparisonOperatorType, String>(
            ComparisonOperatorType.values().associateBy(ComparisonOperatorType::name)
        )
}