# Job Application Tracker

就職活動における応募情報、選考ステージ、日程、メモを一元管理するためのバックエンド API です。  
Kotlin / Spring Boot を用いて、認証、ユーザーごとのデータ分離、ステータス履歴管理、データ整合性の担保を重視して実装しています。

---

## 概要

本プロジェクトは、就職活動で散在しやすい以下の情報をまとめて管理することを目的としています。

- 応募先企業
- 応募情報
- 選考ステージ
- 面接や締切などの日程
- 志望動機や振り返りメモ
- 応募ステータスおよび選考ステータスの変更履歴

単純な CRUD にとどまらず、JWT 認証、所有者チェック、履歴自動生成、Flyway によるスキーマ管理、DB 制約による重複防止を含めて構成しています。

---

## 技術スタック

- Kotlin 1.9
- Spring Boot 3
- Spring Web
- Spring Security
- Spring Data JPA
- PostgreSQL
- H2 Database
- Flyway
- JJWT
- Gradle Kotlin DSL

---

## 現在の実装範囲

### 認証

- ユーザー登録
- ログイン
- 現在のログインユーザー取得
- JWT Bearer 認証

### 企業管理

- 企業の作成
- 企業一覧取得
- 企業詳細取得
- 企業更新
- 企業削除

### 応募管理

- 応募情報の作成
- 応募一覧取得
- 応募詳細取得
- 応募更新
- 応募削除
- 応募ステータス履歴取得

### 選考ステージ管理

- 応募配下のステージ作成
- 応募配下のステージ一覧取得
- ステージ更新
- ステージ削除
- ステージステータス履歴取得

### 日程管理

- 応募配下の日程作成
- 応募配下の日程一覧取得
- 日程更新
- 日程削除

### メモ管理

- 応募配下のメモ作成
- 応募配下のメモ一覧取得
- メモ更新
- メモ削除

---

## 設計上のポイント

### 1. ユーザー単位のデータ分離

すべての主要リソースはログイン中ユーザーの所有データとして扱います。  
他ユーザーの企業、応募、ステージ、日程、メモにはアクセスできません。

### 2. 履歴の自動保存

- `applications.status` が変化した場合、`application_status_histories` を自動生成
- `stages.status` が変化した場合、`stage_status_histories` を自動生成

### 3. 状態遷移の制御

応募とステージのステータスは任意に変更できるわけではなく、サービス層で許可された遷移のみを受け付けます。

応募ステータスの例:

- `NOT_STARTED -> APPLICATION`
- `APPLICATION -> INTERVIEW`
- `INTERVIEW -> OFFERED`
- `INTERVIEW -> REJECTED`
- `OFFERED` と `REJECTED` は終端状態

ステージステータスの例:

- `PENDING -> SCHEDULED`
- `SCHEDULED -> COMPLETED`
- `COMPLETED -> PASSED`
- `COMPLETED -> FAILED`
- `PASSED` と `FAILED` は終端状態

### 4. DB 制約による整合性担保

Flyway 管理のスキーマ上で、以下の制約を DB レベルでも保証しています。

- `applications.priority` は `0..10`
- `stages` は `(application_id, stage_order)` を一意に制約
- `schedules.end_at >= start_at`
- `schedules` は応募単位またはステージ単位で重複登録を防止

### 5. 設定分離

- `application.yaml`: 共通設定
- `application-local.yaml`: ローカル開発用設定
- `application-prod.yaml`: 本番想定設定
- `application-test.yaml`: テスト用設定

---

## データモデル

主なテーブルは以下の通りです。

- `users`
- `companies`
- `applications`
- `stages`
- `schedules`
- `notes`
- `application_status_histories`
- `stage_status_histories`

ERD:

![ERD](./DB.png)

---

## API 一覧

### Auth

- `POST /auth/signup`
- `POST /auth/login`
- `GET /auth/me`

### Companies

- `POST /companies`
- `GET /companies`
- `GET /companies/{id}`
- `PATCH /companies/{id}`
- `DELETE /companies/{id}`

### Applications

- `POST /applications`
- `GET /applications`
- `GET /applications/{id}`
- `PATCH /applications/{id}`
- `DELETE /applications/{id}`
- `GET /applications/{id}/status-histories`

### Stages

- `POST /applications/{id}/stages`
- `GET /applications/{id}/stages`
- `PATCH /stages/{id}`
- `DELETE /stages/{id}`
- `GET /stages/{id}/status-histories`

### Schedules

- `POST /applications/{id}/schedules`
- `GET /applications/{id}/schedules`
- `PATCH /schedules/{id}`
- `DELETE /schedules/{id}`

### Notes

- `POST /applications/{id}/notes`
- `GET /applications/{id}/notes`
- `PATCH /notes/{id}`
- `DELETE /notes/{id}`

---

## ローカル実行方法

### 前提

- Java 17
- PostgreSQL

### 1. PostgreSQL を起動

例:

```bash
docker run --name postgres-db ^
  -e POSTGRES_USER=postgres ^
  -e POSTGRES_PASSWORD=1234 ^
  -e POSTGRES_DB=jobtracker ^
  -p 5432:5432 ^
  -d postgres:15
```

すでにコンテナを作成済みの場合:

```bash
docker start postgres-db
```

### 2. 環境変数を設定

PowerShell の例:

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/jobtracker"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="1234"
$env:JWT_SECRET="change-this-secret-key-for-local-environment-only"
```

未指定の場合、`application-local.yaml` のデフォルト値が使用されます。  
ただし、実運用を想定する場合は必ず明示的に設定してください。

### 3. アプリケーションを起動

```bash
./gradlew bootRun
```

デフォルトでは `local` プロファイルで起動します。

---

## マイグレーション

スキーマは Flyway で管理しています。

- `src/main/resources/db/migration/V1__init.sql`
- `src/main/resources/db/migration/V2__integrity_constraints.sql`

Hibernate はスキーマ自動更新を行わず、`validate` のみ実行します。  
テーブル定義や制約変更は必ず migration を追加して管理します。

---

## テスト

テストは H2 インメモリ DB と `test` プロファイルを使用して実行します。

```bash
./gradlew test
```

現在は主に以下を検証しています。

- 認証 API の基本動作
- ユーザー所有データへのアクセス制御
- バリデーションエラー
- ステータス履歴の生成
- ステータス遷移制御
- 重複データの防止

---

## 動作確認例

### ユーザー登録

```bash
curl.exe --% -X POST http://localhost:8080/auth/signup -H "Content-Type: application/json" -d "{\"email\":\"test@example.com\",\"password\":\"password123\",\"name\":\"テストユーザー\"}"
```

### ログイン

```bash
curl.exe --% -X POST http://localhost:8080/auth/login -H "Content-Type: application/json" -d "{\"email\":\"test@example.com\",\"password\":\"password123\"}"
```

### 現在ユーザー取得

```bash
curl.exe --% http://localhost:8080/auth/me -H "Authorization: Bearer <accessToken>"
```

### 企業作成

```bash
curl.exe --% -X POST http://localhost:8080/companies -H "Authorization: Bearer <accessToken>" -H "Content-Type: application/json" -d "{\"name\":\"OpenAI\",\"industry\":\"AI\",\"websiteUrl\":\"https://openai.com\",\"memo\":\"志望度が高い企業\"}"
```

### 応募作成

```bash
curl.exe --% -X POST http://localhost:8080/applications -H "Authorization: Bearer <accessToken>" -H "Content-Type: application/json" -d "{\"companyId\":1,\"jobTitle\":\"Backend Engineer\",\"applicationRoute\":\"Wantedly\",\"status\":\"APPLICATION\",\"appliedAt\":\"2026-04-05\",\"priority\":1,\"isArchived\":false}"
```

### ステージ作成

```bash
curl.exe --% -X POST http://localhost:8080/applications/1/stages -H "Authorization: Bearer <accessToken>" -H "Content-Type: application/json" -d "{\"stageOrder\":1,\"stageType\":\"FIRST_INTERVIEW\",\"stageName\":\"一次面接\",\"status\":\"SCHEDULED\",\"scheduledAt\":\"2026-04-10T14:00:00\",\"memo\":\"オンライン面接\"}"
```

### 日程作成

```bash
curl.exe --% -X POST http://localhost:8080/applications/1/schedules -H "Authorization: Bearer <accessToken>" -H "Content-Type: application/json" -d "{\"scheduleType\":\"RESULT_ANNOUNCEMENT\",\"title\":\"結果連絡予定\",\"description\":\"メールで通知予定\",\"startAt\":\"2026-04-12T18:00:00\",\"isAllDay\":false}"
```

### メモ作成

```bash
curl.exe --% -X POST http://localhost:8080/applications/1/notes -H "Authorization: Bearer <accessToken>" -H "Content-Type: application/json" -d "{\"title\":\"面接準備\",\"noteType\":\"PREPARATION\",\"content\":\"自己紹介と志望動機を整理する。\"}"
```

---

## 今後の改善候補

- 一覧 API のフィルタ・ソート・ページング
- ステータス遷移ルールのより詳細な業務化
- Docker / docker-compose によるローカル統合実行
- CI/CD 構築
- フロントエンドの追加
- AWS へのデプロイと IaC 対応

---

## 補足

このリポジトリは現在、バックエンド API を中心に実装しています。  
README には、リポジトリ内に実装済みの内容のみを記載しています。
