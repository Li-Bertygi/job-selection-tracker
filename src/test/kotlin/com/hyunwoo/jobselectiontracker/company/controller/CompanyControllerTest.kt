package com.hyunwoo.jobselectiontracker.company.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.hyunwoo.jobselectiontracker.application.repository.ApplicationRepository
import com.hyunwoo.jobselectiontracker.company.entity.Company
import com.hyunwoo.jobselectiontracker.company.repository.CompanyRepository
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "test@example.com")
class CompanyControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var companyRepository: CompanyRepository

    @Autowired
    private lateinit var applicationRepository: ApplicationRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @BeforeEach
    fun setUp() {
        applicationRepository.deleteAll()
        companyRepository.deleteAll()
        userRepository.deleteAll()
        createUser()
    }

    @Test
    fun `POST companies creates company`() {
        val request = mapOf(
            "name" to "OpenAI",
            "industry" to "AI",
            "websiteUrl" to "https://openai.com",
            "memo" to "志望度高め"
        )

        mockMvc.perform(
            post("/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").isNumber)
            .andExpect(jsonPath("$.name").value("OpenAI"))
            .andExpect(jsonPath("$.industry").value("AI"))
    }

    @Test
    fun `GET companies returns only current user companies`() {
        val myUser = createUser()
        val myCompany = createCompany(myUser, "OpenAI")
        createCompany(createOtherUser(), "Google")

        mockMvc.perform(get("/companies"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(myCompany.id!!))
    }

    @Test
    fun `POST companies without name returns bad request`() {
        val request = mapOf(
            "industry" to "AI"
        )

        mockMvc.perform(
            post("/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
    }

    @Test
    fun `GET companies with missing id returns not found`() {
        mockMvc.perform(get("/companies/9999"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
    }

    @Test
    fun `PATCH companies owned by another user returns not found`() {
        val otherCompany = createCompany(createOtherUser(), "Google")
        val request = mapOf("memo" to "更新テスト")

        mockMvc.perform(
            patch("/companies/${otherCompany.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
    }

    @Test
    fun `DELETE companies with linked applications returns conflict`() {
        val company = createCompany(createUser(), "OpenAI")

        mockMvc.perform(
            post("/applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        mapOf(
                            "companyId" to company.id,
                            "jobTitle" to "Backend Engineer",
                            "status" to "APPLICATION"
                        )
                    )
                )
        )
            .andExpect(status().isCreated)

        mockMvc.perform(delete("/companies/${company.id}"))
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.code").value("DATA_INTEGRITY_VIOLATION"))
    }

    private fun createCompany(user: User, name: String): Company {
        return companyRepository.save(
            Company(
                user = user,
                name = name,
                industry = "AI",
                websiteUrl = "https://example.com",
                memo = "テストメモ"
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
