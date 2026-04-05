package com.hyunwoo.jobselectiontracker.note.service

import com.hyunwoo.jobselectiontracker.application.entity.Application
import com.hyunwoo.jobselectiontracker.application.repository.ApplicationRepository
import com.hyunwoo.jobselectiontracker.note.dto.CreateNoteRequest
import com.hyunwoo.jobselectiontracker.note.dto.NoteResponse
import com.hyunwoo.jobselectiontracker.note.dto.UpdateNoteRequest
import com.hyunwoo.jobselectiontracker.note.entity.Note
import com.hyunwoo.jobselectiontracker.note.repository.NoteRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.NoSuchElementException

/**
 * メモドメインのビジネスロジックを担当するサービス。
 * 応募情報単位のメモ登録、取得、更新、削除を管理する。
 */
@Service
@Transactional(readOnly = true)
class NoteService(
    /** メモエンティティの保存と検索を担当するリポジトリ。 */
    private val noteRepository: NoteRepository,
    /** 応募情報の存在確認に使用するリポジトリ。 */
    private val applicationRepository: ApplicationRepository
) {

    /** 指定した応募情報に新しいメモを登録する。 */
    @Transactional
    fun createNote(applicationId: Long, request: CreateNoteRequest): NoteResponse {
        val application = findApplicationById(applicationId)

        val note = Note(
            application = application,
            title = request.title?.trim()?.takeIf { it.isNotEmpty() },
            noteType = request.noteType,
            content = request.content.trim()
        )

        return NoteResponse.from(noteRepository.save(note))
    }

    /** 指定した応募情報に属するメモ一覧を新しい順で取得する。 */
    fun getNotes(applicationId: Long): List<NoteResponse> {
        findApplicationById(applicationId)

        return noteRepository.findAllByApplicationIdOrderByCreatedAtDesc(applicationId)
            .map(NoteResponse::from)
    }

    /** 指定したメモを部分更新する。 */
    @Transactional
    fun updateNote(id: Long, request: UpdateNoteRequest): NoteResponse {
        val note = findNoteById(id)

        request.title?.let { note.title = it.trim().takeIf(String::isNotEmpty) }
        request.noteType?.let { note.noteType = it }
        request.content?.let { note.content = it.trim() }

        return NoteResponse.from(noteRepository.saveAndFlush(note))
    }

    /** 指定したメモを削除する。 */
    @Transactional
    fun deleteNote(id: Long) {
        val note = findNoteById(id)
        noteRepository.delete(note)
    }

    /** 応募情報IDで応募情報を取得し、存在しなければ404対象の例外を投げる。 */
    private fun findApplicationById(id: Long): Application {
        return applicationRepository.findById(id)
            .orElseThrow {
                NoSuchElementException("応募情報ID $id に該当する応募情報が見つかりません。")
            }
    }

    /** メモIDでメモを取得し、存在しなければ404対象の例外を投げる。 */
    private fun findNoteById(id: Long): Note {
        return noteRepository.findById(id)
            .orElseThrow {
                NoSuchElementException("メモID $id に該当するメモが見つかりません。")
            }
    }
}
