package io.github.yearnlune.search.core.type

import io.github.yearnlune.search.graphql.PropertyType
import java.time.LocalDateTime
import java.time.ZoneOffset

enum class ReservedWordType(
    val process: (PropertyType) -> String
) {
    CURRENT_DATE({
        when (it) {
            PropertyType.DATE, PropertyType.LONG, PropertyType.OBJECT_ID ->
                LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli().toString()
            else -> throw RuntimeException("Process fail, Not supported type ${it.name}")
        }
    });

    companion object :
        EnumCompanion<ReservedWordType, String>(ReservedWordType.values().associateBy(ReservedWordType::name))
}