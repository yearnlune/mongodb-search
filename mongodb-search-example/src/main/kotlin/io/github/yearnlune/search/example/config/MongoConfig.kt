package io.github.yearnlune.search.example.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource
import org.springframework.data.repository.init.Jackson2RepositoryPopulatorFactoryBean

@Configuration
class MongoConfig {

    @Value("classpath:data.json")
    private lateinit var data: Resource

    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    @Bean
    fun repositoryPopulator(objectMapper: ObjectMapper): Jackson2RepositoryPopulatorFactoryBean {
        val factory = Jackson2RepositoryPopulatorFactoryBean()

        factory.setMapper(objectMapper)
        factory.setResources(arrayOf(data))
        return factory
    }
}