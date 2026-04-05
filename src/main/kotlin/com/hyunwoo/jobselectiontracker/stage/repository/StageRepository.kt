package com.hyunwoo.jobselectiontracker.stage.repository

import com.hyunwoo.jobselectiontracker.stage.entity.Stage
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Stageエンティティに対するDBアクセスを担当するリポジトリ。
 * ステージ一覧取得や同一応募内の並び順重複確認に利用する。
 */
interface StageRepository : JpaRepository<Stage, Long> {

    /** 指定した応募情報に紐づくステージ一覧を表示順で取得する。 */
    fun findAllByApplicationIdOrderByStageOrderAsc(applicationId: Long): List<Stage>

    /** 同一応募内で指定した表示順がすでに使われているか確認する。 */
    fun existsByApplicationIdAndStageOrder(applicationId: Long, stageOrder: Int): Boolean
}
