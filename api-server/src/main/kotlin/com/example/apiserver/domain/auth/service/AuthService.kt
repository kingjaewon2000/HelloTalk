package com.example.apiserver.domain.auth.service

import com.example.apiserver.domain.auth.dto.LoginRequest
import com.example.apiserver.domain.auth.dto.LoginResponse
import com.example.apiserver.domain.user.repository.UserRepository
import com.example.core.global.exception.ApiException
import com.example.core.global.exception.ErrorCode
import org.mindrot.jbcrypt.BCrypt
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository
) {

    fun login(request: LoginRequest): LoginResponse {
        val user = userRepository.findByUsername(request.username)
            ?: throw ApiException(ErrorCode.UNAUTHORIZED)

        if (passwordMatches(request.password, user.password)) {
            throw ApiException(ErrorCode.UNAUTHORIZED)
        }

        return LoginResponse(user.id)
    }

    private fun passwordMatches(
        rawPassword: String,
        encodedPassword: String
    ) = !BCrypt.checkpw(rawPassword, encodedPassword)

}