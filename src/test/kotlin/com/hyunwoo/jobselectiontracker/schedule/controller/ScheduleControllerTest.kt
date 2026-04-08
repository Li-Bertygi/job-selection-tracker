package com.hyunwoo.jobselectiontracker.schedule.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.hyunwoo.jobselectiontracker.application.entity.Application
import com.hyunwoo.jobselectiontracker.application.entity.ApplicationStatus
import com.hyunwoo.jobselectiontracker.application.repository.ApplicationRepository
import com.hyunwoo.jobselectiontracker.company.entity.Company
import com.hyunwoo.jobselectiontracker.company.repository.CompanyRepository
import com.hyunwoo.jobselectiontracker.schedule.entity.Schedule
import com.hyunwoo.jobselectiontracker.schedule.entity.ScheduleType
import com.hyunwoo.jobselectiontracker.schedule.repository.ScheduleRepository
import com.hyunwoo.jobselectiontracker.stage.repository.StageRepository
import com.hyunwoo.jobselectiontracker.user.entity.User
import com.hyunwoo.jobselectiontracker.user.repository.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "test@example.com")
class ScheduleControllerTest {

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
    private lateinit var userRepository: UserRepository

    @BeforeEach
    fun setUp() {
        scheduleRepository.deleteAll()
        stageRepository.deleteAll()
        applicationRepository.deleteAll()
        companyRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun `POST schedules creates schedule`() {
        val application = createApplication()
        val request = mapOf(
            "scheduleType" to "RESULT_ANNOUNCEMENT",
            "title" to "結果連絡予定",
            "description" to "メールで通知予定",
            "startAt" to "2026-04-12T18:00:00",
            "isAllDay" to false
        )

        mockMvc.perform(
            post("/applications/${application.id}/schedules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").isNumber)
            .andExpect(jsonPath("$.applicationId").value(application.id!!))
            .andExpect(jsonPath("$.scheduleType").value("RESULT_ANNOUNCEMENT"))
    }

    @Test
    fun `POST schedules with duplicate key returns bad request`() {
        val application = createApplication()
        scheduleRepository.save(
            Schedule(
                application = application,
                stage = null,
                scheduleType = ScheduleType.RESULT_ANNOUNCEMENT,
                title = "結果連絡予定",
                description = "メールで通知予定",
                startAt = LocalDateTime.of(2026, 4, 12, 18, 0),
                isAllDay = false
            )
        )

        val request = mapOf(
            "scheduleType" to "RESULT_ANNOUNCEMENT",
            "title" to "結果連絡予定",
            "description" to "メールで通知予定",
            "startAt" to "2026-04-12T18:00:00",
            "isAllDay" to false
        )

        mockMvc.perform(
            post("/applications/${application.id}/schedules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
    }

    @Test
    fun `POST schedules with missing stage returns not found`() {
        val application = createApplication()
        val request = mapOf(
            "stageId" to 9999,
            "scheduleType" to "EVENT",
            "title" to "一次面接実施",
            "startAt" to "2026-04-10T14:00:00",
            "isAllDay" to false
        )

        mockMvc.perform(
            post("/applications/${application.id}/schedules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
    }

    private fun createApplication(): Application {
        val company = companyRepository.save(
            Company(
                name = "OpenAI",
                industry = "AI",
                websiteUrl = "https://openai.com",
                memo = "志望度高め"
            )
        )

        return applicationRepository.save(
            Application(
                user = createUser(),
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
