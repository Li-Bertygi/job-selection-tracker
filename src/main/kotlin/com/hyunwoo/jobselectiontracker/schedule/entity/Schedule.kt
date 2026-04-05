package com.hyunwoo.jobselectiontracker.schedule.entity

import com.hyunwoo.jobselectiontracker.application.entity.Application
import com.hyunwoo.jobselectiontracker.stage.entity.Stage
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
import java.time.LocalDateTime

/**
 * 応募情報や選考ステージに紐づく実際の予定を表すJPAエンティティ。
 * ERDの schedules テーブルに対応し、応募全体の予定とステージ単位の予定の両方を扱う。
 */
@Entity
@Table(name = "schedules")
class Schedule(

    /** スケジュールを一意に識別する主キー。 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    /** この予定が属する応募情報。 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    var application: Application,

    /** 特定ステージに紐づく予定の場合のみ設定される関連ステージ。 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_id")
    var stage: Stage? = null,

    /** 予定の性格を表す分類。 */
    @Enumerated(EnumType.STRING)
    @Column(name = "schedule_type", nullable = false, length = 50)
    var scheduleType: ScheduleType,

    /** 予定のタイトル。 */
    @Column(nullable = false, length = 255)
    var title: String,

    /** 詳細説明。 */
    @Column(name = "description", columnDefinition = "TEXT")
    var description: String? = null,

    /** 予定の開始日時。 */
    @Column(name = "start_at", nullable = false)
    var startAt: LocalDateTime,

    /** 予定の終了日時。 */
    @Column(name = "end_at")
    var endAt: LocalDateTime? = null,

    /** 場所やURLなどの位置情報。 */
    @Column(length = 255)
    var location: String? = null,

    /** 終日予定かどうかを表すフラグ。 */
    @Column(name = "is_all_day", nullable = false)
    var isAllDay: Boolean = false,

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
