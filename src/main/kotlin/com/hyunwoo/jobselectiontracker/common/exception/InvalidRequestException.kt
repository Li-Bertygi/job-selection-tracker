package com.hyunwoo.jobselectiontracker.common.exception

class InvalidRequestException(
    message: String
) : BusinessException(ErrorCode.INVALID_REQUEST, message)
