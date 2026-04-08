package com.hyunwoo.jobselectiontracker.company.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.hyunwoo.jobselectiontracker.company.entity.Company
import com.hyunwoo.jobselectiontracker.company.repository.CompanyRepository
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser
class CompanyControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var companyRepository: CompanyRepository

    @BeforeEach
    fun setUp() {
        companyRepository.deleteAll()
    }

    @Test
    fun `POST companies creates company`() {
        val request = mapOf(
            "name" to "OpenAI",
            "industry" to "AI",
            "websiteUrl" to "https://openai.com",
            "memo" to "第一志望"
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
    }

    @Test
    fun `GET companies with missing id returns not found`() {
        mockMvc.perform(get("/companies/9999"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
    }
}
