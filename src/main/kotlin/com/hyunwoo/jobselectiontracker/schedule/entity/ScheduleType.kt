package com.hyunwoo.jobselectiontracker.schedule.entity

/**
 * スケジュールの性格を表す列挙型。
 * 選考の種類ではなく、締切・実施日・結果発表日といった予定の意味を分類するために使用する。
 */
enum class ScheduleType {
    /** 提出締切や回答期限などの期限系予定。 */
    DEADLINE,

    /** 面接、面談、説明会、試験実施日などのイベント系予定。 */
    EVENT,

    /** 合否や結果通知の予定。 */
    RESULT_ANNOUNCEMENT,

    /** 上記に分類しにくいその他の予定。 */
    OTHER
}
