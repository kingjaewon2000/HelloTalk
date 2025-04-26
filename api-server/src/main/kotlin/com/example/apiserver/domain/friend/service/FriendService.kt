package com.example.apiserver.domain.friend.service

import com.example.apiserver.domain.friend.dto.FriendInfoResponse
import com.example.apiserver.domain.friend.entity.Friend
import com.example.apiserver.domain.friend.entity.FriendStatus
import com.example.apiserver.domain.friend.entity.FriendStatus.*
import com.example.apiserver.domain.friend.repository.FriendRepository
import com.example.apiserver.domain.user.entity.User
import com.example.apiserver.domain.user.service.UserService
import com.example.core.global.api.ApiCursorResponse
import com.example.core.global.common.CursorInfo
import com.example.core.global.common.CursorInfo.Companion.DELIMITER
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

    companion object {
        private const val PAGE_SIZE = 20
        private const val CURSOR_KEY_SIZE = 2
    }

    fun findByIdOrThrow(friendId: Long): Friend {
        return friendRepository.findFriendById(friendId)
            ?: throw ApiException(ErrorCode.UN_SUPPORTED_OPERATION)
    }

    fun getFriends(loginUserId: Long, status: FriendStatus, cursorId: String?): ApiCursorResponse<FriendInfoResponse> {
        // 조회 상태 검사(PENDING, ACCEPTED, BLOCKED) 지원
        validateStatus(status)

        val cursorInfo = CursorInfo.decode(cursorId, CURSOR_KEY_SIZE)
        val response = friendRepository.findAllByFromUserId(loginUserId, status, cursorInfo, PAGE_SIZE)
        val nextCursor = createNextCursorId(response)

        return ApiCursorResponse(
            hasNext = nextCursor.first,
            cursorId = nextCursor.second,
            data = response
        )
    }

    @Transactional
    fun addFriend(fromUserId: Long, toUsername: String) {
        val fromUser = userService.findByIdOrThrow(fromUserId)
        val toUser = userService.findByUsernameOrThrow(toUsername)

        // 자기 자신을 친구 추가 시 예외
        if (isSameUser(fromUser.id, toUser.id)) throw ApiException(ErrorCode.NOT_ALLOW_SELF_ADD_FRIEND)

        // 이미 친구 추가된 사용자면 예외
        if (isFriendExists(fromUser.id, toUser.id)) throw ApiException(ErrorCode.NOT_ALLOW_ALREADY_ADDED_FRIEND)

        // 친구 관계 생성(양방향 관계 생성)
        val requester = createFriendship(fromUser, toUser)

        friendRepository.saveAll(requester)
    }

    @Transactional
    fun acceptFriend(loginUserId: Long, friendId: Long) {
        updateFriendStatus(
            loginUserId = loginUserId,
            friendId = friendId,
            targetStatus = ACCEPTED,
            expectedStatus = setOf(PENDING)
        )
    }

    @Transactional
    fun rejectFriend(loginUserId: Long, friendId: Long) {
        updateFriendStatus(
            loginUserId = loginUserId,
            friendId = friendId,
            targetStatus = REJECTED,
            expectedStatus = setOf(PENDING)
        )
    }

    @Transactional
    fun blockFriend(loginUserId: Long, friendId: Long) {
        updateFriendStatus(
            loginUserId = loginUserId,
            friendId = friendId,
            targetStatus = BLOCKED,
            expectedStatus = setOf(PENDING, ACCEPTED)
        )
    }

    /*
     * 내부 메서드
     */
    private fun validateStatus(status: FriendStatus) =
        when (status) {
            PENDING -> status
            ACCEPTED -> status
            BLOCKED -> status
            else -> throw ApiException(ErrorCode.UN_SUPPORTED_OPERATION)
        }

    private fun createNextCursorId(friends: MutableList<FriendInfoResponse>): Pair<Boolean, String?> {
        if (friends.isEmpty() || friends.size <= PAGE_SIZE) return Pair(false, null)

        friends.removeLast()

        val tailName = friends[friends.size - 1].name
        val tailUserId = friends[friends.size - 1].userId
        val cursorId = "${tailName}${DELIMITER}${tailUserId}"

        return Pair(true, cursorId)
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
            status = PENDING
        )

        return listOf(requester, requested)
    }

    private fun isSameUser(fromUserId: Long, toUserId: Long): Boolean {
        return fromUserId == toUserId
    }

    private fun isFriendExists(fromUserId: Long, toUserId: Long): Boolean {
        return friendRepository.existsByFromUserIdAndToUserId(fromUserId, toUserId)
    }

    private fun updateFriendStatus(
        loginUserId: Long,
        friendId: Long,
        targetStatus: FriendStatus,
        expectedStatus: Set<FriendStatus>
    ) {
        val friend = findByIdOrThrow(friendId)

        if (isDifferentUser(loginUserId, friend.fromUser.id)) {
            throw ApiException(ErrorCode.UN_SUPPORTED_OPERATION)
        }

        if (friend.status == targetStatus) return

        if (friend.status !in expectedStatus) {
            throw ApiException(ErrorCode.UN_SUPPORTED_OPERATION)
        }

        friend.status = targetStatus
    }

    private fun isDifferentUser(loginUserId: Long, fromUserId: Long) =
        !isSameUser(loginUserId, fromUserId)

}