package com.hyunwoo.jobselectiontracker.stage.service

import com.hyunwoo.jobselectiontracker.application.entity.Application
import com.hyunwoo.jobselectiontracker.application.repository.ApplicationRepository
import com.hyunwoo.jobselectiontracker.stage.dto.CreateStageRequest
import com.hyunwoo.jobselectiontracker.stage.dto.StageResponse
import com.hyunwoo.jobselectiontracker.stage.dto.UpdateStageRequest
import com.hyunwoo.jobselectiontracker.stage.entity.Stage
import com.hyunwoo.jobselectiontracker.stage.entity.StageStatus
import com.hyunwoo.jobselectiontracker.stage.history.entity.StageStatusHistory
import com.hyunwoo.jobselectiontracker.stage.history.repository.StageStatusHistoryRepository
import com.hyunwoo.jobselectiontracker.stage.repository.StageRepository
import com.hyunwoo.jobselectiontracker.user.entity.User
import com.hyunwoo.jobselectiontracker.user.repository.UserRepository
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.NoSuchElementException

/**
 * 選考ステージの作成、取得、更新、削除を担当するサービス。
 * 参照対象は現在ログイン中ユーザーの応募情報配下に限定する。
 */
@Service
@Transactional(readOnly = true)
class StageService(
    private val stageRepository: StageRepository,
    private val applicationRepository: ApplicationRepository,
    private val userRepository: UserRepository,
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
                    stage.application.id ?: throw IllegalStateException("応募情報IDが存在しません。"),
                    it
                )
                stage.stageOrder = it
            }
        }
        request.stageType?.let { stage.stageType = it }
        request.stageName?.let { stage.stageName = it.trim() }
        request.status?.let { stage.status = it }
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
        return applicationRepository.findByIdAndUserId(id, userId)
            ?: throw NoSuchElementException("応募情報ID $id に該当する応募情報が見つかりません。")
    }

    private fun findStageByIdAndUserId(id: Long, userId: Long): Stage {
        return stageRepository.findByIdAndApplicationUserId(id, userId)
            ?: throw NoSuchElementException("ステージID $id に該当するステージが見つかりません。")
    }

    private fun findCurrentUser(): User {
        val email = SecurityContextHolder.getContext().authentication?.name
            ?: throw IllegalStateException("現在の認証ユーザー情報を取得できません。")

        return userRepository.findByEmail(email)
            ?: throw NoSuchElementException("メールアドレス $email に該当するユーザーが見つかりません。")
    }

    private fun validateStageOrder(applicationId: Long, stageOrder: Int) {
        if (stageRepository.existsByApplicationIdAndStageOrder(applicationId, stageOrder)) {
            throw IllegalArgumentException("応募情報ID $applicationId ではステージ順序 $stageOrder がすでに使用されています。")
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
}
