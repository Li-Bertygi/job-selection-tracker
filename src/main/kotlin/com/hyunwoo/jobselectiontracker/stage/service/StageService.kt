package com.hyunwoo.jobselectiontracker.stage.service

import com.hyunwoo.jobselectiontracker.application.entity.Application
import com.hyunwoo.jobselectiontracker.application.repository.ApplicationRepository
import com.hyunwoo.jobselectiontracker.stage.dto.CreateStageRequest
import com.hyunwoo.jobselectiontracker.stage.dto.StageResponse
import com.hyunwoo.jobselectiontracker.stage.dto.UpdateStageRequest
import com.hyunwoo.jobselectiontracker.stage.entity.Stage
import com.hyunwoo.jobselectiontracker.stage.repository.StageRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.NoSuchElementException

/**
 * 選考ステージドメインの業務ロジックを担当するサービス。
 * 応募情報との紐づけ確認、表示順重複チェック、ステージのCRUDをここで扱う。
 */
@Service
@Transactional(readOnly = true)
class StageService(
    /** StageテーブルへのCRUDを実行するリポジトリ。 */
    private val stageRepository: StageRepository,
    /** 応募情報の存在確認に使うリポジトリ。 */
    private val applicationRepository: ApplicationRepository
) {

    /** 指定した応募情報に新しい選考ステージを追加する。 */
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

    /** 指定した応募情報に紐づく選考ステージ一覧を順序付きで取得する。 */
    fun getStages(applicationId: Long): List<StageResponse> {
        findApplicationById(applicationId)

        return stageRepository.findAllByApplicationIdOrderByStageOrderAsc(applicationId)
            .map(StageResponse::from)
    }

    /** 指定したステージを部分更新し、更新後の内容を返す。 */
    @Transactional
    fun updateStage(id: Long, request: UpdateStageRequest): StageResponse {
        val stage = findStageById(id)

        request.stageOrder?.let {
            if (it != stage.stageOrder) {
                validateStageOrder(stage.application.id ?: throw IllegalStateException("応募情報IDが存在しません。"), it)
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

        return StageResponse.from(stageRepository.saveAndFlush(stage))
    }

    /** 指定したステージを削除する。 */
    @Transactional
    fun deleteStage(id: Long) {
        val stage = findStageById(id)
        stageRepository.delete(stage)
    }

    /** 共通で利用する応募情報検索処理。存在しない場合は404用の例外を発生させる。 */
    private fun findApplicationById(id: Long): Application {
        return applicationRepository.findById(id)
            .orElseThrow { NoSuchElementException("応募情報ID $id に該当する応募情報が見つかりません。") }
    }

    /** 共通で利用するステージ検索処理。存在しない場合は404用の例外を発生させる。 */
    private fun findStageById(id: Long): Stage {
        return stageRepository.findById(id)
            .orElseThrow { NoSuchElementException("ステージID $id に該当するステージが見つかりません。") }
    }

    /** 同一応募内で表示順が重複しないことを確認する。 */
    private fun validateStageOrder(applicationId: Long, stageOrder: Int) {
        if (stageRepository.existsByApplicationIdAndStageOrder(applicationId, stageOrder)) {
            throw IllegalArgumentException("応募情報ID $applicationId ではステージ順序 $stageOrder がすでに使用されています。")
        }
    }
}
