package com.example.apiserver.domain.auth.service

import com.example.apiserver.domain.auth.dto.LoginRequest
import com.example.apiserver.domain.user.entity.User
import com.example.apiserver.domain.user.repository.UserRepository
import com.example.apiserver.dummy.Dummy.Companion.mockUser
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
class AuthServiceTest {

    @InjectMocks
    private lateinit var authService: AuthService

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
     * 로그인 테스트
     */
    @Test
    @DisplayName("로그인 성공")
    fun 로그인_성공() {
        // given
        val request = LoginRequest(
            username = USERNAME,
            password = PASSWORD
        )

        // when
        whenever(userRepository.findByUsername(request.username)).thenReturn(user)
        val response = authService.login(request)

        // then
        assertThat(response).isNotNull
        assertThat(response.userId).isEqualTo(user.id)
        assertThat(response.username).isEqualTo(user.username)
    }

    @Test
    @DisplayName("로그인 실패 - 아이디 틀림")
    fun 로그인_실패_아이디_틀림() {
        // given
        val request = LoginRequest(
            username = "fail",
            password = PASSWORD
        )

        // when
        whenever(userRepository.findByUsername(request.username)).thenReturn(null)
        assertThatThrownBy {
            authService.login(request)
        }
            .isInstanceOf(ApiException::class.java)
            .hasMessage(ErrorCode.UNAUTHORIZED.message)
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 틀림")
    fun 로그인_실패_비밀번호_틀림() {
        // given
        val request = LoginRequest(
            username = USERNAME,
            password = "fail"
        )

        // when
        whenever(userRepository.findByUsername(request.username)).thenReturn(user)
        assertThatThrownBy {
            authService.login(request)
        }
            .isInstanceOf(ApiException::class.java)
            .hasMessage(ErrorCode.UNAUTHORIZED.message)
    }

}