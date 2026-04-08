package com.hyunwoo.jobselectiontracker.application.history.service

import com.hyunwoo.jobselectiontracker.application.history.dto.ApplicationStatusHistoryResponse
import com.hyunwoo.jobselectiontracker.application.history.repository.ApplicationStatusHistoryRepository
import com.hyunwoo.jobselectiontracker.application.repository.ApplicationRepository
import com.hyunwoo.jobselectiontracker.user.entity.User
import com.hyunwoo.jobselectiontracker.user.repository.UserRepository
import java.util.NoSuchElementException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 応募ステータス履歴の取得を担当するサービス。
 * 現在ログイン中ユーザーに属する応募情報の履歴のみ返却する。
 */
@Service
@Transactional(readOnly = true)
class ApplicationStatusHistoryService(
    private val applicationStatusHistoryRepository: ApplicationStatusHistoryRepository,
    private val applicationRepository: ApplicationRepository,
    private val userRepository: UserRepository
) {

    fun getApplicationStatusHistories(applicationId: Long): List<ApplicationStatusHistoryResponse> {
        val currentUser = findCurrentUser()
        if (applicationRepository.findByIdAndUserId(applicationId, currentUser.id!!) == null) {
            throw NoSuchElementException("応募情報ID $applicationId に該当する応募情報が見つかりません。")
        }

        return applicationStatusHistoryRepository.findAllByApplicationIdOrderByChangedAtDesc(applicationId)
            .map(ApplicationStatusHistoryResponse::from)
    }

    private fun findCurrentUser(): User {
        val email = SecurityContextHolder.getContext().authentication?.name
            ?: throw IllegalStateException("現在の認証ユーザー情報を取得できません。")

        return userRepository.findByEmail(email)
            ?: throw NoSuchElementException("メールアドレス $email に該当するユーザーが見つかりません。")
    }
}
