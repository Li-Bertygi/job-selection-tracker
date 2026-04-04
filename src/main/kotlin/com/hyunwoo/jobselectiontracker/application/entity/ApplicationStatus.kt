package com.hyunwoo.jobselectiontracker.application.entity

/**
 * 応募全体の進捗状態を表す列挙型。
 * 個別の面接回や細かい通過可否ではなく、応募全体の大きなカテゴリを管理する。
 */
enum class ApplicationStatus {
    /** まだ応募活動を開始していない状態。 */
    NOT_STARTED,

    /** 応募段階。 */
    APPLICATION,

    /** 説明会段階。 */
    INFO_SESSION,

    /** 書類選考段階。 */
    DOCUMENT_SCREENING,

    /** Webテスト段階。 */
    WEB_TEST,

    /** 面談段階。 */
    CASUAL_MEETING,

    /** 面接段階。 */
    INTERVIEW,

    /** 最終合格または内定に至った状態。 */
    OFFERED,

    /** 不合格で終了した状態。 */
    REJECTED
}
