package com.hyunwoo.jobselectiontracker.auth.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.Date
import javax.crypto.SecretKey

/**
 * JWT access token の生成、検証、payload 取得を担当するコンポーネント。
 */
@Component
class JwtTokenProvider(
    /** JWT 関連設定を保持するプロパティ。 */
    private val jwtProperties: JwtProperties
) {

    private val secretKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray(StandardCharsets.UTF_8))
    }

    /**
     * 指定したユーザー情報を元に access token を生成する。
     */
    fun generateAccessToken(userId: Long, email: String, name: String): String {
        val now = Instant.now()
        val expiresAt = now.plusSeconds(jwtProperties.accessTokenExpirationSeconds)

        return Jwts.builder()
            .subject(userId.toString())
            .claim("email", email)
            .claim("name", name)
            .claim("type", "access")
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiresAt))
            .signWith(secretKey)
            .compact()
    }

    /**
     * access token の有効期限を秒単位で返す。
     */
    fun getAccessTokenExpirationSeconds(): Long {
        return jwtProperties.accessTokenExpirationSeconds
    }

    /**
     * Authorization ヘッダーで使用するトークン種別を返す。
     */
    fun getTokenType(): String {
        return jwtProperties.tokenType
    }

    /**
     * トークンの署名と有効期限を検証し、有効な場合は true を返す。
     */
    fun validateToken(token: String): Boolean {
        return runCatching {
            parseClaims(token)
            true
        }.getOrDefault(false)
    }

    /**
     * トークンから subject を読み取り、ユーザーIDとして返す。
     */
    fun getUserId(token: String): Long {
        return parseClaims(token).subject.toLong()
    }

    /**
     * トークンからメールアドレス claim を取得する。
     */
    fun getEmail(token: String): String {
        return parseClaims(token).get("email", String::class.java)
    }

    /**
     * トークンから名前 claim を取得する。
     */
    fun getName(token: String): String {
        return parseClaims(token).get("name", String::class.java)
    }

    private fun parseClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload
    }
}
