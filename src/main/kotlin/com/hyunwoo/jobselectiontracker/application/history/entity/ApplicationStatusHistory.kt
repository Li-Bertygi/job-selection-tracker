package com.hyunwoo.jobselectiontracker.application.history.entity

import com.hyunwoo.jobselectiontracker.application.entity.Application
import com.hyunwoo.jobselectiontracker.application.entity.ApplicationStatus
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
 * 応募全体のステータス変更履歴を表すエンティティ。
 * 現在状態そのものではなく、状態がどのように変化したかを記録する。
 */
@Entity
@Table(name = "application_status_histories")
class ApplicationStatusHistory(

    /** 履歴の主キー。 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    /** この履歴が属する応募情報。 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    var application: Application,

    /** 変更前の応募ステータス。初回変更時は null を許容する。 */
    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 50)
    var fromStatus: ApplicationStatus? = null,

    /** 変更後の応募ステータス。 */
    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 50)
    var toStatus: ApplicationStatus,

    /** ステータス変更が発生した日時。 */
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
