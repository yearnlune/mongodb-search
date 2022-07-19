import org.jetbrains.kotlin.backend.common.push

plugins {
    kotlin("jvm")

    id("com.graphql_java_generator.graphql-gradle-plugin") version "1.18.6"
}

dependencies {
    implementation("com.graphql-java:graphql-java-extended-scalars:18.1")
    implementation("com.graphql-java-generator:graphql-java-common-runtime:1.18.5")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
}

tasks {
    compileKotlin {
        dependsOn("generatePojo")
    }
    test {
        useJUnitPlatform()
    }
    generatePojo {
        dependsOn("clean")
    }
}

generatePojoConf {
    packageName = "$group.graphql"
    setSchemaFileFolder("$rootDir/${project.name}/src/main/resources/graphql")
    mode = com.graphql_java_generator.plugin.conf.PluginMode.server
    skipGenerationIfSchemaHasNotChanged = true

    customScalars.push(
        com.graphql_java_generator.plugin.conf.CustomScalarDefinition(
            "Long",
            "java.lang.Long",
            null,
            "graphql.scalars.ExtendedScalars.GraphQLLong",
            null
        )
    )
    customScalars.push(
        com.graphql_java_generator.plugin.conf.CustomScalarDefinition(
            "JSON",
            "java.lang.Object",
            "graphql.scalars.object.JsonScalar",
            "graphql.scalars.ExtendedScalars.Json",
            null
        )
    )

    customScalars.push(
        com.graphql_java_generator.plugin.conf.CustomScalarDefinition(
            "Aggregation",
            "java.lang.Object",
            null,
            "io.github.yearnlune.search.interface.AggregationScalar.INSTANCE",
            null
        )
    )
}

sourceSets {
    named("main") {
        java.srcDirs("/build/generated/sources/graphqlGradlePlugin")
    }
}
