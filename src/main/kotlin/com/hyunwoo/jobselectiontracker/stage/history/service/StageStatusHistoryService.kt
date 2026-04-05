package com.hyunwoo.jobselectiontracker.stage.history.service

import com.hyunwoo.jobselectiontracker.stage.history.dto.StageStatusHistoryResponse
import com.hyunwoo.jobselectiontracker.stage.history.repository.StageStatusHistoryRepository
import com.hyunwoo.jobselectiontracker.stage.repository.StageRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.NoSuchElementException

/**
 * 各選考ステージの状態変更履歴の参照ロジックを担当するサービス。
 */
@Service
@Transactional(readOnly = true)
class StageStatusHistoryService(
    /** ステージ状態変更履歴の取得を担当するリポジトリ。 */
    private val stageStatusHistoryRepository: StageStatusHistoryRepository,
    /** ステージの存在確認に使用するリポジトリ。 */
    private val stageRepository: StageRepository
) {

    /** 指定したステージに属する状態変更履歴を新しい順で取得する。 */
    fun getStageStatusHistories(stageId: Long): List<StageStatusHistoryResponse> {
        if (!stageRepository.existsById(stageId)) {
            throw NoSuchElementException("ステージID $stageId に該当するステージが見つかりません。")
        }

        return stageStatusHistoryRepository.findAllByStageIdOrderByChangedAtDesc(stageId)
            .map(StageStatusHistoryResponse::from)
    }
}
