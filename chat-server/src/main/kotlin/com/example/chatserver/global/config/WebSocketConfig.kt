package com.example.chatserver.global.config

import com.example.chatserver.global.interceptor.AuthHandshakeInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig(
    private val authHandshakeInterceptor: AuthHandshakeInterceptor
) : WebSocketMessageBrokerConfigurer {

    companion object {
        private val HEARTBEAT_INTERVAL = longArrayOf(25000, 25000)
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/ws")
            .addInterceptors(authHandshakeInterceptor)
            .setAllowedOrigins("http://localhost:3000", "http://localhost:5173")
    }

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        registry.setApplicationDestinationPrefixes("/pub")
        registry.enableSimpleBroker("/sub")
    }

}