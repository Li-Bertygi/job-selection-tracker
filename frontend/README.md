# Frontend

Next.js ベースのフロントエンドです。  
バックエンド API と接続し、ログイン、応募一覧、応募詳細、会社登録、応募作成までを確認できます。

## 前提

- Node.js 20 以上
- バックエンドが `http://localhost:8080` で起動していること

## 起動方法

```powershell
npm install
npm run dev
```

ブラウザ:

- `http://localhost:3000`

## 利用できる画面

- `/`
  - ログイン
- `/applications`
  - 応募一覧
- `/applications/new`
  - 応募作成
- `/applications/[id]`
  - 応募詳細
- `/companies/new`
  - 会社登録

## 検証

```powershell
npm run lint
npm run build
```
