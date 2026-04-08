package com.hyunwoo.jobselectiontracker.stage.repository

import com.hyunwoo.jobselectiontracker.stage.entity.Stage
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Stage エンティティの永続化を担当するリポジトリ。
 * 応募情報および所有ユーザーを基準にステージを検索する。
 */
interface StageRepository : JpaRepository<Stage, Long> {

    /**
     * 指定した応募情報かつ指定したユーザーに属するステージ一覧を順序昇順で取得する。
     */
    fun findAllByApplicationIdAndApplicationUserIdOrderByStageOrderAsc(
        applicationId: Long,
        userId: Long
    ): List<Stage>

    /**
     * 指定したステージIDかつ指定したユーザーに属するステージを取得する。
     */
    fun findByIdAndApplicationUserId(id: Long, userId: Long): Stage?

    /**
     * 同一応募情報内で stageOrder がすでに使用されているかを確認する。
     */
    fun existsByApplicationIdAndStageOrder(applicationId: Long, stageOrder: Int): Boolean
}
