package com.hyunwoo.jobselectiontracker.stage.history.entity

import com.hyunwoo.jobselectiontracker.stage.entity.Stage
import com.hyunwoo.jobselectiontracker.stage.entity.StageStatus
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
import jakarta.persistence.Table
import java.time.LocalDateTime

/**
 * 各選考ステージの状態変更履歴を表すエンティティ。
 * 現在の stage.status とは別に、状態変化の履歴のみを記録する。
 */
@Entity
@Table(name = "stage_status_histories")
class StageStatusHistory(

    /** 履歴の主キー。 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    /** この履歴が属するステージ。 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_id", nullable = false)
    var stage: Stage,

    /** 変更前のステージ状態。初回変更時は null を許容する。 */
    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 50)
    var fromStatus: StageStatus? = null,

    /** 変更後のステージ状態。 */
    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 50)
    var toStatus: StageStatus,

    /** 状態変更が発生した日時。 */
    @Column(name = "changed_at", nullable = false)
    var changedAt: LocalDateTime? = null
) {

    /** 保存前に変更日時が未設定なら現在時刻を設定する。 */
    @PrePersist
    fun prePersist() {
        if (changedAt == null) {
            changedAt = LocalDateTime.now()
        }
    }
}
