package io.github.yearnlune.search.plugin

object BuildProperties {

    const val INTERFACE_NAME = "mongodb-search-interface"

    const val EXTRACT_INTERFACE_TASK = "extractMongodbSearchInterface"

    const val COPY_INTERFACE_TASK = "copyMongodbSearchInterface"

    const val APPLY_MONGODB_SEARCH_TASK = "applyMongodbSearch"

    const val GRAPHQL_SCHEMA_FILE_NAME = "mongodb-search.graphqls"

    const val OUTPUT_DIRECTORY = "build/generated/resources/mongodbSearch"

    const val OUTPUT_SCHEMA_FILE = "$OUTPUT_DIRECTORY/graphql/$GRAPHQL_SCHEMA_FILE_NAME"

    const val TARGET_RESOURCES_DIRECTORY = "build/resources/main/graphql"
}