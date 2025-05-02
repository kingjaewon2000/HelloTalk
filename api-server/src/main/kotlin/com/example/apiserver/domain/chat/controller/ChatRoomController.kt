package com.example.apiserver.domain.chat.controller

import com.example.apiserver.domain.chat.dto.DirectRoomCreateRequest
import com.example.apiserver.domain.chat.dto.GroupRoomCreateRequest
import com.example.core.domain.chat.dto.RoomInfoResponse
import com.example.apiserver.domain.chat.service.ChatRoomService
import com.example.apiserver.global.session.Login
import com.example.core.global.api.ApiCursorResponse
import com.example.core.global.model.LoginUser
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/rooms")
class ChatRoomController(
    private val chatRoomService: ChatRoomService
) {

    @GetMapping("/{roomId}")
    fun getRoom(
        @Login auth: LoginUser,
        @PathVariable roomId: Long
    ): RoomInfoResponse {
        return chatRoomService.findByRoomId(roomId)
    }

    @GetMapping
    fun getRooms(
        @Login auth: LoginUser,
        @RequestParam(required = false) cursorId: String?
    ): ApiCursorResponse<RoomInfoResponse> {
        return chatRoomService.findAllByUserId(auth.userId, cursorId)
    }

    @PostMapping("/direct")
    fun createDirectChatRoom(
        @Login auth: LoginUser,
        @RequestBody request: DirectRoomCreateRequest
    ) {
        chatRoomService.createDirectChatRoom(auth.userId, request)
    }

    @PostMapping("/group")
    fun createGroupChatRoom(
        @Login auth: LoginUser,
        @RequestBody request: GroupRoomCreateRequest
    ) {
        chatRoomService.createGroupChatRoom(auth.userId, request)
    }

}