package com.hyunwoo.jobselectiontracker.auth.dto

import com.hyunwoo.jobselectiontracker.user.entity.User

/**
 * 認証成功時に返却するユーザー基本情報レスポンスDTO。
 * 1次実装では access token 情報まで含めて返却する。
 */
data class AuthResponse(

    /** 認証に成功したユーザーID。 */
    val userId: Long,

    /** 認証に成功したユーザーのメールアドレス。 */
    val email: String,

    /** 画面表示用のユーザー名。 */
    val name: String,

    /** API 認証に使用する JWT access token。 */
    val accessToken: String? = null,

    /** Authorization ヘッダーで使用するトークン種別。 */
    val tokenType: String? = null,

    /** access token の有効期限を秒単位で表現した値。 */
    val expiresIn: Long? = null
) {
    companion object {
        /**
         * User エンティティを認証レスポンスDTOに変換する。
         */
        fun from(
            user: User,
            accessToken: String? = null,
            tokenType: String? = null,
            expiresIn: Long? = null
        ): AuthResponse {
            return AuthResponse(
                userId = user.id ?: throw IllegalStateException("ユーザーIDが存在しません。"),
                email = user.email,
                name = user.name,
                accessToken = accessToken,
                tokenType = tokenType,
                expiresIn = expiresIn
            )
        }
    }
}
