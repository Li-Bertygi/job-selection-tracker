package com.hyunwoo.jobselectiontracker.application.repository

import com.hyunwoo.jobselectiontracker.application.entity.Application
import com.hyunwoo.jobselectiontracker.application.entity.ApplicationStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

/**
 * 応募情報エンティティの永続化を担当するリポジトリ。
 * ユーザー単位で応募情報を検索するためのクエリも提供する。
 */
interface ApplicationRepository : JpaRepository<Application, Long> {

    /**
     * 指定したユーザーに属する応募情報を、任意の条件で検索する。
     */
    @Query(
        """
        select a
        from Application a
        join a.company c
        where a.user.id = :userId
          and (:status is null or a.status = :status)
          and (:isArchived is null or a.isArchived = :isArchived)
          and (
            :keyword is null
            or lower(a.jobTitle) like lower(concat('%', :keyword, '%'))
            or lower(c.name) like lower(concat('%', :keyword, '%'))
          )
        """
    )
    fun searchByUser(
        @Param("userId") userId: Long,
        @Param("status") status: ApplicationStatus?,
        @Param("isArchived") isArchived: Boolean?,
        @Param("keyword") keyword: String?,
        pageable: Pageable
    ): Page<Application>

    /**
     * 指定した応募情報IDとユーザーIDに一致する応募情報を取得する。
     */
    fun findByIdAndUserId(id: Long, userId: Long): Application?
}
