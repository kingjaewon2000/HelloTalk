package com.example.apiserver.domain.auth.service

import com.example.apiserver.domain.auth.dto.LoginRequest
import com.example.apiserver.domain.auth.dto.LoginUser
import com.example.apiserver.domain.user.repository.UserRepository
import com.example.core.global.exception.ApiException
import com.example.core.global.exception.ErrorCode
import org.mindrot.jbcrypt.BCrypt
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository
) {

    fun login(request: LoginRequest): LoginUser {
        val user = userRepository.findByUsername(request.username)
            ?: throw ApiException(ErrorCode.UNAUTHORIZED)

        if (passwordMatches(request.password, user.password)) {
            throw ApiException(ErrorCode.UNAUTHORIZED)
        }

        return LoginUser(user.id, user.username)
    }

    private fun passwordMatches(
        rawPassword: String,
        encodedPassword: String
    ) = !BCrypt.checkpw(rawPassword, encodedPassword)

}