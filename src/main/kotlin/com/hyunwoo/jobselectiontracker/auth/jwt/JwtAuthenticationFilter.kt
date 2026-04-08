package com.hyunwoo.jobselectiontracker.auth.jwt

import com.hyunwoo.jobselectiontracker.auth.security.AuthenticatedUser
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Authorization ヘッダーの Bearer token を検証し、SecurityContext に認証情報を設定するフィルタ。
 */
@Component
class JwtAuthenticationFilter(
    /** JWT access token の検証と claim 抽出を担当するプロバイダ。 */
    private val jwtTokenProvider: JwtTokenProvider
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = resolveToken(request)

        if (token != null &&
            jwtTokenProvider.validateToken(token) &&
            SecurityContextHolder.getContext().authentication == null
        ) {
            val principal = AuthenticatedUser(
                userId = jwtTokenProvider.getUserId(token),
                email = jwtTokenProvider.getEmail(token),
                name = jwtTokenProvider.getName(token)
            )

            val authentication = UsernamePasswordAuthenticationToken(
                principal,
                null,
                emptyList()
            ).apply {
                details = WebAuthenticationDetailsSource().buildDetails(request)
            }

            SecurityContextHolder.getContext().authentication = authentication
        }

        filterChain.doFilter(request, response)
    }

    /**
     * Authorization ヘッダーから Bearer token 本体のみを取り出す。
     */
    private fun resolveToken(request: HttpServletRequest): String? {
        val authorizationHeader = request.getHeader("Authorization")

        if (authorizationHeader.isNullOrBlank()) {
            return null
        }

        val prefix = "${jwtTokenProvider.getTokenType()} "
        if (!authorizationHeader.startsWith(prefix)) {
            return null
        }

        return authorizationHeader.removePrefix(prefix).trim().ifBlank { null }
    }
}
