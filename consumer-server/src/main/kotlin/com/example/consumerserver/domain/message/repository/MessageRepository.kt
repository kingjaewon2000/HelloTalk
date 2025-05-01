package com.example.consumerserver.domain.message.repository

import com.example.core.domain.chat.entity.Message
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MessageRepository : JpaRepository<Message, Long>