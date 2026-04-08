package com.hyunwoo.jobselectiontracker.common.security

import com.hyunwoo.jobselectiontracker.common.exception.NotFoundException
import com.hyunwoo.jobselectiontracker.common.exception.UnauthorizedException
import com.hyunwoo.jobselectiontracker.user.entity.User
import com.hyunwoo.jobselectiontracker.user.repository.UserRepository
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class CurrentUserProvider(
    private val userRepository: UserRepository
) {

    fun getCurrentUser(): User {
        val email = SecurityContextHolder.getContext().authentication?.name
            ?: throw UnauthorizedException("Authenticated user information is missing.")

        return userRepository.findByEmail(email)
            ?: throw NotFoundException("User with email $email was not found.")
    }
}
