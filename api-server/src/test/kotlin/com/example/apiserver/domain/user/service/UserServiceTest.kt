package com.example.apiserver.domain.user.service

import com.example.apiserver.domain.user.dto.UserCreateRequest
import com.example.apiserver.domain.user.entity.User
import com.example.apiserver.domain.user.repository.UserRepository
import com.example.apiserver.dummy.Dummy.Companion.mockUser
import com.example.core.common.exception.ApiException
import com.example.core.common.exception.ErrorCode
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class UserServiceTest {

    @InjectMocks
    private lateinit var userService: UserService

    @Mock
    private lateinit var userRepository: UserRepository

    private val USERNAME: String = "testUser"
    private val PASSWORD: String = "password"
    private val NAME: String = "테스트"

    private lateinit var user: User

    @BeforeEach
    fun setUp() {
        user = mockUser(
            id = 1,
            username = USERNAME,
            password = PASSWORD,
            name = NAME
        )
    }

    /*
     * 회원가입 테스트
     */
    @Test
    @DisplayName("회원가입 성공")
    fun 회원가입_성공() {
        // given
        val request = UserCreateRequest(
            username = USERNAME,
            password = PASSWORD,
            name = NAME,
        )

        // when
        whenever(userRepository.existsByUsername(request.username)).thenReturn(false)
        whenever(userRepository.save(any<User>())).thenReturn(user)
        val response = userService.createUser(request)

        // then
        assertThat(response).isNotNull
        assertThat(response.id).isEqualTo(user.id)
    }

    @Test
    @DisplayName("회원가입 실패 - 아이디 중복")
    fun 회원가입_실패_아이디_중복() {
        // given
        val request = UserCreateRequest(
            username = USERNAME,
            password = PASSWORD,
            name = NAME,
        )

        // when
        whenever(userRepository.existsByUsername(request.username)).thenReturn(true)
        assertThatThrownBy {
            userService.createUser(request)
        }
            .isInstanceOf(ApiException::class.java)
            .hasMessage(ErrorCode.USER_ALREADY_EXISTS.message)
    }

}