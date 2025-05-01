package com.example.apiserver.domain.chat.repository

import com.example.apiserver.domain.chat.dto.RoomInfoResponse
import com.example.core.global.model.Cursor

interface CustomChatRoomRepository {

    fun findAllByUserId(userId: Long, cursor: Cursor?, limit: Int): MutableList<RoomInfoResponse>

}