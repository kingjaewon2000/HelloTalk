package com.example.apiserver.domain.chat.service

import com.example.apiserver.domain.chat.dto.DirectRoomCreateRequest
import com.example.apiserver.domain.chat.dto.GroupRoomCreateRequest
import com.example.apiserver.domain.user.repository.UserRepository
import com.example.core.domain.chat.dto.RoomInfoResponse
import com.example.core.domain.chat.entity.ChatRoom
import com.example.core.domain.chat.entity.ChatRoomUser
import com.example.core.domain.chat.entity.RoomType
import com.example.core.domain.chat.entity.RoomType.DIRECT
import com.example.core.domain.chat.entity.RoomType.GROUP
import com.example.core.domain.chat.repository.ChatRoomRepository
import com.example.core.domain.chat.repository.ChatRoomUserRepository
import com.example.core.global.api.ApiCursorResponse
import com.example.core.global.exception.ApiException
import com.example.core.global.exception.ErrorCode
import com.example.core.global.model.Cursor
import com.example.core.global.model.Cursor.Companion.DELIMITER
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import kotlin.jvm.optionals.getOrNull

@Service
@Transactional(readOnly = true)
class ChatRoomService(
    private val chatRoomRepository: ChatRoomRepository,
    private val chatRoomUserRepository: ChatRoomUserRepository,
    private val userRepository: UserRepository
) {

    companion object {
        private const val GROUP_MIN_SIZE = 3
        private const val GROUP_MAX_SIZE = 1000
        private const val DEFAULT_GROUP_NAME = "그룹 채팅방"

        // 커서 관련
        private const val PAGE_SIZE = 20
        private const val CURSOR_KEY_SIZE = 2
    }

    fun findByRoomId(roomId: Long): RoomInfoResponse {
        val room = chatRoomRepository.findById(roomId).getOrNull() ?: throw ApiException(ErrorCode.CHAT_ROOM_NOT_FOUND)

        return RoomInfoResponse(
            roomId = room.id,
            type = room.type,
            roomName = room.name,
            lastActivityAt = room.lastActivityAt
        )
    }

    fun findAllByUserId(loginUserId: Long, cursorId: String?): ApiCursorResponse<RoomInfoResponse> {
        val cursor = Cursor.decode(cursorId, CURSOR_KEY_SIZE)
        val response = chatRoomRepository.findAllByUserId(loginUserId, cursor, PAGE_SIZE)
        val nextCursor = createNextCursorId(response)

        return ApiCursorResponse(
            hasNext = nextCursor.first,
            cursorId = nextCursor.second,
            data = response
        )
    }

    @Transactional
    fun createDirectChatRoom(loginUserId: Long, request: DirectRoomCreateRequest): ChatRoom {
        val participantId = request.userId

        if (loginUserId == participantId) {
            throw ApiException(ErrorCode.NOT_ALLOWED_SELF_INVITATION)
        }

        val participantIds = mutableSetOf(loginUserId, participantId)
        validateUsersExist(participantIds)

        val roomName = participantIds.joinToString(separator = "_")

        return createChatRoomInternal(
            type = DIRECT,
            roomName = roomName,
            participantIds = participantIds
        )
    }

    @Transactional
    fun createGroupChatRoom(loginUserId: Long, request: GroupRoomCreateRequest): ChatRoom {
        val participantIds = request.participantIds.toMutableSet()
        participantIds.add(loginUserId)

        if (participantIds.size < GROUP_MIN_SIZE || participantIds.size > GROUP_MAX_SIZE) {
            throw ApiException(ErrorCode.INVALID_GROUP_SIZE)
        }

        validateUsersExist(participantIds)

        val roomName = request.roomName.ifBlank { DEFAULT_GROUP_NAME }

        return createChatRoomInternal(
            type = GROUP,
            roomName = roomName,
            participantIds = participantIds
        )
    }

    private fun validateUsersExist(userIds: Set<Long>) {
        if (userIds.isEmpty()) throw ApiException(ErrorCode.USER_NOT_FOUND)

        val verifiedUserIds = userRepository.findByIdsIn(userIds)
        if (verifiedUserIds.size != userIds.size) {
            throw ApiException(ErrorCode.USER_NOT_FOUND)
        }
    }

    /*
     * 내부 메서드
     */
    private fun createChatRoomInternal(
        type: RoomType,
        roomName: String,
        participantIds: Set<Long>
    ): ChatRoom {
        val room = ChatRoom(
            type = type,
            name = roomName,
            lastActivityAt = LocalDateTime.now()
        )

        chatRoomRepository.save(room)
        val roomId = room.id

        val participantUsers = createChatRoomUserEntities(roomId, participantIds)
        chatRoomUserRepository.saveAll(participantUsers)

        return room
    }

    private fun createChatRoomUserEntities(roomId: Long, userIds: Set<Long>): List<ChatRoomUser> {
        return userIds.map { userId ->
            ChatRoomUser(
                roomId = roomId,
                userId = userId,
                lastReadMessageId = null
            )
        }
    }

    private fun createNextCursorId(rooms: MutableList<RoomInfoResponse>): Pair<Boolean, String?> {
        if (rooms.isEmpty() || rooms.size <= PAGE_SIZE) return Pair(false, null)

        rooms.removeLast()

        val tailActivityAt = rooms[rooms.size - 1].lastActivityAt
        val tailRoomId = rooms[rooms.size - 1].roomId
        val cursorId = "${tailActivityAt}$DELIMITER${tailRoomId}"

        return Pair(true, cursorId)
    }

}
