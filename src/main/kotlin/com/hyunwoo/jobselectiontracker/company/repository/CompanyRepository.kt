package com.hyunwoo.jobselectiontracker.company.repository

import com.hyunwoo.jobselectiontracker.company.entity.Company
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Companyエンティティに対するDBアクセスを担当するリポジトリ。
 * 基本的なCRUDメソッドはSpring Data JPAが自動で提供する。
 */
interface CompanyRepository : JpaRepository<Company, Long>
