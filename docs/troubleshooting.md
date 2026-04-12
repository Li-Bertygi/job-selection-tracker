# Troubleshooting

AWS EC2 への初回デプロイ検証時に発生した問題と対応を整理します。  
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

## 7. 確認済みの正常状態

### フロントエンド

```text
http://<EC2_PUBLIC_IP>:3000
```

ブラウザからアクセスできることを確認しました。

### バックエンドヘルスチェック

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
