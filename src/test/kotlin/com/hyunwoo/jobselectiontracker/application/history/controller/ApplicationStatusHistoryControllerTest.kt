package com.hyunwoo.jobselectiontracker.application.history.controller

import com.hyunwoo.jobselectiontracker.application.entity.Application
import com.hyunwoo.jobselectiontracker.application.entity.ApplicationStatus
import com.hyunwoo.jobselectiontracker.application.history.entity.ApplicationStatusHistory
import com.hyunwoo.jobselectiontracker.application.history.repository.ApplicationStatusHistoryRepository
import com.hyunwoo.jobselectiontracker.application.repository.ApplicationRepository
import com.hyunwoo.jobselectiontracker.company.entity.Company
import com.hyunwoo.jobselectiontracker.company.repository.CompanyRepository
import com.hyunwoo.jobselectiontracker.user.entity.User
import com.hyunwoo.jobselectiontracker.user.repository.UserRepository
import java.time.LocalDateTime
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "test@example.com")
class ApplicationStatusHistoryControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var companyRepository: CompanyRepository

    @Autowired
    private lateinit var applicationRepository: ApplicationRepository

    @Autowired
    private lateinit var applicationStatusHistoryRepository: ApplicationStatusHistoryRepository

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
    fun `GET status histories returns histories in changedAt desc order`() {
        val user = createUser()
        val application = createApplication(user, status = ApplicationStatus.INTERVIEW)
        applicationStatusHistoryRepository.save(
            ApplicationStatusHistory(
                application = application,
                fromStatus = ApplicationStatus.APPLICATION,
                toStatus = ApplicationStatus.INTERVIEW,
                changedAt = LocalDateTime.of(2026, 4, 6, 2, 0, 0)
            )
        )
        applicationStatusHistoryRepository.save(
            ApplicationStatusHistory(
                application = application,
                fromStatus = ApplicationStatus.INTERVIEW,
                toStatus = ApplicationStatus.OFFERED,
                changedAt = LocalDateTime.of(2026, 4, 6, 3, 0, 0)
            )
        )

        mockMvc.perform(get("/applications/${application.id}/status-histories"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].toStatus").value("OFFERED"))
            .andExpect(jsonPath("$[1].toStatus").value("INTERVIEW"))
    }

    @Test
    fun `GET status histories with missing application id returns not found`() {
        mockMvc.perform(get("/applications/9999/status-histories"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
    }

    private fun createApplication(user: User, status: ApplicationStatus): Application {
        return applicationRepository.save(
            Application(
                user = user,
                company = createCompany(user),
                jobTitle = "Backend Engineer",
                applicationRoute = "Wantedly",
                status = status,
                priority = 1,
                isArchived = false
            )
        )
    }

    private fun createCompany(user: User): Company {
        return companyRepository.save(
            Company(
                user = user,
                name = "OpenAI",
                industry = "AI",
                websiteUrl = "https://openai.com",
                memo = "志望度高め"
            )
        )
    }

    private fun createUser(): User {
        return userRepository.findByEmail("test@example.com")
            ?: userRepository.save(
                User(
                    email = "test@example.com",
                    password = "encoded-password",
                    name = "テストユーザー"
                )
            )
    }
}
