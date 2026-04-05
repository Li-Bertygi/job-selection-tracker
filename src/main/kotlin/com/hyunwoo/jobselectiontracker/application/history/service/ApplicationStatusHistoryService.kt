package com.hyunwoo.jobselectiontracker.application.history.service

import com.hyunwoo.jobselectiontracker.application.history.dto.ApplicationStatusHistoryResponse
import com.hyunwoo.jobselectiontracker.application.history.repository.ApplicationStatusHistoryRepository
import com.hyunwoo.jobselectiontracker.application.repository.ApplicationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.NoSuchElementException

/**
 * 応募全体のステータス変更履歴の参照ロジックを担当するサービス。
 */
@Service
@Transactional(readOnly = true)
class ApplicationStatusHistoryService(
    /** ステータス変更履歴の取得を担当するリポジトリ。 */
    private val applicationStatusHistoryRepository: ApplicationStatusHistoryRepository,
    /** 応募情報の存在確認に使用するリポジトリ。 */
    private val applicationRepository: ApplicationRepository
) {

    /** 指定した応募情報に属するステータス変更履歴を新しい順で取得する。 */
    fun getApplicationStatusHistories(applicationId: Long): List<ApplicationStatusHistoryResponse> {
        if (!applicationRepository.existsById(applicationId)) {
            throw NoSuchElementException("応募情報ID $applicationId に該当する応募情報が見つかりません。")
        }

        return applicationStatusHistoryRepository.findAllByApplicationIdOrderByChangedAtDesc(applicationId)
            .map(ApplicationStatusHistoryResponse::from)
    }
}
