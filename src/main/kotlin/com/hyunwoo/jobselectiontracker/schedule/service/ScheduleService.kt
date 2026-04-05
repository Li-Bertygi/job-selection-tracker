package com.hyunwoo.jobselectiontracker.schedule.service

import com.hyunwoo.jobselectiontracker.application.entity.Application
import com.hyunwoo.jobselectiontracker.application.repository.ApplicationRepository
import com.hyunwoo.jobselectiontracker.schedule.dto.CreateScheduleRequest
import com.hyunwoo.jobselectiontracker.schedule.dto.ScheduleResponse
import com.hyunwoo.jobselectiontracker.schedule.dto.UpdateScheduleRequest
import com.hyunwoo.jobselectiontracker.schedule.entity.Schedule
import com.hyunwoo.jobselectiontracker.schedule.entity.ScheduleType
import com.hyunwoo.jobselectiontracker.schedule.repository.ScheduleRepository
import com.hyunwoo.jobselectiontracker.stage.entity.Stage
import com.hyunwoo.jobselectiontracker.stage.repository.StageRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.NoSuchElementException

/**
 * 日程ドメインのビジネスロジックを担当するサービス。
 * 応募情報単位の日程とステージ単位の日程を一元的に管理する。
 */
@Service
@Transactional(readOnly = true)
class ScheduleService(
    /** 日程エンティティの保存と検索を担当するリポジトリ。 */
    private val scheduleRepository: ScheduleRepository,
    /** 応募情報の存在確認と所属確認に使用するリポジトリ。 */
    private val applicationRepository: ApplicationRepository,
    /** ステージの存在確認と応募情報所属確認に使用するリポジトリ。 */
    private val stageRepository: StageRepository
) {

    /** 指定した応募情報に新しい日程を登録する。 */
    @Transactional
    fun createSchedule(applicationId: Long, request: CreateScheduleRequest): ScheduleResponse {
        val application = findApplicationById(applicationId)
        val stage = request.stageId?.let { findStageById(it) }

        validateStageBelongsToApplication(application, stage)
        validateDateRange(request.startAt, request.endAt)
        validateDuplicateSchedule(
            applicationId = applicationId,
            stage = stage,
            scheduleType = request.scheduleType,
            startAt = request.startAt
        )

        val schedule = Schedule(
            application = application,
            stage = stage,
            scheduleType = request.scheduleType,
            title = request.title.trim(),
            description = request.description?.trim(),
            startAt = request.startAt,
            endAt = request.endAt,
            location = request.location?.trim(),
            isAllDay = request.isAllDay
        )

        return ScheduleResponse.from(scheduleRepository.save(schedule))
    }

    /** 指定した応募情報に属する日程一覧を取得する。 */
    fun getSchedules(applicationId: Long): List<ScheduleResponse> {
        findApplicationById(applicationId)

        return scheduleRepository.findAllByApplicationIdOrderByStartAtAsc(applicationId)
            .map(ScheduleResponse::from)
    }

    /** 既存の日程を部分更新する。 */
    @Transactional
    fun updateSchedule(id: Long, request: UpdateScheduleRequest): ScheduleResponse {
        val schedule = findScheduleById(id)

        val nextStage = when {
            request.stageId != null -> findStageById(request.stageId)
            else -> schedule.stage
        }
        val nextScheduleType = request.scheduleType ?: schedule.scheduleType
        val nextStartAt = request.startAt ?: schedule.startAt
        val nextEndAt = request.endAt ?: schedule.endAt

        validateStageBelongsToApplication(schedule.application, nextStage)
        validateDateRange(nextStartAt, nextEndAt)
        validateDuplicateScheduleOnUpdate(
            schedule = schedule,
            stage = nextStage,
            scheduleType = nextScheduleType,
            startAt = nextStartAt
        )

        request.stageId?.let { schedule.stage = nextStage }
        request.scheduleType?.let { schedule.scheduleType = it }
        request.title?.let { schedule.title = it.trim() }
        request.description?.let { schedule.description = it.trim() }
        request.startAt?.let { schedule.startAt = it }
        request.endAt?.let { schedule.endAt = it }
        request.location?.let { schedule.location = it.trim() }
        request.isAllDay?.let { schedule.isAllDay = it }

        return ScheduleResponse.from(scheduleRepository.saveAndFlush(schedule))
    }

    /** 指定した日程を削除する。 */
    @Transactional
    fun deleteSchedule(id: Long) {
        val schedule = findScheduleById(id)
        scheduleRepository.delete(schedule)
    }

    /** 応募情報IDで応募情報を取得し、存在しなければ404対象の例外を投げる。 */
    private fun findApplicationById(id: Long): Application {
        return applicationRepository.findById(id)
            .orElseThrow {
                NoSuchElementException("応募情報ID $id に該当する応募情報が見つかりません。")
            }
    }

    /** ステージIDでステージを取得し、存在しなければ404対象の例外を投げる。 */
    private fun findStageById(id: Long): Stage {
        return stageRepository.findById(id)
            .orElseThrow {
                NoSuchElementException("ステージID $id に該当するステージが見つかりません。")
            }
    }

    /** 日程IDで日程を取得し、存在しなければ404対象の例外を投げる。 */
    private fun findScheduleById(id: Long): Schedule {
        return scheduleRepository.findById(id)
            .orElseThrow {
                NoSuchElementException("日程ID $id に該当する日程が見つかりません。")
            }
    }

    /** 指定したステージが対象の応募情報に属しているかを検証する。 */
    private fun validateStageBelongsToApplication(application: Application, stage: Stage?) {
        if (stage != null && stage.application.id != application.id) {
            throw IllegalArgumentException("指定したステージは対象の応募情報に属していません。")
        }
    }

    /** 終了日時が開始日時より前にならないように検証する。 */
    private fun validateDateRange(startAt: LocalDateTime, endAt: LocalDateTime?) {
        if (endAt != null && endAt.isBefore(startAt)) {
            throw IllegalArgumentException("終了日時は開始日時より前に設定できません。")
        }
    }

    /** 新規作成時に主要キーの組み合わせが重複していないかを検証する。 */
    private fun validateDuplicateSchedule(
        applicationId: Long,
        stage: Stage?,
        scheduleType: ScheduleType,
        startAt: LocalDateTime
    ) {
        val isDuplicate = if (stage != null) {
            scheduleRepository.existsByApplicationIdAndStageIdAndScheduleTypeAndStartAt(
                applicationId = applicationId,
                stageId = stage.id ?: throw IllegalStateException("ステージIDが存在しません。"),
                scheduleType = scheduleType,
                startAt = startAt
            )
        } else {
            scheduleRepository.existsByApplicationIdAndStageIsNullAndScheduleTypeAndStartAt(
                applicationId = applicationId,
                scheduleType = scheduleType,
                startAt = startAt
            )
        }

        if (isDuplicate) {
            throw IllegalArgumentException(buildDuplicateScheduleMessage(applicationId, stage, scheduleType, startAt))
        }
    }

    /** 更新時に自分自身を除いた重複チェックが必要かを判定して検証する。 */
    private fun validateDuplicateScheduleOnUpdate(
        schedule: Schedule,
        stage: Stage?,
        scheduleType: ScheduleType,
        startAt: LocalDateTime
    ) {
        val currentStageId = schedule.stage?.id
        val nextStageId = stage?.id
        val currentKeyIsSame =
            currentStageId == nextStageId &&
                schedule.scheduleType == scheduleType &&
                schedule.startAt == startAt

        if (currentKeyIsSame) {
            return
        }

        validateDuplicateSchedule(
            applicationId = schedule.application.id
                ?: throw IllegalStateException("応募情報IDが存在しません。"),
            stage = stage,
            scheduleType = scheduleType,
            startAt = startAt
        )
    }

    /** 重複した日程キーに対する業務エラーメッセージを生成する。 */
    private fun buildDuplicateScheduleMessage(
        applicationId: Long,
        stage: Stage?,
        scheduleType: ScheduleType,
        startAt: LocalDateTime
    ): String {
        return if (stage != null) {
            "応募情報ID $applicationId のステージID ${stage.id} では、日程種別 $scheduleType と開始日時 $startAt の組み合わせがすでに使用されています。"
        } else {
            "応募情報ID $applicationId では、ステージ未指定の日程種別 $scheduleType と開始日時 $startAt の組み合わせがすでに使用されています。"
        }
    }
}
