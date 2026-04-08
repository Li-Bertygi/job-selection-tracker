package com.hyunwoo.jobselectiontracker.auth.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.hyunwoo.jobselectiontracker.application.history.repository.ApplicationStatusHistoryRepository
import com.hyunwoo.jobselectiontracker.application.repository.ApplicationRepository
import com.hyunwoo.jobselectiontracker.company.repository.CompanyRepository
import com.hyunwoo.jobselectiontracker.note.repository.NoteRepository
import com.hyunwoo.jobselectiontracker.schedule.repository.ScheduleRepository
import com.hyunwoo.jobselectiontracker.stage.history.repository.StageStatusHistoryRepository
import com.hyunwoo.jobselectiontracker.stage.repository.StageRepository
import com.hyunwoo.jobselectiontracker.user.entity.User
import com.hyunwoo.jobselectiontracker.user.repository.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: UserRepository

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
    private lateinit var applicationStatusHistoryRepository: ApplicationStatusHistoryRepository

    @Autowired
    private lateinit var stageStatusHistoryRepository: StageStatusHistoryRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @BeforeEach
    fun setUp() {
        scheduleRepository.deleteAll()
        stageStatusHistoryRepository.deleteAll()
        applicationStatusHistoryRepository.deleteAll()
        stageRepository.deleteAll()
        noteRepository.deleteAll()
        applicationRepository.deleteAll()
        companyRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun `POST signup creates user`() {
        val request = mapOf(
            "email" to "test@example.com",
            "password" to "password123",
            "name" to "テストユーザー"
        )

        mockMvc.perform(
            post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.userId").isNumber)
            .andExpect(jsonPath("$.email").value("test@example.com"))
            .andExpect(jsonPath("$.name").value("テストユーザー"))
    }

    @Test
    fun `POST login returns user info when credentials are valid`() {
        createUser(
            email = "test@example.com",
            rawPassword = "password123",
            name = "テストユーザー"
        )

        val request = mapOf(
            "email" to "test@example.com",
            "password" to "password123"
        )

        mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.userId").isNumber)
            .andExpect(jsonPath("$.email").value("test@example.com"))
            .andExpect(jsonPath("$.name").value("テストユーザー"))
    }

    @Test
    fun `POST signup with duplicate email returns bad request`() {
        createUser(
            email = "test@example.com",
            rawPassword = "password123",
            name = "既存ユーザー"
        )

        val request = mapOf(
            "email" to "test@example.com",
            "password" to "password123",
            "name" to "重複ユーザー"
        )

        mockMvc.perform(
            post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.code").value("DUPLICATE_RESOURCE"))
    }

    @Test
    fun `POST login with wrong password returns bad request`() {
        createUser(
            email = "test@example.com",
            rawPassword = "password123",
            name = "テストユーザー"
        )

        val request = mapOf(
            "email" to "test@example.com",
            "password" to "wrongpass123"
        )

        mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
    }

    private fun createUser(email: String, rawPassword: String, name: String): User {
        return userRepository.save(
            User(
                email = email,
                password = passwordEncoder.encode(rawPassword),
                name = name
            )
        )
    }
}
