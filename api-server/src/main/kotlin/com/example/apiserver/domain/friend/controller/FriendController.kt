package com.example.apiserver.domain.friend.controller

import com.example.apiserver.domain.auth.dto.LoginUser
import com.example.apiserver.domain.friend.dto.FriendAddRequest
import com.example.apiserver.domain.friend.service.FriendService
import com.example.apiserver.global.session.Login
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/friends")
class FriendController(
    val friendService: FriendService
) {

    @PostMapping
    fun addFriend(
        @Login auth: LoginUser,
        @RequestBody request: FriendAddRequest
    ) {
        friendService.addFriend(auth.id, request.toUsername)
    }

}