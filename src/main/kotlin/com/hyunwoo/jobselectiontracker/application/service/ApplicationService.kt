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
import com.hyunwoo.jobselectiontracker.user.entity.User
import com.hyunwoo.jobselectiontracker.user.repository.UserRepository
import java.util.NoSuchElementException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 応募情報の作成、取得、更新、削除を担当するサービス。
 * すべての参照と更新は現在ログイン中のユーザーに紐づく応募情報のみを対象とする。
 */
@Service
@Transactional(readOnly = true)
class ApplicationService(
    /** 応募情報の永続化を担当するリポジトリ。 */
    private val applicationRepository: ApplicationRepository,

    /** 応募先企業の取得に使用するリポジトリ。 */
    private val companyRepository: CompanyRepository,

    /** 現在ログイン中ユーザーの取得に使用するリポジトリ。 */
    private val userRepository: UserRepository,

    /** 応募ステータス変更履歴の保存に使用するリポジトリ。 */
    private val applicationStatusHistoryRepository: ApplicationStatusHistoryRepository
) {

    /**
     * 現在認証されているユーザーを所有者として応募情報を作成する。
     */
    @Transactional
    fun createApplication(request: CreateApplicationRequest): ApplicationResponse {
        val user = findCurrentUser()
        val company = findCompanyByIdAndUserId(request.companyId, user.id!!)

        val application = Application(
            user = user,
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

    /**
     * 現在ログイン中ユーザーに属する応募情報一覧を取得する。
     */
    fun getApplications(): List<ApplicationResponse> {
        val currentUser = findCurrentUser()
        return applicationRepository.findAllByUserIdOrderByUpdatedAtDesc(currentUser.id!!)
            .map(ApplicationResponse::from)
    }

    /**
     * 指定した応募情報IDの詳細を取得する。
     * 他ユーザーの応募情報は存在しないものとして扱う。
     */
    fun getApplication(id: Long): ApplicationResponse {
        val currentUser = findCurrentUser()
        return ApplicationResponse.from(findApplicationByIdAndUserId(id, currentUser.id!!))
    }

    /**
     * 指定した応募情報を更新する。
     * ステータスが変化した場合は履歴を自動保存する。
     */
    @Transactional
    fun updateApplication(id: Long, request: UpdateApplicationRequest): ApplicationResponse {
        val currentUser = findCurrentUser()
        val application = findApplicationByIdAndUserId(id, currentUser.id!!)
        val previousStatus = application.status

        request.companyId?.let {
            application.company = findCompanyByIdAndUserId(it, currentUser.id!!)
        }
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

    /**
     * 指定した応募情報を削除する。
     * 他ユーザーの応募情報は存在しないものとして扱う。
     */
    @Transactional
    fun deleteApplication(id: Long) {
        val currentUser = findCurrentUser()
        applicationRepository.delete(findApplicationByIdAndUserId(id, currentUser.id!!))
    }

    /**
     * 応募情報IDとユーザーIDで応募情報を取得し、存在しない場合は 404 用例外を送出する。
     */
    private fun findApplicationByIdAndUserId(id: Long, userId: Long): Application {
        return applicationRepository.findByIdAndUserId(id, userId)
            ?: throw NoSuchElementException("応募情報ID $id に該当する応募情報が見つかりません。")
    }

    /**
     * 企業IDとユーザーIDで企業を取得し、存在しない場合は 404 用例外を送出する。
     */
    private fun findCompanyByIdAndUserId(id: Long, userId: Long): Company {
        return companyRepository.findByIdAndUserId(id, userId)
            ?: throw NoSuchElementException("企業ID $id に該当する企業が見つかりません。")
    }

    /**
     * SecurityContext に格納された認証情報から現在ユーザーを取得する。
     */
    private fun findCurrentUser(): User {
        val email = SecurityContextHolder.getContext().authentication?.name
            ?: throw IllegalStateException("現在の認証ユーザー情報を取得できません。")

        return userRepository.findByEmail(email)
            ?: throw NoSuchElementException("メールアドレス $email に該当するユーザーが見つかりません。")
    }

    /**
     * 応募ステータスが実際に変化した場合のみ変更履歴を保存する。
     */
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
