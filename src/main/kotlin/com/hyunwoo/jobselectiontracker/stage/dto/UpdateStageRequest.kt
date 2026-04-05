package com.hyunwoo.jobselectiontracker.stage.dto

import com.hyunwoo.jobselectiontracker.stage.entity.StageStatus
import com.hyunwoo.jobselectiontracker.stage.entity.StageType
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 選考ステージ更新APIで受け取るリクエストDTO。
 * PATCHを想定しているため、各フィールドは未指定を表現できるよう nullable にしている。
 */
data class UpdateStageRequest(

    /** 更新後の表示順序。 */
    @field:Min(value = 1, message = "ステージ順序は1以上で入力してください。")
    val stageOrder: Int? = null,

    /** 更新後のステージ種別。 */
    val stageType: StageType? = null,

    /** 更新後のステージ表示名。 */
    @field:Size(min = 1, max = 100, message = "ステージ名は1文字以上100文字以内で入力してください。")
    val stageName: String? = null,

    /** 更新後のステージ状態。 */
    val status: StageStatus? = null,

    /** 更新後の実施予定日時。 */
    val scheduledAt: LocalDateTime? = null,

    /** 更新後の実施完了日時。 */
    val completedAt: LocalDateTime? = null,

    /** 更新後の結果通知日。 */
    val resultDate: LocalDate? = null,

    /** 更新後の補足メモ。 */
    val memo: String? = null
)
