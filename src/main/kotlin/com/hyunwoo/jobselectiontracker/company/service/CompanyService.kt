package com.hyunwoo.jobselectiontracker.company.service

import com.hyunwoo.jobselectiontracker.company.dto.CompanyResponse
import com.hyunwoo.jobselectiontracker.company.dto.CreateCompanyRequest
import com.hyunwoo.jobselectiontracker.company.dto.UpdateCompanyRequest
import com.hyunwoo.jobselectiontracker.company.entity.Company
import com.hyunwoo.jobselectiontracker.company.repository.CompanyRepository
import com.hyunwoo.jobselectiontracker.user.entity.User
import com.hyunwoo.jobselectiontracker.user.repository.UserRepository
import java.util.NoSuchElementException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 企業情報の作成、取得、更新、削除を担当するサービス。
 * すべての参照と更新は現在ログイン中のユーザーに属する企業情報のみを対象とする。
 */
@Service
@Transactional(readOnly = true)
class CompanyService(
    /** 企業情報の永続化を担当するリポジトリ。 */
    private val companyRepository: CompanyRepository,

    /** 現在ログイン中ユーザーの取得に使用するリポジトリ。 */
    private val userRepository: UserRepository
) {

    /**
     * 現在認証されているユーザーを所有者として企業情報を作成する。
     */
    @Transactional
    fun createCompany(request: CreateCompanyRequest): CompanyResponse {
        val company = Company(
            user = findCurrentUser(),
            name = request.name.trim(),
            industry = request.industry?.trim(),
            websiteUrl = request.websiteUrl?.trim(),
            memo = request.memo?.trim()
        )

        return CompanyResponse.from(companyRepository.save(company))
    }

    /**
     * 現在ログイン中ユーザーに属する企業一覧を取得する。
     */
    fun getCompanies(): List<CompanyResponse> {
        val currentUser = findCurrentUser()
        return companyRepository.findAllByUserIdOrderByUpdatedAtDesc(currentUser.id!!)
            .map(CompanyResponse::from)
    }

    /**
     * 指定した企業IDの詳細を取得する。
     * 他ユーザーの企業情報は存在しないものとして扱う。
     */
    fun getCompany(id: Long): CompanyResponse {
        val currentUser = findCurrentUser()
        return CompanyResponse.from(findCompanyByIdAndUserId(id, currentUser.id!!))
    }

    /**
     * 指定した企業情報を更新する。
     * 他ユーザーの企業情報は存在しないものとして扱う。
     */
    @Transactional
    fun updateCompany(id: Long, request: UpdateCompanyRequest): CompanyResponse {
        val currentUser = findCurrentUser()
        val company = findCompanyByIdAndUserId(id, currentUser.id!!)

        request.name?.let { company.name = it.trim() }
        request.industry?.let { company.industry = it.trim() }
        request.websiteUrl?.let { company.websiteUrl = it.trim() }
        request.memo?.let { company.memo = it.trim() }

        return CompanyResponse.from(companyRepository.saveAndFlush(company))
    }

    /**
     * 指定した企業情報を削除する。
     * 他ユーザーの企業情報は存在しないものとして扱う。
     */
    @Transactional
    fun deleteCompany(id: Long) {
        val currentUser = findCurrentUser()
        companyRepository.delete(findCompanyByIdAndUserId(id, currentUser.id!!))
    }

    /**
     * SecurityContext に格納された認証情報から現在ユーザーを取得する。
     */
    private fun findCurrentUser(): User {
        val email = SecurityContextHolder.getContext().authentication?.name
            ?: throw IllegalStateException("現在の認証ユーザー情報を取得できません。")

        return userRepository.findByEmail(email)
            ?: throw NoSuchElementException("メールアドレス $email に該当するユーザーが見つかりません。")
    }

    /**
     * 企業IDとユーザーIDで企業を取得し、存在しない場合は 404 用例外を送出する。
     */
    private fun findCompanyByIdAndUserId(id: Long, userId: Long): Company {
        return companyRepository.findByIdAndUserId(id, userId)
            ?: throw NoSuchElementException("企業ID $id に該当する企業が見つかりません。")
    }
}
