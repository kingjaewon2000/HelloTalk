package com.example.apiserver.domain.friend.service

import com.example.apiserver.domain.friend.dto.FriendInfoResponse
import com.example.apiserver.domain.friend.entity.Friend
import com.example.apiserver.domain.friend.entity.FriendStatus
import com.example.apiserver.domain.friend.entity.FriendStatus.ACCEPTED
import com.example.apiserver.domain.friend.repository.FriendRepository
import com.example.apiserver.domain.user.entity.User
import com.example.apiserver.domain.user.service.UserService
import com.example.core.global.exception.ApiException
import com.example.core.global.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class FriendService(
    val friendRepository: FriendRepository,
    val userService: UserService
) {

    fun getFriends(userId: Long): List<FriendInfoResponse> {
        return friendRepository.findAllByFromUserId(userId, ACCEPTED)
            .map { FriendInfoResponse(it.id, it.toUser.id, it.toUser.username, it.toUser.name, it.status.toString()) }
            .toList()
    }

    @Transactional
    fun addFriend(fromUserId: Long, toUsername: String) {
        val fromUser = userService.getByUserId(fromUserId)
        val toUser = userService.getByUsername(toUsername)

        // 자기 자신을 친구 추가 시 예외
        if (isSameUser(fromUser.id, toUser.id)) throw ApiException(ErrorCode.NOT_ALLOW_SELF_ADD_FRIEND)

        // 이미 친구 추가된 사용자면 예외
        if (isFriendExists(fromUser.id, toUser.id)) throw ApiException(ErrorCode.NOT_ALLOW_ALREADY_ADDED_FRIEND)

        // 친구 관계 생성(양방향 관계 생성)
        val requester = createFriendship(fromUser, toUser)

        friendRepository.saveAll(requester)
    }

    private fun createFriendship(fromUser: User, toUser: User): List<Friend> {
        val requester = Friend(
            fromUser = fromUser,
            toUser = toUser,
            requesterUser = fromUser,
            status = ACCEPTED
        )

        val requested = Friend(
            fromUser = toUser,
            toUser = fromUser,
            requesterUser = fromUser,
            status = FriendStatus.PENDING
        )

        return listOf(requester, requested)
    }

    private fun isSameUser(fromUserId: Long, toUserId: Long): Boolean {
        return fromUserId == toUserId
    }

    private fun isFriendExists(fromUserId: Long, toUserId: Long): Boolean {
        return friendRepository.existsByFromUserIdAndToUserId(fromUserId, toUserId)
    }

}