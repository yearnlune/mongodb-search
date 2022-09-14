plugins {
    `java-gradle-plugin`

    id("com.gradle.plugin-publish") version "1.0.0"
}

tasks {
    java {
        withSourcesJar()
    }
}

pluginBundle {
    val projectDescription: String by project

    website = "https://github.com/yearnlune/mongodb-search"
    vcsUrl = "https://github.com/yearnlune/mongodb-search"
    description = projectDescription

    tags = listOf("mongodb-search", "mongodb")
}

gradlePlugin {
    plugins {
        create("mongodbSearchPlugin") {
            id = "io.github.yearnlune.search.plugin"
            implementationClass = "io.github.yearnlune.search.plugin.MongoSearchPlugin"
            displayName = "MongoDB Search plugin"
        }
    }
}