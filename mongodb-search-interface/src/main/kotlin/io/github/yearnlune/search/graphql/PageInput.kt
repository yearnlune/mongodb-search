package io.github.yearnlune.search.graphql

class PageInput(
    val pageNumber: Long,
    val pageSize: Long,
    var sort: MutableList<SortInput> = mutableListOf(),
    var searches: MutableList<SearchInput> = mutableListOf()
)