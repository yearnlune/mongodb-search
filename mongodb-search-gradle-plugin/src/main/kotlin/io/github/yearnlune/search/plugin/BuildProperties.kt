package io.github.yearnlune.search.plugin

object BuildProperties {

    const val INTERFACE_NAME = "mongodb-search-interface"

    const val EXTRACT_INTERFACE_TASK = "extractMongodbSearchInterface"

    const val COPY_INTERFACE_TASK = "copyMongodbSearchInterface"

    const val APPLY_MONGODB_SEARCH_TASK = "applyMongodbSearch"

    const val OUTPUT_DIRECTORY = "generated/resources/mongodbSearch"

    const val OUTPUT_RESOURCE_DIRECTORY = "$OUTPUT_DIRECTORY/resources"

    const val OUTPUT_GRAPHQL_RESOURCE_DIRECTORY = "$OUTPUT_RESOURCE_DIRECTORY/graphql"

    const val EXTRACTED_GRAPHQL_DIRECTORY = "$OUTPUT_DIRECTORY/graphql"
}