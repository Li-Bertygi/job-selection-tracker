package com.hyunwoo.jobselectiontracker.stage.history.service

import com.hyunwoo.jobselectiontracker.stage.history.dto.StageStatusHistoryResponse
import com.hyunwoo.jobselectiontracker.stage.history.repository.StageStatusHistoryRepository
import com.hyunwoo.jobselectiontracker.stage.repository.StageRepository
import com.hyunwoo.jobselectiontracker.user.entity.User
import com.hyunwoo.jobselectiontracker.user.repository.UserRepository
import java.util.NoSuchElementException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * ステージステータス履歴の取得を担当するサービス。
 * 現在ログイン中ユーザーに属するステージの履歴のみ返却する。
 */
@Service
@Transactional(readOnly = true)
class StageStatusHistoryService(
    private val stageStatusHistoryRepository: StageStatusHistoryRepository,
    private val stageRepository: StageRepository,
    private val userRepository: UserRepository
) {

    fun getStageStatusHistories(stageId: Long): List<StageStatusHistoryResponse> {
        val currentUser = findCurrentUser()
        if (stageRepository.findByIdAndApplicationUserId(stageId, currentUser.id!!) == null) {
            throw NoSuchElementException("ステージID $stageId に該当するステージが見つかりません。")
        }

        return stageStatusHistoryRepository.findAllByStageIdOrderByChangedAtDesc(stageId)
            .map(StageStatusHistoryResponse::from)
    }

    private fun findCurrentUser(): User {
        val email = SecurityContextHolder.getContext().authentication?.name
            ?: throw IllegalStateException("現在の認証ユーザー情報を取得できません。")

        return userRepository.findByEmail(email)
            ?: throw NoSuchElementException("メールアドレス $email に該当するユーザーが見つかりません。")
    }
}
