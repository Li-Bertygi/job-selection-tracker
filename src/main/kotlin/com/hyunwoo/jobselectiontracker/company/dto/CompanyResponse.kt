package com.hyunwoo.jobselectiontracker.company.dto

import com.hyunwoo.jobselectiontracker.company.entity.Company
import java.time.LocalDateTime

/**
 * 企業APIのレスポンスとして返すDTO。
 * エンティティをそのまま公開せず、必要な項目だけを返すために利用する。
 */
data class CompanyResponse(
    /** 企業ID。 */
    val id: Long,
    /** 企業名。 */
    val name: String,
    /** 業種。 */
    val industry: String?,
    /** 企業の公式サイトURL。 */
    val websiteUrl: String?,
    /** 補足メモ。 */
    val memo: String?,
    /** 作成日時。 */
    val createdAt: LocalDateTime?,
    /** 更新日時。 */
    val updatedAt: LocalDateTime?
) {
    companion object {
        /**
         * CompanyエンティティをAPIレスポンス形式へ変換する。
         * 永続化前の不正なエンティティが渡された場合は例外を投げる。
         */
        fun from(company: Company): CompanyResponse {
            return CompanyResponse(
                id = company.id ?: throw IllegalStateException("企業IDが存在しません。"),
                name = company.name,
                industry = company.industry,
                websiteUrl = company.websiteUrl,
                memo = company.memo,
                createdAt = company.createdAt,
                updatedAt = company.updatedAt
            )
        }
    }
}
