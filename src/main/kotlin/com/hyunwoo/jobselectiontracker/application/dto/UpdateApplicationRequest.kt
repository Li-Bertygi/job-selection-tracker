package com.hyunwoo.jobselectiontracker.application.dto

import com.hyunwoo.jobselectiontracker.application.entity.ApplicationStatus
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size
import java.time.LocalDate

/**
 * 応募情報更新APIで受け取るリクエストDTO。
 * PATCHを想定しているため、各フィールドは未指定を表現できるよう nullable にしている。
 */
data class UpdateApplicationRequest(

    /** 更新先の企業ID。必要な場合のみ変更する。 */
    val companyId: Long? = null,

    /** 更新後の応募職種。 */
    @field:Size(min = 1, max = 255, message = "応募職種は1文字以上255文字以内で入力してください。")
    val jobTitle: String? = null,

    /** 更新後の応募経路。 */
    @field:Size(max = 100, message = "応募経路は100文字以内で入力してください。")
    val applicationRoute: String? = null,

    /** 更新後の応募全体ステータス。 */
    val status: ApplicationStatus? = null,

    /** 更新後の応募日。 */
    val appliedAt: LocalDate? = null,

    /** 更新後の結果通知日。 */
    val resultDate: LocalDate? = null,

    /** 更新後の内定承諾期限。 */
    val offerDeadline: LocalDate? = null,

    /** 更新後の優先度。 */
    @field:Min(value = 0, message = "優先度は0以上で入力してください。")
    @field:Max(value = 10, message = "優先度は10以下で入力してください。")
    val priority: Int? = null,

    /** 更新後の保管状態。 */
    val isArchived: Boolean? = null
)
