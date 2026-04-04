package com.hyunwoo.jobselectiontracker.company.controller

import com.hyunwoo.jobselectiontracker.company.dto.CompanyResponse
import com.hyunwoo.jobselectiontracker.company.dto.CreateCompanyRequest
import com.hyunwoo.jobselectiontracker.company.dto.UpdateCompanyRequest
import com.hyunwoo.jobselectiontracker.company.service.CompanyService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

/**
 * 企業情報に対するHTTPリクエストを受け付けるRESTコントローラ。
 * 実際の業務処理はServiceへ委譲し、ここではルーティングと入出力の橋渡しを行う。
 */
@RestController
@RequestMapping("/companies")
class CompanyController(
    /** 企業関連の業務ロジックを扱うサービス。 */
    private val companyService: CompanyService
) {

    /** 企業を新規登録するAPI。成功時は201 Createdを返す。 */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createCompany(
        @Valid @RequestBody request: CreateCompanyRequest
    ): CompanyResponse {
        return companyService.createCompany(request)
    }

    /** 企業一覧を取得するAPI。 */
    @GetMapping
    fun getCompanies(): List<CompanyResponse> {
        return companyService.getCompanies()
    }

    /** 指定した企業IDの詳細を取得するAPI。 */
    @GetMapping("/{id}")
    fun getCompany(
        @PathVariable id: Long
    ): CompanyResponse {
        return companyService.getCompany(id)
    }

    /** 指定した企業IDの情報を部分更新するAPI。 */
    @PatchMapping("/{id}")
    fun updateCompany(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateCompanyRequest
    ): CompanyResponse {
        return companyService.updateCompany(id, request)
    }

    /** 指定した企業IDの情報を削除するAPI。成功時は204 No Contentを返す。 */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteCompany(
        @PathVariable id: Long
    ) {
        companyService.deleteCompany(id)
    }
}
