package com.hyunwoo.jobselectiontracker.application.repository

import com.hyunwoo.jobselectiontracker.application.entity.Application
import org.springframework.data.jpa.repository.JpaRepository

/**
 * 応募情報エンティティの永続化を担当するリポジトリ。
 * ユーザー単位で応募情報を検索するためのクエリも提供する。
 */
interface ApplicationRepository : JpaRepository<Application, Long> {

    /**
     * 指定したユーザーに属する応募情報を更新日時の降順で取得する。
     */
    fun findAllByUserIdOrderByUpdatedAtDesc(userId: Long): List<Application>

    /**
     * 指定した応募情報IDとユーザーIDに一致する応募情報を取得する。
     */
    fun findByIdAndUserId(id: Long, userId: Long): Application?
}
