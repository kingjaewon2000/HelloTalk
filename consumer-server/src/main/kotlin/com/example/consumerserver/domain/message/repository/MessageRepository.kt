package com.example.consumerserver.domain.message.repository

import com.example.consumerserver.domain.message.entity.Message
import org.springframework.data.jpa.repository.JpaRepository

interface MessageRepository : JpaRepository<Message, Long> {
}