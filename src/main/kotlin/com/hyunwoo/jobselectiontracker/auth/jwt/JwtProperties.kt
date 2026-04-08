package com.hyunwoo.jobselectiontracker.auth.jwt

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

/**
 * JWT 発行と検証に使用する設定値を保持するプロパティクラス。
 */
@Component
@ConfigurationProperties(prefix = "jwt")
class JwtProperties {

    /** 署名キー生成に使用するシークレット文字列。 */
    lateinit var secret: String

    /** access token の有効期限を秒単位で保持する。 */
    var accessTokenExpirationSeconds: Long = 3600

    /** Authorization ヘッダーで使用するトークン種別。 */
    var tokenType: String = "Bearer"
}
