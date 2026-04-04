package com.hyunwoo.jobselectiontracker.company.dto

import jakarta.validation.constraints.Size

/**
 * 企業更新APIで受け取るリクエストDTO。
 * PATCHを想定しているため、各フィールドは未指定を表現できるよう nullable にしている。
 */
data class UpdateCompanyRequest(

    /** 更新後の企業名。値が渡された場合のみ変更対象になる。 */
    @field:Size(min = 1, max = 255, message = "企業名は1文字以上255文字以内で入力してください。")
    val name: String? = null,

    /** 更新後の業種。 */
    @field:Size(max = 100, message = "業種は100文字以内で入力してください。")
    val industry: String? = null,

    /** 更新後のWebサイトURL。 */
    @field:Size(max = 500, message = "WebサイトURLは500文字以内で入力してください。")
    val websiteUrl: String? = null,

    /** 更新後のメモ。 */
    val memo: String? = null
)
