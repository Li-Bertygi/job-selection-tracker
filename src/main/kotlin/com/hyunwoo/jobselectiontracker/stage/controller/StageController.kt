package com.hyunwoo.jobselectiontracker.stage.controller

import com.hyunwoo.jobselectiontracker.stage.dto.CreateStageRequest
import com.hyunwoo.jobselectiontracker.stage.dto.StageResponse
import com.hyunwoo.jobselectiontracker.stage.dto.UpdateStageRequest
import com.hyunwoo.jobselectiontracker.stage.service.StageService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

/**
 * 選考ステージに対するHTTPリクエストを受け付けるRESTコントローラ。
 * 実際の業務処理はServiceへ委譲し、ここではルーティングと入出力の橋渡しを行う。
 */
@RestController
@RequestMapping
class StageController(
    /** 選考ステージ関連の業務ロジックを扱うサービス。 */
    private val stageService: StageService
) {

    /** 指定した応募情報に選考ステージを追加するAPI。成功時は201 Createdを返す。 */
    @PostMapping("/applications/{applicationId}/stages")
    @ResponseStatus(HttpStatus.CREATED)
    fun createStage(
        @PathVariable applicationId: Long,
        @Valid @RequestBody request: CreateStageRequest
    ): StageResponse {
        return stageService.createStage(applicationId, request)
    }

    /** 指定した応募情報に紐づく選考ステージ一覧を取得するAPI。 */
    @GetMapping("/applications/{applicationId}/stages")
    fun getStages(
        @PathVariable applicationId: Long
    ): List<StageResponse> {
        return stageService.getStages(applicationId)
    }

    /** 指定したステージIDの内容を部分更新するAPI。 */
    @PatchMapping("/stages/{id}")
    fun updateStage(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateStageRequest
    ): StageResponse {
        return stageService.updateStage(id, request)
    }

    /** 指定したステージIDを削除するAPI。成功時は204 No Contentを返す。 */
    @DeleteMapping("/stages/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteStage(
        @PathVariable id: Long
    ) {
        stageService.deleteStage(id)
    }
}
