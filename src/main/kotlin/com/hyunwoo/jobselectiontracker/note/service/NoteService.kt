package com.hyunwoo.jobselectiontracker.note.service

import com.hyunwoo.jobselectiontracker.application.entity.Application
import com.hyunwoo.jobselectiontracker.application.repository.ApplicationRepository
import com.hyunwoo.jobselectiontracker.note.dto.CreateNoteRequest
import com.hyunwoo.jobselectiontracker.note.dto.NoteResponse
import com.hyunwoo.jobselectiontracker.note.dto.UpdateNoteRequest
import com.hyunwoo.jobselectiontracker.note.entity.Note
import com.hyunwoo.jobselectiontracker.note.repository.NoteRepository
import com.hyunwoo.jobselectiontracker.user.entity.User
import com.hyunwoo.jobselectiontracker.user.repository.UserRepository
import java.util.NoSuchElementException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * メモの作成、取得、更新、削除を担当するサービス。
 * 対象は現在ログイン中ユーザーの応募情報配下に限定する。
 */
@Service
@Transactional(readOnly = true)
class NoteService(
    private val noteRepository: NoteRepository,
    private val applicationRepository: ApplicationRepository,
    private val userRepository: UserRepository
) {

    @Transactional
    fun createNote(applicationId: Long, request: CreateNoteRequest): NoteResponse {
        val currentUser = findCurrentUser()
        val application = findApplicationByIdAndUserId(applicationId, currentUser.id!!)

        val note = Note(
            application = application,
            title = request.title?.trim()?.takeIf { it.isNotEmpty() },
            noteType = request.noteType,
            content = request.content.trim()
        )

        return NoteResponse.from(noteRepository.save(note))
    }

    fun getNotes(applicationId: Long): List<NoteResponse> {
        val currentUser = findCurrentUser()
        findApplicationByIdAndUserId(applicationId, currentUser.id!!)

        return noteRepository.findAllByApplicationIdAndApplicationUserIdOrderByCreatedAtDesc(
            applicationId = applicationId,
            userId = currentUser.id!!
        ).map(NoteResponse::from)
    }

    @Transactional
    fun updateNote(id: Long, request: UpdateNoteRequest): NoteResponse {
        val currentUser = findCurrentUser()
        val note = findNoteByIdAndUserId(id, currentUser.id!!)

        request.title?.let { note.title = it.trim().takeIf(String::isNotEmpty) }
        request.noteType?.let { note.noteType = it }
        request.content?.let { note.content = it.trim() }

        return NoteResponse.from(noteRepository.saveAndFlush(note))
    }

    @Transactional
    fun deleteNote(id: Long) {
        val currentUser = findCurrentUser()
        noteRepository.delete(findNoteByIdAndUserId(id, currentUser.id!!))
    }

    private fun findApplicationByIdAndUserId(id: Long, userId: Long): Application {
        return applicationRepository.findByIdAndUserId(id, userId)
            ?: throw NoSuchElementException("応募情報ID $id に該当する応募情報が見つかりません。")
    }

    private fun findNoteByIdAndUserId(id: Long, userId: Long): Note {
        return noteRepository.findByIdAndApplicationUserId(id, userId)
            ?: throw NoSuchElementException("メモID $id に該当するメモが見つかりません。")
    }

    private fun findCurrentUser(): User {
        val email = SecurityContextHolder.getContext().authentication?.name
            ?: throw IllegalStateException("現在の認証ユーザー情報を取得できません。")

        return userRepository.findByEmail(email)
            ?: throw NoSuchElementException("メールアドレス $email に該当するユーザーが見つかりません。")
    }
}
