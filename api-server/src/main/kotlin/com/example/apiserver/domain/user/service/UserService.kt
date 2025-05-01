package com.example.apiserver.domain.user.service

import com.example.apiserver.domain.user.dto.MemberIdResponse
import com.example.apiserver.domain.user.dto.UserCreateRequest
import com.example.apiserver.domain.user.repository.UserRepository
import com.example.core.global.exception.ApiException
import com.example.core.global.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class UserService(
    private val userRepository: UserRepository
) {

    fun findByIdOrThrow(id: Long) =
        userRepository.findUserById(id) ?: throw ApiException(ErrorCode.USER_NOT_FOUND)

    fun findByUsernameOrThrow(username: String) =
        userRepository.findByUsername(username) ?: throw ApiException(ErrorCode.USER_NOT_FOUND)

    @Transactional
    fun createUser(request: UserCreateRequest): MemberIdResponse {
        if (userRepository.existsByUsername(request.username)) {
            throw ApiException(ErrorCode.USER_ALREADY_EXISTS)
        }

        val user = userRepository.save(request.toEntity())

        return MemberIdResponse(user.id)
    }

}