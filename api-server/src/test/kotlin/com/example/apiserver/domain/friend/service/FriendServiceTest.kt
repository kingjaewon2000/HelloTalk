package com.example.apiserver.domain.friend.service

import com.example.apiserver.domain.friend.dto.FriendInfoResponse
import com.example.apiserver.domain.friend.entity.Friend
import com.example.apiserver.domain.friend.entity.FriendStatus
import com.example.apiserver.domain.friend.entity.FriendStatus.*
import com.example.apiserver.domain.friend.repository.FriendRepository
import com.example.apiserver.domain.user.entity.User
import com.example.apiserver.domain.user.service.UserService
import com.example.core.global.model.Cursor
import com.example.core.global.exception.ApiException
import com.example.core.global.exception.ErrorCode
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class FriendServiceTest {

    @InjectMocks
    private lateinit var friendService: FriendService

    @Mock
    private lateinit var friendRepository: FriendRepository

    @Mock
    private lateinit var userService: UserService

    private val userId1 = 1L
    private val userId2 = 2L
    private val userId3 = 3L
    private lateinit var user1: User
    private lateinit var user2: User
    private lateinit var user3: User

    private val friendId1 = 10L

    private val TEST_PAGE_SIZE = 20
    private val TEST_CURSOR_KEY_SIZE = 2
    private val DELIMITER = "_"

    private fun createFriendInfoResponse(
        id: Long,
        name: String,
        userId: Long,
        username: String,
        status: FriendStatus
    ): FriendInfoResponse {
        return FriendInfoResponse(
            friendId = id,
            userId = userId,
            username = username,
            name = name,
            status = status
        )
    }

    @BeforeEach
    fun setUp() {
        user1 = User(id = userId1, username = "user1", password = "test1234", name = "테스트 계정1")
        user2 = User(id = userId2, username = "user2", password = "test1234", name = "테스트 계정2")
        user3 = User(id = userId3, username = "user3", password = "test1234", name = "테스트 계정3")
    }

    @Test
    @DisplayName("첫 페이지 조회 성공")
    fun 첫_페이지_조회_성공() {
        // given
        val loginUserId = userId1
        val status = ACCEPTED
        val cursorId: String? = null
        val fetchSize = TEST_PAGE_SIZE + 1
        val responses = (1..fetchSize).map {
            createFriendInfoResponse(it.toLong(), "테스트 $it", it.toLong(), "test$it", status)
        }
        val expectedCursor = Cursor.decode(cursorId, TEST_CURSOR_KEY_SIZE)

        // when
        whenever(
            friendRepository.findAllByFromUserId(
                loginUserId,
                status,
                expectedCursor,
                TEST_PAGE_SIZE
            )
        ).thenReturn(responses.toMutableList())

        val result = friendService.getFriends(loginUserId, status, cursorId)

        // then
        val expectedLastItem = responses[TEST_PAGE_SIZE - 1]
        val expectedNextCursorId = "${expectedLastItem.name}${DELIMITER}${expectedLastItem.userId}"

        assertThat(result.hasNext).isTrue()
        assertThat(result.cursorId).isNotNull()
        assertThat(result.data.size).isEqualTo(TEST_PAGE_SIZE)
        assertThat(result.cursorId).isEqualTo(expectedNextCursorId)
    }

    @Test
    @DisplayName("마지막 페이지 조회 성공")
    fun 마지막_페이지_조회_성공() {
        // given
        val loginUserId = userId1
        val status = ACCEPTED
        val cursorId: String? = null
        val fetchSize = TEST_PAGE_SIZE - 5
        val responses = (1..fetchSize).map {
            createFriendInfoResponse(it.toLong(), "테스트 $it", it.toLong(), "test$it", status)
        }
        val expectedCursor = Cursor.decode(cursorId, TEST_CURSOR_KEY_SIZE)

        // when
        whenever(
            friendRepository.findAllByFromUserId(
                loginUserId,
                status,
                expectedCursor,
                TEST_PAGE_SIZE
            )
        ).thenReturn(responses.toMutableList())

        val result = friendService.getFriends(loginUserId, status, cursorId)

        // then
        assertThat(result.hasNext).isFalse()
        assertThat(result.cursorId).isNull()
        assertThat(result.data.size).isEqualTo(fetchSize)
    }

    @Test
    @DisplayName("빈 결과 조회 성공")
    fun 빈_결과_조회_성공() {
        // given
        val loginUserId = userId1
        val status = ACCEPTED
        val cursorId: String? = null
        val responses = mutableListOf<FriendInfoResponse>()
        val expectedCursor = Cursor.decode(cursorId, TEST_CURSOR_KEY_SIZE)


        // when
        whenever(
            friendRepository.findAllByFromUserId(
                loginUserId,
                status,
                expectedCursor,
                TEST_PAGE_SIZE
            )
        ).thenReturn(responses.toMutableList())

        val result = friendService.getFriends(loginUserId, status, cursorId)

        // then
        assertThat(result.hasNext).isFalse()
        assertThat(result.cursorId).isNull()
        assertThat(result.data.isEmpty()).isTrue()
    }

    @Test
    @DisplayName("거절한 유저 조회 시 예외")
    fun 거절한_유저_조회_시_예외() {
        // given
        val loginUserId = userId1
        val status = REJECTED
        val cursorId: String? = null

        // when
        assertThatThrownBy {
            friendService.getFriends(loginUserId, status, cursorId)
        }.isInstanceOf(ApiException::class.java)
            .hasMessage(ErrorCode.UN_SUPPORTED_OPERATION.message)
    }

    @Test
    @DisplayName("친구 추가 성공")
    fun 친구_추가_성공() {
        // given
        val fromUser = user1
        val toUser = user2

        // when
        whenever(userService.findByIdOrThrow(fromUser.id)).thenReturn(fromUser)
        whenever(userService.findByUsernameOrThrow(toUser.username)).thenReturn(toUser)
        friendService.addFriend(fromUser.id, toUser.username)
    }

    @Test
    @DisplayName("자기 자신 추가 시 예외 발생")
    fun 자기_자신_추가_시_예외_발생() {
        // given
        val fromUser = user1
        val toUser = user1

        // when
        whenever(userService.findByIdOrThrow(fromUser.id)).thenReturn(fromUser)
        whenever(userService.findByUsernameOrThrow(toUser.username)).thenReturn(toUser)
        assertThatThrownBy {
            friendService.addFriend(fromUser.id, toUser.username)
        }.isInstanceOf(ApiException::class.java)
            .hasMessage(ErrorCode.NOT_ALLOW_SELF_ADD_FRIEND.message)
    }

    @Test
    @DisplayName("이미 친구 요청 상태일 때 예외 발생")
    fun 이미_친구_요청_상태일_때_예외_발생() {
        // given
        val fromUser = user1
        val toUser = user2

        // when
        whenever(userService.findByIdOrThrow(fromUser.id)).thenReturn(fromUser)
        whenever(userService.findByUsernameOrThrow(toUser.username)).thenReturn(toUser)
        whenever(friendRepository.existsByFromUserIdAndToUserId(fromUser.id, toUser.id)).thenReturn(true)
        assertThatThrownBy {
            friendService.addFriend(fromUser.id, toUser.username)
        }.isInstanceOf(ApiException::class.java)
            .hasMessage(ErrorCode.NOT_ALLOW_ALREADY_ADDED_FRIEND.message)
    }

    @Test
    @DisplayName("친구 요청 수락 성공")
    fun 친구_요청_수락_성공() {
        // given
        val loginUserId = userId1
        val friendId = friendId1
        val mockFriend = Friend(
            id = friendId,
            fromUser = user1,
            toUser = user2,
            requesterUser = user1,
            status = PENDING,
        )

        // when
        whenever(friendRepository.findFriendById(friendId)).thenReturn(mockFriend)

        friendService.acceptFriend(loginUserId, friendId)

        // then
        assertThat(mockFriend.status).isEqualTo(ACCEPTED)
    }

    @Test
    @DisplayName("친구 요청 거절 성공")
    fun 친구_요청_거절_성공() {
        // given
        val loginUserId = userId1
        val friendId = friendId1
        val mockFriend = Friend(
            id = friendId,
            fromUser = user1,
            toUser = user2,
            requesterUser = user1,
            status = PENDING,
        )

        // when
        whenever(friendRepository.findFriendById(friendId)).thenReturn(mockFriend)

        friendService.rejectFriend(loginUserId, friendId)

        // then
        assertThat(mockFriend.status).isEqualTo(REJECTED)
    }

    @Test
    @DisplayName("친구 차단 성공 - PENDING")
    fun 친구_차단_성공_PENDING() {
        // given
        val loginUserId = userId1
        val friendId = friendId1
        val mockFriend = Friend(
            id = friendId,
            fromUser = user1,
            toUser = user2,
            requesterUser = user1,
            status = PENDING,
        )

        // when
        whenever(friendRepository.findFriendById(friendId)).thenReturn(mockFriend)

        friendService.blockFriend(loginUserId, friendId)

        // then
        assertThat(mockFriend.status).isEqualTo(BLOCKED)
    }

    @Test
    @DisplayName("친구 차단 성공 - ACCEPTED")
    fun 친구_차단_성공_ACCEPTED() {
        // given
        val loginUserId = userId1
        val friendId = friendId1
        val mockFriend = Friend(
            id = friendId,
            fromUser = user1,
            toUser = user2,
            requesterUser = user1,
            status = ACCEPTED,
        )

        // when
        whenever(friendRepository.findFriendById(friendId)).thenReturn(mockFriend)

        friendService.blockFriend(loginUserId, friendId)

        // then
        assertThat(mockFriend.status).isEqualTo(BLOCKED)
    }

}