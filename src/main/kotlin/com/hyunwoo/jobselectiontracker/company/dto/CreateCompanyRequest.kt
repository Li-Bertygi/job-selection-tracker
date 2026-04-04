package com.hyunwoo.jobselectiontracker.company.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * 企業新規登録APIで受け取るリクエストDTO。
 * クライアント入力のバリデーションもこのクラスで行う。
 */
data class CreateCompanyRequest(

    /** 登録対象となる企業名。空文字は許可しない。 */
    @field:NotBlank(message = "企業名は必須です。")
    @field:Size(max = 255, message = "企業名は255文字以内で入力してください。")
    val name: String,

    /** 業種。ERDに合わせて100文字以内に制限する。 */
    @field:Size(max = 100, message = "業種は100文字以内で入力してください。")
    val industry: String? = null,

    /** 企業の公式サイトURL。ERDに合わせて500文字以内に制限する。 */
    @field:Size(max = 500, message = "WebサイトURLは500文字以内で入力してください。")
    val websiteUrl: String? = null,

    /** 企業に対する自由記述のメモ。 */
    val memo: String? = null
)
