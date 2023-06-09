# MongoDB Search Plugin

This plugin that makes it easy to set mongodb-search configuration

## USAGE

```kotlin
plugins {
    id("io.github.yearnlune.search.plugin") version "0.0.1"
}

configurations {
    implementation {
        isCanBeResolved = true
    }
}

tasks {
    compileKotlin {
        dependsOn(io.github.yearnlune.search.plugin.BuildProperties.APPLY_MONGODB_SEARCH_TASK)
    }
}
```
