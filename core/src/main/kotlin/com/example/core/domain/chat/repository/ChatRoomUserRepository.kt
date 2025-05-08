package com.example.core.domain.chat.repository

import com.example.core.domain.chat.entity.ChatRoomUser
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ChatRoomUserRepository : JpaRepository<ChatRoomUser, Long> {

    @Query("SELECT cu.userId FROM ChatRoomUser cu WHERE cu.roomId = :roomId")
    fun findUserIdsByRoomId(@Param("roomId") roomId: Long): Set<Long>

    fun existsByRoomIdAndUserId(@Param("roomId") roomId: Long, @Param("userId") userId: Long): Boolean

    fun findByUserIdAndRoomId(userId: Long, roomId: Long): ChatRoomUser?

}