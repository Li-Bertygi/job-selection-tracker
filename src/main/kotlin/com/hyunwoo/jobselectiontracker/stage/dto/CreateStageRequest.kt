package com.hyunwoo.jobselectiontracker.stage.dto

import com.hyunwoo.jobselectiontracker.stage.entity.StageStatus
import com.hyunwoo.jobselectiontracker.stage.entity.StageType
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 選考ステージ新規登録APIで受け取るリクエストDTO。
 * 応募情報配下に追加するステージの詳細を受け取る。
 */
data class CreateStageRequest(

    /** 同一応募内での表示順序。1以上を想定する。 */
    @field:Min(value = 1, message = "ステージ順序は1以上で入力してください。")
    val stageOrder: Int,

    /** ステージの中分類。 */
    @field:NotNull(message = "ステージ種別は必須です。")
    val stageType: StageType,

    /** 一次面接、SPI などの表示名。 */
    @field:Size(min = 1, max = 100, message = "ステージ名は1文字以上100文字以内で入力してください。")
    val stageName: String,

    /** ステージ単位の進捗状態。未指定時はPENDINGを使用する。 */
    val status: StageStatus = StageStatus.PENDING,

    /** 実施予定日時。 */
    val scheduledAt: LocalDateTime? = null,

    /** 実施完了日時。 */
    val completedAt: LocalDateTime? = null,

    /** 合否や結果通知日。 */
    val resultDate: LocalDate? = null,

    /** ステージに関する補足メモ。 */
    val memo: String? = null
)
