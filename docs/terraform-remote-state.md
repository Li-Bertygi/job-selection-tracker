# Terraform Remote State

Terraform state はローカルファイルではなく、S3 backend と DynamoDB lock table で管理できる構成を用意しています。

## 目的

- Terraform state をローカル PC に依存させない
- state file の versioning と server-side encryption を有効化する
- DynamoDB による state lock で同時実行による破損を防ぐ
- `dev` 検証インフラと `github-oidc` 認証基盤を別 state として管理する

## 構成

```text
terraform/envs/remote-state
  - S3 bucket
  - S3 bucket versioning
  - S3 server-side encryption
  - S3 public access block
  - DynamoDB lock table

terraform/envs/dev
  - backend.tf.example
  - backend.hcl.example

terraform/envs/github-oidc
  - backend.tf.example
  - backend.hcl.example
```

## 1. Remote state 用リソースを作成

`terraform/envs/remote-state/terraform.tfvars` を作成します。

```hcl
aws_region   = "ap-northeast-1"
project_name = "job-selection-tracker"
environment  = "shared"

state_bucket_name = "job-selection-tracker-terraform-state-<AWS_ACCOUNT_ID>-ap-northeast-1"
lock_table_name   = "job-selection-tracker-terraform-locks"
```

S3 bucket 名はグローバルで一意である必要があります。

作成:

```powershell
cd terraform/envs/remote-state
terraform init
terraform plan
terraform apply
```

## 2. 各 env の backend を有効化

例: `terraform/envs/dev`

```powershell
cd terraform/envs/dev
Copy-Item backend.tf.example backend.tf
Copy-Item backend.hcl.example backend.hcl
```

`backend.hcl` の bucket 名を、remote-state stack で作成した bucket 名に合わせます。

```hcl
bucket         = "job-selection-tracker-terraform-state-<AWS_ACCOUNT_ID>-ap-northeast-1"
key            = "job-selection-tracker/dev/terraform.tfstate"
region         = "ap-northeast-1"
dynamodb_table = "job-selection-tracker-terraform-locks"
encrypt        = true
```

`terraform/envs/github-oidc` も同様に `backend.tf` と `backend.hcl` を作成します。

```hcl
key = "job-selection-tracker/github-oidc/terraform.tfstate"
```

## 3. State を remote backend へ移行

既存の local state がある場合:

```powershell
terraform init -backend-config=backend.hcl -migrate-state
```

新規に remote backend を使い始める場合:

```powershell
terraform init -backend-config=backend.hcl
```

## 注意

- `backend.tf` と `backend.hcl` は環境ごとの実設定です。bucket 名や account ID を含むため、必要に応じて repository 管理対象から外します。
- `terraform/envs/remote-state` 自体は remote backend の bootstrap 用 stack です。最初の作成時は local state で実行します。
- GitHub Actions OIDC 用 stack は `terraform/envs/github-oidc` として dev 環境から分離しているため、`terraform/envs/dev` を destroy しても deploy 用 IAM Role は削除されません。
