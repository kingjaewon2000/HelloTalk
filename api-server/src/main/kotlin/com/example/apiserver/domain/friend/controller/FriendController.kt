package com.example.apiserver.domain.friend.controller

import com.example.apiserver.domain.friend.dto.FriendAddRequest
import com.example.apiserver.domain.friend.dto.FriendInfoResponse
import com.example.apiserver.domain.friend.entity.FriendStatus
import com.example.apiserver.domain.friend.entity.FriendStatus.ACCEPTED
import com.example.apiserver.domain.friend.service.FriendService
import com.example.apiserver.global.session.Login
import com.example.core.common.model.LoginUser
import com.example.core.common.api.ApiCursorResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/friends")
class FriendController(
    val friendService: FriendService
) {

    @GetMapping
    fun getFriends(
        @Login auth: LoginUser,
        @RequestParam(required = false) status: FriendStatus = ACCEPTED,
        @RequestParam(required = false) cursorId: String?,
    ): ResponseEntity<ApiCursorResponse<FriendInfoResponse>> {
        return ResponseEntity
            .ok()
            .body(friendService.getFriends(auth.userId, status, cursorId))
    }

    @PostMapping
    fun addFriend(
        @Login auth: LoginUser,
        @RequestBody request: FriendAddRequest
    ): ResponseEntity<Void> {
        friendService.addFriend(auth.userId, request.toUsername)

        return ResponseEntity
            .ok()
            .build()
    }

    @PatchMapping("/{friendId}/accept")
    fun acceptFriend(
        @Login auth: LoginUser,
        @PathVariable friendId: Long
    ): ResponseEntity<Void> {
        friendService.acceptFriend(auth.userId, friendId)

        return ResponseEntity
            .ok()
            .build()
    }

    @PatchMapping("/{friendId}/reject")
    fun rejectFriend(
        @Login auth: LoginUser,
        @PathVariable friendId: Long
    ): ResponseEntity<Void> {
        friendService.rejectFriend(auth.userId, friendId)

        return ResponseEntity
            .ok()
            .build()
    }

    @PatchMapping("/{friendId}/block")
    fun blockFriend(
        @Login auth: LoginUser,
        @PathVariable friendId: Long
    ): ResponseEntity<Void> {
        friendService.blockFriend(auth.userId, friendId)

        return ResponseEntity
            .ok()
            .build()
    }

}