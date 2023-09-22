dependencies {
    api("com.graphql-java:graphql-java-extended-scalars:20.0")
}

tasks.register<Jar>("sourcesJar") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    dependsOn(JavaPlugin.CLASSES_TASK_NAME)
    from(sourceSets["main"].allSource)
    archiveClassifier.set("sources")
}

tasks {
    test {
        useJUnitPlatform()
    }
    java {
        withSourcesJar()
    }
}