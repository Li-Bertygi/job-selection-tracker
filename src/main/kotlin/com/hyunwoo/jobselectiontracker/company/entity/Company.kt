package com.hyunwoo.jobselectiontracker.company.entity

import com.hyunwoo.jobselectiontracker.user.entity.User
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import java.time.LocalDateTime

/**
 * 応募先企業を表す JPA エンティティ。
 * 各企業データは現在ログイン中ユーザーごとに所有される。
 */
@Entity
@Table(name = "companies")
class Company(

    /** 企業ID。 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    /** この企業データの所有者ユーザー。 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    /** 企業名。 */
    @Column(nullable = false, length = 255)
    var name: String,

    /** 業界名。 */
    @Column(length = 100)
    var industry: String? = null,

    /** 企業公式サイトURL。 */
    @Column(name = "website_url", length = 500)
    var websiteUrl: String? = null,

    /** 企業に関する個人メモ。 */
    @Column(name = "memo", columnDefinition = "TEXT")
    var memo: String? = null,

    /** 作成日時。 */
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null,

    /** 更新日時。 */
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime? = null
) {

    /** 初回保存時に作成日時と更新日時を同じ現在時刻で設定する。 */
    @PrePersist
    fun prePersist() {
        val now = LocalDateTime.now()
        createdAt = now
        updatedAt = now
    }

    /** 更新時に更新日時のみを現在時刻で更新する。 */
    @PreUpdate
    fun preUpdate() {
        updatedAt = LocalDateTime.now()
    }
}
