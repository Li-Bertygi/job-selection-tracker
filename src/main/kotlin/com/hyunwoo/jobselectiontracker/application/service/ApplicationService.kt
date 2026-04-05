package com.hyunwoo.jobselectiontracker.application.service

import com.hyunwoo.jobselectiontracker.application.dto.ApplicationResponse
import com.hyunwoo.jobselectiontracker.application.dto.CreateApplicationRequest
import com.hyunwoo.jobselectiontracker.application.dto.UpdateApplicationRequest
import com.hyunwoo.jobselectiontracker.application.entity.Application
import com.hyunwoo.jobselectiontracker.application.entity.ApplicationStatus
import com.hyunwoo.jobselectiontracker.application.history.entity.ApplicationStatusHistory
import com.hyunwoo.jobselectiontracker.application.history.repository.ApplicationStatusHistoryRepository
import com.hyunwoo.jobselectiontracker.application.repository.ApplicationRepository
import com.hyunwoo.jobselectiontracker.company.entity.Company
import com.hyunwoo.jobselectiontracker.company.repository.CompanyRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.NoSuchElementException

/**
 * 応募情報ドメインのビジネスロジックを担当するサービス。
 * 企業存在確認、応募情報CRUD、ステータス変更履歴の自動記録を行う。
 */
@Service
@Transactional(readOnly = true)
class ApplicationService(
    /** 応募情報エンティティの保存と取得を担当するリポジトリ。 */
    private val applicationRepository: ApplicationRepository,
    /** 企業存在確認に使用するリポジトリ。 */
    private val companyRepository: CompanyRepository,
    /** 応募全体のステータス変更履歴を保存するリポジトリ。 */
    private val applicationStatusHistoryRepository: ApplicationStatusHistoryRepository
) {

    /** 新しい応募情報を登録する。 */
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

    /** 指定した応募情報の詳細を取得する。 */
    fun getApplication(id: Long): ApplicationResponse {
        val application = findApplicationById(id)
        return ApplicationResponse.from(application)
    }

    /** 指定した応募情報を部分更新する。 */
    @Transactional
    fun updateApplication(id: Long, request: UpdateApplicationRequest): ApplicationResponse {
        val application = findApplicationById(id)
        val previousStatus = application.status

        request.companyId?.let { application.company = findCompanyById(it) }
        request.jobTitle?.let { application.jobTitle = it.trim() }
        request.applicationRoute?.let { application.applicationRoute = it.trim() }
        request.status?.let { application.status = it }
        request.appliedAt?.let { application.appliedAt = it }
        request.resultDate?.let { application.resultDate = it }
        request.offerDeadline?.let { application.offerDeadline = it }
        request.priority?.let { application.priority = it }
        request.isArchived?.let { application.isArchived = it }

        val savedApplication = applicationRepository.saveAndFlush(application)
        saveStatusHistoryIfChanged(savedApplication, previousStatus, savedApplication.status)

        return ApplicationResponse.from(savedApplication)
    }

    /** 指定した応募情報を削除する。 */
    @Transactional
    fun deleteApplication(id: Long) {
        val application = findApplicationById(id)
        applicationRepository.delete(application)
    }

    /** 応募情報IDで応募情報を取得し、存在しなければ404対象の例外を投げる。 */
    private fun findApplicationById(id: Long): Application {
        return applicationRepository.findById(id)
            .orElseThrow {
                NoSuchElementException("応募情報ID $id に該当する応募情報が見つかりません。")
            }
    }

    /** 企業IDで企業を取得し、存在しなければ404対象の例外を投げる。 */
    private fun findCompanyById(id: Long): Company {
        return companyRepository.findById(id)
            .orElseThrow {
                NoSuchElementException("企業ID $id に該当する企業が見つかりません。")
            }
    }

    /** 応募全体のステータスが変更された場合のみ履歴を自動保存する。 */
    private fun saveStatusHistoryIfChanged(
        application: Application,
        previousStatus: ApplicationStatus,
        currentStatus: ApplicationStatus
    ) {
        if (previousStatus == currentStatus) {
            return
        }

        applicationStatusHistoryRepository.save(
            ApplicationStatusHistory(
                application = application,
                fromStatus = previousStatus,
                toStatus = currentStatus
            )
        )
    }
}
