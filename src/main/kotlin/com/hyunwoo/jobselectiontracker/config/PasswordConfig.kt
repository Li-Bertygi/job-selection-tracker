package com.hyunwoo.jobselectiontracker.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

/**
 * 認証で使用するパスワードエンコーダを提供する設定クラス。
 */
@Configuration
class PasswordConfig {

    /**
     * BCrypt ベースのパスワードエンコーダを Bean として登録する。
     */
    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}
