package com.hyunwoo.jobselectiontracker.stage.entity

/**
 * 各選考ステージの種類を表す列挙型。
 * 応募全体の大分類ではなく、個別ステージの中間レベル分類として利用する。
 */
enum class StageType {
    /** 説明会。 */
    INFO_SESSION,

    /** 書類選考。 */
    DOCUMENT_SCREENING,

    /** コーディングテスト。 */
    CODING_TEST,

    /** 適性検査や性格検査などを含む試験。 */
    APTITUDE_TEST,

    /** 1次面談。 */
    FIRST_CASUAL_MEETING,

    /** 2次面談。 */
    SECOND_CASUAL_MEETING,

    /** 3次面談。 */
    THIRD_CASUAL_MEETING,

    /** 1次面接。 */
    FIRST_INTERVIEW,

    /** 2次面接。 */
    SECOND_INTERVIEW,

    /** 3次面接。 */
    THIRD_INTERVIEW,

    /** 4次面接。 */
    FOURTH_INTERVIEW,

    /** 最終面接。 */
    FINAL_INTERVIEW,

    /** 上記に分類しにくいその他のステージ。 */
    OTHER
}
