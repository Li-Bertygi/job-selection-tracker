package com.hyunwoo.jobselectiontracker.application.history.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.hyunwoo.jobselectiontracker.application.entity.Application
import com.hyunwoo.jobselectiontracker.application.entity.ApplicationStatus
import com.hyunwoo.jobselectiontracker.application.history.entity.ApplicationStatusHistory
import com.hyunwoo.jobselectiontracker.application.history.repository.ApplicationStatusHistoryRepository
import com.hyunwoo.jobselectiontracker.application.repository.ApplicationRepository
import com.hyunwoo.jobselectiontracker.company.entity.Company
import com.hyunwoo.jobselectiontracker.company.repository.CompanyRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApplicationStatusHistoryControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var companyRepository: CompanyRepository

    @Autowired
    private lateinit var applicationRepository: ApplicationRepository

    @Autowired
    private lateinit var applicationStatusHistoryRepository: ApplicationStatusHistoryRepository

    @BeforeEach
    fun setUp() {
        applicationStatusHistoryRepository.deleteAll()
        applicationRepository.deleteAll()
        companyRepository.deleteAll()
    }

    @Test
    fun `GET status histories returns histories in changedAt desc order`() {
        val application = createApplication(status = ApplicationStatus.INTERVIEW)
        applicationStatusHistoryRepository.save(
            ApplicationStatusHistory(
                application = application,
                fromStatus = ApplicationStatus.APPLICATION,
                toStatus = ApplicationStatus.INTERVIEW
            )
        )
        applicationStatusHistoryRepository.save(
            ApplicationStatusHistory(
                application = application,
                fromStatus = ApplicationStatus.INTERVIEW,
                toStatus = ApplicationStatus.OFFERED
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

    private fun createApplication(status: ApplicationStatus): Application {
        val company = companyRepository.save(
            Company(
                name = "OpenAI",
                industry = "AI",
                websiteUrl = "https://openai.com",
                memo = "テスト企業"
            )
        )

        return applicationRepository.save(
            Application(
                company = company,
                jobTitle = "Backend Engineer",
                applicationRoute = "Wantedly",
                status = status,
                priority = 1,
                isArchived = false
            )
        )
    }
}
