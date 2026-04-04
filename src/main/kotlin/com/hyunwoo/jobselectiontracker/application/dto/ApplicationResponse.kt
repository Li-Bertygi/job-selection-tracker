package com.hyunwoo.jobselectiontracker.application.dto

import com.hyunwoo.jobselectiontracker.application.entity.Application
import com.hyunwoo.jobselectiontracker.application.entity.ApplicationStatus
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 応募情報APIのレスポンスとして返すDTO。
 * エンティティ全体をそのまま公開せず、必要な情報だけを返すために利用する。
 */
data class ApplicationResponse(
    /** 応募情報ID。 */
    val id: Long,
    /** 紐づく企業ID。 */
    val companyId: Long,
    /** 応募職種。 */
    val jobTitle: String,
    /** 応募経路。 */
    val applicationRoute: String?,
    /** 応募全体の進捗状態。 */
    val status: ApplicationStatus,
    /** 応募日。 */
    val appliedAt: LocalDate?,
    /** 結果通知日。 */
    val resultDate: LocalDate?,
    /** 内定承諾期限。 */
    val offerDeadline: LocalDate?,
    /** 優先度。 */
    val priority: Int,
    /** 保管状態。 */
    val isArchived: Boolean,
    /** 作成日時。 */
    val createdAt: LocalDateTime?,
    /** 更新日時。 */
    val updatedAt: LocalDateTime?
) {
    companion object {
        /**
         * ApplicationエンティティをAPIレスポンス形式へ変換する。
         * 必須のIDが存在しない場合は不正な状態として例外を投げる。
         */
        fun from(application: Application): ApplicationResponse {
            return ApplicationResponse(
                id = application.id ?: throw IllegalStateException("応募情報IDが存在しません。"),
                companyId = application.company.id
                    ?: throw IllegalStateException("企業IDが存在しません。"),
                jobTitle = application.jobTitle,
                applicationRoute = application.applicationRoute,
                status = application.status,
                appliedAt = application.appliedAt,
                resultDate = application.resultDate,
                offerDeadline = application.offerDeadline,
                priority = application.priority,
                isArchived = application.isArchived,
                createdAt = application.createdAt,
                updatedAt = application.updatedAt
            )
        }
    }
}
