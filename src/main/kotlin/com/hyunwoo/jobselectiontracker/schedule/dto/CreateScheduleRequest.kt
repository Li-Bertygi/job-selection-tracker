package com.hyunwoo.jobselectiontracker.schedule.dto

import com.hyunwoo.jobselectiontracker.schedule.entity.ScheduleType
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

/**
 * スケジュール新規登録APIで受け取るリクエストDTO。
 * 応募情報配下に追加する予定の詳細を受け取る。
 */
data class CreateScheduleRequest(

    /** 特定ステージに紐づける場合のみ指定するステージID。 */
    val stageId: Long? = null,

    /** 予定の性格を表す分類。 */
    @field:NotNull(message = "スケジュール種別は必須です。")
    val scheduleType: ScheduleType,

    /** 予定のタイトル。 */
    @field:Size(min = 1, max = 255, message = "タイトルは1文字以上255文字以内で入力してください。")
    val title: String,

    /** 予定の詳細説明。 */
    val description: String? = null,

    /** 予定の開始日時。 */
    @field:NotNull(message = "開始日時は必須です。")
    val startAt: LocalDateTime,

    /** 予定の終了日時。 */
    val endAt: LocalDateTime? = null,

    /** 場所やURL。 */
    @field:Size(max = 255, message = "場所は255文字以内で入力してください。")
    val location: String? = null,

    /** 終日予定かどうかを表すフラグ。 */
    val isAllDay: Boolean = false
)
