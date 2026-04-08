package com.hyunwoo.jobselectiontracker.note.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.hyunwoo.jobselectiontracker.application.entity.Application
import com.hyunwoo.jobselectiontracker.application.entity.ApplicationStatus
import com.hyunwoo.jobselectiontracker.application.repository.ApplicationRepository
import com.hyunwoo.jobselectiontracker.company.entity.Company
import com.hyunwoo.jobselectiontracker.company.repository.CompanyRepository
import com.hyunwoo.jobselectiontracker.note.repository.NoteRepository
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "test@example.com")
class NoteControllerTest {

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
    private lateinit var noteRepository: NoteRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @BeforeEach
    fun setUp() {
        scheduleRepository.deleteAll()
        stageRepository.deleteAll()
        noteRepository.deleteAll()
        applicationRepository.deleteAll()
        companyRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun `POST notes creates note with null title`() {
        val application = createApplication()
        val request = mapOf(
            "noteType" to "REVIEW",
            "content" to "面接後に振り返りを追加する。"
        )

        mockMvc.perform(
            post("/applications/${application.id}/notes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").isNumber)
            .andExpect(jsonPath("$.applicationId").value(application.id!!))
            .andExpect(jsonPath("$.title").doesNotExist())
            .andExpect(jsonPath("$.noteType").value("REVIEW"))
    }

    @Test
    fun `POST notes with blank content returns bad request`() {
        val application = createApplication()
        val request = mapOf(
            "title" to "空メモ",
            "noteType" to "OTHER",
            "content" to ""
        )

        mockMvc.perform(
            post("/applications/${application.id}/notes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.errors.content").exists())
    }

    @Test
    fun `PATCH notes with missing id returns not found`() {
        val request = mapOf("content" to "更新テスト")

        mockMvc.perform(
            patch("/notes/9999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
    }

    private fun createApplication(): Application {
        return applicationRepository.save(
            Application(
                user = createUser(),
                company = createCompany(),
                jobTitle = "Backend Engineer",
                applicationRoute = "Wantedly",
                status = ApplicationStatus.APPLICATION,
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
                memo = "企業メモ"
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
