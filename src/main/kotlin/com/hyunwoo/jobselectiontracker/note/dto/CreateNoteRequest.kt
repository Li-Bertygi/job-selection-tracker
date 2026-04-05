package com.hyunwoo.jobselectiontracker.note.dto

import com.hyunwoo.jobselectiontracker.note.entity.NoteType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * メモ作成時にクライアントから受け取るリクエストDTO。
 * タイトルは任意、分類は未指定をデフォルトとする。
 */
data class CreateNoteRequest(

    /** 一覧表示用の短いタイトル。未入力の場合は null を許容する。 */
    @field:Size(max = 100, message = "タイトルは100文字以内で入力してください。")
    val title: String? = null,

    /** メモ分類。未指定の場合は UNSPECIFIED を使用する。 */
    val noteType: NoteType = NoteType.UNSPECIFIED,

    /** メモ本文。空文字は許可しない。 */
    @field:NotBlank(message = "メモ本文は必須です。")
    @field:Size(max = 5000, message = "メモ本文は5000文字以内で入力してください。")
    val content: String
)
