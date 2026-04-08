package com.hyunwoo.jobselectiontracker.application.entity

import com.hyunwoo.jobselectiontracker.company.entity.Company
import com.hyunwoo.jobselectiontracker.user.entity.User
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
 * 応募情報の中心データを表す JPA エンティティ。
 * 所有者ユーザー、応募先企業、進行状況、日付関連情報をまとめて管理する。
 */
@Entity
@Table(name = "applications")
class Application(

    /** 応募情報ID。 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    /** この応募情報の所有者となるユーザー。 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    /** 応募先企業。 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    var company: Company,

    /** 応募職種。 */
    @Column(name = "job_title", nullable = false, length = 255)
    var jobTitle: String,

    /** 応募経路。 */
    @Column(name = "application_route", length = 100)
    var applicationRoute: String? = null,

    /** 応募全体の現在ステータス。 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    var status: ApplicationStatus = ApplicationStatus.NOT_STARTED,

    /** 実際に応募した日付。 */
    @Column(name = "applied_at")
    var appliedAt: LocalDate? = null,

    /** 結果通知日。 */
    @Column(name = "result_date")
    var resultDate: LocalDate? = null,

    /** オファー承諾期限。 */
    @Column(name = "offer_deadline")
    var offerDeadline: LocalDate? = null,

    /** 応募優先順位。 */
    @Column(nullable = false)
    var priority: Int = 0,

    /** 一覧から除外するためのアーカイブフラグ。 */
    @Column(name = "is_archived", nullable = false)
    var isArchived: Boolean = false,

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
