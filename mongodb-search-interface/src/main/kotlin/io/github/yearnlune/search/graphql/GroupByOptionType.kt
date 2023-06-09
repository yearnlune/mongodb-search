package io.github.yearnlune.search.graphql

enum class GroupByOptionType {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY,
    EXISTS;

    companion object : EnumCompanion<GroupByOptionType, String>(
        GroupByOptionType.values().associateBy(GroupByOptionType::name)
    )
}