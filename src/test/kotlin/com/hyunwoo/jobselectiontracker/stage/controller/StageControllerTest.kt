package com.hyunwoo.jobselectiontracker.stage.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.hyunwoo.jobselectiontracker.application.entity.Application
import com.hyunwoo.jobselectiontracker.application.entity.ApplicationStatus
import com.hyunwoo.jobselectiontracker.application.repository.ApplicationRepository
import com.hyunwoo.jobselectiontracker.company.entity.Company
import com.hyunwoo.jobselectiontracker.company.repository.CompanyRepository
import com.hyunwoo.jobselectiontracker.schedule.repository.ScheduleRepository
import com.hyunwoo.jobselectiontracker.stage.repository.StageRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
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

    @BeforeEach
    fun setUp() {
        scheduleRepository.deleteAll()
        stageRepository.deleteAll()
        applicationRepository.deleteAll()
        companyRepository.deleteAll()
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
            com.hyunwoo.jobselectiontracker.stage.entity.Stage(
                application = application,
                stageOrder = 1,
                stageType = com.hyunwoo.jobselectiontracker.stage.entity.StageType.FIRST_INTERVIEW,
                stageName = "一次面接",
                status = com.hyunwoo.jobselectiontracker.stage.entity.StageStatus.PENDING
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

    private fun createApplication(): Application {
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
                status = ApplicationStatus.APPLICATION,
                priority = 1,
                isArchived = false
            )
        )
    }
}
