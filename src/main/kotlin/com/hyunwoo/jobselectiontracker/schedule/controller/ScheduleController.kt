package com.hyunwoo.jobselectiontracker.schedule.controller

import com.hyunwoo.jobselectiontracker.schedule.dto.CreateScheduleRequest
import com.hyunwoo.jobselectiontracker.schedule.dto.ScheduleResponse
import com.hyunwoo.jobselectiontracker.schedule.dto.UpdateScheduleRequest
import com.hyunwoo.jobselectiontracker.schedule.service.ScheduleService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

/**
 * スケジュールに対するHTTPリクエストを受け付けるRESTコントローラ。
 * 実際の業務処理はServiceへ委譲し、ここではルーティングと入出力の橋渡しを行う。
 */
@RestController
@RequestMapping
class ScheduleController(
    /** スケジュール関連の業務ロジックを扱うサービス。 */
    private val scheduleService: ScheduleService
) {

    /** 指定した応募情報にスケジュールを追加するAPI。成功時は201 Createdを返す。 */
    @PostMapping("/applications/{applicationId}/schedules")
    @ResponseStatus(HttpStatus.CREATED)
    fun createSchedule(
        @PathVariable applicationId: Long,
        @Valid @RequestBody request: CreateScheduleRequest
    ): ScheduleResponse {
        return scheduleService.createSchedule(applicationId, request)
    }

    /** 指定した応募情報に紐づくスケジュール一覧を取得するAPI。 */
    @GetMapping("/applications/{applicationId}/schedules")
    fun getSchedules(
        @PathVariable applicationId: Long
    ): List<ScheduleResponse> {
        return scheduleService.getSchedules(applicationId)
    }

    /** 指定したスケジュールIDの内容を部分更新するAPI。 */
    @PatchMapping("/schedules/{id}")
    fun updateSchedule(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateScheduleRequest
    ): ScheduleResponse {
        return scheduleService.updateSchedule(id, request)
    }

    /** 指定したスケジュールIDを削除するAPI。成功時は204 No Contentを返す。 */
    @DeleteMapping("/schedules/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteSchedule(
        @PathVariable id: Long
    ) {
        scheduleService.deleteSchedule(id)
    }
}
