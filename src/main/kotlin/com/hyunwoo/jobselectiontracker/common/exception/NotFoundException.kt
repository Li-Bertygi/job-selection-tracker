package com.hyunwoo.jobselectiontracker.common.exception

class NotFoundException(
    message: String
) : BusinessException(ErrorCode.RESOURCE_NOT_FOUND, message)
