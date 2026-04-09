# Job Application Tracker

就職活動における企業、応募、選考ステージ、日程、メモを一元管理するためのアプリケーションです。  
バックエンドは Kotlin / Spring Boot、フロントエンドは Next.js で構成しています。

本リポジトリは、単純な CRUD 実装にとどまらず、認証、ユーザー単位のデータ分離、状態遷移制御、履歴管理、Flyway によるスキーマ管理まで含めて、バックエンド中心のポートフォリオとして整理しています。

---

## 構成

- `src/main/...`
  - Spring Boot バックエンド API
- `src/test/...`
  - バックエンドテスト
- `src/main/resources/db/migration`
  - Flyway マイグレーション
- `frontend/`
  - Next.js フロントエンド

---

## 技術スタック

### バックエンド

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

### フロントエンド

- Next.js 16
- React 19
- TypeScript
- Tailwind CSS 4

---

## 現在の実装範囲

### バックエンド API

- ユーザー登録
- ログイン
- 現在のログインユーザー取得
- 企業の作成 / 一覧 / 詳細 / 更新 / 削除
- 応募の作成 / 一覧 / 詳細 / 更新 / 削除
- 応募ステータス履歴取得
- 選考ステージの作成 / 一覧 / 更新 / 削除
- ステージステータス履歴取得
- 日程の作成 / 一覧 / 更新 / 削除
- メモの作成 / 一覧 / 更新 / 削除

### フロントエンド

- ログイン画面
- 応募一覧画面
- 応募作成画面
- 応募詳細画面
- 会社登録画面
- 応募詳細画面でのステージ / 日程 / メモの追加
- 応募詳細画面でのステージ / 日程 / メモの修正 / 削除
- 応募一覧上部の簡易ダッシュボード

---

## 設計上のポイント

### 1. ユーザー単位のデータ分離

すべての主要リソースはログイン中ユーザーに紐づいて管理されます。  
他ユーザーの企業、応募、ステージ、日程、メモにはアクセスできません。

### 2. ステータス履歴の自動生成

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

### 4. Flyway によるスキーマ管理

Hibernate の `ddl-auto` に依存せず、スキーマ変更は Flyway migration で管理しています。

- `V1__init.sql`
- `V2__integrity_constraints.sql`

### 5. DB 制約による整合性担保

現時点では以下を DB レベルで保証しています。

- `applications.priority` は `0..10`
- `stages` は `(application_id, stage_order)` を一意制約
- `schedules.end_at >= start_at`

### 6. 共通例外応答

エラー応答は `status`, `error`, `code`, `message`, `timestamp`, `errors` 形式に統一しています。

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

- `POST /applications/{applicationId}/stages`
- `GET /applications/{applicationId}/stages`
- `PATCH /stages/{id}`
- `DELETE /stages/{id}`
- `GET /stages/{id}/status-histories`

### Schedules

- `POST /applications/{applicationId}/schedules`
- `GET /applications/{applicationId}/schedules`
- `PATCH /schedules/{id}`
- `DELETE /schedules/{id}`

### Notes

- `POST /applications/{applicationId}/notes`
- `GET /applications/{applicationId}/notes`
- `PATCH /notes/{id}`
- `DELETE /notes/{id}`

---

## ローカル実行

### 前提

- Java 17
- Node.js 20 以上
- Docker

### 1. PostgreSQL を起動

```powershell
docker run --name postgres-db `
  -e POSTGRES_USER=postgres `
  -e POSTGRES_PASSWORD=1234 `
  -e POSTGRES_DB=jobtracker `
  -p 5432:5432 `
  -d postgres:15
```

すでに作成済みの場合:

```powershell
docker start postgres-db
```

### 2. バックエンド環境変数を設定

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/jobtracker"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="1234"
$env:JWT_SECRET="change-this-secret-key-for-local-environment-only"
```

`.env` 相当の例:

```dotenv
DB_URL=jdbc:postgresql://localhost:5432/jobtracker
DB_USERNAME=postgres
DB_PASSWORD=1234
JWT_SECRET=change-this-secret-key-for-local-environment-only
JWT_ACCESS_TOKEN_EXPIRATION_SECONDS=3600
JWT_TOKEN_TYPE=Bearer
SERVER_PORT=8080
CORS_ALLOWED_ORIGINS=http://localhost:3000
```

### 3. バックエンド起動

```powershell
./gradlew bootRun
```

### 4. フロントエンド起動

```powershell
cd frontend
npm install
npm run dev
```

ブラウザでは以下を開きます。

- バックエンド: `http://localhost:8080`
- フロントエンド: `http://localhost:3000`

---

## 画面確認手順

### 1. ユーザー登録

現時点ではフロントに会員登録画面がないため、最初のユーザー作成は API から行います。

```powershell
curl.exe --% -X POST http://localhost:8080/auth/signup -H "Content-Type: application/json" -d "{\"email\":\"test@example.com\",\"password\":\"password123\",\"name\":\"テストユーザー\"}"
```

### 2. フロントエンドからログイン

- URL: `http://localhost:3000`
- メールアドレス: `test@example.com`
- パスワード: `password123`

### 3. 会社登録

- 画面右上ナビゲーションの `会社登録`
- または `http://localhost:3000/companies/new`

### 4. 応募作成

- `会社登録` 完了後、自動的に応募作成画面へ移動
- または `http://localhost:3000/applications/new`

### 5. 応募詳細確認

- 応募一覧から対象応募を選択
- ステージ / 日程 / メモの追加、修正、削除を確認

---

## テスト

### バックエンド

```powershell
./gradlew test
```

検証している主な内容:

- 認証 API の基本動作
- ユーザー所有データへのアクセス制御
- 例外応答コード
- ステータス履歴生成
- ステータス遷移制御
- DB 制約違反時の応答
- サービス層のドメインルール

### フロントエンド

```powershell
cd frontend
npm run lint
npm run build
```

---

## 現在の到達点

- バックエンド整備は完了
- フロント実装も完了
- Docker / CI / デプロイ / IaC / モニタリングは未着手

---

## 今後の予定

- Dockerfile
- docker-compose
- GitHub Actions CI
- バックエンド / フロントエンドのコンテナイメージ化
- AWS デプロイ
- Terraform 導入
- Actuator / ログ / モニタリング追加
- 最終 README、アーキテクチャ図、トラブルシューティング整理
