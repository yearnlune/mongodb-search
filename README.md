# MONGODB SEARCH
> Simple, Easily, Flexible 

## Installation

---

```kotlin
plugins {
  id("io.github.yearnlune.search.plugin") version "1.1.8"
}

configurations {
    implementation {
        isCanBeResolved = true
    }
}

dependencies {
  implementation("io.github.yearnlune.search:mongodb-search-core:1.1.8")
}
```
