package com.hyunwoo.jobselectiontracker.application.history.controller

import com.hyunwoo.jobselectiontracker.application.history.dto.ApplicationStatusHistoryResponse
import com.hyunwoo.jobselectiontracker.application.history.service.ApplicationStatusHistoryService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 応募全体のステータス変更履歴に関するAPIを提供するコントローラ。
 */
@RestController
@RequestMapping
class ApplicationStatusHistoryController(
    /** ステータス変更履歴の参照ロジックを委譲するサービス。 */
    private val applicationStatusHistoryService: ApplicationStatusHistoryService
) {

    /** 指定した応募情報に属するステータス変更履歴一覧を取得する。 */
    @GetMapping("/applications/{applicationId}/status-histories")
    fun getApplicationStatusHistories(
        @PathVariable applicationId: Long
    ): List<ApplicationStatusHistoryResponse> {
        return applicationStatusHistoryService.getApplicationStatusHistories(applicationId)
    }
}
