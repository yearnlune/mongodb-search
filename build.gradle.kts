import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.0"
    id("org.jetbrains.dokka") version "1.6.20"
    `maven-publish`
    signing
}

allprojects {
    group = "io.github.yearnlune.search"
    version = "1.0.1"

    repositories {
        mavenCentral()
        maven { url = uri("https://repo.spring.io/milestone") }
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }
}

dependencies {
    api(project(":mongodb-search-interface"))
    api(project(":mongodb-search-core"))
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

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