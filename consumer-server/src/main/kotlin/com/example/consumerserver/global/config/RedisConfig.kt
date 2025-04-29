package com.example.consumerserver.global.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory

@Configuration
class RedisConfig {

    @Value("\${spring.data.redis.host}")
    private lateinit var redisHost: String

    @Value("\${spring.data.redis.port}")
    private val redisPort: Int = 0

    @Bean
    fun redisConnectionFactory(): RedisConnectionFactory {
        return LettuceConnectionFactory(redisHost, redisPort)
    }

}