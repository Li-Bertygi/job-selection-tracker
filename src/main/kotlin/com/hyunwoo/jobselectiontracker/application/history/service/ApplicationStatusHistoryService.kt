package com.hyunwoo.jobselectiontracker.application.history.service

import com.hyunwoo.jobselectiontracker.application.history.dto.ApplicationStatusHistoryResponse
import com.hyunwoo.jobselectiontracker.application.history.repository.ApplicationStatusHistoryRepository
import com.hyunwoo.jobselectiontracker.application.repository.ApplicationRepository
import com.hyunwoo.jobselectiontracker.common.exception.NotFoundException
import com.hyunwoo.jobselectiontracker.common.security.CurrentUserProvider
import com.hyunwoo.jobselectiontracker.user.entity.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ApplicationStatusHistoryService(
    private val applicationStatusHistoryRepository: ApplicationStatusHistoryRepository,
    private val applicationRepository: ApplicationRepository,
    private val currentUserProvider: CurrentUserProvider
) {

    fun getApplicationStatusHistories(applicationId: Long): List<ApplicationStatusHistoryResponse> {
        val currentUser = findCurrentUser()
        if (applicationRepository.findByIdAndUserId(applicationId, currentUser.id!!) == null) {
            throw NotFoundException("Application $applicationId was not found.")
        }

        return applicationStatusHistoryRepository.findAllByApplicationIdOrderByChangedAtDesc(applicationId)
            .map(ApplicationStatusHistoryResponse::from)
    }

    private fun findCurrentUser(): User {
        return currentUserProvider.getCurrentUser()
    }
}
