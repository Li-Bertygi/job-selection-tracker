package com.hyunwoo.jobselectiontracker.logging

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

class RequestLoggingFilter : OncePerRequestFilter() {

    private val requestLogger = LoggerFactory.getLogger(javaClass)

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        return request.requestURI.startsWith("/actuator/prometheus")
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val requestId = request.getHeader(REQUEST_ID_HEADER)?.takeIf { it.isNotBlank() }
            ?: UUID.randomUUID().toString()
        val startTime = System.currentTimeMillis()

        MDC.put(MDC_REQUEST_ID_KEY, requestId)
        response.setHeader(REQUEST_ID_HEADER, requestId)

        try {
            filterChain.doFilter(request, response)
        } finally {
            val durationMs = System.currentTimeMillis() - startTime
            requestLogger.info(
                "{} {} -> {} ({} ms)",
                request.method,
                request.requestURI,
                response.status,
                durationMs
            )
            MDC.remove(MDC_REQUEST_ID_KEY)
        }
    }

    companion object {
        private const val REQUEST_ID_HEADER = "X-Request-Id"
        private const val MDC_REQUEST_ID_KEY = "requestId"
    }
}
