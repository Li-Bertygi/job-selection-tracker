package com.hyunwoo.jobselectiontracker.note.dto

import com.hyunwoo.jobselectiontracker.note.entity.Note
import com.hyunwoo.jobselectiontracker.note.entity.NoteType
import java.time.LocalDateTime

/**
 * メモAPIのレスポンスとして返却するDTO。
 * エンティティをそのまま公開せず、必要な項目だけを返す。
 */
data class NoteResponse(

    /** メモID。 */
    val id: Long,

    /** このメモが属する応募情報ID。 */
    val applicationId: Long,

    /** 一覧表示用タイトル。未入力時は null のまま返す。 */
    val title: String?,

    /** メモ分類。 */
    val noteType: NoteType,

    /** メモ本文。 */
    val content: String,

    /** 作成日時。 */
    val createdAt: LocalDateTime?,

    /** 更新日時。 */
    val updatedAt: LocalDateTime?
) {
    companion object {
        /** NoteエンティティをAPIレスポンス用DTOに変換する。 */
        fun from(note: Note): NoteResponse {
            return NoteResponse(
                id = note.id ?: throw IllegalStateException("メモIDが存在しません。"),
                applicationId = note.application.id
                    ?: throw IllegalStateException("応募情報IDが存在しません。"),
                title = note.title,
                noteType = note.noteType,
                content = note.content,
                createdAt = note.createdAt,
                updatedAt = note.updatedAt
            )
        }
    }
}
