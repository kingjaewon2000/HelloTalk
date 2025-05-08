package com.example.core.domain.chat.repository

import com.example.core.domain.chat.entity.Message
import org.springframework.data.jpa.repository.JpaRepository

interface MessageRepository : JpaRepository<Message, Long> {

    fun findMessageById(messageId: Long): Message?

}