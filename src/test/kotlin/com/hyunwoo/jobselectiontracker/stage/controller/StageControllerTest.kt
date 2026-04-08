package com.hyunwoo.jobselectiontracker.stage.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.hyunwoo.jobselectiontracker.application.entity.Application
import com.hyunwoo.jobselectiontracker.application.entity.ApplicationStatus
import com.hyunwoo.jobselectiontracker.application.repository.ApplicationRepository
import com.hyunwoo.jobselectiontracker.company.entity.Company
import com.hyunwoo.jobselectiontracker.company.repository.CompanyRepository
import com.hyunwoo.jobselectiontracker.schedule.repository.ScheduleRepository
import com.hyunwoo.jobselectiontracker.stage.entity.Stage
import com.hyunwoo.jobselectiontracker.stage.entity.StageStatus
import com.hyunwoo.jobselectiontracker.stage.entity.StageType
import com.hyunwoo.jobselectiontracker.stage.history.repository.StageStatusHistoryRepository
import com.hyunwoo.jobselectiontracker.stage.repository.StageRepository
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
class StageControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var companyRepository: CompanyRepository

    @Autowired
    private lateinit var applicationRepository: ApplicationRepository

    @Autowired
    private lateinit var stageRepository: StageRepository

    @Autowired
    private lateinit var scheduleRepository: ScheduleRepository

    @Autowired
    private lateinit var stageStatusHistoryRepository: StageStatusHistoryRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @BeforeEach
    fun setUp() {
        scheduleRepository.deleteAll()
        stageStatusHistoryRepository.deleteAll()
        stageRepository.deleteAll()
        applicationRepository.deleteAll()
        companyRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun `POST stages creates stage`() {
        val application = createApplication()
        val request = mapOf(
            "stageOrder" to 1,
            "stageType" to "FIRST_INTERVIEW",
            "stageName" to "一次面接",
            "status" to "SCHEDULED"
        )

        mockMvc.perform(
            post("/applications/${application.id}/stages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").isNumber)
            .andExpect(jsonPath("$.applicationId").value(application.id!!))
            .andExpect(jsonPath("$.stageType").value("FIRST_INTERVIEW"))
    }

    @Test
    fun `POST stages with duplicate stageOrder returns bad request`() {
        val application = createApplication()
        stageRepository.save(
            Stage(
                application = application,
                stageOrder = 1,
                stageType = StageType.FIRST_INTERVIEW,
                stageName = "一次面接",
                status = StageStatus.PENDING
            )
        )

        val request = mapOf(
            "stageOrder" to 1,
            "stageType" to "SECOND_INTERVIEW",
            "stageName" to "二次面接",
            "status" to "PENDING"
        )

        mockMvc.perform(
            post("/applications/${application.id}/stages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
    }

    @Test
    fun `GET stages with missing application id returns not found`() {
        mockMvc.perform(get("/applications/9999/stages"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
    }

    @Test
    fun `PATCH stages with changed status creates status history`() {
        val application = createApplication()
        val stage = stageRepository.save(
            Stage(
                application = application,
                stageOrder = 1,
                stageType = StageType.FIRST_INTERVIEW,
                stageName = "一次面接",
                status = StageStatus.SCHEDULED
            )
        )

        val request = mapOf("status" to "COMPLETED")

        mockMvc.perform(
            patch("/stages/${stage.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("COMPLETED"))

        val histories = stageStatusHistoryRepository.findAllByStageIdOrderByChangedAtDesc(stage.id!!)
        assertEquals(1, histories.size)
        assertEquals(StageStatus.SCHEDULED, histories[0].fromStatus)
        assertEquals(StageStatus.COMPLETED, histories[0].toStatus)
    }

    @Test
    fun `PATCH stages with invalid status transition returns bad request`() {
        val application = createApplication()
        val stage = stageRepository.save(
            Stage(
                application = application,
                stageOrder = 1,
                stageType = StageType.FIRST_INTERVIEW,
                stageName = "Final interview",
                status = StageStatus.PASSED
            )
        )

        val request = mapOf("status" to "SCHEDULED")

        mockMvc.perform(
            patch("/stages/${stage.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
    }

    private fun createApplication(): Application {
        val user = createUser()
        val company = companyRepository.save(
            Company(
                user = user,
                name = "OpenAI",
                industry = "AI",
                websiteUrl = "https://openai.com",
                memo = "志望度高め"
            )
        )

        return applicationRepository.save(
            Application(
                user = user,
                company = company,
                jobTitle = "Backend Engineer",
                applicationRoute = "Wantedly",
                status = ApplicationStatus.APPLICATION,
                priority = 1,
                isArchived = false
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
