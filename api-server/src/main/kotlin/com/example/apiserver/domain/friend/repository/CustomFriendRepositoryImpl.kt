package com.example.apiserver.domain.friend.repository

import com.example.apiserver.domain.friend.dto.FriendInfoInitial
import com.example.apiserver.domain.friend.entity.Friend
import com.example.apiserver.domain.friend.entity.FriendStatus
import com.example.apiserver.domain.user.entity.User
import com.example.core.global.model.Cursor
import com.linecorp.kotlinjdsl.dsl.jpql.Jpql
import com.linecorp.kotlinjdsl.dsl.jpql.jpql
import com.linecorp.kotlinjdsl.querymodel.jpql.predicate.Predicate
import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderContext
import com.linecorp.kotlinjdsl.support.spring.data.jpa.extension.createQuery
import jakarta.persistence.EntityManager

class CustomFriendRepositoryImpl(
    private val context: JpqlRenderContext,
    private val entityManager: EntityManager
) : CustomFriendRepository {

    companion object {
        const val NEXT_PAGE = 1
    }

    override fun findAllByFromUserId(
        userId: Long,
        status: FriendStatus,
        cursor: Cursor?,
        limit: Int
    ): MutableList<FriendInfoInitial> {
        val query = jpql {
            selectNew<FriendInfoInitial>(
                path(Friend::id),
                path(Friend::toUser)(User::id),
                path(Friend::toUser)(User::username),
                path(Friend::toUser)(User::name),
                path(Friend::status)
            ).from(
                entity(Friend::class),
                innerJoin(Friend::toUser)
            ).where(
                and(
                    path(Friend::fromUser)(User::id).eq(userId),
                    path(Friend::status).eq(status),
                    cursorCondition(cursor)
                )
            ).orderBy(
                path(Friend::toUser)(User::name).asc(),
                path(Friend::toUser)(User::id).asc()
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

        val (name, userId) = cursor

        return path(Friend::toUser)(User::name).gt(name).or(
            path(Friend::toUser)(User::name).eq(name).and(
                path(Friend::toUser)(User::id).gt(userId?.toLong())
            )
        )
    }

}