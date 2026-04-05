package com.hyunwoo.jobselectiontracker.note.entity

/**
 * メモの用途を分類するための種別。
 * 入力時に明確な分類がない場合は UNSPECIFIED を使用する。
 */
enum class NoteType {
    /** 分類未設定のメモ。 */
    UNSPECIFIED,

    /** 面接準備、企業研究、想定質問整理などの事前準備メモ。 */
    PREPARATION,

    /** 面接や選考で実際に出た内容、当日の事実記録用メモ。 */
    ACTUAL_CONTENT,

    /** 選考後の振り返り、改善点、所感を残すメモ。 */
    REVIEW,

    /** 上記に当てはまらない自由用途のメモ。 */
    OTHER
}
