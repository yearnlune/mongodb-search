plugins {
    kotlin("jvm")

    id("org.jetbrains.dokka") version "1.6.20"
    jacoco
    `maven-publish`
    signing
}

dependencies {
    api(project(":mongodb-search-interface"))
    api("org.springframework.data:spring-data-mongodb:3.4.0")

    testImplementation("com.fasterxml.jackson.core:jackson-databind:2.13.3")
    testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.3")
    testImplementation("io.kotest:kotest-runner-junit5:5.3.1")
}

tasks {
    test {
        useJUnitPlatform()

        finalizedBy(jacocoTestReport)
    }
    jacocoTestCoverageVerification {
        violationRules {
            rule {
                limit {
                    minimum = "0.80".toBigDecimal()
                }
            }
        }
    }
    java {
        withSourcesJar()
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
                name.set(rootProject.name)
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