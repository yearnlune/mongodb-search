plugins {
    `java-gradle-plugin`
}

tasks {
    java {
        withSourcesJar()
    }
}

gradlePlugin {
    plugins {
        create("basePlugin") {
            id = "io.github.yearnlune.search.plugin"
            implementationClass = "io.github.yearnlune.search.plugin.MongoSearchPlugin"
        }
    }
}