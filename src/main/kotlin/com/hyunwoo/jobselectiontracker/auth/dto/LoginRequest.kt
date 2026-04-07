package com.hyunwoo.jobselectiontracker.auth.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * ログイン時に受け取る入力DTO。
 */
data class LoginRequest(

    /** ログイン対象ユーザーのメールアドレス。 */
    @field:NotBlank(message = "メールアドレスは必須です。")
    @field:Email(message = "有効なメールアドレスを入力してください。")
    @field:Size(max = 255, message = "メールアドレスは255文字以内で入力してください。")
    val email: String,

    /** 認証時に照合する平文パスワード。 */
    @field:NotBlank(message = "パスワードは必須です。")
    @field:Size(min = 8, max = 100, message = "パスワードは8文字以上100文字以内で入力してください。")
    val password: String
)
