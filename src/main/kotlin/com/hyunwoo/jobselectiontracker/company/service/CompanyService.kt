package com.hyunwoo.jobselectiontracker.company.service

import com.hyunwoo.jobselectiontracker.common.security.CurrentUserProvider
import com.hyunwoo.jobselectiontracker.common.security.OwnedResourceFinder
import com.hyunwoo.jobselectiontracker.company.dto.CompanyResponse
import com.hyunwoo.jobselectiontracker.company.dto.CreateCompanyRequest
import com.hyunwoo.jobselectiontracker.company.dto.UpdateCompanyRequest
import com.hyunwoo.jobselectiontracker.company.entity.Company
import com.hyunwoo.jobselectiontracker.company.repository.CompanyRepository
import com.hyunwoo.jobselectiontracker.user.entity.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class CompanyService(
    private val companyRepository: CompanyRepository,
    private val currentUserProvider: CurrentUserProvider,
    private val ownedResourceFinder: OwnedResourceFinder
) {

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

    fun getCompanies(): List<CompanyResponse> {
        val currentUser = findCurrentUser()
        return companyRepository.findAllByUserIdOrderByUpdatedAtDesc(currentUser.id!!)
            .map(CompanyResponse::from)
    }

    fun getCompany(id: Long): CompanyResponse {
        val currentUser = findCurrentUser()
        return CompanyResponse.from(findCompanyByIdAndUserId(id, currentUser.id!!))
    }

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

    @Transactional
    fun deleteCompany(id: Long) {
        val currentUser = findCurrentUser()
        companyRepository.delete(findCompanyByIdAndUserId(id, currentUser.id!!))
    }

    private fun findCurrentUser(): User {
        return currentUserProvider.getCurrentUser()
    }

    private fun findCompanyByIdAndUserId(id: Long, userId: Long): Company {
        return ownedResourceFinder.findCompany(id, userId)
    }
}
