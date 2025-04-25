package com.example.apiserver.domain.friend.controller

import com.example.apiserver.domain.auth.dto.LoginUser
import com.example.apiserver.domain.friend.dto.FriendAddRequest
import com.example.apiserver.domain.friend.dto.FriendInfoResponse
import com.example.apiserver.domain.friend.service.FriendService
import com.example.apiserver.global.session.Login
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/friends")
class FriendController(
    val friendService: FriendService
) {

    @GetMapping
    fun getFriends(
        @Login auth: LoginUser
    ): ResponseEntity<List<FriendInfoResponse>> {
        return ResponseEntity
            .ok()
            .body(friendService.getFriends(auth.id))
    }

    @PostMapping
    fun addFriend(
        @Login auth: LoginUser,
        @RequestBody request: FriendAddRequest
    ): ResponseEntity<Void> {
        friendService.addFriend(auth.id, request.toUsername)

        return ResponseEntity
            .ok()
            .build()
    }

}