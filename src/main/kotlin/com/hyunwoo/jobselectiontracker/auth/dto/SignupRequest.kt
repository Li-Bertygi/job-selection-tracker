package com.hyunwoo.jobselectiontracker.auth.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * ユーザー新規登録時に受け取る入力DTO。
 */
data class SignupRequest(

    /** ログインIDとして使用するメールアドレス。 */
    @field:NotBlank(message = "メールアドレスは必須です。")
    @field:Email(message = "有効なメールアドレスを入力してください。")
    @field:Size(max = 255, message = "メールアドレスは255文字以内で入力してください。")
    val email: String,

    /** BCrypt で暗号化して保存する元のパスワード。 */
    @field:NotBlank(message = "パスワードは必須です。")
    @field:Size(min = 8, max = 100, message = "パスワードは8文字以上100文字以内で入力してください。")
    val password: String,

    /** 画面表示やプロフィール確認に使用する名前。 */
    @field:NotBlank(message = "名前は必須です。")
    @field:Size(min = 1, max = 100, message = "名前は1文字以上100文字以内で入力してください。")
    val name: String
)
