package com.hyunwoo.jobselectiontracker.application.dto

import com.hyunwoo.jobselectiontracker.application.entity.ApplicationStatus
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDate

/**
 * 応募情報新規登録APIで受け取るリクエストDTO。
 * 企業IDを基準に、応募職種や進捗状態などの初期値を受け取る。
 */
data class CreateApplicationRequest(

    /** 応募先企業ID。既存のCompanyレコードを参照する。 */
    @field:NotNull(message = "企業IDは必須です。")
    val companyId: Long,

    /** 応募した職種名。 */
    @field:NotNull(message = "応募職種は必須です。")
    @field:Size(min = 1, max = 255, message = "応募職種は1文字以上255文字以内で入力してください。")
    val jobTitle: String,

    /** 応募経路。 */
    @field:Size(max = 100, message = "応募経路は100文字以内で入力してください。")
    val applicationRoute: String? = null,

    /** 応募全体の現在ステータス。未指定時はNOT_STARTEDを使用する。 */
    val status: ApplicationStatus = ApplicationStatus.NOT_STARTED,

    /** 実際に応募した日付。 */
    val appliedAt: LocalDate? = null,

    /** 結果通知日。 */
    val resultDate: LocalDate? = null,

    /** 内定承諾期限。 */
    val offerDeadline: LocalDate? = null,

    /** 応募の優先度。 */
    @field:Min(value = 0, message = "優先度は0以上で入力してください。")
    @field:Max(value = 10, message = "優先度は10以下で入力してください。")
    val priority: Int = 0,

    /** 保管済み応募かどうかを示すフラグ。 */
    val isArchived: Boolean = false
)
