package com.hyunwoo.jobselectiontracker.auth.controller

import com.hyunwoo.jobselectiontracker.auth.dto.AuthResponse
import com.hyunwoo.jobselectiontracker.auth.dto.LoginRequest
import com.hyunwoo.jobselectiontracker.auth.dto.SignupRequest
import com.hyunwoo.jobselectiontracker.auth.security.AuthenticatedUser
import com.hyunwoo.jobselectiontracker.auth.service.AuthService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

/**
 * ユーザー新規登録、ログイン、現在ユーザー取得を提供する認証コントローラ。
 */
@RestController
@RequestMapping("/auth")
class AuthController(
    /** 認証関連ユースケースを担当するサービス。 */
    private val authService: AuthService
) {

    /**
     * 入力値を検証して新規ユーザーを登録する。
     */
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    fun signup(
        @Valid @RequestBody request: SignupRequest
    ): AuthResponse {
        return authService.signup(request)
    }

    /**
     * メールアドレスとパスワードを照合してログインを処理する。
     */
    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequest
    ): AuthResponse {
        return authService.login(request)
    }

    /**
     * 現在認証されているユーザー情報を返す。
     */
    @GetMapping("/me")
    fun me(
        @AuthenticationPrincipal authenticatedUser: AuthenticatedUser
    ): AuthResponse {
        return authService.getCurrentUser(authenticatedUser)
    }
}
