# MONGODB SEARCH
> Simple, Easily, Flexible

## Installation

---

### Gradle

```kotlin
plugins {
  id("io.github.yearnlune.search.plugin") version "1.2.2"
}

configurations {
    implementation {
        isCanBeResolved = true
    }
}

dependencies {
  implementation("io.github.yearnlune.search:mongodb-search-core:1.2.2")
}
```

### Configuration

**Naming Strategy**

| object                       | description     |
|------------------------------|-----------------|
| PropertyNamingStrategyOrigin | origin[default] |
| PropertyNamingStrategyCamel  | camel case      |
| PropertyNamingStrategySnake  | snake case      |

**Custom property**

Supports custom mapping

```kotlin
PropertyFactory.add("A", "B")
PropertyFactory.add("B", "C")
```


**Example**
```kotlin
@Configuration
class MongodbSearchConfig {

    init {
        PropertyFactory.namingStrategy = PropertyNamingStrategySnake
        PropertyFactory.add("A", "B") // A to B mapping
    }
}
```

**Scalar Property**

```kotlin
@Configuration
class GraphqlWiringConfig : RuntimeWiringConfigurer {

    override fun configure(builder: RuntimeWiring.Builder) {
        builder
            .scalar(MongodbSearchScalars.Property)
            .build()
    }
}
```

## Example project

[SpringBoot 2](https://github.com/yearnlune/mongodb-search/tree/main/mongodb-search-example)
