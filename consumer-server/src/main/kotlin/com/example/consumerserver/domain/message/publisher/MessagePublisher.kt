package com.example.consumerserver.domain.message.publisher

interface MessagePublisher {

    fun publish(instanceId: String, message: String)

}