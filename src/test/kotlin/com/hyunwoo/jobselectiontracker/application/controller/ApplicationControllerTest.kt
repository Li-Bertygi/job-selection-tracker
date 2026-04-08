package com.hyunwoo.jobselectiontracker.application.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.hyunwoo.jobselectiontracker.application.entity.Application
import com.hyunwoo.jobselectiontracker.application.entity.ApplicationStatus
import com.hyunwoo.jobselectiontracker.application.history.repository.ApplicationStatusHistoryRepository
import com.hyunwoo.jobselectiontracker.application.repository.ApplicationRepository
import com.hyunwoo.jobselectiontracker.company.entity.Company
import com.hyunwoo.jobselectiontracker.company.repository.CompanyRepository
import com.hyunwoo.jobselectiontracker.user.entity.User
import com.hyunwoo.jobselectiontracker.user.repository.UserRepository
import kotlin.test.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "test@example.com")
class ApplicationControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

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
        createUser()
    }

    @Test
    fun `POST applications creates application`() {
        val company = createCompany()
        val request = mapOf(
            "companyId" to company.id,
            "jobTitle" to "Backend Engineer",
            "applicationRoute" to "Wantedly",
            "status" to "APPLICATION",
            "priority" to 1,
            "isArchived" to false
        )

        mockMvc.perform(
            post("/applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").isNumber)
            .andExpect(jsonPath("$.companyId").value(company.id!!))
            .andExpect(jsonPath("$.status").value("APPLICATION"))
    }

    @Test
    fun `POST applications with blank job title returns bad request`() {
        val company = createCompany()
        val request = mapOf(
            "companyId" to company.id,
            "jobTitle" to "",
            "status" to "APPLICATION"
        )

        mockMvc.perform(
            post("/applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.errors.jobTitle").exists())
    }

    @Test
    fun `GET applications with missing id returns not found`() {
        mockMvc.perform(get("/applications/9999"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
    }

    @Test
    fun `PATCH applications with changed status creates status history`() {
        val application = createApplication(status = ApplicationStatus.APPLICATION)
        val request = mapOf("status" to "INTERVIEW")

        mockMvc.perform(
            patch("/applications/${application.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("INTERVIEW"))

        val histories = applicationStatusHistoryRepository.findAllByApplicationIdOrderByChangedAtDesc(application.id!!)
        assertEquals(1, histories.size)
        assertEquals(ApplicationStatus.APPLICATION, histories[0].fromStatus)
        assertEquals(ApplicationStatus.INTERVIEW, histories[0].toStatus)
    }

    private fun createApplication(status: ApplicationStatus): Application {
        return applicationRepository.save(
            Application(
                user = createUser(),
                company = createCompany(),
                jobTitle = "Backend Engineer",
                applicationRoute = "Wantedly",
                status = status,
                priority = 1,
                isArchived = false
            )
        )
    }

    private fun createCompany(): Company {
        return companyRepository.save(
            Company(
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
