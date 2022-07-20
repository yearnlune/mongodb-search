import org.jetbrains.kotlin.backend.common.push

plugins {
    kotlin("jvm")

    id("org.jetbrains.dokka") version "1.6.20"
    id("com.graphql_java_generator.graphql-gradle-plugin") version "1.18.6"
    `maven-publish`
    signing
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
    java {
        withSourcesJar()
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

val dokkaJavadocJar by tasks.register<Jar>("dokkaJavadocJar") {
    dependsOn(tasks.dokkaJavadoc)
    from(tasks.dokkaJavadoc.flatMap { it.outputDirectory })
    archiveClassifier.set("javadoc")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifact(dokkaJavadocJar)

            pom {
                name.set(project.name)
                description.set("simple search on mongodb")
                url.set("https://github.com/yearnlune/mongodb-search")
                licenses {
                    license {
                        name.set("Apache License 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("yearnlune")
                        name.set("DONGHWAN KIM")
                        email.set("kdhpopyoa@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/yearnlune/mongodb-search.git")
                    developerConnection.set("scm:git:ssh://git@github.com:yearnlune/mongodb-search.git")
                    url.set("https://github.com/yearnlune/mongodb-search")
                }
            }
        }
    }
    repositories {
        maven {
            val releasesRepoUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            val snapshotsRepoUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
            configure<SigningExtension> {
                val signingKey: String? by project
                val signingPassword: String? by project
                useInMemoryPgpKeys(signingKey, signingPassword)
                sign(publications["mavenJava"])
            }
            credentials {
                username = System.getenv("SONATYPE_USERNAME")
                password = System.getenv("SONATYPE_PASSWORD")
            }
        }
    }
}