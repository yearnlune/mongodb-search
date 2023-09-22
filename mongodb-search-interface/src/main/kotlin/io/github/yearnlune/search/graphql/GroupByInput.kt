package io.github.yearnlune.search.graphql

class GroupByInput(
    var key: String,
    option: GroupByOptionType? = null,
    val options: GroupByOptionInput? = null
) {
    @Deprecated("option is deprecated since v1.0.26, Use options instead.")
    val option: GroupByOptionType? = option
}