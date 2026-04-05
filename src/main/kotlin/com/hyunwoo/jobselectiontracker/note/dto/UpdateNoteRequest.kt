package com.hyunwoo.jobselectiontracker.note.dto

import com.hyunwoo.jobselectiontracker.note.entity.NoteType
import jakarta.validation.constraints.Size

/**
 * メモ更新時にクライアントから受け取るリクエストDTO。
 * PATCHを前提とし、入力された項目のみ更新対象とする。
 */
data class UpdateNoteRequest(

    /** タイトル変更用フィールド。null の場合は既存値を維持する。 */
    @field:Size(max = 100, message = "タイトルは100文字以内で入力してください。")
    val title: String? = null,

    /** メモ分類変更用フィールド。null の場合は既存値を維持する。 */
    val noteType: NoteType? = null,

    /** 本文変更用フィールド。null の場合は既存値を維持する。 */
    @field:Size(min = 1, max = 5000, message = "メモ本文は1文字以上5000文字以内で入力してください。")
    val content: String? = null
)
