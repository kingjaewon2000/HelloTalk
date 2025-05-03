package com.example.apiserver.domain.chat.service

import com.example.apiserver.domain.chat.dto.DirectRoomCreateRequest
import com.example.apiserver.domain.chat.dto.GroupRoomCreateRequest
import com.example.apiserver.domain.user.repository.UserRepository
import com.example.core.domain.chat.dto.RoomInfoResponse
import com.example.core.domain.chat.entity.ChatRoom
import com.example.core.domain.chat.entity.RoomType
import com.example.core.domain.chat.entity.RoomType.DIRECT
import com.example.core.domain.chat.repository.ChatRoomRepository
import com.example.core.domain.chat.repository.ChatRoomUserRepository
import com.example.core.global.exception.ApiException
import com.example.core.global.exception.ErrorCode
import com.example.core.global.model.Cursor
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
class ChatRoomServiceTest {

    @InjectMocks
    private lateinit var chatRoomService: ChatRoomService

    @Mock
    private lateinit var chatRoomRepository: ChatRoomRepository

    @Mock
    private lateinit var chatRoomUserRepository: ChatRoomUserRepository

    @Mock
    private lateinit var userRepository: UserRepository

    private val PAGE_SIZE = 20
    private val CURSOR_KEY_SIZE = 2

    private val loginUserId = 1.toLong()
    private val roomId = 1.toLong()
    private val now: LocalDateTime = LocalDateTime.now()

    private fun createRoomInfoResponseList(size: Int): MutableList<RoomInfoResponse> {
        return (1..size).map { i ->
            RoomInfoResponse(
                roomId = (roomId + i),
                type = if (i % 2 == 0) DIRECT else RoomType.GROUP,
                roomName = "Room $i",
                lastActivityAt = now.minusMinutes(i.toLong())
            )
        }.toMutableList()
    }

    @Test
    @DisplayName("채팅방 ID로 조회")
    fun 아이디로_채팅방_조회() {
        // given
        val roomName = "1_2"
        val room = ChatRoom(id = roomId, type = DIRECT, name = roomName, lastActivityAt = now)

        // when
        whenever(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(room))

        val response = chatRoomService.findByRoomId(roomId)

        // then
        assertThat(response).isNotNull
        assertThat(response.roomId).isEqualTo(roomId)
        assertThat(response.type).isEqualTo(DIRECT)
        assertThat(response.roomName).isEqualTo(roomName)
        assertThat(response.lastActivityAt).isEqualTo(now)
    }

    @Test
    @DisplayName("채팅방 ID로 조회 실패")
    fun 아이디로_채팅방_조회_실패() {
        // when
        whenever(chatRoomRepository.findById(roomId)).thenReturn(Optional.empty())

        // then
        assertThatThrownBy {
            chatRoomService.findByRoomId(roomId)
        }
            .isInstanceOf(ApiException::class.java)
            .hasMessage(ErrorCode.CHAT_ROOM_NOT_FOUND.message)
    }

    @Test
    @DisplayName("사용자 ID로 채팅방 목록 조회 - 첫 페이지")
    fun 사용자_아이디로_채팅방_목록_조회_첫_페이지() {
        // given
        val rooms = createRoomInfoResponseList(PAGE_SIZE + 1)
        val cursorId = null
        val expectedCursor = Cursor.decode(cursorId, CURSOR_KEY_SIZE)

        // when
        whenever(chatRoomRepository.findAllByUserId(loginUserId, expectedCursor, PAGE_SIZE)).thenReturn(rooms)

        val response = chatRoomService.findAllByUserId(loginUserId, cursorId)

        // then
        assertThat(response.hasNext).isEqualTo(true)
        assertThat(response.cursorId).isNotNull()
        assertThat(response.data.size).isEqualTo(PAGE_SIZE)
    }

    @Test
    @DisplayName("사용자 ID로 채팅방 목록 조회 - 마지막 페이지")
    fun 사용자_아이디로_채팅방_목록_조회_마지막_페이지() {
        // given
        val rooms = createRoomInfoResponseList(PAGE_SIZE - 1)
        val cursorId = null
        val expectedCursor = Cursor.decode(cursorId, CURSOR_KEY_SIZE)

        // when
        whenever(chatRoomRepository.findAllByUserId(loginUserId, expectedCursor, PAGE_SIZE)).thenReturn(rooms)

        val response = chatRoomService.findAllByUserId(loginUserId, cursorId)

        // then
        assertThat(response.hasNext).isEqualTo(false)
        assertThat(response.cursorId).isNull()
        assertThat(response.data.size).isEqualTo(PAGE_SIZE - 1)
    }

    @Test
    @DisplayName("1:1 채팅방 생성")
    fun 일대일_채팅방_생성() {
        // given
        val participantId = 2.toLong()
        val roomName = "${loginUserId}_${participantId}"

        val request = DirectRoomCreateRequest(userId = participantId)
        val participantIds = setOf(loginUserId, participantId)
        val lastActivityAt = LocalDateTime.now()
        val room = ChatRoom(
            id = roomId,
            type = DIRECT,
            name = roomName,
            lastActivityAt = lastActivityAt
        )

        // when
        whenever(userRepository.findByIdsIn(participantIds)).thenReturn(participantIds)
        whenever(chatRoomRepository.save(any<ChatRoom>())).thenReturn(room)

        val createdRoom = chatRoomService.createDirectChatRoom(loginUserId, request)

        // then
        assertThat(createdRoom).isNotNull
        assertThat(createdRoom.id).isEqualTo(roomId)
        assertThat(createdRoom.type).isEqualTo(DIRECT)
        assertThat(createdRoom.name).isEqualTo(roomName)
        assertThat(createdRoom.lastActivityAt).isEqualTo(lastActivityAt)
    }

    @Test
    @DisplayName("1:1 채팅방 생성 - 자기 자신으로 채팅방 생성 시도")
    fun 일대일_채팅방_생성_자기_자신을_초대() {
        // given
        val request = DirectRoomCreateRequest(userId = loginUserId)

        // when
        assertThatThrownBy {
            chatRoomService.createDirectChatRoom(loginUserId, request)
        }
            .isInstanceOf(ApiException::class.java)
            .hasMessage(ErrorCode.NOT_ALLOWED_SELF_INVITATION.message)
    }

    @Test
    @DisplayName("그룹 채팅방 생성")
    fun 그룹_채팅방_생성() {
        // given
        val participantIds = setOf(1L, 2L, 3L)
        val roomName = "그룹 채팅방 생성 테스트"

        val request = GroupRoomCreateRequest(roomName = "그룹 채팅방 생성 테스트", participantIds = participantIds.toList())
        val lastActivityAt = LocalDateTime.now()
        val room = ChatRoom(
            id = roomId,
            type = DIRECT,
            name = roomName,
            lastActivityAt = lastActivityAt
        )

        // when
        whenever(userRepository.findByIdsIn(participantIds)).thenReturn(participantIds)
        whenever(chatRoomRepository.save(any<ChatRoom>())).thenReturn(room)

        val createdRoom = chatRoomService.createGroupChatRoom(loginUserId, request)

        // then
        assertThat(createdRoom).isNotNull
        assertThat(createdRoom.id).isEqualTo(roomId)
        assertThat(createdRoom.type).isEqualTo(DIRECT)
        assertThat(createdRoom.name).isEqualTo(roomName)
        assertThat(createdRoom.lastActivityAt).isEqualTo(lastActivityAt)
    }

    @Test
    @DisplayName("그룹 채팅방 생성 - 2명으로 채팅방 생성 시도")
    fun 그룹_채팅방_생성_2명_이하로_생성() {
        // given
        val participantIds = setOf(1L, 2L)
        val request = GroupRoomCreateRequest(roomName = "그룹 채팅방 생성 테스트", participantIds = participantIds.toList())

        // when
        assertThatThrownBy {
            chatRoomService.createGroupChatRoom(loginUserId, request)
        }
            .isInstanceOf(ApiException::class.java)
            .hasMessage(ErrorCode.INVALID_GROUP_SIZE.message)
    }

    @Test
    @DisplayName("그룹 채팅방 생성 - 1001명으로 채팅방 생성 시도")
    fun 그룹_채팅방_생성_1001명_이상으로_생성() {
        // given
        val participantIds = LongRange(1, 1001).toSet()
        val request = GroupRoomCreateRequest(roomName = "그룹 채팅방 생성 테스트", participantIds = participantIds.toList())

        // when
        assertThatThrownBy {
            chatRoomService.createGroupChatRoom(loginUserId, request)
        }
            .isInstanceOf(ApiException::class.java)
            .hasMessage(ErrorCode.INVALID_GROUP_SIZE.message)
    }

}