package com.hyunwoo.jobselectiontracker.note.entity

import com.hyunwoo.jobselectiontracker.application.entity.Application
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import java.time.LocalDateTime

/**
 * 応募情報に紐づく自由記述メモを表すエンティティ。
 * タイトル、分類、本文を保持し、時刻情報は自動で更新する。
 */
@Entity
@Table(name = "notes")
class Note(

    /** メモの主キー。 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    /** このメモが属する応募情報。 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    var application: Application,

    /** メモ一覧で表示する短いタイトル。未入力の場合は null を許容する。 */
    @Column(length = 100)
    var title: String? = null,

    /** メモの分類。未分類の場合は UNSPECIFIED を使用する。 */
    @Enumerated(EnumType.STRING)
    @Column(name = "note_type", nullable = false, length = 50)
    var noteType: NoteType = NoteType.UNSPECIFIED,

    /** メモの本文。自由入力のため TEXT で管理する。 */
    @Column(nullable = false, columnDefinition = "TEXT")
    var content: String,

    /** メモの作成日時。初回保存時に自動設定する。 */
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null,

    /** メモの更新日時。更新時に自動で書き換える。 */
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime? = null
) {

    /** 保存前に作成日時と更新日時を同じ値で初期化する。 */
    @PrePersist
    fun prePersist() {
        val now = LocalDateTime.now()
        createdAt = now
        updatedAt = now
    }

    /** 更新前に更新日時のみ最新時刻へ変更する。 */
    @PreUpdate
    fun preUpdate() {
        updatedAt = LocalDateTime.now()
    }
}
