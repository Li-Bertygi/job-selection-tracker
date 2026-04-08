package com.hyunwoo.jobselectiontracker.stage.history.service

import com.hyunwoo.jobselectiontracker.common.exception.NotFoundException
import com.hyunwoo.jobselectiontracker.common.security.CurrentUserProvider
import com.hyunwoo.jobselectiontracker.stage.history.dto.StageStatusHistoryResponse
import com.hyunwoo.jobselectiontracker.stage.history.repository.StageStatusHistoryRepository
import com.hyunwoo.jobselectiontracker.stage.repository.StageRepository
import com.hyunwoo.jobselectiontracker.user.entity.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class StageStatusHistoryService(
    private val stageStatusHistoryRepository: StageStatusHistoryRepository,
    private val stageRepository: StageRepository,
    private val currentUserProvider: CurrentUserProvider
) {

    fun getStageStatusHistories(stageId: Long): List<StageStatusHistoryResponse> {
        val currentUser = findCurrentUser()
        if (stageRepository.findByIdAndApplicationUserId(stageId, currentUser.id!!) == null) {
            throw NotFoundException("Stage $stageId was not found.")
        }

        return stageStatusHistoryRepository.findAllByStageIdOrderByChangedAtDesc(stageId)
            .map(StageStatusHistoryResponse::from)
    }

    private fun findCurrentUser(): User {
        return currentUserProvider.getCurrentUser()
    }
}
