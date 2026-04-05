package com.hyunwoo.jobselectiontracker.application.history.repository

import com.hyunwoo.jobselectiontracker.application.history.entity.ApplicationStatusHistory
import org.springframework.data.jpa.repository.JpaRepository

/**
 * 応募全体のステータス変更履歴を扱うリポジトリ。
 * 応募情報単位の履歴取得に使用する。
 */
interface ApplicationStatusHistoryRepository : JpaRepository<ApplicationStatusHistory, Long> {

    /** 指定した応募情報に属するステータス変更履歴を新しい順で取得する。 */
    fun findAllByApplicationIdOrderByChangedAtDesc(applicationId: Long): List<ApplicationStatusHistory>
}
