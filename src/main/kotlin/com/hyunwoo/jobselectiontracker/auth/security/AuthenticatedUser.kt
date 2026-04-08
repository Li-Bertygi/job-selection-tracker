package com.hyunwoo.jobselectiontracker.auth.security

/**
 * JWT 認証後に SecurityContext へ格納する認証済みユーザー情報。
 */
data class AuthenticatedUser(

    /** 認証済みユーザーID。 */
    val userId: Long,

    /** 認証済みユーザーのメールアドレス。 */
    val email: String,

    /** 認証済みユーザーの表示名。 */
    val name: String
)
