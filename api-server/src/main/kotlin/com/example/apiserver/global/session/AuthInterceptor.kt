package com.example.apiserver.global.session

import com.example.core.global.constant.SessionConstants.Companion.LOGIN_USER_ATTRIBUTE
import com.example.core.global.model.LoginUser
import com.example.core.global.exception.ApiException
import com.example.core.global.exception.ErrorCode
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class AuthInterceptor : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val session = request.getSession(false) ?: throw ApiException(ErrorCode.FORBIDDEN)
        val loginUser = session[LOGIN_USER_ATTRIBUTE] ?: throw ApiException(ErrorCode.FORBIDDEN)

        if (loginUser is LoginUser) {
            request.setAttribute(LOGIN_USER_ATTRIBUTE, loginUser)
        } else {
            throw ApiException(ErrorCode.BAD_REQUEST)
        }

        return true
    }

}