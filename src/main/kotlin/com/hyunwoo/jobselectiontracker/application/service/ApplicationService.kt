package com.hyunwoo.jobselectiontracker.application.service

import com.hyunwoo.jobselectiontracker.application.dto.ApplicationResponse
import com.hyunwoo.jobselectiontracker.application.dto.CreateApplicationRequest
import com.hyunwoo.jobselectiontracker.application.dto.UpdateApplicationRequest
import com.hyunwoo.jobselectiontracker.application.entity.Application
import com.hyunwoo.jobselectiontracker.application.repository.ApplicationRepository
import com.hyunwoo.jobselectiontracker.company.entity.Company
import com.hyunwoo.jobselectiontracker.company.repository.CompanyRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.NoSuchElementException

/**
 * 応募情報ドメインの業務ロジックを担当するサービス。
 * 企業存在確認、応募情報の生成・更新・削除、レスポンスDTO変換をここで行う。
 */
@Service
@Transactional(readOnly = true)
class ApplicationService(
    /** ApplicationテーブルへのCRUDを実行するリポジトリ。 */
    private val applicationRepository: ApplicationRepository,
    /** 応募先企業の存在確認に使うリポジトリ。 */
    private val companyRepository: CompanyRepository
) {

    /** 新しい応募情報を保存し、保存結果をレスポンスDTOへ変換して返す。 */
    @Transactional
    fun createApplication(request: CreateApplicationRequest): ApplicationResponse {
        val company = findCompanyById(request.companyId)

        val application = Application(
            company = company,
            jobTitle = request.jobTitle.trim(),
            applicationRoute = request.applicationRoute?.trim(),
            status = request.status,
            appliedAt = request.appliedAt,
            resultDate = request.resultDate,
            offerDeadline = request.offerDeadline,
            priority = request.priority,
            isArchived = request.isArchived
        )

        return ApplicationResponse.from(applicationRepository.save(application))
    }

    /** 登録済みの応募情報一覧を取得する。 */
    fun getApplications(): List<ApplicationResponse> {
        return applicationRepository.findAll()
            .map(ApplicationResponse::from)
    }

    /** 指定された応募情報IDの詳細を取得する。 */
    fun getApplication(id: Long): ApplicationResponse {
        val application = findApplicationById(id)
        return ApplicationResponse.from(application)
    }

    /** 指定された応募情報を部分更新し、更新後の内容を返す。 */
    @Transactional
    fun updateApplication(id: Long, request: UpdateApplicationRequest): ApplicationResponse {
        val application = findApplicationById(id)

        request.companyId?.let { application.company = findCompanyById(it) }
        request.jobTitle?.let { application.jobTitle = it.trim() }
        request.applicationRoute?.let { application.applicationRoute = it.trim() }
        request.status?.let { application.status = it }
        request.appliedAt?.let { application.appliedAt = it }
        request.resultDate?.let { application.resultDate = it }
        request.offerDeadline?.let { application.offerDeadline = it }
        request.priority?.let { application.priority = it }
        request.isArchived?.let { application.isArchived = it }

        return ApplicationResponse.from(applicationRepository.saveAndFlush(application))
    }

    /** 指定された応募情報IDのレコードを削除する。 */
    @Transactional
    fun deleteApplication(id: Long) {
        val application = findApplicationById(id)
        applicationRepository.delete(application)
    }

    /** 共通で利用する応募情報検索処理。存在しない場合は404用の例外を発生させる。 */
    private fun findApplicationById(id: Long): Application {
        return applicationRepository.findById(id)
            .orElseThrow { NoSuchElementException("応募情報ID $id に該当する応募情報が見つかりません。") }
    }

    /** 共通で利用する企業検索処理。存在しない場合は404用の例外を発生させる。 */
    private fun findCompanyById(id: Long): Company {
        return companyRepository.findById(id)
            .orElseThrow { NoSuchElementException("企業ID $id に該当する企業が見つかりません。") }
    }
}
