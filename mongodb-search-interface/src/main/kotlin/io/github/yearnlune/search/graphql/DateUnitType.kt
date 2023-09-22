package io.github.yearnlune.search.graphql

enum class DateUnitType {
    DAYS,
    WEEKS,
    MONTHS,
    YEARS;

    companion object : EnumCompanion<DateUnitType, String>(
        DateUnitType.values().associateBy(DateUnitType::name)
    )
}