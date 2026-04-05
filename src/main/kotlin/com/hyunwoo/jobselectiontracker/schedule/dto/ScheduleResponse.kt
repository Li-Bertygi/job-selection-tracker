package com.hyunwoo.jobselectiontracker.schedule.dto

import com.hyunwoo.jobselectiontracker.schedule.entity.Schedule
import com.hyunwoo.jobselectiontracker.schedule.entity.ScheduleType
import java.time.LocalDateTime

/**
 * スケジュールAPIのレスポンスとして返すDTO。
 * エンティティ全体をそのまま公開せず、必要な情報だけを返すために利用する。
 */
data class ScheduleResponse(
    /** スケジュールID。 */
    val id: Long,
    /** 紐づく応募情報ID。 */
    val applicationId: Long,
    /** 紐づくステージID。 */
    val stageId: Long?,
    /** スケジュール種別。 */
    val scheduleType: ScheduleType,
    /** タイトル。 */
    val title: String,
    /** 詳細説明。 */
    val description: String?,
    /** 開始日時。 */
    val startAt: LocalDateTime,
    /** 終了日時。 */
    val endAt: LocalDateTime?,
    /** 場所やURL。 */
    val location: String?,
    /** 終日予定フラグ。 */
    val isAllDay: Boolean,
    /** 作成日時。 */
    val createdAt: LocalDateTime?,
    /** 更新日時。 */
    val updatedAt: LocalDateTime?
) {
    companion object {
        /**
         * ScheduleエンティティをAPIレスポンス形式へ変換する。
         * 必須のIDが存在しない場合は不正な状態として例外を投げる。
         */
        fun from(schedule: Schedule): ScheduleResponse {
            return ScheduleResponse(
                id = schedule.id ?: throw IllegalStateException("スケジュールIDが存在しません。"),
                applicationId = schedule.application.id
                    ?: throw IllegalStateException("応募情報IDが存在しません。"),
                stageId = schedule.stage?.id,
                scheduleType = schedule.scheduleType,
                title = schedule.title,
                description = schedule.description,
                startAt = schedule.startAt,
                endAt = schedule.endAt,
                location = schedule.location,
                isAllDay = schedule.isAllDay,
                createdAt = schedule.createdAt,
                updatedAt = schedule.updatedAt
            )
        }
    }
}
