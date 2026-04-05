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
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.NoSuchElementException

/**
 * 選考ステージドメインのビジネスロジックを担当するサービス。
 * 応募情報配下の順序管理、ステージCRUD、ステータス変更履歴の自動記録を行う。
 */
@Service
@Transactional(readOnly = true)
class StageService(
    /** Stageエンティティの保存と取得を担当するリポジトリ。 */
    private val stageRepository: StageRepository,
    /** 応募情報の存在確認に使用するリポジトリ。 */
    private val applicationRepository: ApplicationRepository,
    /** ステージ状態変更履歴を保存するリポジトリ。 */
    private val stageStatusHistoryRepository: StageStatusHistoryRepository
) {

    /** 指定した応募情報に新しい選考ステージを登録する。 */
    @Transactional
    fun createStage(applicationId: Long, request: CreateStageRequest): StageResponse {
        val application = findApplicationById(applicationId)
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

    /** 指定した応募情報に属する選考ステージ一覧を順序順で取得する。 */
    fun getStages(applicationId: Long): List<StageResponse> {
        findApplicationById(applicationId)

        return stageRepository.findAllByApplicationIdOrderByStageOrderAsc(applicationId)
            .map(StageResponse::from)
    }

    /** 指定した選考ステージを部分更新する。 */
    @Transactional
    fun updateStage(id: Long, request: UpdateStageRequest): StageResponse {
        val stage = findStageById(id)
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

    /** 指定した選考ステージを削除する。 */
    @Transactional
    fun deleteStage(id: Long) {
        val stage = findStageById(id)
        stageRepository.delete(stage)
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

    /** 同じ応募情報内で stageOrder が重複しないように検証する。 */
    private fun validateStageOrder(applicationId: Long, stageOrder: Int) {
        if (stageRepository.existsByApplicationIdAndStageOrder(applicationId, stageOrder)) {
            throw IllegalArgumentException("応募情報ID $applicationId ではステージ順序 $stageOrder がすでに使用されています。")
        }
    }

    /** ステージ状態が変更された場合のみ履歴を自動保存する。 */
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
