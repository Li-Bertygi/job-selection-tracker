package com.hyunwoo.jobselectiontracker.common.security

import com.hyunwoo.jobselectiontracker.application.entity.Application
import com.hyunwoo.jobselectiontracker.application.repository.ApplicationRepository
import com.hyunwoo.jobselectiontracker.company.entity.Company
import com.hyunwoo.jobselectiontracker.company.repository.CompanyRepository
import com.hyunwoo.jobselectiontracker.common.exception.NotFoundException
import com.hyunwoo.jobselectiontracker.note.entity.Note
import com.hyunwoo.jobselectiontracker.note.repository.NoteRepository
import com.hyunwoo.jobselectiontracker.schedule.entity.Schedule
import com.hyunwoo.jobselectiontracker.schedule.repository.ScheduleRepository
import com.hyunwoo.jobselectiontracker.stage.entity.Stage
import com.hyunwoo.jobselectiontracker.stage.repository.StageRepository
import org.springframework.stereotype.Component

@Component
class OwnedResourceFinder(
    private val companyRepository: CompanyRepository,
    private val applicationRepository: ApplicationRepository,
    private val stageRepository: StageRepository,
    private val scheduleRepository: ScheduleRepository,
    private val noteRepository: NoteRepository
) {

    fun findCompany(id: Long, userId: Long): Company {
        return companyRepository.findByIdAndUserId(id, userId)
            ?: throw NotFoundException("Company $id was not found.")
    }

    fun findApplication(id: Long, userId: Long): Application {
        return applicationRepository.findByIdAndUserId(id, userId)
            ?: throw NotFoundException("Application $id was not found.")
    }

    fun findStage(id: Long, userId: Long): Stage {
        return stageRepository.findByIdAndApplicationUserId(id, userId)
            ?: throw NotFoundException("Stage $id was not found.")
    }

    fun findSchedule(id: Long, userId: Long): Schedule {
        return scheduleRepository.findByIdAndApplicationUserId(id, userId)
            ?: throw NotFoundException("Schedule $id was not found.")
    }

    fun findNote(id: Long, userId: Long): Note {
        return noteRepository.findByIdAndApplicationUserId(id, userId)
            ?: throw NotFoundException("Note $id was not found.")
    }
}
