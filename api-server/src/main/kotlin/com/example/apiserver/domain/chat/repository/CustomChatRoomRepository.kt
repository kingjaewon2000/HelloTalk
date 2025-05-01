package com.example.apiserver.domain.chat.repository

import com.example.apiserver.domain.chat.dto.RoomInfoResponse
import com.example.core.common.model.CursorInfo

interface CustomChatRoomRepository {

    fun findAllByUserId(userId: Long, cursorInfo: CursorInfo?, limit: Int): MutableList<RoomInfoResponse>

}