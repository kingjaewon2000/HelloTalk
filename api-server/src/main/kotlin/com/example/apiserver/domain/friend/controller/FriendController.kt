package com.example.apiserver.domain.friendship.controller

import com.example.apiserver.domain.friendship.service.FriendService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/friends")
class FriendController(
    val friendService: FriendService
) {

    @GetMapping
    fun getFriends() {
        friendService.getFriends()
    }

    @PostMapping
    fun addFriend() {
        friendService.addFriend()
    }

}