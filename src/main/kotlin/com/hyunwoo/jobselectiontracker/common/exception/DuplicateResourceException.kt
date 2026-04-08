package com.hyunwoo.jobselectiontracker.common.exception

class DuplicateResourceException(
    message: String
) : BusinessException(ErrorCode.DUPLICATE_RESOURCE, message)
