package com.hyunwoo.jobselectiontracker.user.repository

import com.hyunwoo.jobselectiontracker.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository

/**
 * ユーザーエンティティの永続化と認証時の検索を担当するリポジトリ。
 */
interface UserRepository : JpaRepository<User, Long> {

    /**
     * メールアドレスでユーザーを検索する。
     */
    fun findByEmail(email: String): User?

    /**
     * 指定したメールアドレスのユーザーがすでに存在するか確認する。
     */
    fun existsByEmail(email: String): Boolean
}
