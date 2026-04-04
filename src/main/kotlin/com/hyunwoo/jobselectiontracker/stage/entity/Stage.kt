package com.hyunwoo.jobselectiontracker.stage.entity

import com.hyunwoo.jobselectiontracker.application.entity.Application
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
 * 応募情報に紐づく個別の選考ステージを表すJPAエンティティ。
 * 面接、面談、Webテストなど、会社ごとに異なる選考フローを柔軟に管理するために使用する。
 */
@Entity
@Table(name = "stages")
class Stage(

    /** ステージを一意に識別する主キー。 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    /** このステージが属する応募情報。 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    var application: Application,

    /** 同一応募内での表示順序。 */
    @Column(name = "stage_order", nullable = false)
    var stageOrder: Int,

    /** ステージの大分類。 */
    @Enumerated(EnumType.STRING)
    @Column(name = "stage_type", nullable = false, length = 50)
    var stageType: StageType,

    /** 人事面接、SPIなどの実際の表示名。 */
    @Column(name = "stage_name", nullable = false, length = 100)
    var stageName: String,

    /** ステージ単位の進捗状態。 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    var status: StageStatus = StageStatus.PENDING,

    /** 実施予定日時。 */
    @Column(name = "scheduled_at")
    var scheduledAt: LocalDateTime? = null,

    /** 実施完了日時。 */
    @Column(name = "completed_at")
    var completedAt: LocalDateTime? = null,

    /** 合否や結果通知日。 */
    @Column(name = "result_date")
    var resultDate: LocalDate? = null,

    /** ステージに関する補足メモ。 */
    @Column(name = "memo", columnDefinition = "TEXT")
    var memo: String? = null,

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
