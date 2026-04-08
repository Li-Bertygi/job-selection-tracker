package com.hyunwoo.jobselectiontracker.company.repository

import com.hyunwoo.jobselectiontracker.company.entity.Company
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Company エンティティの永続化を担当するリポジトリ。
 * 現在ログイン中ユーザーに紐づく企業情報のみを検索する。
 */
interface CompanyRepository : JpaRepository<Company, Long> {

    /**
     * 指定したユーザーに属する企業情報を更新日時の降順で取得する。
     */
    fun findAllByUserIdOrderByUpdatedAtDesc(userId: Long): List<Company>

    /**
     * 指定した企業IDとユーザーIDに一致する企業情報を取得する。
     */
    fun findByIdAndUserId(id: Long, userId: Long): Company?
}
