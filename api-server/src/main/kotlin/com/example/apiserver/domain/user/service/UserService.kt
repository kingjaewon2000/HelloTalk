package com.example.apiserver.domain.user.service

import com.example.apiserver.domain.user.dto.UserCreateRequest
import com.example.apiserver.domain.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class UserService(
    private val userRepository: UserRepository
) {

    @Transactional
    fun createUser(request: UserCreateRequest) {
        if (userRepository.existsByUsername(request.username)) {
            throw RuntimeException("User already exists")
        }

        val user = request.toEntity()

        userRepository.save(user)
    }

}