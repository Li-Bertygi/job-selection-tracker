package com.hyunwoo.jobselectiontracker.application.controller

import com.hyunwoo.jobselectiontracker.application.dto.ApplicationResponse
import com.hyunwoo.jobselectiontracker.application.dto.CreateApplicationRequest
import com.hyunwoo.jobselectiontracker.application.dto.UpdateApplicationRequest
import com.hyunwoo.jobselectiontracker.application.entity.ApplicationStatus
import com.hyunwoo.jobselectiontracker.application.service.ApplicationService
import com.hyunwoo.jobselectiontracker.common.response.PageResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

/**
 * 応募情報に対するHTTPリクエストを受け付けるRESTコントローラ。
 * 実際の業務処理はServiceへ委譲し、ここではルーティングと入出力の橋渡しを行う。
 */
@RestController
@RequestMapping("/applications")
class ApplicationController(
    /** 応募情報関連の業務ロジックを扱うサービス。 */
    private val applicationService: ApplicationService
) {

    /** 応募情報を新規登録するAPI。成功時は201 Createdを返す。 */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createApplication(
        @Valid @RequestBody request: CreateApplicationRequest
    ): ApplicationResponse {
        return applicationService.createApplication(request)
    }

    /** 応募情報一覧を検索条件とページング条件つきで取得するAPI。 */
    @GetMapping
    fun getApplications(
        @RequestParam(required = false) status: ApplicationStatus?,
        @RequestParam(required = false) isArchived: Boolean?,
        @RequestParam(required = false) keyword: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): PageResponse<ApplicationResponse> {
        return applicationService.getApplications(
            status = status,
            isArchived = isArchived,
            keyword = keyword,
            page = page,
            size = size
        )
    }

    /** 指定した応募情報IDの詳細を取得するAPI。 */
    @GetMapping("/{id}")
    fun getApplication(
        @PathVariable id: Long
    ): ApplicationResponse {
        return applicationService.getApplication(id)
    }

    /** 指定した応募情報IDの内容を部分更新するAPI。 */
    @PatchMapping("/{id}")
    fun updateApplication(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateApplicationRequest
    ): ApplicationResponse {
        return applicationService.updateApplication(id, request)
    }

    /** 指定した応募情報IDを削除するAPI。成功時は204 No Contentを返す。 */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteApplication(
        @PathVariable id: Long
    ) {
        applicationService.deleteApplication(id)
    }
}
