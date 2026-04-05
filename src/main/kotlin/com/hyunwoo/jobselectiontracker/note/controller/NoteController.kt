package com.hyunwoo.jobselectiontracker.note.controller

import com.hyunwoo.jobselectiontracker.note.dto.CreateNoteRequest
import com.hyunwoo.jobselectiontracker.note.dto.NoteResponse
import com.hyunwoo.jobselectiontracker.note.dto.UpdateNoteRequest
import com.hyunwoo.jobselectiontracker.note.service.NoteService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

/**
 * メモ関連APIのエンドポイントを提供するコントローラ。
 * 応募情報配下の登録・一覧取得と、単一メモの更新・削除を担当する。
 */
@RestController
@RequestMapping
class NoteController(
    /** メモの業務処理を委譲するサービス。 */
    private val noteService: NoteService
) {

    /** 指定した応募情報に新しいメモを登録する。 */
    @PostMapping("/applications/{applicationId}/notes")
    @ResponseStatus(HttpStatus.CREATED)
    fun createNote(
        @PathVariable applicationId: Long,
        @Valid @RequestBody request: CreateNoteRequest
    ): NoteResponse {
        return noteService.createNote(applicationId, request)
    }

    /** 指定した応募情報に属するメモ一覧を取得する。 */
    @GetMapping("/applications/{applicationId}/notes")
    fun getNotes(
        @PathVariable applicationId: Long
    ): List<NoteResponse> {
        return noteService.getNotes(applicationId)
    }

    /** 指定したメモを部分更新する。 */
    @PatchMapping("/notes/{id}")
    fun updateNote(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateNoteRequest
    ): NoteResponse {
        return noteService.updateNote(id, request)
    }

    /** 指定したメモを削除する。 */
    @DeleteMapping("/notes/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteNote(
        @PathVariable id: Long
    ) {
        noteService.deleteNote(id)
    }
}
