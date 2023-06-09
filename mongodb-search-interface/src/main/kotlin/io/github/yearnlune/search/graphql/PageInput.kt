package io.github.yearnlune.search.graphql

class PageInput(
    val pageNumber: Long,
    val pageSize: Long,
    val sort: List<SortInput> = listOf(),
    val searches: List<SearchInput> = listOf()
)