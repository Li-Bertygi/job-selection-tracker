package com.hyunwoo.jobselectiontracker.schedule.service

import com.hyunwoo.jobselectiontracker.application.entity.Application
import com.hyunwoo.jobselectiontracker.application.entity.ApplicationStatus
import com.hyunwoo.jobselectiontracker.application.repository.ApplicationRepository
import com.hyunwoo.jobselectiontracker.common.exception.InvalidRequestException
import com.hyunwoo.jobselectiontracker.company.entity.Company
import com.hyunwoo.jobselectiontracker.company.repository.CompanyRepository
import com.hyunwoo.jobselectiontracker.schedule.dto.CreateScheduleRequest
import com.hyunwoo.jobselectiontracker.schedule.entity.Schedule
import com.hyunwoo.jobselectiontracker.schedule.entity.ScheduleType
import com.hyunwoo.jobselectiontracker.schedule.repository.ScheduleRepository
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
import java.time.LocalDateTime

@SpringBootTest
@ActiveProfiles("test")
@WithMockUser(username = "test@example.com")
class ScheduleServiceTest {

    @Autowired
    private lateinit var scheduleService: ScheduleService

    @Autowired
    private lateinit var scheduleRepository: ScheduleRepository

    @Autowired
    private lateinit var applicationRepository: ApplicationRepository

    @Autowired
    private lateinit var companyRepository: CompanyRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @BeforeEach
    fun setUp() {
        scheduleRepository.deleteAll()
        applicationRepository.deleteAll()
        companyRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun `schedule with endAt before startAt throws exception`() {
        val application = createApplication()

        val exception = assertThrows(InvalidRequestException::class.java) {
            scheduleService.createSchedule(
                application.id!!,
                CreateScheduleRequest(
                    scheduleType = ScheduleType.EVENT,
                    title = "Interview",
                    startAt = LocalDateTime.of(2026, 4, 10, 15, 0),
                    endAt = LocalDateTime.of(2026, 4, 10, 14, 0),
                    isAllDay = false
                )
            )
        }

        assertEquals("終了日時は開始日時以降である必要があります。", exception.message)
    }

    @Test
    fun `duplicate schedule throws exception`() {
        val application = createApplication()
        scheduleRepository.save(
            Schedule(
                application = application,
                stage = null,
                scheduleType = ScheduleType.RESULT_ANNOUNCEMENT,
                title = "Result notice",
                description = "Email notification",
                startAt = LocalDateTime.of(2026, 4, 12, 18, 0),
                isAllDay = false
            )
        )

        val exception = assertThrows(InvalidRequestException::class.java) {
            scheduleService.createSchedule(
                application.id!!,
                CreateScheduleRequest(
                    scheduleType = ScheduleType.RESULT_ANNOUNCEMENT,
                    title = "Result notice",
                    startAt = LocalDateTime.of(2026, 4, 12, 18, 0),
                    isAllDay = false
                )
            )
        }

        assertEquals(
            "同じ応募・同じ種別・同じ開始日時の予定はすでに存在します。",
            exception.message
        )
    }

    private fun createApplication(): Application {
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
        return userRepository.save(
            User(
                email = "test@example.com",
                password = "encoded-password",
                name = "Test User"
            )
        )
    }
}
