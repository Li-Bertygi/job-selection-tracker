package com.hyunwoo.jobselectiontracker.common.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime
import java.util.NoSuchElementException

/**
 * アプリケーション全体で発生した例外を共通形式のJSONレスポンスへ変換する。
 * Controllerごとに重複した例外処理を書かないための共通ハンドラ。
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    /** データ未存在時の例外を404 Not Foundへ変換する。 */
    @ExceptionHandler(NoSuchElementException::class)
    fun handleNoSuchElementException(
        exception: NoSuchElementException
    ): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(
                ErrorResponse(
                    status = HttpStatus.NOT_FOUND.value(),
                    error = HttpStatus.NOT_FOUND.reasonPhrase,
                    message = exception.message ?: "対象データが見つかりません。",
                    timestamp = LocalDateTime.now()
                )
            )
    }

    /** バリデーション失敗時の例外を400 Bad Requestへ変換する。 */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(
        exception: MethodArgumentNotValidException
    ): ResponseEntity<ValidationErrorResponse> {
        val errors = exception.bindingResult.fieldErrors.associate { fieldError ->
            fieldError.field to (fieldError.defaultMessage ?: "入力値が不正です。")
        }

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ValidationErrorResponse(
                    status = HttpStatus.BAD_REQUEST.value(),
                    error = HttpStatus.BAD_REQUEST.reasonPhrase,
                    message = "入力値を確認してください。",
                    timestamp = LocalDateTime.now(),
                    errors = errors
                )
            )
    }

    /** JSON構造不正や必須値欠落によるリクエスト読み取り失敗を400へ変換する。 */
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(
        exception: HttpMessageNotReadableException
    ): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse(
                    status = HttpStatus.BAD_REQUEST.value(),
                    error = HttpStatus.BAD_REQUEST.reasonPhrase,
                    message = exception.mostSpecificCause?.message ?: "リクエスト本文を正しく読み取れませんでした。",
                    timestamp = LocalDateTime.now()
                )
            )
    }

    /** 想定外の例外を500 Internal Server Errorへ変換する。 */
    @ExceptionHandler(Exception::class)
    fun handleException(
        exception: Exception
    ): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(
                ErrorResponse(
                    status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    error = HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase,
                    message = exception.message ?: "サーバー内部でエラーが発生しました。",
                    timestamp = LocalDateTime.now()
                )
            )
    }
}

/** 単一のエラー情報を返すための共通レスポンス。 */
data class ErrorResponse(
    val status: Int,
    val error: String,
    val message: String,
    val timestamp: LocalDateTime
)

/** バリデーションエラーの詳細一覧を含むレスポンス。 */
data class ValidationErrorResponse(
    val status: Int,
    val error: String,
    val message: String,
    val timestamp: LocalDateTime,
    val errors: Map<String, String>
)
