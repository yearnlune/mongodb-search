package io.github.yearnlune.search.example

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MongodbSearchExampleApplication

fun main(args: Array<String>) {
    runApplication<MongodbSearchExampleApplication>(*args)
}
