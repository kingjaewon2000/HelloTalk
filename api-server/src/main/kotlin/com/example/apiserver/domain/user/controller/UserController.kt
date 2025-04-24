package com.example.apiserver.domain.user.controller

import com.example.apiserver.domain.user.dto.MemberIdResponse
import com.example.apiserver.domain.user.dto.UserCreateRequest
import com.example.apiserver.domain.user.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
) {

    @PostMapping
    fun createUser(@RequestBody request: UserCreateRequest): ResponseEntity<MemberIdResponse> {
        return ResponseEntity
            .ok()
            .body(userService.createUser(request))
    }

}