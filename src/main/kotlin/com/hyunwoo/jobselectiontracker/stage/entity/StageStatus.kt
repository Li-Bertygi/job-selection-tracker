package com.hyunwoo.jobselectiontracker.stage.entity

/**
 * 各選考ステージの進捗状態を表す列挙型。
 * 応募全体の大きな進捗ではなく、個別ステージ単位の詳細な状態管理に使用する。
 */
enum class StageStatus {
    /** まだ開始していない待機状態。 */
    PENDING,

    /** 日程や実施予定が確定している状態。 */
    SCHEDULED,

    /** ステージの実施が完了した状態。 */
    COMPLETED,

    /** ステージを通過した状態。 */
    PASSED,

    /** ステージで不合格となった状態。 */
    FAILED
}
