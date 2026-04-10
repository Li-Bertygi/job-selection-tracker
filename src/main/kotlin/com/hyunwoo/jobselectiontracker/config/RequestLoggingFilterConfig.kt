package com.hyunwoo.jobselectiontracker.config

import com.hyunwoo.jobselectiontracker.logging.RequestLoggingFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RequestLoggingFilterConfig {

    @Bean
    fun requestLoggingFilter(): RequestLoggingFilter {
        return RequestLoggingFilter()
    }
}
