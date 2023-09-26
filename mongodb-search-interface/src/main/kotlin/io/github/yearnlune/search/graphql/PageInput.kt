package io.github.yearnlune.search.graphql

class PageInput(
    val pageNumber: Long,
    val pageSize: Long,
    val sort: MutableList<SortInput> = mutableListOf(),
    val searches: MutableList<SearchInput> = mutableListOf()
)