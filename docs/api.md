# API Reference

Job Application Tracker の主要 API 仕様を整理します。  
すべての業務 API はログイン済みユーザーを前提とし、ユーザー単位でデータを分離します。

## Base URL

ローカル:

```text
http://localhost:8080
```

EC2 検証環境:

```text
http://<EC2_PUBLIC_IP>:8080
```

## 認証

`/auth/signup`, `/auth/login`, `/actuator/health` を除き、API 呼び出しには JWT access token が必要です。

```http
Authorization: Bearer <accessToken>
Content-Type: application/json
```

## 共通エラーレスポンス

```json
{
  "status": 400,
  "error": "Bad Request",
  "code": "INVALID_REQUEST",
  "message": "Request validation failed.",
  "timestamp": "2026-04-13T00:00:00",
  "errors": {
    "field": "validation message"
  }
}
```

主なエラーコード:

- `INVALID_REQUEST`: 入力値不正、状態遷移不正、JSON 形式不正
- `UNAUTHORIZED`: 認証情報なし、または無効な認証情報
- `RESOURCE_NOT_FOUND`: 対象リソースが存在しない、または現在ユーザーの所有リソースではない
- `DUPLICATE_RESOURCE`: 一意制約に違反する登録
- `DATA_INTEGRITY_VIOLATION`: DB 制約違反
- `INTERNAL_SERVER_ERROR`: 想定外のサーバーエラー

## Enum

### ApplicationStatus

```text
NOT_STARTED
APPLICATION
INFO_SESSION
DOCUMENT_SCREENING
TEST
CASUAL_MEETING
INTERVIEW
OFFERED
REJECTED
```

### StageStatus

```text
PENDING
SCHEDULED
COMPLETED
PASSED
FAILED
```

### StageType

```text
INFO_SESSION
DOCUMENT_SCREENING
CODING_TEST
APTITUDE_TEST
FIRST_CASUAL_MEETING
SECOND_CASUAL_MEETING
THIRD_CASUAL_MEETING
FIRST_INTERVIEW
SECOND_INTERVIEW
THIRD_INTERVIEW
FOURTH_INTERVIEW
FINAL_INTERVIEW
OTHER
```

### ScheduleType

```text
DEADLINE
EVENT
RESULT_ANNOUNCEMENT
OTHER
```

### NoteType

```text
UNSPECIFIED
PREPARATION
ACTUAL_CONTENT
REVIEW
OTHER
```

---

## Auth

### POST `/auth/signup`

ユーザーを登録します。

Request:

```json
{
  "email": "test@example.com",
  "password": "password123",
  "name": "テストユーザー"
}
```

Response `201 Created`:

```json
{
  "userId": 1,
  "email": "test@example.com",
  "name": "テストユーザー",
  "accessToken": null,
  "tokenType": null,
  "expiresIn": null
}
```

### POST `/auth/login`

ログインし、JWT access token を取得します。

Request:

```json
{
  "email": "test@example.com",
  "password": "password123"
}
```

Response `200 OK`:

```json
{
  "userId": 1,
  "email": "test@example.com",
  "name": "テストユーザー",
  "accessToken": "<JWT_ACCESS_TOKEN>",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

### GET `/auth/me`

現在ログイン中のユーザー情報を取得します。

Response `200 OK`:

```json
{
  "userId": 1,
  "email": "test@example.com",
  "name": "テストユーザー",
  "accessToken": null,
  "tokenType": null,
  "expiresIn": null
}
```

---

## Companies

### POST `/companies`

企業を登録します。

Request:

```json
{
  "name": "OpenAI",
  "industry": "AI",
  "websiteUrl": "https://openai.com",
  "memo": "志望度高め"
}
```

Response `201 Created`:

```json
{
  "id": 1,
  "name": "OpenAI",
  "industry": "AI",
  "websiteUrl": "https://openai.com",
  "memo": "志望度高め",
  "createdAt": "2026-04-13T00:00:00",
  "updatedAt": "2026-04-13T00:00:00"
}
```

### GET `/companies`

現在ユーザーの企業一覧を取得します。

Response `200 OK`:

```json
[
  {
    "id": 1,
    "name": "OpenAI",
    "industry": "AI",
    "websiteUrl": "https://openai.com",
    "memo": "志望度高め",
    "createdAt": "2026-04-13T00:00:00",
    "updatedAt": "2026-04-13T00:00:00"
  }
]
```

### GET `/companies/{id}`

指定した企業を取得します。

### PATCH `/companies/{id}`

企業情報を部分更新します。

Request:

```json
{
  "memo": "カジュアル面談後に再確認"
}
```

### DELETE `/companies/{id}`

企業を削除します。

Response:

```text
204 No Content
```

---

## Applications

### POST `/applications`

応募情報を登録します。

Request:

```json
{
  "companyId": 1,
  "jobTitle": "Backend Engineer",
  "applicationRoute": "Wantedly",
  "status": "APPLICATION",
  "appliedAt": "2026-04-13",
  "resultDate": null,
  "offerDeadline": null,
  "priority": 8,
  "isArchived": false
}
```

Response `201 Created`:

```json
{
  "id": 1,
  "companyId": 1,
  "jobTitle": "Backend Engineer",
  "applicationRoute": "Wantedly",
  "status": "APPLICATION",
  "appliedAt": "2026-04-13",
  "resultDate": null,
  "offerDeadline": null,
  "priority": 8,
  "isArchived": false,
  "createdAt": "2026-04-13T00:00:00",
  "updatedAt": "2026-04-13T00:00:00"
}
```

### GET `/applications`

現在ユーザーの応募一覧を、検索条件とページング条件つきで取得します。

Query Parameters:

| Name | Type | Required | Description |
| --- | --- | --- | --- |
| `status` | `ApplicationStatus` | no | 応募ステータスで絞り込みます。 |
| `isArchived` | `boolean` | no | `true` でアーカイブ済み、`false` で未アーカイブのみを取得します。未指定の場合は両方を含みます。 |
| `keyword` | `string` | no | 会社名または職種名を部分一致で検索します。 |
| `page` | `number` | no | 0始まりのページ番号です。デフォルトは `0` です。 |
| `size` | `number` | no | 1ページあたりの件数です。デフォルトは `20`、最大は `100` です。 |

Example:

```http
GET /applications?status=INTERVIEW&isArchived=false&keyword=backend&page=0&size=10
```

Response `200 OK`:

```json
{
  "content": [
    {
      "id": 1,
      "companyId": 1,
      "jobTitle": "Backend Engineer",
      "applicationRoute": "Wantedly",
      "status": "INTERVIEW",
      "appliedAt": "2026-04-13",
      "resultDate": null,
      "offerDeadline": null,
      "priority": 8,
      "isArchived": false,
      "createdAt": "2026-04-13T00:00:00",
      "updatedAt": "2026-04-13T00:00:00"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

### GET `/applications/{id}`

指定した応募情報を取得します。

### PATCH `/applications/{id}`

応募情報を部分更新します。  
`status` を変更した場合、許可された状態遷移であれば `application_status_histories` が自動作成されます。

Request:

```json
{
  "status": "INTERVIEW",
  "priority": 9
}
```

### DELETE `/applications/{id}`

応募情報を削除します。

Response:

```text
204 No Content
```

### GET `/applications/{applicationId}/status-histories`

応募ステータス変更履歴を取得します。

Response `200 OK`:

```json
[
  {
    "id": 1,
    "applicationId": 1,
    "fromStatus": "APPLICATION",
    "toStatus": "INTERVIEW",
    "changedAt": "2026-04-13T00:00:00"
  }
]
```

---

## Stages

### POST `/applications/{applicationId}/stages`

応募に選考ステージを追加します。

Request:

```json
{
  "stageOrder": 1,
  "stageType": "FIRST_INTERVIEW",
  "stageName": "一次面接",
  "status": "SCHEDULED",
  "scheduledAt": "2026-04-20T10:00:00",
  "completedAt": null,
  "resultDate": null,
  "memo": "オンライン面接"
}
```

Response `201 Created`:

```json
{
  "id": 1,
  "applicationId": 1,
  "stageOrder": 1,
  "stageType": "FIRST_INTERVIEW",
  "stageName": "一次面接",
  "status": "SCHEDULED",
  "scheduledAt": "2026-04-20T10:00:00",
  "completedAt": null,
  "resultDate": null,
  "memo": "オンライン面接",
  "createdAt": "2026-04-13T00:00:00",
  "updatedAt": "2026-04-13T00:00:00"
}
```

### GET `/applications/{applicationId}/stages`

応募に紐づく選考ステージ一覧を取得します。

### PATCH `/stages/{id}`

選考ステージを部分更新します。  
`status` を変更した場合、許可された状態遷移であれば `stage_status_histories` が自動作成されます。

Request:

```json
{
  "status": "COMPLETED",
  "completedAt": "2026-04-20T11:00:00"
}
```

### DELETE `/stages/{id}`

選考ステージを削除します。

Response:

```text
204 No Content
```

### GET `/stages/{stageId}/status-histories`

ステージステータス変更履歴を取得します。

Response `200 OK`:

```json
[
  {
    "id": 1,
    "stageId": 1,
    "fromStatus": "SCHEDULED",
    "toStatus": "COMPLETED",
    "changedAt": "2026-04-13T00:00:00"
  }
]
```

---

## Schedules

### POST `/applications/{applicationId}/schedules`

応募に日程を追加します。

Request:

```json
{
  "stageId": 1,
  "scheduleType": "EVENT",
  "title": "一次面接",
  "description": "オンライン面接",
  "startAt": "2026-04-20T10:00:00",
  "endAt": "2026-04-20T11:00:00",
  "location": "Google Meet",
  "isAllDay": false
}
```

### GET `/applications/{applicationId}/schedules`

応募に紐づく日程一覧を取得します。

### PATCH `/schedules/{id}`

日程を部分更新します。

### DELETE `/schedules/{id}`

日程を削除します。

Response:

```text
204 No Content
```

---

## Notes

### POST `/applications/{applicationId}/notes`

応募にメモを追加します。

Request:

```json
{
  "title": "面接準備",
  "noteType": "PREPARATION",
  "content": "事業内容、技術スタック、想定質問を整理する。"
}
```

### GET `/applications/{applicationId}/notes`

応募に紐づくメモ一覧を取得します。

### PATCH `/notes/{id}`

メモを部分更新します。

Request:

```json
{
  "noteType": "REVIEW",
  "content": "回答内容と改善点を追記する。"
}
```

### DELETE `/notes/{id}`

メモを削除します。

Response:

```text
204 No Content
```

---

## Actuator

### GET `/actuator/health`

ヘルスチェックを取得します。local / prod の両方で公開します。

Response `200 OK`:

```json
{
  "status": "UP",
  "groups": [
    "liveness",
    "readiness"
  ]
}
```

### GET `/actuator/info`

アプリケーション情報を取得します。local profile で公開します。

### GET `/actuator/prometheus`

Prometheus scrape 用メトリクスを取得します。local profile で公開します。
