package com.hyunwoo.jobselectiontracker.stage.history.dto

import com.hyunwoo.jobselectiontracker.stage.entity.StageStatus
import com.hyunwoo.jobselectiontracker.stage.history.entity.StageStatusHistory
import java.time.LocalDateTime

/**
 * 各選考ステージの状態変更履歴をAPIレスポンスとして返却するDTO。
 */
data class StageStatusHistoryResponse(

    /** 履歴ID。 */
    val id: Long,

    /** この履歴が属するステージID。 */
    val stageId: Long,

    /** 変更前のステージ状態。初回変更時は null を許容する。 */
    val fromStatus: StageStatus?,

    /** 変更後のステージ状態。 */
    val toStatus: StageStatus,

    /** 状態変更日時。 */
    val changedAt: LocalDateTime?
) {
    companion object {
        /** StageStatusHistoryエンティティをレスポンスDTOへ変換する。 */
        fun from(history: StageStatusHistory): StageStatusHistoryResponse {
            return StageStatusHistoryResponse(
                id = history.id ?: throw IllegalStateException("ステータス履歴IDが存在しません。"),
                stageId = history.stage.id ?: throw IllegalStateException("ステージIDが存在しません。"),
                fromStatus = history.fromStatus,
                toStatus = history.toStatus,
                changedAt = history.changedAt
            )
        }
    }
}
