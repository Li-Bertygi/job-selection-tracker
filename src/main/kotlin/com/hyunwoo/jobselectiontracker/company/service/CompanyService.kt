package com.hyunwoo.jobselectiontracker.company.service

import com.hyunwoo.jobselectiontracker.company.dto.CompanyResponse
import com.hyunwoo.jobselectiontracker.company.dto.CreateCompanyRequest
import com.hyunwoo.jobselectiontracker.company.dto.UpdateCompanyRequest
import com.hyunwoo.jobselectiontracker.company.entity.Company
import com.hyunwoo.jobselectiontracker.company.repository.CompanyRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.NoSuchElementException

/**
 * 企業ドメインの業務ロジックを担当するサービス。
 * Controllerから受け取ったDTOをエンティティへ変換し、Repository経由で永続化する。
 */
@Service
@Transactional(readOnly = true)
class CompanyService(
    /** CompanyテーブルへのCRUDを実行するリポジトリ。 */
    private val companyRepository: CompanyRepository
) {

    /** 新しい企業情報を保存し、保存結果をレスポンスDTOへ変換して返す。 */
    @Transactional
    fun createCompany(request: CreateCompanyRequest): CompanyResponse {
        val company = Company(
            name = request.name.trim(),
            industry = request.industry?.trim(),
            websiteUrl = request.websiteUrl?.trim(),
            memo = request.memo?.trim()
        )

        return CompanyResponse.from(companyRepository.save(company))
    }

    /** 登録済みの企業一覧を取得する。 */
    fun getCompanies(): List<CompanyResponse> {
        return companyRepository.findAll()
            .map(CompanyResponse::from)
    }

    /** 指定された企業IDの詳細を取得する。 */
    fun getCompany(id: Long): CompanyResponse {
        val company = findCompanyById(id)
        return CompanyResponse.from(company)
    }

    /** 指定された企業の情報を部分更新し、更新後の内容を返す。 */
    @Transactional
    fun updateCompany(id: Long, request: UpdateCompanyRequest): CompanyResponse {
        val company = findCompanyById(id)

        request.name?.let { company.name = it.trim() }
        request.industry?.let { company.industry = it.trim() }
        request.websiteUrl?.let { company.websiteUrl = it.trim() }
        request.memo?.let { company.memo = it.trim() }

        return CompanyResponse.from(companyRepository.saveAndFlush(company))
    }

    /** 指定された企業IDのレコードを削除する。 */
    @Transactional
    fun deleteCompany(id: Long) {
        val company = findCompanyById(id)
        companyRepository.delete(company)
    }

    /** 共通で利用する企業検索処理。存在しない場合は404用の例外を発生させる。 */
    private fun findCompanyById(id: Long): Company {
        return companyRepository.findById(id)
            .orElseThrow { NoSuchElementException("企業ID $id に該当する企業が見つかりません。") }
    }
}
