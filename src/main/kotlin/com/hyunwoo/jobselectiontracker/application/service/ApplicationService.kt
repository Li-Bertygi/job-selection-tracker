package com.hyunwoo.jobselectiontracker.application.service

import com.hyunwoo.jobselectiontracker.application.dto.ApplicationResponse
import com.hyunwoo.jobselectiontracker.application.dto.CreateApplicationRequest
import com.hyunwoo.jobselectiontracker.application.dto.UpdateApplicationRequest
import com.hyunwoo.jobselectiontracker.application.entity.Application
import com.hyunwoo.jobselectiontracker.application.entity.ApplicationStatus
import com.hyunwoo.jobselectiontracker.application.history.entity.ApplicationStatusHistory
import com.hyunwoo.jobselectiontracker.application.history.repository.ApplicationStatusHistoryRepository
import com.hyunwoo.jobselectiontracker.application.repository.ApplicationRepository
import com.hyunwoo.jobselectiontracker.common.exception.InvalidRequestException
import com.hyunwoo.jobselectiontracker.common.response.PageResponse
import com.hyunwoo.jobselectiontracker.common.security.CurrentUserProvider
import com.hyunwoo.jobselectiontracker.common.security.OwnedResourceFinder
import com.hyunwoo.jobselectiontracker.company.entity.Company
import com.hyunwoo.jobselectiontracker.user.entity.User
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ApplicationService(
    private val applicationRepository: ApplicationRepository,
    private val currentUserProvider: CurrentUserProvider,
    private val ownedResourceFinder: OwnedResourceFinder,
    private val applicationStatusHistoryRepository: ApplicationStatusHistoryRepository
) {

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

    fun getApplications(
        status: ApplicationStatus?,
        isArchived: Boolean?,
        keyword: String?,
        page: Int,
        size: Int
    ): PageResponse<ApplicationResponse> {
        val currentUser = findCurrentUser()
        val normalizedKeyword = keyword?.trim()?.takeIf { it.isNotEmpty() }
        val pageable = PageRequest.of(
            validatePage(page),
            validateSize(size),
            Sort.by(Sort.Direction.DESC, "updatedAt")
        )

        return PageResponse.from(
            applicationRepository.searchByUser(
                userId = currentUser.id!!,
                status = status,
                isArchived = isArchived,
                keyword = normalizedKeyword,
                pageable = pageable
            ).map(ApplicationResponse::from)
        )
    }

    fun getApplication(id: Long): ApplicationResponse {
        val currentUser = findCurrentUser()
        return ApplicationResponse.from(findApplicationByIdAndUserId(id, currentUser.id!!))
    }

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
        request.status?.let {
            validateStatusTransition(application.status, it)
            application.status = it
        }
        request.appliedAt?.let { application.appliedAt = it }
        request.resultDate?.let { application.resultDate = it }
        request.offerDeadline?.let { application.offerDeadline = it }
        request.priority?.let { application.priority = it }
        request.isArchived?.let { application.isArchived = it }

        val savedApplication = applicationRepository.saveAndFlush(application)
        saveStatusHistoryIfChanged(savedApplication, previousStatus, savedApplication.status)

        return ApplicationResponse.from(savedApplication)
    }

    @Transactional
    fun deleteApplication(id: Long) {
        val currentUser = findCurrentUser()
        applicationRepository.delete(findApplicationByIdAndUserId(id, currentUser.id!!))
    }

    private fun findApplicationByIdAndUserId(id: Long, userId: Long): Application {
        return ownedResourceFinder.findApplication(id, userId)
    }

    private fun findCompanyByIdAndUserId(id: Long, userId: Long): Company {
        return ownedResourceFinder.findCompany(id, userId)
    }

    private fun findCurrentUser(): User {
        return currentUserProvider.getCurrentUser()
    }

    private fun validatePage(page: Int): Int {
        if (page < 0) {
            throw InvalidRequestException("page は0以上を指定してください。")
        }

        return page
    }

    private fun validateSize(size: Int): Int {
        if (size !in 1..100) {
            throw InvalidRequestException("size は1以上100以下を指定してください。")
        }

        return size
    }

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

    private fun validateStatusTransition(
        currentStatus: ApplicationStatus,
        nextStatus: ApplicationStatus
    ) {
        if (currentStatus == nextStatus) {
            return
        }

        val allowedNextStatuses = when (currentStatus) {
            ApplicationStatus.NOT_STARTED -> setOf(
                ApplicationStatus.APPLICATION,
                ApplicationStatus.INFO_SESSION,
                ApplicationStatus.DOCUMENT_SCREENING,
                ApplicationStatus.TEST,
                ApplicationStatus.CASUAL_MEETING,
                ApplicationStatus.INTERVIEW,
                ApplicationStatus.REJECTED
            )
            ApplicationStatus.APPLICATION -> setOf(
                ApplicationStatus.INFO_SESSION,
                ApplicationStatus.DOCUMENT_SCREENING,
                ApplicationStatus.TEST,
                ApplicationStatus.CASUAL_MEETING,
                ApplicationStatus.INTERVIEW,
                ApplicationStatus.REJECTED
            )
            ApplicationStatus.INFO_SESSION -> setOf(
                ApplicationStatus.DOCUMENT_SCREENING,
                ApplicationStatus.TEST,
                ApplicationStatus.CASUAL_MEETING,
                ApplicationStatus.INTERVIEW,
                ApplicationStatus.REJECTED
            )
            ApplicationStatus.DOCUMENT_SCREENING -> setOf(
                ApplicationStatus.TEST,
                ApplicationStatus.CASUAL_MEETING,
                ApplicationStatus.INTERVIEW,
                ApplicationStatus.REJECTED
            )
            ApplicationStatus.TEST -> setOf(
                ApplicationStatus.CASUAL_MEETING,
                ApplicationStatus.INTERVIEW,
                ApplicationStatus.REJECTED
            )
            ApplicationStatus.CASUAL_MEETING -> setOf(
                ApplicationStatus.INTERVIEW,
                ApplicationStatus.REJECTED
            )
            ApplicationStatus.INTERVIEW -> setOf(
                ApplicationStatus.OFFERED,
                ApplicationStatus.REJECTED
            )
            ApplicationStatus.OFFERED,
            ApplicationStatus.REJECTED -> emptySet()
        }

        if (nextStatus !in allowedNextStatuses) {
            throw InvalidRequestException(
                "応募ステータスを $currentStatus から $nextStatus へ変更することはできません。"
            )
        }
    }
}
