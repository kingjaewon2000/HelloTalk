package com.example.chatserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication
@EntityScan(
    basePackages = [
        "com.example.core",
        "com.example.chatserver",
    ]
)
@EnableJpaRepositories(
    basePackages = [
        "com.example.core",
        "com.example.chatserver"
    ]
)
class ChatServerApplication

fun main(args: Array<String>) {
    runApplication<ChatServerApplication>(*args)
}
