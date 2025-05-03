package com.example.consumerserver.global.config

import com.example.consumerserver.domain.message.listener.InboundMessageProcessor
import com.example.core.global.constant.RedisConstants
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.stream.Consumer
import org.springframework.data.redis.connection.stream.MapRecord
import org.springframework.data.redis.connection.stream.ReadOffset
import org.springframework.data.redis.connection.stream.StreamOffset
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.stream.StreamMessageListenerContainer
import org.springframework.data.redis.stream.StreamMessageListenerContainer.StreamMessageListenerContainerOptions
import java.time.Duration
import java.util.*

@Configuration
class RedisStreamConfig(
    private val inboundMessageProcessor: InboundMessageProcessor
) {

    @Bean
    fun streamMessageListenerContainer(connectionFactory: RedisConnectionFactory): StreamMessageListenerContainer<String, MapRecord<String, String, String>> {
        val options = StreamMessageListenerContainerOptions
            .builder()
            .pollTimeout(Duration.ZERO)
            .build()

        val listenerContainer = StreamMessageListenerContainer.create(connectionFactory, options)

        val streamKey = RedisConstants.INBOUND_STREAM_KEY
        val consumerGroupName = "consumer-group"
        val consumerName = "consumer-${UUID.randomUUID()}}"

        try {
            val template = StringRedisTemplate(connectionFactory)
            template.opsForStream<String, String>().createGroup(streamKey, consumerGroupName)
        } catch (_: Exception) {
        }

        listenerContainer.receive(
            Consumer.from(consumerGroupName, consumerName),
            StreamOffset.create(streamKey, ReadOffset.lastConsumed()),
            inboundMessageProcessor
        )

        listenerContainer.start()

        return listenerContainer
    }

}