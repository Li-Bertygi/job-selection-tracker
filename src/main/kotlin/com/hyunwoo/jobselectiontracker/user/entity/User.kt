package com.hyunwoo.jobselectiontracker.user.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDateTime

/**
 * 認証と応募情報の所有者判定に使用するユーザーエンティティ。
 */
@Entity
@Table(
    name = "users",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_users_email", columnNames = ["email"])
    ]
)
class User(

    /** ユーザーID。 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    /** ログイン時に使用する一意なメールアドレス。 */
    @Column(nullable = false, length = 255, unique = true)
    var email: String,

    /** BCrypt などで暗号化して保存するパスワード。 */
    @Column(nullable = false, length = 255)
    var password: String,

    /** 画面表示やプロフィール確認に使用する名前。 */
    @Column(nullable = false, length = 100)
    var name: String,

    /** ユーザー作成日時。 */
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null,

    /** ユーザー更新日時。 */
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime? = null
) {

    /** 初回保存前に作成日時と更新日時を同じ現在時刻で設定する。 */
    @PrePersist
    fun prePersist() {
        val now = LocalDateTime.now()
        createdAt = now
        updatedAt = now
    }

    /** 更新前に更新日時のみを現在時刻で更新する。 */
    @PreUpdate
    fun preUpdate() {
        updatedAt = LocalDateTime.now()
    }
}
