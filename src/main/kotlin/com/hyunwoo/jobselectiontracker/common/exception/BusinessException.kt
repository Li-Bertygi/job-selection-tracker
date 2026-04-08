package com.hyunwoo.jobselectiontracker.common.exception

open class BusinessException(
    val errorCode: ErrorCode,
    override val message: String
) : RuntimeException(message)
