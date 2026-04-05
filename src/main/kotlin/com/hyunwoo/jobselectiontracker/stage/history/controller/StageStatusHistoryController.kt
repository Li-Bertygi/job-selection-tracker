package com.hyunwoo.jobselectiontracker.stage.history.controller

import com.hyunwoo.jobselectiontracker.stage.history.dto.StageStatusHistoryResponse
import com.hyunwoo.jobselectiontracker.stage.history.service.StageStatusHistoryService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 各選考ステージの状態変更履歴に関するAPIを提供するコントローラ。
 */
@RestController
@RequestMapping
class StageStatusHistoryController(
    /** ステージ状態変更履歴の参照ロジックを委譲するサービス。 */
    private val stageStatusHistoryService: StageStatusHistoryService
) {

    /** 指定したステージに属する状態変更履歴一覧を取得する。 */
    @GetMapping("/stages/{stageId}/status-histories")
    fun getStageStatusHistories(
        @PathVariable stageId: Long
    ): List<StageStatusHistoryResponse> {
        return stageStatusHistoryService.getStageStatusHistories(stageId)
    }
}
