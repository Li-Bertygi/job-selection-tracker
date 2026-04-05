package com.hyunwoo.jobselectiontracker.schedule.repository

import com.hyunwoo.jobselectiontracker.schedule.entity.Schedule
import com.hyunwoo.jobselectiontracker.schedule.entity.ScheduleType
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

/**
 * ScheduleエンティティのDBアクセスを担当するリポジトリ。
 * 応募情報単位の一覧取得と日程重複チェックに使用する。
 */
interface ScheduleRepository : JpaRepository<Schedule, Long> {

    /** 指定した応募情報に属する日程を開始日時の昇順で取得する。 */
    fun findAllByApplicationIdOrderByStartAtAsc(applicationId: Long): List<Schedule>

    /** ステージに紐づく日程で、主要キーの組み合わせが既に存在するかを確認する。 */
    fun existsByApplicationIdAndStageIdAndScheduleTypeAndStartAt(
        applicationId: Long,
        stageId: Long,
        scheduleType: ScheduleType,
        startAt: LocalDateTime
    ): Boolean

    /** ステージ未指定の日程で、主要キーの組み合わせが既に存在するかを確認する。 */
    fun existsByApplicationIdAndStageIsNullAndScheduleTypeAndStartAt(
        applicationId: Long,
        scheduleType: ScheduleType,
        startAt: LocalDateTime
    ): Boolean
}
