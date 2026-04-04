package com.hyunwoo.jobselectiontracker.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain

/**
 * 開発初期段階向けのセキュリティ設定。
 * 認証機能を実装する前にCRUD検証を進められるよう、全リクエストを一時的に許可している。
 */
@Configuration
@EnableWebSecurity
class SecurityConfig {

    /**
     * Spring Securityのフィルタチェーンを定義する。
     * テスト用でCSRFとデフォルトログインを無効化し、ステートレスなAPI構成にしている。
     * 今後修正する予定
     */
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .authorizeHttpRequests { auth ->
                auth.anyRequest().permitAll()
            }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }

        return http.build()
    }
}
