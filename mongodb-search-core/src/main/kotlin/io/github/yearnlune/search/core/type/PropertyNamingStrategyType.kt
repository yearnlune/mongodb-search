package io.github.yearnlune.search.core.type

enum class PropertyNamingStrategyType {
    ORIGIN,
    CAMEL_CASE,
    SNAKE_CASE;

    companion object :
        EnumCompanion<PropertyNamingStrategyType, String>(
            PropertyNamingStrategyType.values().associateBy(PropertyNamingStrategyType::name)
        )
}