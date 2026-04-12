package com.hyunwoo.jobselectiontracker.auth.controller

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `protected API without JWT returns unauthorized`() {
        mockMvc.perform(get("/companies"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `protected API with invalid JWT returns unauthorized`() {
        mockMvc.perform(
            get("/companies")
                .header("Authorization", "Bearer invalid-token")
        )
            .andExpect(status().isUnauthorized)
    }
}
