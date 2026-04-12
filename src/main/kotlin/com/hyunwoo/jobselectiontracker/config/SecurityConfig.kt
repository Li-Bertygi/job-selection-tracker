package com.hyunwoo.jobselectiontracker.config

import com.hyunwoo.jobselectiontracker.auth.jwt.JwtAuthenticationFilter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

/**
 * JWT ベースの認証構成を定義する Security 設定クラス。
 */
@Configuration
@EnableWebSecurity
class SecurityConfig(
    /** Bearer token を検証して SecurityContext に認証情報を設定するフィルタ。 */
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    @Value("\${app.security.public-actuator-paths:/actuator/health,/actuator/health/**}")
    private val publicActuatorPaths: Array<String>
) {

    /**
     * 認証・認可ルールと JWT フィルタ登録を含む SecurityFilterChain を構成する。
     */
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        val publicPaths = arrayOf(
            "/auth/signup",
            "/auth/login"
        ) + publicActuatorPaths

        http
            .csrf { it.disable() }
            .cors { }
            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .exceptionHandling { exceptions ->
                exceptions
                    .authenticationEntryPoint { _, response, _ ->
                        response.sendError(HttpStatus.UNAUTHORIZED.value())
                    }
                    .accessDeniedHandler { _, response, _ ->
                        response.sendError(HttpStatus.FORBIDDEN.value())
                    }
            }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(*publicPaths).permitAll()
                    .anyRequest().authenticated()
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .formLogin { it.disable() }
            .httpBasic { it.disable() }

        return http.build()
    }
}
