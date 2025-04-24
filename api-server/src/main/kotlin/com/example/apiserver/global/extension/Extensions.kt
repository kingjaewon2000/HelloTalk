package com.example.apiserver.global.extension

import jakarta.servlet.http.HttpSession

operator fun <T> HttpSession.set(key: String, value: T) {
    this.setAttribute(key, value)
}