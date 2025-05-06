package com.example.apiserver.global.init

import com.example.apiserver.domain.friend.entity.Friend
import com.example.apiserver.domain.friend.entity.FriendStatus
import com.example.apiserver.domain.friend.repository.FriendRepository
import com.example.apiserver.domain.user.entity.User
import com.example.apiserver.domain.user.repository.UserRepository
import jakarta.annotation.PostConstruct
import org.mindrot.jbcrypt.BCrypt
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class DataInit(
    private val userRepository: UserRepository,
    private val friendRepository: FriendRepository,
) {

    companion object {
        private const val VIRTUAL_USERS: Int = 50
    }

    @PostConstruct
    @Transactional
    fun init() {
        val users = userDataInit()
        friendDataInit(users)
    }

    fun userDataInit(): List<User> {
        // 30개 성씨
        val firstNames = listOf(
            "김", "이", "박", "최", "정", "강", "조", "윤", "장", "임", "한", "오", "서", "신", "권", "황", "안", "송", "유", "홍",
            "전", "고", "문", "손", "양", "배", "백", "허", "남", "심"
        )

        // 100개 이름
        val secondNames = listOf(
            "지훈", "서연", "민준", "지우", "서준", "하윤", "하준", "서아", "도윤", "아윤", "시우", "지아", "주원", "아린", "예준", "하은",
            "유준", "나은", "현우", "수 아", "건우", "예나", "우진", "유나", "선우", "시아", "다온", "소율", "이안", "예린", "로운", "지유",
            "연우", "다은", "은우", "채원", "정우", "예서", "준서", "리안", "재현", "가은", "태민", "라온", "은성", "소은", "도현", "민서",
            "지성", "아영", "민재", "보민", "유찬", "서현", "시현", "하율", "승현", "지민", "재윤", "예원", "유현", "서영", "지환", "나윤",
            "도하", "하린", "하민", "예주", "시윤", "윤서", "태윤", "민지", "주하", "지안", "은찬", "예지", "승우", "하영", "준영", "소현",
            "찬희", "아현", "시온", "아라", "승민", "민아", "민성", "수현", "은호", "유진", "준혁", "윤아", "민규", "지윤", "현준", "서윤",
            "지호", "예은", "찬우", "다현"
        )

        val users = mutableListOf<User>()
        val hashpw = BCrypt.hashpw("test1234", BCrypt.gensalt())

        for (id in 1..VIRTUAL_USERS) {
            val firstNameIndex = (id - 1) % firstNames.size
            val secondNameIndex = (id - 1) % secondNames.size

            val name = "${firstNames[firstNameIndex]}${secondNames[secondNameIndex]}"

            val user = User(
                username = "test${id}",
                password = hashpw,
                name = name,
            )

            users.add(user)
        }

        return userRepository.saveAll(users)
    }

    fun friendDataInit(users: List<User>) {
        val friends = mutableListOf<Friend>()

        for (i in 0 until VIRTUAL_USERS) {
            for (j in i until VIRTUAL_USERS) {
                if (i == j) continue

                val fromUser = users[i]
                val toUser = users[j]

                val requester = Friend(
                    fromUser = fromUser,
                    toUser = toUser,
                    requesterUser = fromUser,
                    status = FriendStatus.ACCEPTED
                )

                val requested = Friend(
                    fromUser = toUser,
                    toUser = fromUser,
                    requesterUser = fromUser,
                    status = FriendStatus.PENDING
                )

                friends.add(requester)
                friends.add(requested)
            }
        }

        friendRepository.saveAll(friends)
    }
}