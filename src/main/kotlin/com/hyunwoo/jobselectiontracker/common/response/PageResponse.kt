package com.hyunwoo.jobselectiontracker.common.response

import org.springframework.data.domain.Page

/**
 * ページングされた一覧APIの共通レスポンス形式。
 * Spring Data の Page をそのまま公開せず、API利用者に必要な情報だけを返す。
 */
data class PageResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val first: Boolean,
    val last: Boolean
) {
    companion object {
        fun <T> from(page: Page<T>): PageResponse<T> {
            return PageResponse(
                content = page.content,
                page = page.number,
                size = page.size,
                totalElements = page.totalElements,
                totalPages = page.totalPages,
                first = page.isFirst,
                last = page.isLast
            )
        }
    }
}
