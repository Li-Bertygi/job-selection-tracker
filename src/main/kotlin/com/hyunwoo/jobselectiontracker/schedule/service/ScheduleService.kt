package com.hyunwoo.jobselectiontracker.schedule.service

import com.hyunwoo.jobselectiontracker.application.entity.Application
import com.hyunwoo.jobselectiontracker.common.exception.InvalidRequestException
import com.hyunwoo.jobselectiontracker.common.security.CurrentUserProvider
import com.hyunwoo.jobselectiontracker.common.security.OwnedResourceFinder
import com.hyunwoo.jobselectiontracker.schedule.dto.CreateScheduleRequest
import com.hyunwoo.jobselectiontracker.schedule.dto.ScheduleResponse
import com.hyunwoo.jobselectiontracker.schedule.dto.UpdateScheduleRequest
import com.hyunwoo.jobselectiontracker.schedule.entity.Schedule
import com.hyunwoo.jobselectiontracker.schedule.entity.ScheduleType
import com.hyunwoo.jobselectiontracker.schedule.repository.ScheduleRepository
import com.hyunwoo.jobselectiontracker.stage.entity.Stage
import com.hyunwoo.jobselectiontracker.stage.repository.StageRepository
import com.hyunwoo.jobselectiontracker.user.entity.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class ScheduleService(
    private val scheduleRepository: ScheduleRepository,
    private val stageRepository: StageRepository,
    private val currentUserProvider: CurrentUserProvider,
    private val ownedResourceFinder: OwnedResourceFinder
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
        return ownedResourceFinder.findApplication(id, userId)
    }

    private fun findStageByIdAndUserId(id: Long, userId: Long): Stage {
        return ownedResourceFinder.findStage(id, userId)
    }

    private fun findScheduleByIdAndUserId(id: Long, userId: Long): Schedule {
        return ownedResourceFinder.findSchedule(id, userId)
    }

    private fun findCurrentUser(): User {
        return currentUserProvider.getCurrentUser()
    }

    private fun validateStageBelongsToApplication(application: Application, stage: Stage?) {
        if (stage != null && stage.application.id != application.id) {
            throw InvalidRequestException("The selected stage does not belong to the target application.")
        }
    }

    private fun validateDateRange(startAt: LocalDateTime, endAt: LocalDateTime?) {
        if (endAt != null && endAt.isBefore(startAt)) {
            throw InvalidRequestException("endAt must be greater than or equal to startAt.")
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
                stageId = stage.id ?: throw IllegalStateException("Stage id is missing."),
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
            throw InvalidRequestException(buildDuplicateScheduleMessage(stage))
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
            applicationId = schedule.application.id ?: throw IllegalStateException("Application id is missing."),
            stage = stage,
            scheduleType = scheduleType,
            startAt = startAt
        )
    }

    private fun buildDuplicateScheduleMessage(stage: Stage?): String {
        return if (stage != null) {
            "A schedule with the same application, stage, type, and startAt already exists."
        } else {
            "A schedule with the same application, type, and startAt already exists."
        }
    }
}
