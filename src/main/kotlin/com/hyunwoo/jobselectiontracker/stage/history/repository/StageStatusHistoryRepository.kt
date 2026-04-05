package com.hyunwoo.jobselectiontracker.stage.history.repository

import com.hyunwoo.jobselectiontracker.stage.history.entity.StageStatusHistory
import org.springframework.data.jpa.repository.JpaRepository

/**
 * 各選考ステージの状態変更履歴を扱うリポジトリ。
 */
interface StageStatusHistoryRepository : JpaRepository<StageStatusHistory, Long> {

    /** 指定したステージに属する状態変更履歴を新しい順で取得する。 */
    fun findAllByStageIdOrderByChangedAtDesc(stageId: Long): List<StageStatusHistory>
}
