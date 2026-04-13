# Troubleshooting

AWS EC2 への初回デプロイ検証および Terraform 検証時に発生した問題と対応を整理します。  
同じ構成を再現する際に詰まりやすいポイントを残すことを目的としています。

---

## 1. GitHub Actions から EC2 への SSH 接続が timeout になる

### 症状

GitHub Actions の `Copy deployment files to EC2` ステップで以下のエラーが発生しました。

```text
dial tcp ***:22: i/o timeout
```

### 原因

EC2 の Security Group で SSH `22` 番ポートを自分の IP のみに制限していたため、GitHub-hosted runner から EC2 に接続できませんでした。

### 対応

初回デプロイ検証時のみ、Security Group の SSH ルールを一時的に開放しました。

```text
SSH / TCP 22 / 0.0.0.0/0
```

デプロイ検証後は、すぐに以下へ戻しました。

```text
SSH / TCP 22 / My IP
```

### 補足

`22 / 0.0.0.0/0` は長時間開けたままにしない方針です。  
将来的には GitHub Actions から SSH ではなく、AWS Systems Manager Run Command 経由でデプロイする構成に変更する予定です。

今回の検証では、GitHub Actions OIDC による ECR push までは成功しており、失敗箇所は EC2 への SCP 接続でした。
Security Group の SSH inbound を一時的に開放した後、`Copy deployment files to EC2` と `Deploy on EC2` まで成功しました。

---

## 2. EC2 上で `docker: command not found` が発生する

### 症状

GitHub Actions の `Deploy on EC2` ステップで以下のエラーが発生しました。

```text
docker: command not found
```

### 原因

EC2 インスタンスに Docker がインストールされていませんでした。

### 対応

EC2 に SSH 接続し、Docker、Docker Compose plugin、AWS CLI をインストールしました。

```bash
sudo dnf update -y
sudo dnf install -y docker awscli
sudo systemctl enable --now docker
sudo usermod -aG docker ec2-user
```

Docker Compose plugin:

```bash
sudo mkdir -p /usr/local/lib/docker/cli-plugins
sudo curl -SL https://github.com/docker/compose/releases/download/v2.29.7/docker-compose-linux-x86_64 -o /usr/local/lib/docker/cli-plugins/docker-compose
sudo chmod +x /usr/local/lib/docker/cli-plugins/docker-compose
```

確認:

```bash
docker --version
docker compose version
aws --version
```

`ec2-user` の Docker 権限を反映するため、設定後に一度ログアウトして再接続しました。

---

## 3. EC2 上で `Unable to locate credentials` が発生する

### 症状

EC2 上で ECR ログインを実行した際に以下のエラーが発生しました。

```text
Unable to locate credentials
```

### 原因

GitHub Actions 側には AWS credentials を設定していましたが、EC2 インスタンス自体には ECR へアクセスする IAM Role が付与されていませんでした。

### 対応

EC2 用 IAM Role を作成し、対象インスタンスに付与しました。

付与したポリシー:

```text
AmazonEC2ContainerRegistryReadOnly
AmazonSSMManagedInstanceCore
```

Role 名:

```text
job-selection-tracker-ec2-role
```

EC2 へ Role を付与した後、以下で確認できます。

```bash
aws sts get-caller-identity
```

---

## 4. Windows から SSH 接続時に `.pem` の権限エラーが出る

### 症状

Windows PowerShell から SSH 接続した際、以下のエラーが発生しました。

```text
WARNING: UNPROTECTED PRIVATE KEY FILE!
Permissions for '.pem' are too open.
This private key will be ignored.
```

### 原因

`.pem` ファイルの権限が広すぎたため、OpenSSH が秘密鍵の使用を拒否しました。

### 対応

PowerShell で `.pem` ファイルの権限を現在のユーザーのみに制限しました。

```powershell
icacls "D:\job-selection-tracker-key.pem" /inheritance:r
icacls "D:\job-selection-tracker-key.pem" /remove "Authenticated Users"
icacls "D:\job-selection-tracker-key.pem" /remove "Users"
icacls "D:\job-selection-tracker-key.pem" /grant:r "${env:USERNAME}:R"
```

その後、以下で SSH 接続できました。

```powershell
ssh -i "D:\job-selection-tracker-key.pem" ec2-user@<EC2_PUBLIC_IP>
```

---

## 5. ローカルで `docker-compose.prod.yml` を実行して `.env` が見つからない

### 症状

ローカル PC で以下を実行した際に `.env` が見つからないエラーが発生しました。

```powershell
docker compose -f docker-compose.prod.yml --env-file .env ps
```

### 原因

`docker-compose.prod.yml` と `.env` は GitHub Actions によって EC2 上の `~/job-selection-tracker` に配置されるファイルです。  
ローカル PC のプロジェクトルートに存在する前提ではありません。

### 対応

EC2 に SSH 接続した上で、EC2 内で以下を実行します。

```bash
cd ~/job-selection-tracker
docker compose -f docker-compose.prod.yml --env-file .env ps
```

---

## 6. GitHub Secrets の入力単位を間違える

### 症状

GitHub Secrets 登録時に以下のようなエラーが表示されました。

```text
Secret names can only contain alphanumeric characters or underscores.
```

### 原因

`Name` に `AWS_REGION=ap-northeast-1` のように、名前と値をまとめて入力していました。

### 対応

GitHub Secrets は `Name` と `Secret` を分けて登録します。

例:

```text
Name: AWS_REGION
Secret: ap-northeast-1
```

---

## 7. Terraform が AWS credentials を見つけられない

### 症状

ローカルで `terraform plan` または `terraform destroy` を実行した際、以下のエラーが発生しました。

```text
No valid credential sources found
```

### 原因

GitHub Actions のデプロイは OIDC で AWS IAM Role を assume しますが、ローカル PowerShell から Terraform を実行する場合は別途 AWS 認証情報が必要です。
この時点では Terraform 用の AWS 環境変数がローカルセッションに設定されていませんでした。

### 対応

同じ PowerShell セッションで以下を設定してから Terraform を実行しました。

```powershell
$env:AWS_ACCESS_KEY_ID="..."
$env:AWS_SECRET_ACCESS_KEY="..."
$env:AWS_REGION="ap-northeast-1"
```

PowerShell セッションを閉じると環境変数は消えるため、別セッションで実行する場合は再設定が必要です。

---

## 8. Terraform 実行時に `ssm:GetParameter` 権限不足になる

### 症状

Terraform が Amazon Linux 2023 の AMI ID を取得する際、以下のエラーが発生しました。

```text
not authorized to perform: ssm:GetParameter
```

### 原因

Terraform コードで SSM Parameter Store から Amazon Linux 2023 AMI を取得していますが、実行ユーザーに SSM Parameter 参照権限がありませんでした。

対象:

```hcl
data "aws_ssm_parameter" "al2023_ami"
```

### 対応

Terraform 実行ユーザーに以下の権限を追加しました。

```text
AmazonSSMReadOnlyAccess
```

---

## 9. Terraform 実行時に `iam:CreateRole` 権限不足になる

### 症状

`terraform apply` 実行時、IAM Role 作成で以下のエラーが発生しました。

```text
not authorized to perform: iam:CreateRole
```

また、`terraform destroy` 実行時にも IAM Role / Role Policy の状態確認で以下のエラーが発生しました。

```text
not authorized to perform: iam:GetRole
not authorized to perform: iam:GetRolePolicy
```

### 原因

Terraform が EC2 用 IAM Role / Instance Profile / Policy Attachment、および GitHub Actions OIDC 用 IAM Role / Role Policy を作成・参照・削除する構成でしたが、実行ユーザーに IAM リソース操作権限がありませんでした。

Terraform の `destroy` は削除だけでなく、削除前の refresh のために `GetRole` や `GetRolePolicy` などの参照権限も必要です。

### 対応

Terraform 検証中のみ、実行ユーザーに一時的に以下を付与しました。

```text
IAMFullAccess
```

`terraform apply` と `terraform destroy` の完了後、この強い権限は削除対象としました。

---

## 10. GitHub Actions OIDC 設定後も AWS credentials が読み込めない

### 症状

GitHub Actions の `Configure AWS credentials` ステップで以下のエラーが発生しました。

```text
Credentials could not be loaded, please check your action inputs:
Could not load credentials from any providers
```

### 原因

ローカルでは `deploy-ec2.yml` を OIDC 方式へ変更していましたが、GitHub の `main` branch にまだ push されていませんでした。
そのため、GitHub Actions 上では古い access key 方式の workflow が実行されていました。

古い設定:

```yaml
with:
  aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
  aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
  aws-region: ${{ secrets.AWS_REGION }}
```

この状態で GitHub Secrets から `AWS_ACCESS_KEY_ID` / `AWS_SECRET_ACCESS_KEY` を削除したため、AWS credentials を読み込めませんでした。

### 対応

`deploy-ec2.yml` を以下の OIDC Role assume 方式へ変更し、commit / push してから workflow を再実行しました。

```yaml
permissions:
  contents: read
  id-token: write
```

```yaml
with:
  role-to-assume: ${{ secrets.AWS_ROLE_TO_ASSUME }}
  aws-region: ${{ secrets.AWS_REGION }}
```

GitHub Secrets には以下を登録しました。

```text
Name: AWS_ROLE_TO_ASSUME
Secret: arn:aws:iam::<AWS_ACCOUNT_ID>:role/job-selection-tracker-prod-github-actions-deploy-role
```

### 補足

GitHub Actions はローカルファイルではなく、GitHub repository に push 済みの workflow ファイルを実行します。
workflow を変更した場合は、commit / push 後に GitHub 上の `.github/workflows/deploy-ec2.yml` が更新されていることを確認してから手動実行します。

---

## 11. OIDC Role が dev 環境の destroy で削除される

### 症状

`terraform/envs/dev` に GitHub Actions OIDC Role を含めた状態で `terraform destroy` を実行すると、以下の Role も削除対象になりました。

```text
job-selection-tracker-tf-dev-github-actions-role
```

その結果、GitHub Secret `AWS_ROLE_TO_ASSUME` が既存の ARN を指していても、AWS 側の Role が存在しない状態になります。

### 原因

dev 検証用インフラと、GitHub Actions のデプロイ認証基盤を同じ Terraform state で管理していたためです。
EC2 / ECR / Security Group は検証後に destroy したい一方で、OIDC Provider / IAM Role はデプロイ基盤として維持する必要があります。

### 対応

OIDC 関連リソースを `terraform/envs/github-oidc` に分離しました。

```text
terraform/envs/github-oidc
  - GitHub Actions OIDC Provider
  - GitHub Actions deploy IAM Role
  - ECR push policy

terraform/envs/dev
  - ECR repository
  - EC2
  - Security Group
  - EC2 IAM Role / Instance Profile
```

`terraform/envs/github-oidc` は維持する stack とし、`terraform/envs/dev` は apply / destroy 検証用として扱います。

OIDC stack の適用:

```powershell
cd terraform/envs/github-oidc
terraform init
terraform apply
terraform output -raw github_actions_role_arn
```

出力された ARN を GitHub Secret `AWS_ROLE_TO_ASSUME` に登録します。

---

## 12. Remote state 用 S3 bucket 名が長すぎる

### 症状

`terraform/envs/remote-state` で `terraform plan` を実行した際、S3 bucket 名の length validation で失敗しました。

```text
expected length of bucket to be in the range (0 - 63)
```

### 原因

初期値として `<AWS_ACCOUNT_ID>` を含む placeholder のまま bucket 名を使っていたため、S3 bucket 名の最大長を超えていました。

問題の値:

```text
job-selection-tracker-terraform-state-<AWS_ACCOUNT_ID>-ap-northeast-1
```

### 対応

実際の AWS Account ID を含めた短い bucket 名に変更しました。

```hcl
state_bucket_name = "jst-tfstate-<AWS_ACCOUNT_ID>-apne1"
```

S3 bucket 名はグローバルに一意である必要があるため、プロジェクト名、用途、Account ID、region の短縮表記を組み合わせています。

---

## 13. Remote state 作成時に S3 / DynamoDB 権限不足になる

### 症状

`terraform/envs/remote-state` で `terraform apply` を実行した際、以下のエラーが発生しました。

```text
not authorized to perform: s3:CreateBucket
not authorized to perform: dynamodb:CreateTable
```

### 原因

Terraform 実行ユーザー `github-actions-deployer` に、remote state bootstrap 用の S3 bucket と DynamoDB lock table を作成する権限がありませんでした。

GitHub Actions のデプロイは OIDC Role で実行しますが、ローカル PowerShell から remote state 基盤を作成する場合は、ローカル Terraform 実行ユーザーに bootstrap 用の権限が必要です。

### 対応

remote state bootstrap 時に必要な S3 / DynamoDB 権限を付与し、以下のリソース作成を確認しました。

```text
Apply complete! Resources: 5 added, 0 changed, 0 destroyed.
```

作成したリソース:

- S3 bucket
- S3 bucket versioning
- S3 bucket server-side encryption
- S3 bucket public access block
- DynamoDB lock table

補足:

この権限は GitHub Actions のデプロイ用 OIDC Role とは別です。
ローカルで Terraform bootstrap / migration を行う管理ユーザーにだけ必要です。

---

## 14. S3 backend migration 時に state object へ 403 になる

### 症状

`terraform/envs/dev` を S3 backend に切り替える際、以下のエラーが発生しました。

```text
Unable to access object "job-selection-tracker/dev/terraform.tfstate"
in S3 bucket "jst-tfstate-<AWS_ACCOUNT_ID>-apne1"
StatusCode: 403
```

また、`terraform/envs/github-oidc` の remote state migration 後に `terraform plan` を実行した際、IAM OIDC Provider の参照で以下のエラーが発生しました。

```text
not authorized to perform: iam:GetOpenIDConnectProvider
```

### 原因

Terraform の S3 backend は、state object に対する `s3:GetObject` / `s3:PutObject` / `s3:DeleteObject` と bucket list 権限を必要とします。
また、`terraform plan` は既存リソースを refresh するため、IAM Role / OIDC Provider / Role Policy の参照権限も必要です。

`dev` と `github-oidc` は state key が異なるため、それぞれの prefix に対する S3 権限が必要でした。

```text
job-selection-tracker/dev/terraform.tfstate
job-selection-tracker/github-oidc/terraform.tfstate
```

### 対応

Terraform 実行ユーザーに、remote state 参照・更新と DynamoDB lock 用の権限を追加しました。

ポリシー名:

```text
job-selection-tracker-terraform-state-access
```

また、`github-oidc` stack の refresh に必要な IAM read 権限を追加しました。

確認結果:

`github-oidc`:

```powershell
terraform init -backend-config="backend.hcl" -migrate-state
terraform state list
terraform plan
```

```text
No changes. Your infrastructure matches the configuration.
```

`dev`:

```powershell
terraform init -backend-config="backend.hcl" -migrate-state
terraform plan
```

```text
Plan: 8 to add, 0 to change, 0 to destroy.
```

`dev` は検証後に destroy 済みのため、作成予定リソースが表示されるのは正常です。

### 補足

`terraform init` の `-migrate-state` と `-reconfigure` は同時に指定できません。

```text
The -migrate-state and -reconfigure options are mutually-exclusive.
```

local state を S3 backend へ移行する場合は、まず以下を使用します。

```powershell
terraform init -backend-config="backend.hcl" -migrate-state
```

backend 設定だけを再読み込みしたい場合に限り、`-reconfigure` を使います。

---

## 15. 確認済みの正常状態

### AWS EC2 デプロイ

GitHub Actions の `deploy-ec2.yml` を手動実行し、`Build, Push, and Deploy` job が成功することを確認しました。

確認済みの主なステップ:

```text
Configure AWS credentials
Login to Amazon ECR
Build and push backend image
Build and push frontend image
Copy deployment files to EC2
Deploy on EC2
```

フロントエンド:

```text
http://<EC2_PUBLIC_IP>:3000
```

バックエンドヘルスチェック:

```text
http://<EC2_PUBLIC_IP>:8080/actuator/health
```

レスポンス:

```json
{
  "status": "UP",
  "groups": [
    "liveness",
    "readiness"
  ]
}
```

### Terraform 検証

以下を確認しました。

```text
terraform plan
terraform apply
terraform destroy
```

検証用リソースは `job-selection-tracker-tf-*` の名前で作成し、検証後に `terraform destroy` で削除しました。

GitHub Actions OIDC 用の永続リソースは、dev 検証環境とは別に `terraform/envs/github-oidc` で管理します。

Terraform remote state については、以下を確認しました。

```text
remote-state apply: Resources: 5 added
github-oidc plan: No changes
dev plan: Plan: 8 to add, 0 to change, 0 to destroy
```

`dev plan` は、dev 検証リソースを destroy 済みの状態で remote backend 経由の plan が正常実行できることを確認したものです。
