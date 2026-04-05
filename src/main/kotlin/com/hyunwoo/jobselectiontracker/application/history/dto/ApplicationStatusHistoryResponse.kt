package com.hyunwoo.jobselectiontracker.application.history.dto

import com.hyunwoo.jobselectiontracker.application.entity.ApplicationStatus
import com.hyunwoo.jobselectiontracker.application.history.entity.ApplicationStatusHistory
import java.time.LocalDateTime

/**
 * 応募全体のステータス変更履歴をAPIレスポンスとして返却するDTO。
 */
data class ApplicationStatusHistoryResponse(

    /** 履歴ID。 */
    val id: Long,

    /** この履歴が属する応募情報ID。 */
    val applicationId: Long,

    /** 変更前のステータス。初回変更時は null を許容する。 */
    val fromStatus: ApplicationStatus?,

    /** 変更後のステータス。 */
    val toStatus: ApplicationStatus,

    /** ステータス変更日時。 */
    val changedAt: LocalDateTime?
) {
    companion object {
        /** ApplicationStatusHistoryエンティティをレスポンスDTOに変換する。 */
        fun from(history: ApplicationStatusHistory): ApplicationStatusHistoryResponse {
            return ApplicationStatusHistoryResponse(
                id = history.id ?: throw IllegalStateException("ステータス履歴IDが存在しません。"),
                applicationId = history.application.id
                    ?: throw IllegalStateException("応募情報IDが存在しません。"),
                fromStatus = history.fromStatus,
                toStatus = history.toStatus,
                changedAt = history.changedAt
            )
        }
    }
}
