package com.hyunwoo.jobselectiontracker.company.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import java.time.LocalDateTime

/**
 * 応募先企業の基本情報を表すJPAエンティティ。
 * READMEのERDにある companies テーブルに対応する。
 */
@Entity
@Table(name = "companies")
class Company(

    /** 企業を一意に識別する主キー。 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    /** 企業名。登録時の必須項目。 */
    @Column(nullable = false, length = 255)
    var name: String,

    /** 企業の業種。 */
    @Column(length = 100)
    var industry: String? = null,

    /** 企業の公式サイトURL。 */
    @Column(name = "website_url", length = 500)
    var websiteUrl: String? = null,

    /** 企業に関する補足メモ。 */
    @Column(name = "memo", columnDefinition = "TEXT")
    var memo: String? = null,

    /** レコードの作成日時。初回保存時に設定される。 */
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null,

    /** レコードの更新日時。更新のたびに書き換えられる。 */
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime? = null
) {

    /** 保存前に作成日時と更新日時を現在時刻で初期化する。 */
    @PrePersist
    fun prePersist() {
        val now = LocalDateTime.now()
        createdAt = now
        updatedAt = now
    }

    /** 更新前に更新日時を現在時刻へ変更する。 */
    @PreUpdate
    fun preUpdate() {
        updatedAt = LocalDateTime.now()
    }
}
