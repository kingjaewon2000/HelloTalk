package com.example.chatserver.global.config

import com.example.chatserver.domain.chat.listener.OutboundMessageProcessor
import com.example.core.global.constant.RedisConstants
import jakarta.annotation.PreDestroy
import org.springframework.beans.factory.annotation.Value
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
import org.springframework.data.redis.stream.Subscription
import java.time.Duration
import java.util.*

@Configuration
class RedisStreamConfig(
    private val outboundMessageProcessor: OutboundMessageProcessor
) {

    @Value("\${server.instanceId}")
    private lateinit var instanceId: String

    private lateinit var listenerContainer: StreamMessageListenerContainer<String, MapRecord<String, String, String>>
    private lateinit var subscription: Subscription

    @Bean
    fun streamMessageListenerContainer(connectionFactory: RedisConnectionFactory): StreamMessageListenerContainer<String, MapRecord<String, String, String>> {
        val options = StreamMessageListenerContainerOptions
            .builder()
            .pollTimeout(Duration.ZERO)
            .build()

        this.listenerContainer = StreamMessageListenerContainer.create(connectionFactory, options)

        val streamKey = RedisConstants.getOutboundStreamKey(instanceId)
        val consumerGroupName = "consumer-group"
        val consumerName = "consumer-${UUID.randomUUID()}}"

        try {
            val template = StringRedisTemplate(connectionFactory)
            template.opsForStream<String, String>().createGroup(streamKey, consumerGroupName)
        } catch (_: Exception) {
        }

        this.subscription = listenerContainer.receive(
            Consumer.from(consumerGroupName, consumerName),
            StreamOffset.create(streamKey, ReadOffset.lastConsumed()),
            outboundMessageProcessor
        )

        this.listenerContainer.start()

        return this.listenerContainer
    }

    @PreDestroy
    fun shutdownListenerContainer() {
        if (::listenerContainer.isInitialized && listenerContainer.isRunning) {
            try {
                this.subscription.cancel()
                this.listenerContainer.stop()

                Thread.sleep(10000)
            } catch (_: Exception) {
            }
        }
    }

}