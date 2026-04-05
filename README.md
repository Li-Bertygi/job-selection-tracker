# Job Application Tracker

就職活動における応募情報・選考フロー・日程・メモを一元管理するためのアプリケーションです。

---

## 1. サービス概要

### 1-1. サービス一行定義
就職活動における応募状況・選考進捗・スケジュールを一元管理できるアプリ

### 1-2. 解決したい課題
就職活動では、企業ごとに応募状況や選考フロー、面接日程が分散しやすく、
以下のような問題が発生します。

- 応募状況の把握が難しい
- 面接日程や締切の管理が煩雑
- 選考の進捗が整理できない
- 志望動機や面接内容の記録が分散する

本サービスではこれらの情報を一元化し、効率的な就職活動を支援します。

---

### 1-3. MVP範囲

- 応募情報の登録・編集・削除（CRUD）
- 選考ステージの管理
- 面接・締切などのスケジュール管理
- メモ機能（志望動機・面接記録など）
- ステータス管理（応募〜内定まで）

---

## 2. 使用技術

- フロントエンド：React / Next.js
- バックエンド：Kotlin / Spring Boot
- 認証・認可：Spring Security / JWT
- データベース：PostgreSQL
- ORM：JPA (Hibernate)
- インフラ：AWS
- コンテナ：Docker
- キャッシュ：Redis（今後の拡張を想定）

---

## 2-1. 技術選定理由

本プロジェクトでは、単なる実装ではなく「実務で通用する設計・構成」を意識し、各技術を選定しています。

### フロントエンド：React / Next.js
ユーザーインターフェースの構築において、コンポーネント指向による再利用性と保守性を重視しました。  
また、Next.jsを採用することで、ルーティングやデータ取得を効率的に行い、実務に近いフロントエンド構成を意識しています。

### バックエンド：Kotlin / Spring Boot
企業での採用実績が多く、堅牢なAPI設計が可能なSpring Bootを採用しました。  
Kotlinを使用することで、Javaよりも簡潔かつ安全にコードを記述でき、開発効率と可読性の向上を図っています。

### 認証・認可：Spring Security / JWT
実務で一般的に使用される認証基盤としてSpring Securityを採用しました。  
JWTを利用することで、ステートレスな認証を実現し、スケーラビリティを考慮した設計としています。

### データベース：PostgreSQL
複雑なリレーションやトランザクション処理に強く、拡張性の高いRDBであるため採用しました。  
本システムのように複数テーブル間の関係が重要な場合に適していると判断しています。

### ORM：JPA (Hibernate)
オブジェクト指向でデータベース操作を行うことで、開発効率と保守性の向上を目的として採用しました。  
また、エンティティ間のリレーションを明確に表現できる点も重視しています。

### インフラ：AWS
実務での利用が多く、スケーラブルな構成を構築できるため採用しました。  
将来的な本番運用を見据えたインフラ設計を意識しています。

### コンテナ：Docker
開発環境と本番環境の差異をなくし、再現性の高い環境構築を実現するため採用しました。  
チーム開発やデプロイの効率化にも寄与します。

### キャッシュ：Redis（今後の拡張を想定）
現時点では必須ではありませんが、  
今後のパフォーマンス改善（例：ダッシュボード集計・頻繁なクエリ結果のキャッシュ）を想定し、導入を検討しています。

---

## 2-2. 実行方法

### 前提条件
- Java 17
- Docker
- Docker上で起動するPostgreSQL

### PostgreSQL コンテナ起動例
以下はローカル検証時に使用した例です。

```bash
docker run --name postgres-db ^
  -e POSTGRES_USER=postgres ^
  -e POSTGRES_PASSWORD=1234 ^
  -e POSTGRES_DB=jobtracker ^
  -p 5432:5432 ^
  -d postgres:15
```

すでにコンテナを作成済みの場合は、以下で起動できます。

```bash
docker start postgres-db
```

### アプリケーション設定
`src/main/resources/application.yaml` では以下の接続先を想定しています。

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/jobtracker
    username: postgres
    password: 1234
```

### サーバー起動

```bash
./gradlew bootRun
```

起動後は `http://localhost:8080` でAPIにアクセスできます。

### テスト実行

テストは `test` プロファイルとH2インメモリDBを使用して実行されます。

```bash
./gradlew test
```

### テーブル確認

```bash
docker exec -it postgres-db psql -U postgres -d jobtracker
```

```sql
\dt
\d companies
\d applications
\d stages
\d schedules
\d notes
\d application_status_histories
```

### 動作確認例

#### 企業登録

```bash
curl.exe --% -X POST http://localhost:8080/companies -H "Content-Type: application/json" -d "{\"name\":\"OpenAI\",\"industry\":\"AI\",\"websiteUrl\":\"https://openai.com\",\"memo\":\"志望度高め\"}"
```

#### 企業一覧取得

```bash
curl.exe --% http://localhost:8080/companies
```

#### 応募情報登録

```bash
curl.exe --% -X POST http://localhost:8080/applications -H "Content-Type: application/json" -d "{\"companyId\":1,\"jobTitle\":\"Backend Engineer\",\"applicationRoute\":\"Wantedly\",\"status\":\"APPLICATION\",\"appliedAt\":\"2026-04-05\",\"priority\":1,\"isArchived\":false}"
```

#### 応募情報一覧取得

```bash
curl.exe --% http://localhost:8080/applications
```

#### 選考ステージ登録

```bash
curl.exe --% -X POST http://localhost:8080/applications/1/stages -H "Content-Type: application/json" -d "{\"stageOrder\":1,\"stageType\":\"FIRST_INTERVIEW\",\"stageName\":\"一次面接(人事面接)\",\"status\":\"SCHEDULED\",\"scheduledAt\":\"2026-04-10T14:00:00\",\"memo\":\"オンライン面接\"}"
```

#### 選考ステージ一覧取得

```bash
curl.exe --% http://localhost:8080/applications/1/stages
```

#### 応募全体のスケジュール登録

```bash
curl.exe --% -X POST http://localhost:8080/applications/1/schedules -H "Content-Type: application/json" -d "{\"scheduleType\":\"RESULT_ANNOUNCEMENT\",\"title\":\"結果連絡予定\",\"description\":\"メールで通知予定\",\"startAt\":\"2026-04-12T18:00:00\",\"isAllDay\":false}"
```

#### ステージに紐づくスケジュール登録

```bash
curl.exe --% -X POST http://localhost:8080/applications/1/schedules -H "Content-Type: application/json" -d "{\"stageId\":3,\"scheduleType\":\"EVENT\",\"title\":\"一次面接実施\",\"description\":\"Zoom面接\",\"startAt\":\"2026-04-10T14:00:00\",\"endAt\":\"2026-04-10T15:00:00\",\"location\":\"Zoom\",\"isAllDay\":false}"
```

#### スケジュール一覧取得

```bash
curl.exe --% http://localhost:8080/applications/1/schedules
```

#### メモ登録

```bash
curl.exe --% -X POST http://localhost:8080/applications/1/notes -H "Content-Type: application/json" -d "{\"title\":\"一次面接の準備\",\"noteType\":\"PREPARATION\",\"content\":\"自己紹介と志望動機を整理しておく。\"}"
```

#### タイトル未入力メモ登録

```bash
curl.exe --% -X POST http://localhost:8080/applications/1/notes -H "Content-Type: application/json" -d "{\"noteType\":\"REVIEW\",\"content\":\"面接後に振り返りを追加する。\"}"
```

#### メモ一覧取得

```bash
curl.exe --% http://localhost:8080/applications/1/notes
```

#### 応募ステータス変更

```bash
curl.exe --% -X PATCH http://localhost:8080/applications/1 -H "Content-Type: application/json" -d "{\"status\":\"INTERVIEW\"}"
```

```bash
curl.exe --% -X PATCH http://localhost:8080/applications/1 -H "Content-Type: application/json" -d "{\"status\":\"OFFERED\"}"
```

#### 応募ステータス履歴取得

```bash
curl.exe --% http://localhost:8080/applications/1/status-histories
```

#### 存在しないID確認

```bash
curl.exe --% http://localhost:8080/companies/9999
curl.exe --% http://localhost:8080/applications/9999
curl.exe --% -X PATCH http://localhost:8080/stages/9999 -H "Content-Type: application/json" -d "{\"status\":\"COMPLETED\"}"
curl.exe --% -X PATCH http://localhost:8080/notes/9999 -H "Content-Type: application/json" -d "{\"content\":\"更新テスト\"}"
curl.exe --% http://localhost:8080/applications/9999/status-histories
```

### 補足
- 現在のセキュリティ設定は開発初期段階向けであり、CRUD検証のため一時的に全リクエストを許可しています。
- 認証API (`/auth`) は今後の実装対象です。

---

## 3. ERD

![ERD](./DB.png)

---

## 4. データベース設計

本システムでは、応募情報を中心に関連データを紐づける構造で設計しています。

---

### 4-1. users

ユーザーの認証情報および基本情報を管理するテーブル

---

### 4-2. companies

応募先企業の基本情報を管理するテーブル

---

### 4-3. applications

応募情報を管理する中心テーブル

本システムの中心となるテーブルであり、
すべての選考情報はこのテーブルを基準に管理されます。

- ユーザー × 企業 の関係を保持
- 応募職種・応募経路・現在のステータスを管理

#### ステータス例
- `NOT_STARTED`
- `APPLICATION`
- `INFO_SESSION`
- `DOCUMENT_SCREENING`
- `TEST`
- `CASUAL_MEETING`
- `INTERVIEW`
- `OFFERED`
- `REJECTED`

---

### 4-4. stages

各応募に紐づく選考ステージを管理

#### 設計意図
企業ごとに異なる選考フローに対応するため、
ステージを独立テーブルとして管理

#### 主な管理項目
- `application_id`
- `stage_order`
- `stage_type`
- `stage_name`
- `status`
- `scheduled_at`
- `completed_at`
- `result_date`
- `memo`

#### ステータス例
- `PENDING`
- `SCHEDULED`
- `COMPLETED`
- `PASSED`
- `FAILED`

#### ステージ種別例
- `INFO_SESSION`
- `DOCUMENT_SCREENING`
- `CODING_TEST`
- `APTITUDE_TEST`
- `FIRST_CASUAL_MEETING`
- `SECOND_CASUAL_MEETING`
- `THIRD_CASUAL_MEETING`
- `FIRST_INTERVIEW`
- `SECOND_INTERVIEW`
- `THIRD_INTERVIEW`
- `FOURTH_INTERVIEW`
- `FINAL_INTERVIEW`
- `OTHER`

#### 役割分担
- `stage_type` はステージの中分類を表す
- `stage_name` は企業ごとの実際の表示名を自由入力で保持する

例:
- `stage_type = FIRST_INTERVIEW`
- `stage_name = 一次面接(人事面接)`

---

### 4-5. schedules

面接・締切・結果通知などのスケジュールを管理

実務において、選考とは独立した締切や通知も存在するため、
両方を柔軟に扱える構造としています。

#### 設計意図
- 応募全体に紐づく予定
- 特定ステージに紐づく予定

の両方に対応できるように設計

#### 主な管理項目
- `application_id`
- `stage_id`
- `schedule_type`
- `title`
- `description`
- `start_at`
- `end_at`
- `location`
- `is_all_day`

#### ScheduleType 例
- `DEADLINE`
- `EVENT`
- `RESULT_ANNOUNCEMENT`
- `OTHER`

#### 重複防止ルール
- `stage_id` がある場合は `application_id + stage_id + schedule_type + start_at` の組み合わせを一意として扱う
- `stage_id` が `null` の場合は `application_id + schedule_type + start_at` の組み合わせを一意として扱う
- 応募全体の日程は `stage_id = null` の同一キーで重複登録できない
- 同じ応募情報・同じ時刻でも、別のステージに紐づく日程であれば別件として登録できる

---

### 4-6. notes

志望動機・面接内容・振り返りなどを記録

#### 設計意図
応募に紐づく自由記述メモを保持し、
準備内容・実際の内容・振り返りを一元管理できるようにする。

#### 主な管理項目
- `application_id`
- `title`
- `note_type`
- `content`
- `created_at`
- `updated_at`

#### NoteType 例
- `UNSPECIFIED`
- `PREPARATION`
- `ACTUAL_CONTENT`
- `REVIEW`
- `OTHER`

#### 補足
- `title` は任意入力であり、未入力の場合は `null`
- 画面表示上の `タイトルなし` はフロントエンド側で補完する

---

### 4-7. application_status_histories

応募全体のステータス変更履歴を管理

#### 設計意図
- `applications.status` の現在値とは別に、状態がどのように変化したかを追跡する
- ユーザーが直接追加するのではなく、応募ステータス変更時に自動生成する

#### 主な管理項目
- `application_id`
- `from_status`
- `to_status`
- `changed_at`

#### 現在の実装方針
- `PATCH /applications/{id}` で `status` が実際に変化した場合のみ履歴を保存する
- 同じステータスへの更新では履歴を追加しない
- 履歴は `GET /applications/{id}/status-histories` で新しい順に取得する

---

### 4-8. stage_status_histories

各選考ステージの状態変更履歴を管理

---

## 5. テーブル関係

- user 1 : N applications
- company 1 : N applications
- application 1 : N stages
- application 1 : N schedules
- application 1 : N notes
- application 1 : N application_status_histories
- stage 1 : N stage_status_histories
- stage 1 : N schedules

---

## 6. 設計ポイント

### 6-1. 応募中心設計
応募（applications）を中心にデータを集約することで、
就職活動の情報を一元管理できる構造にしました。

---

### 6-2. ステータスとステージの分離

- 応募全体の進捗 → `applications.status`
- 各選考段階の進捗 → `stages.status`

として分離することで、
粒度の異なる進捗管理を可能にしています。

`applications.status` は応募全体の大きなカテゴリを管理し、
`stages.status` は各選考ステージの詳細な進捗を管理します。

例:
- `applications.status = INTERVIEW`
- `stages.stage_type = FIRST_INTERVIEW`
- `stages.stage_name = 一次面接`
- `stages.status = SCHEDULED`

---

### 6-3. 状態変更履歴の管理

履歴テーブルを設けることで、

- いつどの状態に変わったか
- 応募全体の進行がどのように変化したか

を追跡できるようにしました。

現在は `applications.status` の変更時に
`application_status_histories` を自動生成する構成としています。

---

### 6-4. 柔軟なスケジュール設計

スケジュールを

- 応募単位
- ステージ単位

の両方で扱えるようにすることで、
実務に近い柔軟な管理を実現しました。

また、同一キーの重複を防ぐことで、
同じ予定の二重登録を抑止しています。

---

### 6-5. 自由度の高いメモ設計

メモは応募単位で保持しつつ、
`note_type` により用途を分類できるようにしました。

これにより、

- 面接前の準備メモ
- 当日に実際に聞かれた内容
- 選考後の振り返り

を同じ構造で蓄積できます。

---

## 7. 今後の改善

- 通知機能（面接・締切リマインド）
- カレンダー連携
- 分析機能（通過率・進捗可視化）
- モバイル対応UI

---

## 8. 想定ユーザー

- 就職活動中の学生
- 転職活動中の社会人

---

## 9. ステータス遷移の考え方

- `REJECTED` は終了状態
- `OFFERED` は最終状態
- ステータスは基本的に前進方向に遷移し、
  過去の状態に戻ることは想定していません。

応募全体の進捗は、以下のような大きなカテゴリで管理します。
- `NOT_STARTED`
- `APPLICATION`
- `INFO_SESSION`
- `DOCUMENT_SCREENING`
- `TEST`
- `CASUAL_MEETING`
- `INTERVIEW`
- `OFFERED`
- `REJECTED`

---

## 10. API設計

本システムでは、応募情報 (`applications`) を中心に、
選考ステージ・スケジュール・メモを紐づける形でAPIを設計しています。

RESTfulな設計を意識し、リソース単位で責務を分離しています。

---

### 10-1. 認証 API

ユーザー登録、ログイン、認証状態の確認を行うためのAPIです。

| Method | Endpoint | 説明 |
|---|---|---|
| POST | `/auth/signup` | 新規ユーザー登録 |
| POST | `/auth/login` | ログイン |
| POST | `/auth/refresh` | アクセストークンの再発行 |
| GET | `/auth/me` | 現在ログイン中のユーザー情報を取得 |

#### 設計意図
認証・認可はアプリケーション全体の基盤であるため、  
他のリソースとは分離して `/auth` 配下にまとめています。

---

### 10-2. 企業 API

応募先企業の基本情報を管理するためのAPIです。

| Method | Endpoint | 説明 |
|---|---|---|
| POST | `/companies` | 企業情報を登録 |
| GET | `/companies` | 企業一覧を取得 |
| GET | `/companies/{id}` | 特定企業の詳細を取得 |
| PATCH | `/companies/{id}` | 特定企業の情報を更新 |
| DELETE | `/companies/{id}` | 特定企業を削除 |

#### 設計意図
企業情報は応募情報の親となる基礎データであり、  
応募履歴とは独立して管理できるようにしています。

---

### 10-3. 応募情報 API

応募情報を管理する中心APIです。  
本システムでは、応募情報を軸として各種データを紐づけています。

| Method | Endpoint | 説明 |
|---|---|---|
| POST | `/applications` | 応募情報を登録 |
| GET | `/applications` | 応募情報一覧を取得 |
| GET | `/applications/{id}` | 特定応募情報の詳細を取得 |
| PATCH | `/applications/{id}` | 特定応募情報を更新 |
| DELETE | `/applications/{id}` | 特定応募情報を削除 |

#### 拡張API（検索・可視化）

| Method | Endpoint | 説明 |
|---|---|---|
| GET | `/applications?status=INTERVIEW&sort=updatedAt` | ステータス・ソート条件によるフィルタ取得 |
| GET | `/applications/{id}/timeline` | 応募のステータス履歴および選考ステージの進捗を統合し、時系列で可視化するためのAPI |
| GET | `/applications/{id}/status-histories` | 応募全体のステータス変更履歴を新しい順で取得 |

#### 設計意図
応募情報 (`applications`) は本システムの中心リソースであり、  
検索・並び替え・履歴の可視化といった拡張機能もこのリソースに集約しています。

---

### 10-4. 選考ステージ API

応募ごとの選考ステージを管理するためのAPIです。

| Method | Endpoint | 説明 |
|---|---|---|
| POST | `/applications/{id}/stages` | 特定応募に選考ステージを追加 |
| GET | `/applications/{id}/stages` | 特定応募の選考ステージ一覧を取得 |
| PATCH | `/stages/{id}` | 特定選考ステージを更新 |
| DELETE | `/stages/{id}` | 特定選考ステージを削除 |

#### 設計意図
選考フローは企業ごとに異なるため、  
応募単位で複数のステージを柔軟に追加・管理できる構造にしています。

---

### 10-5. スケジュール API

面接日程、締切、結果通知日などのスケジュールを管理するためのAPIです。

| Method | Endpoint | 説明 |
|---|---|---|
| POST | `/applications/{id}/schedules` | 特定応募にスケジュールを追加 |
| GET | `/applications/{id}/schedules` | 特定応募のスケジュール一覧を取得 |
| PATCH | `/schedules/{id}` | 特定スケジュールを更新 |
| DELETE | `/schedules/{id}` | 特定スケジュールを削除 |

#### 実装済みルール
- `stage_id` がある場合は `application_id + stage_id + schedule_type + start_at` の組み合わせで重複登録を禁止
- `stage_id` が `null` の場合は `application_id + schedule_type + start_at` の組み合わせで重複登録を禁止

#### 設計意図
スケジュールは応募全体に紐づく情報として扱い、  
必要に応じて選考ステージ単位の予定にも対応できるように設計しています。

---

### 10-6. メモ API

志望動機、面接内容、企業研究メモなどを管理するためのAPIです。

| Method | Endpoint | 説明 |
|---|---|---|
| POST | `/applications/{id}/notes` | 特定応募にメモを追加 |
| GET | `/applications/{id}/notes` | 特定応募のメモ一覧を取得 |
| PATCH | `/notes/{id}` | 特定メモを更新 |
| DELETE | `/notes/{id}` | 特定メモを削除 |

#### 実装済みルール
- `title` は任意入力であり、未入力時は `null`
- `note_type` は `UNSPECIFIED`, `PREPARATION`, `ACTUAL_CONTENT`, `REVIEW`, `OTHER` を使用
- `content` は必須

#### 設計意図
メモは応募単位で整理することで、  
企業ごとの志望動機、面接記録、振り返りを一元管理できるようにしています。

---

### 10-7. 統計 API（拡張機能）

今後の拡張として、応募状況を可視化するための統計APIを想定しています。

| Method | Endpoint | 説明 |
|---|---|---|
| GET | `/dashboard/summary` | 応募数・進行中件数・合否件数などの要約情報を取得 |
| GET | `/dashboard/stats` | 通過率やステージ別統計情報を取得 |

#### 想定する取得内容
- 総応募数
- 現在進行中の応募数
- 合格 / 不合格件数
- ステージ別通過率
- 応募状況の可視化データ

#### 設計意図
本システムは単なる記録ツールではなく、  
応募状況を分析し、次の行動判断に活かせる構造を目指しています。

---

## 11. API設計上のポイント

- 認証機能は `/auth` に分離し、責務を明確化
- 応募情報 (`applications`) を中心に、子リソースとして `stages` / `schedules` / `notes` を管理
- 企業情報 (`companies`) は応募情報と分離し、再利用可能な基礎データとして扱う
- `application_status_histories` はユーザーが直接操作せず、応募ステータス変更時に自動生成する
- 将来的にダッシュボード・分析機能へ拡張しやすいように統計APIを別系統で設計
