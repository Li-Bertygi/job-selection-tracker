package com.hyunwoo.jobselectiontracker.application.repository

import com.hyunwoo.jobselectiontracker.application.entity.Application
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Applicationエンティティに対するDBアクセスを担当するリポジトリ。
 * 基本的なCRUDメソッドはSpring Data JPAが自動で提供する。
 */
interface ApplicationRepository : JpaRepository<Application, Long>
