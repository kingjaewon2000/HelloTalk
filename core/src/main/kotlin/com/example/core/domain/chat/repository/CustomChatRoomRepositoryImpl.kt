package com.example.core.domain.chat.repository

import com.example.core.domain.chat.dto.RoomInfoResponse
import com.example.core.domain.chat.entity.ChatRoom
import com.example.core.domain.chat.entity.ChatRoomUser
import com.example.core.global.model.Cursor
import com.linecorp.kotlinjdsl.dsl.jpql.Jpql
import com.linecorp.kotlinjdsl.dsl.jpql.jpql
import com.linecorp.kotlinjdsl.querymodel.jpql.predicate.Predicate
import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderContext
import com.linecorp.kotlinjdsl.support.spring.data.jpa.extension.createQuery
import jakarta.persistence.EntityManager
import java.time.LocalDateTime

class CustomChatRoomRepositoryImpl(
    private val context: JpqlRenderContext,
    private val entityManager: EntityManager
) : CustomChatRoomRepository {

    companion object {
        const val NEXT_PAGE = 1
    }

    override fun findAllByUserId(userId: Long, cursor: Cursor?, limit: Int): MutableList<RoomInfoResponse> {
        val query = jpql {
            selectNew<RoomInfoResponse>(
                path(ChatRoom::id),
                path(ChatRoom::type),
                path(ChatRoom::name),
                path(ChatRoom::lastActivityAt)
            ).from(
                entity(ChatRoom::class),
                innerJoin(ChatRoomUser::class).on(path(ChatRoom::id).eq(path(ChatRoomUser::roomId)))
            ).where(
                and(
                    path(ChatRoomUser::userId).eq(userId),
                    cursorCondition(cursor)
                )
            ).orderBy(
                path(ChatRoom::lastActivityAt).desc(),
                path(ChatRoom::id).desc()
            )
        }

        return entityManager.createQuery(query, context)
            .setMaxResults(limit + NEXT_PAGE)
            .resultList
    }

    private fun Jpql.cursorCondition(
        cursor: Cursor?
    ): Predicate? {
        if (cursor == null) return null

        val lastActivityAt = LocalDateTime.parse(cursor.component1())
        val roomId = cursor.component2().toLong()

        return path(ChatRoom::lastActivityAt).lt(lastActivityAt)
            .or(
                path(ChatRoom::lastActivityAt).eq(lastActivityAt).and(
                    path(ChatRoom::id).lt(roomId)
                )
            )
    }

}