package com.example.apiserver.global.session

import jakarta.servlet.http.HttpSession

operator fun <T> HttpSession.set(key: String, value: T) {
    this.setAttribute(key, value)
}

operator fun HttpSession.get(key: String): Any? = this.getAttribute(key)