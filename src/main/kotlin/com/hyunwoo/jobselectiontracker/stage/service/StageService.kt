package com.hyunwoo.jobselectiontracker.stage.service

import com.hyunwoo.jobselectiontracker.application.entity.Application
import com.hyunwoo.jobselectiontracker.common.exception.InvalidRequestException
import com.hyunwoo.jobselectiontracker.common.security.CurrentUserProvider
import com.hyunwoo.jobselectiontracker.common.security.OwnedResourceFinder
import com.hyunwoo.jobselectiontracker.stage.dto.CreateStageRequest
import com.hyunwoo.jobselectiontracker.stage.dto.StageResponse
import com.hyunwoo.jobselectiontracker.stage.dto.UpdateStageRequest
import com.hyunwoo.jobselectiontracker.stage.entity.Stage
import com.hyunwoo.jobselectiontracker.stage.entity.StageStatus
import com.hyunwoo.jobselectiontracker.stage.history.entity.StageStatusHistory
import com.hyunwoo.jobselectiontracker.stage.history.repository.StageStatusHistoryRepository
import com.hyunwoo.jobselectiontracker.stage.repository.StageRepository
import com.hyunwoo.jobselectiontracker.user.entity.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class StageService(
    private val stageRepository: StageRepository,
    private val currentUserProvider: CurrentUserProvider,
    private val ownedResourceFinder: OwnedResourceFinder,
    private val stageStatusHistoryRepository: StageStatusHistoryRepository
) {

    @Transactional
    fun createStage(applicationId: Long, request: CreateStageRequest): StageResponse {
        val currentUser = findCurrentUser()
        val application = findApplicationByIdAndUserId(applicationId, currentUser.id!!)
        validateStageOrder(applicationId, request.stageOrder)

        val stage = Stage(
            application = application,
            stageOrder = request.stageOrder,
            stageType = request.stageType,
            stageName = request.stageName.trim(),
            status = request.status,
            scheduledAt = request.scheduledAt,
            completedAt = request.completedAt,
            resultDate = request.resultDate,
            memo = request.memo?.trim()
        )

        return StageResponse.from(stageRepository.save(stage))
    }

    fun getStages(applicationId: Long): List<StageResponse> {
        val currentUser = findCurrentUser()
        findApplicationByIdAndUserId(applicationId, currentUser.id!!)

        return stageRepository.findAllByApplicationIdAndApplicationUserIdOrderByStageOrderAsc(
            applicationId = applicationId,
            userId = currentUser.id!!
        ).map(StageResponse::from)
    }

    @Transactional
    fun updateStage(id: Long, request: UpdateStageRequest): StageResponse {
        val currentUser = findCurrentUser()
        val stage = findStageByIdAndUserId(id, currentUser.id!!)
        val previousStatus = stage.status

        request.stageOrder?.let {
            if (it != stage.stageOrder) {
                validateStageOrder(
                    stage.application.id ?: throw IllegalStateException("Application id is missing."),
                    it
                )
                stage.stageOrder = it
            }
        }
        request.stageType?.let { stage.stageType = it }
        request.stageName?.let { stage.stageName = it.trim() }
        request.status?.let {
            validateStatusTransition(stage.status, it)
            stage.status = it
        }
        request.scheduledAt?.let { stage.scheduledAt = it }
        request.completedAt?.let { stage.completedAt = it }
        request.resultDate?.let { stage.resultDate = it }
        request.memo?.let { stage.memo = it.trim() }

        val savedStage = stageRepository.saveAndFlush(stage)
        saveStatusHistoryIfChanged(savedStage, previousStatus, savedStage.status)

        return StageResponse.from(savedStage)
    }

    @Transactional
    fun deleteStage(id: Long) {
        val currentUser = findCurrentUser()
        stageRepository.delete(findStageByIdAndUserId(id, currentUser.id!!))
    }

    private fun findApplicationByIdAndUserId(id: Long, userId: Long): Application {
        return ownedResourceFinder.findApplication(id, userId)
    }

    private fun findStageByIdAndUserId(id: Long, userId: Long): Stage {
        return ownedResourceFinder.findStage(id, userId)
    }

    private fun findCurrentUser(): User {
        return currentUserProvider.getCurrentUser()
    }

    private fun validateStageOrder(applicationId: Long, stageOrder: Int) {
        if (stageRepository.existsByApplicationIdAndStageOrder(applicationId, stageOrder)) {
            throw InvalidRequestException("Stage order $stageOrder is already in use for application $applicationId.")
        }
    }

    private fun saveStatusHistoryIfChanged(
        stage: Stage,
        previousStatus: StageStatus,
        currentStatus: StageStatus
    ) {
        if (previousStatus == currentStatus) {
            return
        }

        stageStatusHistoryRepository.save(
            StageStatusHistory(
                stage = stage,
                fromStatus = previousStatus,
                toStatus = currentStatus
            )
        )
    }

    private fun validateStatusTransition(
        currentStatus: StageStatus,
        nextStatus: StageStatus
    ) {
        if (currentStatus == nextStatus) {
            return
        }

        val allowedNextStatuses = when (currentStatus) {
            StageStatus.PENDING -> setOf(
                StageStatus.SCHEDULED,
                StageStatus.COMPLETED,
                StageStatus.FAILED
            )
            StageStatus.SCHEDULED -> setOf(
                StageStatus.COMPLETED,
                StageStatus.FAILED
            )
            StageStatus.COMPLETED -> setOf(
                StageStatus.PASSED,
                StageStatus.FAILED
            )
            StageStatus.PASSED,
            StageStatus.FAILED -> emptySet()
        }

        if (nextStatus !in allowedNextStatuses) {
            throw InvalidRequestException(
                "Cannot change stage status from $currentStatus to $nextStatus."
            )
        }
    }
}
