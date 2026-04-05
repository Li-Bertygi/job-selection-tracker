package com.hyunwoo.jobselectiontracker.note.repository

import com.hyunwoo.jobselectiontracker.note.entity.Note
import org.springframework.data.jpa.repository.JpaRepository

/**
 * NoteエンティティのDBアクセスを担当するリポジトリ。
 * 応募情報単位のメモ一覧取得に使用する。
 */
interface NoteRepository : JpaRepository<Note, Long> {

    /** 指定した応募情報に属するメモを作成日時の降順で取得する。 */
    fun findAllByApplicationIdOrderByCreatedAtDesc(applicationId: Long): List<Note>
}
