package com.hyunwoo.jobselectiontracker.auth.dto

import com.hyunwoo.jobselectiontracker.user.entity.User

/**
 * 認証成功時に返却するユーザー基本情報レスポンスDTO。
 * JWT 導入後は access token などをここに拡張できる。
 */
data class AuthResponse(

    /** 認証に成功したユーザーID。 */
    val userId: Long,

    /** 認証に成功したユーザーのメールアドレス。 */
    val email: String,

    /** 画面表示用のユーザー名。 */
    val name: String
) {
    companion object {
        /**
         * User エンティティを認証レスポンスDTOに変換する。
         */
        fun from(user: User): AuthResponse {
            return AuthResponse(
                userId = user.id ?: throw IllegalStateException("ユーザーIDが存在しません。"),
                email = user.email,
                name = user.name
            )
        }
    }
}
