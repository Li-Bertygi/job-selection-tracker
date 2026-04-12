package com.hyunwoo.jobselectiontracker.common.exception

import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.slf4j.LoggerFactory
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime

@RestControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(
        exception: BusinessException
    ): ResponseEntity<ErrorResponse> {
        return buildResponse(
            status = exception.errorCode.status,
            code = exception.errorCode,
            message = exception.message
        )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(
        exception: MethodArgumentNotValidException
    ): ResponseEntity<ErrorResponse> {
        val errors = exception.bindingResult.fieldErrors.associate { fieldError ->
            fieldError.field to (fieldError.defaultMessage ?: "Invalid input.")
        }

        return buildResponse(
            status = HttpStatus.BAD_REQUEST,
            code = ErrorCode.INVALID_REQUEST,
            message = "Request validation failed.",
            errors = errors
        )
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(
        exception: HttpMessageNotReadableException
    ): ResponseEntity<ErrorResponse> {
        return buildResponse(
            status = HttpStatus.BAD_REQUEST,
            code = ErrorCode.INVALID_REQUEST,
            message = exception.mostSpecificCause.message ?: "Malformed request body."
        )
    }

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrityViolationException(
        exception: DataIntegrityViolationException
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Database integrity constraint was violated.", exception)

        return buildResponse(
            status = HttpStatus.CONFLICT,
            code = ErrorCode.DATA_INTEGRITY_VIOLATION,
            message = "A database integrity constraint was violated."
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleException(
        exception: Exception
    ): ResponseEntity<ErrorResponse> {
        return buildResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR,
            code = ErrorCode.INTERNAL_SERVER_ERROR,
            message = exception.message ?: "An unexpected server error occurred."
        )
    }

    private fun buildResponse(
        status: HttpStatus,
        code: ErrorCode,
        message: String,
        errors: Map<String, String>? = null
    ): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(status)
            .body(
                ErrorResponse(
                    status = status.value(),
                    error = status.reasonPhrase,
                    code = code.name,
                    message = message,
                    timestamp = LocalDateTime.now(),
                    errors = errors
                )
            )
    }
}

data class ErrorResponse(
    val status: Int,
    val error: String,
    val code: String,
    val message: String,
    val timestamp: LocalDateTime,
    val errors: Map<String, String>? = null
)
