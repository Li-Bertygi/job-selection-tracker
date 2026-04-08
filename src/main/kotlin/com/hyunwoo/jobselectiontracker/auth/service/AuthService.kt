package com.hyunwoo.jobselectiontracker.auth.service

import com.hyunwoo.jobselectiontracker.auth.dto.AuthResponse
import com.hyunwoo.jobselectiontracker.auth.dto.LoginRequest
import com.hyunwoo.jobselectiontracker.auth.dto.SignupRequest
import com.hyunwoo.jobselectiontracker.auth.jwt.JwtTokenProvider
import com.hyunwoo.jobselectiontracker.auth.security.AuthenticatedUser
import com.hyunwoo.jobselectiontracker.common.exception.DuplicateResourceException
import com.hyunwoo.jobselectiontracker.common.exception.InvalidRequestException
import com.hyunwoo.jobselectiontracker.common.exception.NotFoundException
import com.hyunwoo.jobselectiontracker.user.entity.User
import com.hyunwoo.jobselectiontracker.user.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class AuthService(
    private val userRepository: UserRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val passwordEncoder: PasswordEncoder
) {

    @Transactional
    fun signup(request: SignupRequest): AuthResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw DuplicateResourceException("Email ${request.email} is already registered.")
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

    fun login(request: LoginRequest): AuthResponse {
        val user = userRepository.findByEmail(request.email.trim())
            ?: throw InvalidRequestException("Invalid email or password.")

        if (!passwordEncoder.matches(request.password, user.password)) {
            throw InvalidRequestException("Invalid email or password.")
        }

        val accessToken = jwtTokenProvider.generateAccessToken(
            userId = user.id ?: throw IllegalStateException("User id is missing."),
            email = user.email,
            name = user.name
        )

        return AuthResponse.from(
            user = user,
            accessToken = accessToken,
            tokenType = jwtTokenProvider.getTokenType(),
            expiresIn = jwtTokenProvider.getAccessTokenExpirationSeconds()
        )
    }

    fun getCurrentUser(authenticatedUser: AuthenticatedUser): AuthResponse {
        val user = userRepository.findById(authenticatedUser.userId)
            .orElseThrow { NotFoundException("User ${authenticatedUser.userId} was not found.") }

        return AuthResponse.from(user)
    }
}
