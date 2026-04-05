package com.hyunwoo.jobselectiontracker.stage.history.controller

import com.hyunwoo.jobselectiontracker.application.entity.Application
import com.hyunwoo.jobselectiontracker.application.entity.ApplicationStatus
import com.hyunwoo.jobselectiontracker.application.repository.ApplicationRepository
import com.hyunwoo.jobselectiontracker.company.entity.Company
import com.hyunwoo.jobselectiontracker.company.repository.CompanyRepository
import com.hyunwoo.jobselectiontracker.stage.entity.Stage
import com.hyunwoo.jobselectiontracker.stage.entity.StageStatus
import com.hyunwoo.jobselectiontracker.stage.entity.StageType
import com.hyunwoo.jobselectiontracker.stage.history.entity.StageStatusHistory
import com.hyunwoo.jobselectiontracker.stage.history.repository.StageStatusHistoryRepository
import com.hyunwoo.jobselectiontracker.stage.repository.StageRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StageStatusHistoryControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var companyRepository: CompanyRepository

    @Autowired
    private lateinit var applicationRepository: ApplicationRepository

    @Autowired
    private lateinit var stageRepository: StageRepository

    @Autowired
    private lateinit var stageStatusHistoryRepository: StageStatusHistoryRepository

    @BeforeEach
    fun setUp() {
        stageStatusHistoryRepository.deleteAll()
        stageRepository.deleteAll()
        applicationRepository.deleteAll()
        companyRepository.deleteAll()
    }

    @Test
    fun `GET stage status histories returns histories in changedAt desc order`() {
        val stage = createStage(status = StageStatus.COMPLETED)
        stageStatusHistoryRepository.save(
            StageStatusHistory(
                stage = stage,
                fromStatus = StageStatus.SCHEDULED,
                toStatus = StageStatus.COMPLETED,
                changedAt = LocalDateTime.of(2026, 4, 6, 5, 0, 0)
            )
        )
        stageStatusHistoryRepository.save(
            StageStatusHistory(
                stage = stage,
                fromStatus = StageStatus.COMPLETED,
                toStatus = StageStatus.PASSED,
                changedAt = LocalDateTime.of(2026, 4, 6, 6, 0, 0)
            )
        )

        mockMvc.perform(get("/stages/${stage.id}/status-histories"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].toStatus").value("PASSED"))
            .andExpect(jsonPath("$[1].toStatus").value("COMPLETED"))
    }

    @Test
    fun `GET stage status histories with missing stage id returns not found`() {
        mockMvc.perform(get("/stages/9999/status-histories"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
    }

    private fun createStage(status: StageStatus): Stage {
        val company = companyRepository.save(
            Company(
                name = "OpenAI",
                industry = "AI",
                websiteUrl = "https://openai.com",
                memo = "企業メモ"
            )
        )

        val application = applicationRepository.save(
            Application(
                company = company,
                jobTitle = "Backend Engineer",
                applicationRoute = "Wantedly",
                status = ApplicationStatus.INTERVIEW,
                priority = 1,
                isArchived = false
            )
        )

        return stageRepository.save(
            Stage(
                application = application,
                stageOrder = 1,
                stageType = StageType.FIRST_INTERVIEW,
                stageName = "一次面接",
                status = status
            )
        )
    }
}
