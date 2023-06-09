package io.github.yearnlune.search.graphql

enum class DateRangeType {
    LAST,
    NEXT;

    companion object : EnumCompanion<DateRangeType, String>(
        DateRangeType.values().associateBy(DateRangeType::name)
    )
}