package com.example.consumerserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication
@EntityScan(
    basePackages = [
        "com.example.core",
        "com.example.consumerserver"
    ]
)
@EnableJpaRepositories(
    basePackages = [
        "com.example.core",
        "com.example.consumerserver"
    ]
)
class ConsumerServerApplication

fun main(args: Array<String>) {
    runApplication<ConsumerServerApplication>(*args)
}
