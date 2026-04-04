package com.hyunwoo.jobselectiontracker

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Spring Bootアプリケーション全体の起点となるメインクラス。
 * 自動設定やコンポーネントスキャンはこのクラスを基準に有効化される。
 */
@SpringBootApplication
class JobSelectionTrackerApplication

/**
 * アプリケーションを起動するエントリーポイント。
 * コマンドライン引数をSpring Bootにそのまま引き渡す。
 */
fun main(args: Array<String>) {
    runApplication<JobSelectionTrackerApplication>(*args)
}
