package io.github.yearnlune.search.example.config

import io.github.yearnlune.search.scalar.PropertyFactory
import io.github.yearnlune.search.scalar.PropertyNamingStrategySnake
import org.springframework.context.annotation.Configuration

@Configuration
class MongodbSearchConfig {

    init {
        PropertyFactory.namingStrategy = PropertyNamingStrategySnake
    }
}