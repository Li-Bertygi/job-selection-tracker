package com.hyunwoo.jobselectiontracker.stage.dto

import com.hyunwoo.jobselectiontracker.stage.entity.Stage
import com.hyunwoo.jobselectiontracker.stage.entity.StageStatus
import com.hyunwoo.jobselectiontracker.stage.entity.StageType
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 選考ステージAPIのレスポンスとして返すDTO。
 * エンティティ全体をそのまま公開せず、必要な情報だけを返すために利用する。
 */
data class StageResponse(
    /** ステージID。 */
    val id: Long,
    /** 紐づく応募情報ID。 */
    val applicationId: Long,
    /** 同一応募内での表示順序。 */
    val stageOrder: Int,
    /** ステージの中分類。 */
    val stageType: StageType,
    /** 実際の表示名。 */
    val stageName: String,
    /** ステージ単位の進捗状態。 */
    val status: StageStatus,
    /** 実施予定日時。 */
    val scheduledAt: LocalDateTime?,
    /** 実施完了日時。 */
    val completedAt: LocalDateTime?,
    /** 合否や結果通知日。 */
    val resultDate: LocalDate?,
    /** 補足メモ。 */
    val memo: String?,
    /** 作成日時。 */
    val createdAt: LocalDateTime?,
    /** 更新日時。 */
    val updatedAt: LocalDateTime?
) {
    companion object {
        /**
         * StageエンティティをAPIレスポンス形式へ変換する。
         * 必須のIDが存在しない場合は不正な状態として例外を投げる。
         */
        fun from(stage: Stage): StageResponse {
            return StageResponse(
                id = stage.id ?: throw IllegalStateException("ステージIDが存在しません。"),
                applicationId = stage.application.id
                    ?: throw IllegalStateException("応募情報IDが存在しません。"),
                stageOrder = stage.stageOrder,
                stageType = stage.stageType,
                stageName = stage.stageName,
                status = stage.status,
                scheduledAt = stage.scheduledAt,
                completedAt = stage.completedAt,
                resultDate = stage.resultDate,
                memo = stage.memo,
                createdAt = stage.createdAt,
                updatedAt = stage.updatedAt
            )
        }
    }
}
