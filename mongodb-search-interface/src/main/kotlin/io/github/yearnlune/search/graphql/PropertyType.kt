package io.github.yearnlune.search.graphql

enum class PropertyType {
    INTEGER,
    LONG,
    FLOAT,
    DOUBLE,
    NUMBER,
    BOOLEAN,
    DATE,
    TIMESTAMP,
    STRING,
    OBJECT_ID,
    PROPERTY,
    CURRENCY,
    IMAGE,
    ENUM,
    USER,
    GROUP,
    ANY;

    companion object : EnumCompanion<PropertyType, String>(
        PropertyType.values().associateBy(PropertyType::name)
    )
}