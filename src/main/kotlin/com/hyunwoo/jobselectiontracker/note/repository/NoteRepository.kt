package com.hyunwoo.jobselectiontracker.note.repository

import com.hyunwoo.jobselectiontracker.note.entity.Note
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Note エンティティの永続化を担当するリポジトリ。
 * 応募情報と所有ユーザーを基準にメモを取得する。
 */
interface NoteRepository : JpaRepository<Note, Long> {

    /**
     * 指定した応募情報かつ指定したユーザーに属するメモを作成日時降順で取得する。
     */
    fun findAllByApplicationIdAndApplicationUserIdOrderByCreatedAtDesc(
        applicationId: Long,
        userId: Long
    ): List<Note>

    /**
     * 指定したメモIDかつ指定したユーザーに属するメモを取得する。
     */
    fun findByIdAndApplicationUserId(id: Long, userId: Long): Note?
}
