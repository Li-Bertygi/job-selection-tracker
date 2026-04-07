package com.hyunwoo.jobselectiontracker.auth.service

import com.hyunwoo.jobselectiontracker.auth.dto.AuthResponse
import com.hyunwoo.jobselectiontracker.auth.dto.LoginRequest
import com.hyunwoo.jobselectiontracker.auth.dto.SignupRequest
import com.hyunwoo.jobselectiontracker.user.entity.User
import com.hyunwoo.jobselectiontracker.user.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * ユーザー新規登録とログイン認証を担当するサービス。
 */
@Service
@Transactional(readOnly = true)
class AuthService(
    /** ユーザー永続化とメールアドレス検索を担当するリポジトリ。 */
    private val userRepository: UserRepository,

    /** パスワードの暗号化と照合を担当するエンコーダ。 */
    private val passwordEncoder: PasswordEncoder
) {

    /**
     * 入力値を検証し、新しいユーザーを保存して基本認証レスポンスを返す。
     */
    @Transactional
    fun signup(request: SignupRequest): AuthResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("メールアドレス ${request.email} はすでに使用されています。")
        }

        val user = userRepository.save(
            User(
                email = request.email.trim(),
                password = passwordEncoder.encode(request.password),
                name = request.name.trim()
            )
        )

        return AuthResponse.from(user)
    }

    /**
     * メールアドレスとパスワードを照合し、認証に成功したユーザー情報を返す。
     */
    fun login(request: LoginRequest): AuthResponse {
        val user = userRepository.findByEmail(request.email.trim())
            ?: throw IllegalArgumentException("メールアドレスまたはパスワードが正しくありません。")

        if (!passwordEncoder.matches(request.password, user.password)) {
            throw IllegalArgumentException("メールアドレスまたはパスワードが正しくありません。")
        }

        return AuthResponse.from(user)
    }
}
