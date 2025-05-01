package com.example.apiserver.domain.auth.service

import com.example.apiserver.domain.auth.dto.LoginRequest
import com.example.apiserver.domain.user.repository.UserRepository
import com.example.core.common.model.LoginUser
import com.example.core.common.exception.ApiException
import com.example.core.common.exception.ErrorCode
import org.mindrot.jbcrypt.BCrypt
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
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

    /*
     * 내부 메서드
     */
    private fun passwordMatches(
        rawPassword: String,
        encodedPassword: String
    ) = !BCrypt.checkpw(rawPassword, encodedPassword)

}