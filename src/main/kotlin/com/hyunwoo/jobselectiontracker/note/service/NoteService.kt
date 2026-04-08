package com.hyunwoo.jobselectiontracker.note.service

import com.hyunwoo.jobselectiontracker.application.entity.Application
import com.hyunwoo.jobselectiontracker.common.security.CurrentUserProvider
import com.hyunwoo.jobselectiontracker.common.security.OwnedResourceFinder
import com.hyunwoo.jobselectiontracker.note.dto.CreateNoteRequest
import com.hyunwoo.jobselectiontracker.note.dto.NoteResponse
import com.hyunwoo.jobselectiontracker.note.dto.UpdateNoteRequest
import com.hyunwoo.jobselectiontracker.note.entity.Note
import com.hyunwoo.jobselectiontracker.note.repository.NoteRepository
import com.hyunwoo.jobselectiontracker.user.entity.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class NoteService(
    private val noteRepository: NoteRepository,
    private val currentUserProvider: CurrentUserProvider,
    private val ownedResourceFinder: OwnedResourceFinder
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
        return ownedResourceFinder.findApplication(id, userId)
    }

    private fun findNoteByIdAndUserId(id: Long, userId: Long): Note {
        return ownedResourceFinder.findNote(id, userId)
    }

    private fun findCurrentUser(): User {
        return currentUserProvider.getCurrentUser()
    }
}
