package com.example.core.domain.chat.repository

import com.example.core.domain.chat.entity.ChatRoom
import org.springframework.data.jpa.repository.JpaRepository

interface ChatRoomRepository : JpaRepository<ChatRoom, Long>, CustomChatRoomRepository