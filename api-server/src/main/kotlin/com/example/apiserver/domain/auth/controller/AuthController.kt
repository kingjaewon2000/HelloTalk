package com.example.apiserver.domain.auth.controller

import com.example.apiserver.domain.auth.dto.LoginRequest
import com.example.apiserver.domain.auth.service.AuthService
import com.example.apiserver.global.session.set
import com.example.core.global.constant.SessionConstants.Companion.LOGIN_USER_ATTRIBUTE
import jakarta.servlet.http.HttpServletRequest
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

        session[LOGIN_USER_ATTRIBUTE] = response

        return ResponseEntity(HttpStatus.OK)
    }

    @PostMapping("/logout")
    fun logout(request: HttpServletRequest): ResponseEntity<Void> {
        val session = request.getSession(false)
        session?.invalidate()

        return ResponseEntity(HttpStatus.OK)
    }

}