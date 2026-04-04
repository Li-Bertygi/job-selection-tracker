package com.hyunwoo.jobselectiontracker.application.entity

import com.hyunwoo.jobselectiontracker.company.entity.Company
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 応募情報の中心となるJPAエンティティ。
 * 企業、応募職種、応募経路、現在の進捗状態などをまとめて管理する。
 */
@Entity
@Table(name = "applications")
class Application(

    /** 応募情報を一意に識別する主キー。 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    /** 応募先企業。複数の応募情報が1つの企業に紐づく。 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    var company: Company,

    /** 応募した職種名。 */
    @Column(name = "job_title", nullable = false, length = 255)
    var jobTitle: String,

    /** 応募経路。例: 自社サイト、求人媒体、エージェント。 */
    @Column(name = "application_route", length = 100)
    var applicationRoute: String? = null,

    /** 応募全体の進捗状態。 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    var status: ApplicationStatus = ApplicationStatus.NOT_STARTED,

    /** 実際に応募した日付。 */
    @Column(name = "applied_at")
    var appliedAt: LocalDate? = null,

    /** 結果通知日。 */
    @Column(name = "result_date")
    var resultDate: LocalDate? = null,

    /** オファーへの回答期限。 */
    @Column(name = "offer_deadline")
    var offerDeadline: LocalDate? = null,

    /** 応募の優先度。数値が高いほど優先度が高い想定。 */
    @Column(nullable = false)
    var priority: Int = 0,

    /** 一覧から除外したい応募を保管するためのフラグ。 */
    @Column(name = "is_archived", nullable = false)
    var isArchived: Boolean = false,

    /** レコードの作成日時。初回保存時に設定される。 */
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null,

    /** レコードの更新日時。更新のたびに変更される。 */
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

    /** 更新前に更新日時のみ最新時刻へ変更する。 */
    @PreUpdate
    fun preUpdate() {
        updatedAt = LocalDateTime.now()
    }
}
