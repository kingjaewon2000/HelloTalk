package com.example.apiserver.domain.chat.repository

import com.example.apiserver.domain.chat.entity.ChatRoomUser
import org.springframework.data.jpa.repository.JpaRepository

interface ChatRoomUserRepository : JpaRepository<ChatRoomUser, Long>