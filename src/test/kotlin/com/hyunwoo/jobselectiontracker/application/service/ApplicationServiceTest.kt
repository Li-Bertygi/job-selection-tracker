package com.hyunwoo.jobselectiontracker.application.service

import com.hyunwoo.jobselectiontracker.application.dto.UpdateApplicationRequest
import com.hyunwoo.jobselectiontracker.application.entity.Application
import com.hyunwoo.jobselectiontracker.application.entity.ApplicationStatus
import com.hyunwoo.jobselectiontracker.application.history.repository.ApplicationStatusHistoryRepository
import com.hyunwoo.jobselectiontracker.application.repository.ApplicationRepository
import com.hyunwoo.jobselectiontracker.common.exception.InvalidRequestException
import com.hyunwoo.jobselectiontracker.company.entity.Company
import com.hyunwoo.jobselectiontracker.company.repository.CompanyRepository
import com.hyunwoo.jobselectiontracker.user.entity.User
import com.hyunwoo.jobselectiontracker.user.repository.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
@WithMockUser(username = "test@example.com")
class ApplicationServiceTest {

    @Autowired
    private lateinit var applicationService: ApplicationService

    @Autowired
    private lateinit var applicationRepository: ApplicationRepository

    @Autowired
    private lateinit var applicationStatusHistoryRepository: ApplicationStatusHistoryRepository

    @Autowired
    private lateinit var companyRepository: CompanyRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @BeforeEach
    fun setUp() {
        applicationStatusHistoryRepository.deleteAll()
        applicationRepository.deleteAll()
        companyRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun `invalid application status transition throws exception`() {
        val application = createApplication(status = ApplicationStatus.OFFERED)

        val exception = assertThrows(InvalidRequestException::class.java) {
            applicationService.updateApplication(
                application.id!!,
                UpdateApplicationRequest(status = ApplicationStatus.INTERVIEW)
            )
        }

        assertEquals(
            "応募ステータスを OFFERED から INTERVIEW へ変更することはできません。",
            exception.message
        )
    }

    @Test
    fun `valid application status transition creates history`() {
        val application = createApplication(status = ApplicationStatus.INTERVIEW)

        val response = applicationService.updateApplication(
            application.id!!,
            UpdateApplicationRequest(status = ApplicationStatus.OFFERED)
        )

        assertEquals(ApplicationStatus.OFFERED, response.status)

        val histories =
            applicationStatusHistoryRepository.findAllByApplicationIdOrderByChangedAtDesc(application.id!!)
        assertEquals(1, histories.size)
        assertEquals(ApplicationStatus.INTERVIEW, histories[0].fromStatus)
        assertEquals(ApplicationStatus.OFFERED, histories[0].toStatus)
    }

    private fun createApplication(status: ApplicationStatus): Application {
        val user = createUser()
        val company = companyRepository.save(
            Company(
                user = user,
                name = "OpenAI",
                industry = "AI",
                websiteUrl = "https://openai.com",
                memo = "Test company"
            )
        )

        return applicationRepository.save(
            Application(
                user = user,
                company = company,
                jobTitle = "Backend Engineer",
                applicationRoute = "Wantedly",
                status = status,
                priority = 1,
                isArchived = false
            )
        )
    }

    private fun createUser(): User {
        return userRepository.save(
            User(
                email = "test@example.com",
                password = "encoded-password",
                name = "Test User"
            )
        )
    }
}
