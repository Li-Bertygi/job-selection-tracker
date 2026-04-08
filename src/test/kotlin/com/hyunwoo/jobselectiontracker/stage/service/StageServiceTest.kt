package com.hyunwoo.jobselectiontracker.stage.service

import com.hyunwoo.jobselectiontracker.application.entity.Application
import com.hyunwoo.jobselectiontracker.application.entity.ApplicationStatus
import com.hyunwoo.jobselectiontracker.application.repository.ApplicationRepository
import com.hyunwoo.jobselectiontracker.common.exception.InvalidRequestException
import com.hyunwoo.jobselectiontracker.company.entity.Company
import com.hyunwoo.jobselectiontracker.company.repository.CompanyRepository
import com.hyunwoo.jobselectiontracker.stage.dto.UpdateStageRequest
import com.hyunwoo.jobselectiontracker.stage.entity.Stage
import com.hyunwoo.jobselectiontracker.stage.entity.StageStatus
import com.hyunwoo.jobselectiontracker.stage.entity.StageType
import com.hyunwoo.jobselectiontracker.stage.history.repository.StageStatusHistoryRepository
import com.hyunwoo.jobselectiontracker.stage.repository.StageRepository
import com.hyunwoo.jobselectiontracker.user.entity.User
import com.hyunwoo.jobselectiontracker.user.repository.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
@WithMockUser(username = "test@example.com")
class StageServiceTest {

    @Autowired
    private lateinit var stageService: StageService

    @Autowired
    private lateinit var stageRepository: StageRepository

    @Autowired
    private lateinit var stageStatusHistoryRepository: StageStatusHistoryRepository

    @Autowired
    private lateinit var applicationRepository: ApplicationRepository

    @Autowired
    private lateinit var companyRepository: CompanyRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @BeforeEach
    fun setUp() {
        stageStatusHistoryRepository.deleteAll()
        stageRepository.deleteAll()
        applicationRepository.deleteAll()
        companyRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun `invalid stage status transition throws exception`() {
        val stage = createStage(status = StageStatus.PASSED)

        val exception = assertThrows(InvalidRequestException::class.java) {
            stageService.updateStage(
                stage.id!!,
                UpdateStageRequest(status = StageStatus.SCHEDULED)
            )
        }

        assertEquals(
            "選考ステージのステータスを PASSED から SCHEDULED へ変更することはできません。",
            exception.message
        )
    }

    @Test
    fun `valid stage status transition creates history`() {
        val stage = createStage(status = StageStatus.COMPLETED)

        val response = stageService.updateStage(
            stage.id!!,
            UpdateStageRequest(status = StageStatus.PASSED)
        )

        assertEquals(StageStatus.PASSED, response.status)

        val histories = stageStatusHistoryRepository.findAllByStageIdOrderByChangedAtDesc(stage.id!!)
        assertEquals(1, histories.size)
        assertEquals(StageStatus.COMPLETED, histories[0].fromStatus)
        assertEquals(StageStatus.PASSED, histories[0].toStatus)
    }

    private fun createStage(status: StageStatus): Stage {
        val user = createUser()
        val company = companyRepository.save(
            Company(
                user = user,
                name = "OpenAI",
                industry = "AI",
                websiteUrl = "https://openai.com",
                memo = "Test company"
            )
        )
        val application = applicationRepository.save(
            Application(
                user = user,
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
                stageName = "First interview",
                status = status
            )
        )
    }

    private fun createUser(): User {
        return userRepository.save(
            User(
                email = "test@example.com",
                password = "encoded-password",
                name = "Test User"
            )
        )
    }
}
