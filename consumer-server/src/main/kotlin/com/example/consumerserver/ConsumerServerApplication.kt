package com.example.consumerserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication

@SpringBootApplication
@EntityScan(basePackages = ["com.example.core.domain.chat"])
class ConsumerServerApplication

fun main(args: Array<String>) {
	runApplication<ConsumerServerApplication>(*args)
}
