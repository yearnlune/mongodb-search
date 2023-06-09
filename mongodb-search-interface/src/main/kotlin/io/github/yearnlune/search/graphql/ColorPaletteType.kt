package io.github.yearnlune.search.graphql

enum class ColorPaletteType {
    BASIC,
    VIVID,
    PASTEL;

    companion object : EnumCompanion<ColorPaletteType, String>(
        ColorPaletteType.values().associateBy(ColorPaletteType::name)
    )
}