package com.hyunwoo.jobselectiontracker.schedule.dto

import com.hyunwoo.jobselectiontracker.schedule.entity.ScheduleType
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

/**
 * スケジュール更新APIで受け取るリクエストDTO。
 * PATCHを想定しているため、各フィールドは未指定を表現できるよう nullable にしている。
 */
data class UpdateScheduleRequest(

    /** 更新後に紐づけるステージID。 */
    val stageId: Long? = null,

    /** 更新後のスケジュール種別。 */
    val scheduleType: ScheduleType? = null,

    /** 更新後のタイトル。 */
    @field:Size(min = 1, max = 255, message = "タイトルは1文字以上255文字以内で入力してください。")
    val title: String? = null,

    /** 更新後の詳細説明。 */
    val description: String? = null,

    /** 更新後の開始日時。 */
    val startAt: LocalDateTime? = null,

    /** 更新後の終了日時。 */
    val endAt: LocalDateTime? = null,

    /** 更新後の場所やURL。 */
    @field:Size(max = 255, message = "場所は255文字以内で入力してください。")
    val location: String? = null,

    /** 更新後の終日予定フラグ。 */
    val isAllDay: Boolean? = null
)
