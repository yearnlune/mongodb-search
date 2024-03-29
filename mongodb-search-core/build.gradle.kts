dependencies {
    api(project(":mongodb-search-interface"))
    api("org.springframework.data:spring-data-mongodb:3.4.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.3")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.3")

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

ktlint {
    ignoreFailures.set(true)
    filter {
        exclude {
            it.file.path.contains("Test.kt")
        }
    }
}