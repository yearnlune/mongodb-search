import org.jetbrains.kotlin.backend.common.push

// upper spring boot 3
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.springframework.boot:spring-boot-starter-graphql:3.0.7")
    }
}

group = "io.github.yearnlune.search.example"

plugins {
    kotlin("jvm")

    id("org.springframework.boot") version "3.0.7"
    kotlin("plugin.spring") version "1.6.21"

    id("com.graphql-java-generator.graphql-gradle-plugin") version "2.0"
    id("io.github.yearnlune.search.plugin") version "1.0.25"
}

configurations {
    implementation {
        isCanBeResolved = true
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web:3.0.7")
    implementation("org.springframework.boot:spring-boot-starter-graphql:3.0.7")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb:3.0.7")

    /* KOTLIN */
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation(project(":mongodb-search-core"))
    implementation("com.graphql-java-generator:graphql-java-common-runtime:2.0")

    implementation("de.flapdoodle.embed:de.flapdoodle.embed.mongo.spring30x:4.6.2")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
}

generatePojoConf {
    packageName = "$group.graphql"
    setSchemaFileFolder("$projectDir/src/main/resources/graphql")
    mode = com.graphql_java_generator.plugin.conf.PluginMode.server
    skipGenerationIfSchemaHasNotChanged = true

    customScalars.push(
        com.graphql_java_generator.plugin.conf.CustomScalarDefinition(
            "Date",
            "java.time.LocalDateTime",
            null,
            "graphql.scalars.ExtendedScalars.DateTime",
            null
        )
    )
}

tasks {
    jar {
        enabled = false
    }
    generatePojo {
        dependsOn(io.github.yearnlune.search.plugin.BuildProperties.APPLY_MONGODB_SEARCH_TASK)
    }
    compileKotlin {
        dependsOn("generatePojo")
    }
    test {
        useJUnitPlatform()

        finalizedBy(jacocoTestReport)
    }
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

sourceSets {
    named("main") {
        java.srcDirs("/build/generated/sources/graphqlGradlePlugin")
    }
}