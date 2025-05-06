package com.example.chatserver.global.interceptor

import com.example.core.global.constant.SessionConstants
import com.example.core.global.model.LoginUser
import jakarta.servlet.http.HttpSession
import org.springframework.http.HttpStatus
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.HandshakeInterceptor

@Component
class AuthHandshakeInterceptor : HandshakeInterceptor {

    companion object {
        private const val USER_ID = "userId"
        private const val USERNAME = "username"
    }

    override fun beforeHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        attributes: MutableMap<String, Any>
    ): Boolean {
        request as ServletServerHttpRequest
        val servletRequest = request.servletRequest
        val session = servletRequest.getSession(false)

        return try {
            val loginUser = getLoginUserFromSession(session)

            loginUser ?: let {
                response.setStatusCode(HttpStatus.UNAUTHORIZED)

                return false
            }

            attributes[USER_ID] = loginUser.userId
            attributes[USERNAME] = loginUser.username

            true
        } catch (_: Exception) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED)

            false
        }
    }

    override fun afterHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        exception: Exception?
    ) {
        exception ?: return
    }

    private fun getLoginUserFromSession(session: HttpSession): LoginUser? {
        return session.getAttribute(SessionConstants.LOGIN_USER_ATTRIBUTE) as LoginUser?
    }
}