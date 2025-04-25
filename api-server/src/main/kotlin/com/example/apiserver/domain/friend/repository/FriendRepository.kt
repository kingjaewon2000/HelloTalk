package com.example.apiserver.domain.friendship.repository

import com.example.apiserver.domain.friendship.entity.Friend
import org.springframework.data.jpa.repository.JpaRepository

interface FriendRepository : JpaRepository<Friend, Long>