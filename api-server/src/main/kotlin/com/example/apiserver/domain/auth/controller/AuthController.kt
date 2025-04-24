package com.example.apiserver.domain.auth.controller

import com.example.apiserver.domain.auth.dto.LoginRequest
import com.example.apiserver.domain.auth.service.AuthService
import com.example.apiserver.global.extension.set
import com.example.apiserver.global.session.SessionConstants.Companion.SESSION_NAME
import jakarta.servlet.http.HttpSession
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/login")
    fun login(
        @RequestBody request: LoginRequest,
        session: HttpSession,
    ): ResponseEntity<Void> {
        val response = authService.login(request)

        session[SESSION_NAME] = response

        return ResponseEntity(HttpStatus.OK)
    }

    @PostMapping("/logout")
    fun logout(session: HttpSession): ResponseEntity<Void> {
        session.invalidate()

        return ResponseEntity(HttpStatus.OK)
    }

}