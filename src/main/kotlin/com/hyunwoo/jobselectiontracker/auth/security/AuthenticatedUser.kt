package com.hyunwoo.jobselectiontracker.auth.security

import java.security.Principal

/**
 * JWT 認証後に SecurityContext へ格納する認証済みユーザー情報。
 */
data class AuthenticatedUser(

    /** 認証済みユーザーID。 */
    val userId: Long,

    /** 認証済みユーザーのメールアドレス。 */
    val email: String,

    /** 認証済みユーザーの表示名。 */
    val displayName: String
) : Principal {

    /**
     * SecurityContext から共通的に参照できる識別子としてメールアドレスを返す。
     */
    override fun getName(): String {
        return email
    }
}
