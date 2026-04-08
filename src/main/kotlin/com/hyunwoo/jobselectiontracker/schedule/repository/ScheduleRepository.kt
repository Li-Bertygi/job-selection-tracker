package com.hyunwoo.jobselectiontracker.schedule.repository

import com.hyunwoo.jobselectiontracker.schedule.entity.Schedule
import com.hyunwoo.jobselectiontracker.schedule.entity.ScheduleType
import java.time.LocalDateTime
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Schedule エンティティの永続化を担当するリポジトリ。
 * 応募情報と所有ユーザーを基準に日程を取得する。
 */
interface ScheduleRepository : JpaRepository<Schedule, Long> {

    /**
     * 指定した応募情報かつ指定したユーザーに属する日程を開始日時昇順で取得する。
     */
    fun findAllByApplicationIdAndApplicationUserIdOrderByStartAtAsc(
        applicationId: Long,
        userId: Long
    ): List<Schedule>

    /**
     * 指定した日程IDかつ指定したユーザーに属する日程を取得する。
     */
    fun findByIdAndApplicationUserId(id: Long, userId: Long): Schedule?

    /**
     * ステージ指定ありの日程重複を確認する。
     */
    fun existsByApplicationIdAndStageIdAndScheduleTypeAndStartAt(
        applicationId: Long,
        stageId: Long,
        scheduleType: ScheduleType,
        startAt: LocalDateTime
    ): Boolean

    /**
     * ステージ未指定の日程重複を確認する。
     */
    fun existsByApplicationIdAndStageIsNullAndScheduleTypeAndStartAt(
        applicationId: Long,
        scheduleType: ScheduleType,
        startAt: LocalDateTime
    ): Boolean
}
