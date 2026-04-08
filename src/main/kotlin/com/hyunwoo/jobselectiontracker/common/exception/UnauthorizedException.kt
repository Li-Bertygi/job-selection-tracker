package com.hyunwoo.jobselectiontracker.common.exception

class UnauthorizedException(
    message: String
) : BusinessException(ErrorCode.UNAUTHORIZED, message)
