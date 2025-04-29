package com.example.chatserver.global.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer

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

    @Bean
    fun redisTemplate(): RedisTemplate<String, Any> {
        val redisTemplate = RedisTemplate<String, Any>()

        // Key: 직렬화
        redisTemplate.keySerializer = StringRedisSerializer()
        redisTemplate.hashKeySerializer = StringRedisSerializer()

        // Value: 직렬화
        redisTemplate.valueSerializer = StringRedisSerializer()
        redisTemplate.hashValueSerializer = StringRedisSerializer()
        redisTemplate.connectionFactory = redisConnectionFactory()

        return redisTemplate
    }

}