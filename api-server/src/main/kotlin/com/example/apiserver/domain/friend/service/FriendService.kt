package com.example.apiserver.domain.friendship.service

import com.example.apiserver.domain.friendship.repository.FriendRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class FriendService(
    val friendRepository: FriendRepository
) {
    
    fun getFriends() {
        TODO("Not yet implemented")
    }

    @Transactional
    fun addFriend() {
        TODO("Not yet implemented")
    }


}