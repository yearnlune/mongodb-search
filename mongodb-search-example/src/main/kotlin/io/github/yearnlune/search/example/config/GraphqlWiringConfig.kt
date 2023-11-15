package io.github.yearnlune.search.example.config

import graphql.scalars.ExtendedScalars
import graphql.schema.idl.RuntimeWiring
import io.github.yearnlune.search.scalar.MongodbSearchScalars
import org.springframework.context.annotation.Configuration
import org.springframework.graphql.execution.RuntimeWiringConfigurer

@Configuration
class GraphqlWiringConfig : RuntimeWiringConfigurer {

    override fun configure(builder: RuntimeWiring.Builder) {
        builder
            .scalar(ExtendedScalars.Date)
            .scalar(ExtendedScalars.DateTime)
            .scalar(ExtendedScalars.Json)
            .scalar(ExtendedScalars.GraphQLLong)
            .scalar(MongodbSearchScalars.Property)
            .build()
    }
}