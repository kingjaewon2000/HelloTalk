package com.example.apiserver.domain.chat.repository

import com.example.apiserver.domain.chat.entity.ChatRoom
import org.springframework.data.jpa.repository.JpaRepository

interface ChatRoomRepository : JpaRepository<ChatRoom, Long>, CustomChatRoomRepository