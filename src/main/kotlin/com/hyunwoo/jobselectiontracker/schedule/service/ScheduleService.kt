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
import com.hyunwoo.jobselectiontracker.user.entity.User
import com.hyunwoo.jobselectiontracker.user.repository.UserRepository
import java.time.LocalDateTime
import java.util.NoSuchElementException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 日程の作成、取得、更新、削除を担当するサービス。
 * 対象は現在ログイン中ユーザーの応募情報配下に限定する。
 */
@Service
@Transactional(readOnly = true)
class ScheduleService(
    private val scheduleRepository: ScheduleRepository,
    private val applicationRepository: ApplicationRepository,
    private val stageRepository: StageRepository,
    private val userRepository: UserRepository
) {

    @Transactional
    fun createSchedule(applicationId: Long, request: CreateScheduleRequest): ScheduleResponse {
        val currentUser = findCurrentUser()
        val application = findApplicationByIdAndUserId(applicationId, currentUser.id!!)
        val stage = request.stageId?.let { findStageByIdAndUserId(it, currentUser.id!!) }

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

    fun getSchedules(applicationId: Long): List<ScheduleResponse> {
        val currentUser = findCurrentUser()
        findApplicationByIdAndUserId(applicationId, currentUser.id!!)

        return scheduleRepository.findAllByApplicationIdAndApplicationUserIdOrderByStartAtAsc(
            applicationId = applicationId,
            userId = currentUser.id!!
        ).map(ScheduleResponse::from)
    }

    @Transactional
    fun updateSchedule(id: Long, request: UpdateScheduleRequest): ScheduleResponse {
        val currentUser = findCurrentUser()
        val schedule = findScheduleByIdAndUserId(id, currentUser.id!!)

        val nextStage = when {
            request.stageId != null -> findStageByIdAndUserId(request.stageId, currentUser.id!!)
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

    @Transactional
    fun deleteSchedule(id: Long) {
        val currentUser = findCurrentUser()
        scheduleRepository.delete(findScheduleByIdAndUserId(id, currentUser.id!!))
    }

    private fun findApplicationByIdAndUserId(id: Long, userId: Long): Application {
        return applicationRepository.findByIdAndUserId(id, userId)
            ?: throw NoSuchElementException("応募情報ID $id に該当する応募情報が見つかりません。")
    }

    private fun findStageByIdAndUserId(id: Long, userId: Long): Stage {
        return stageRepository.findByIdAndApplicationUserId(id, userId)
            ?: throw NoSuchElementException("ステージID $id に該当するステージが見つかりません。")
    }

    private fun findScheduleByIdAndUserId(id: Long, userId: Long): Schedule {
        return scheduleRepository.findByIdAndApplicationUserId(id, userId)
            ?: throw NoSuchElementException("日程ID $id に該当する日程が見つかりません。")
    }

    private fun findCurrentUser(): User {
        val email = SecurityContextHolder.getContext().authentication?.name
            ?: throw IllegalStateException("現在の認証ユーザー情報を取得できません。")

        return userRepository.findByEmail(email)
            ?: throw NoSuchElementException("メールアドレス $email に該当するユーザーが見つかりません。")
    }

    private fun validateStageBelongsToApplication(application: Application, stage: Stage?) {
        if (stage != null && stage.application.id != application.id) {
            throw IllegalArgumentException("指定したステージは対象の応募情報に属していません。")
        }
    }

    private fun validateDateRange(startAt: LocalDateTime, endAt: LocalDateTime?) {
        if (endAt != null && endAt.isBefore(startAt)) {
            throw IllegalArgumentException("終了日時は開始日時より前に設定できません。")
        }
    }

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
