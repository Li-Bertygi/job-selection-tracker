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
        val company = createCompany(createUser())
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
        val company = createCompany(createUser())
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
    fun `GET applications returns only current user applications`() {
        val myApplication = createApplication(user = createUser(), status = ApplicationStatus.APPLICATION)
        createApplication(user = createOtherUser(), status = ApplicationStatus.INTERVIEW)

        mockMvc.perform(get("/applications"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].id").value(myApplication.id!!))
            .andExpect(jsonPath("$.totalElements").value(1))
    }

    @Test
    fun `GET applications filters by status`() {
        val matchingApplication = createApplication(
            user = createUser(),
            status = ApplicationStatus.INTERVIEW
        )
        createApplication(user = createUser(), status = ApplicationStatus.APPLICATION)

        mockMvc.perform(get("/applications").param("status", "INTERVIEW"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].id").value(matchingApplication.id!!))
            .andExpect(jsonPath("$.content[0].status").value("INTERVIEW"))
    }

    @Test
    fun `GET applications filters by archived flag`() {
        val archivedApplication = createApplication(
            user = createUser(),
            status = ApplicationStatus.APPLICATION,
            isArchived = true
        )
        createApplication(
            user = createUser(),
            status = ApplicationStatus.INTERVIEW,
            isArchived = false
        )

        mockMvc.perform(get("/applications").param("isArchived", "true"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].id").value(archivedApplication.id!!))
            .andExpect(jsonPath("$.content[0].isArchived").value(true))
    }

    @Test
    fun `GET applications searches by company name and job title`() {
        val companyMatch = createApplication(
            user = createUser(),
            status = ApplicationStatus.APPLICATION,
            companyName = "Google"
        )
        val jobTitleMatch = createApplication(
            user = createUser(),
            status = ApplicationStatus.INTERVIEW,
            companyName = "OpenAI",
            jobTitle = "Platform Engineer"
        )
        createApplication(
            user = createUser(),
            status = ApplicationStatus.TEST,
            companyName = "LINE",
            jobTitle = "Frontend Engineer"
        )

        mockMvc.perform(get("/applications").param("keyword", "goo"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].id").value(companyMatch.id!!))

        mockMvc.perform(get("/applications").param("keyword", "platform"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].id").value(jobTitleMatch.id!!))
    }

    @Test
    fun `GET applications returns paginated response`() {
        createApplication(user = createUser(), status = ApplicationStatus.APPLICATION)
        createApplication(user = createUser(), status = ApplicationStatus.INTERVIEW)
        createApplication(user = createUser(), status = ApplicationStatus.TEST)

        mockMvc.perform(
            get("/applications")
                .param("page", "0")
                .param("size", "2")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.size").value(2))
            .andExpect(jsonPath("$.totalElements").value(3))
            .andExpect(jsonPath("$.totalPages").value(2))
            .andExpect(jsonPath("$.first").value(true))
            .andExpect(jsonPath("$.last").value(false))
    }

    @Test
    fun `GET applications with invalid pagination returns bad request`() {
        mockMvc.perform(get("/applications").param("page", "-1"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))

        mockMvc.perform(get("/applications").param("size", "101"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
    }

    @Test
    fun `GET applications with missing id returns not found`() {
        mockMvc.perform(get("/applications/9999"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
    }

    @Test
    fun `PATCH applications with changed status creates status history`() {
        val application = createApplication(user = createUser(), status = ApplicationStatus.APPLICATION)
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

    @Test
    fun `PATCH applications with same status does not create status history`() {
        val application = createApplication(user = createUser(), status = ApplicationStatus.APPLICATION)
        val request = mapOf("status" to "APPLICATION")

        mockMvc.perform(
            patch("/applications/${application.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("APPLICATION"))

        val histories = applicationStatusHistoryRepository.findAllByApplicationIdOrderByChangedAtDesc(application.id!!)
        assertEquals(0, histories.size)
    }

    @Test
    fun `PATCH applications owned by another user returns not found`() {
        val application = createApplication(user = createOtherUser(), status = ApplicationStatus.APPLICATION)
        val request = mapOf("status" to "INTERVIEW")

        mockMvc.perform(
            patch("/applications/${application.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
    }

    @Test
    fun `PATCH applications with invalid status transition returns bad request`() {
        val application = createApplication(user = createUser(), status = ApplicationStatus.OFFERED)
        val request = mapOf("status" to "INTERVIEW")

        mockMvc.perform(
            patch("/applications/${application.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
    }

    private fun createApplication(
        user: User,
        status: ApplicationStatus,
        companyName: String = "OpenAI",
        jobTitle: String = "Backend Engineer",
        isArchived: Boolean = false
    ): Application {
        return applicationRepository.save(
            Application(
                user = user,
                company = createCompany(user, companyName),
                jobTitle = jobTitle,
                applicationRoute = "Wantedly",
                status = status,
                priority = 1,
                isArchived = isArchived
            )
        )
    }

    private fun createCompany(user: User, name: String = "OpenAI"): Company {
        return companyRepository.save(
            Company(
                user = user,
                name = name,
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

    private fun createOtherUser(): User {
        return userRepository.findByEmail("other@example.com")
            ?: userRepository.save(
                User(
                    email = "other@example.com",
                    password = "encoded-password",
                    name = "他ユーザー"
                )
            )
    }
}
