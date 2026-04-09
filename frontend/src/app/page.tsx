import { LoginForm } from "@/components/login-form";
import { API_BASE_URL } from "@/lib/api";

export default function Home() {
  return (
    <div className="relative flex min-h-screen items-center justify-center overflow-hidden bg-[var(--page-bg)] px-6 py-16">
      <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_top_left,_rgba(190,218,255,0.85),_transparent_38%),radial-gradient(circle_at_bottom_right,_rgba(255,210,166,0.8),_transparent_32%),linear-gradient(135deg,_#f9f4ea,_#eef6ff_58%,_#f5efe7)]" />

      <main className="relative grid w-full max-w-6xl gap-8 lg:grid-cols-[1.2fr_0.8fr]">
        <section className="rounded-[2rem] border border-white/60 bg-white/80 p-8 shadow-[0_30px_80px_rgba(31,45,61,0.12)] backdrop-blur md:p-12">
          <div className="mb-10 flex items-center gap-3">
            <span className="inline-flex h-3 w-3 rounded-full bg-[var(--accent)]" />
            <p className="text-sm font-semibold uppercase tracking-[0.22em] text-[var(--ink-muted)]">
              Job Application Tracker
            </p>
          </div>

          <div className="grid gap-6">
            <p className="text-sm font-medium text-[var(--accent)]">
              Backend Portfolio Frontend
            </p>
            <h1 className="max-w-2xl text-4xl font-semibold tracking-[-0.04em] text-[var(--ink-strong)] md:text-6xl">
              応募状況を、次の一手が見える形で整理する。
            </h1>
            <p className="max-w-2xl text-base leading-8 text-[var(--ink-soft)] md:text-lg">
              企業、応募、選考ステージ、予定、メモを一つの流れとして扱う Job
              Application Tracker のフロントエンドです。まずは Spring Boot
              バックエンドの `/auth/login` と連携し、ログインから応募一覧確認までを
              最短距離でつなぎます。
            </p>
          </div>

          <div className="mt-10 grid gap-4 md:grid-cols-3">
            <div className="rounded-[1.5rem] border border-[var(--line)] bg-[var(--panel)] p-5">
              <p className="text-xs font-semibold uppercase tracking-[0.18em] text-[var(--ink-muted)]">
                Step 1
              </p>
              <h2 className="mt-3 text-lg font-semibold text-[var(--ink-strong)]">
                ログイン
              </h2>
              <p className="mt-2 text-sm leading-6 text-[var(--ink-soft)]">
                JWT ベースの認証 API と接続し、ユーザー単位のデータへ安全にアクセスします。
              </p>
            </div>
            <div className="rounded-[1.5rem] border border-[var(--line)] bg-[var(--panel)] p-5">
              <p className="text-xs font-semibold uppercase tracking-[0.18em] text-[var(--ink-muted)]">
                Step 2
              </p>
              <h2 className="mt-3 text-lg font-semibold text-[var(--ink-strong)]">
                応募一覧
              </h2>
              <p className="mt-2 text-sm leading-6 text-[var(--ink-soft)]">
                状態、優先度、更新日時をまとめて確認し、今どの応募に向き合うべきかを把握します。
              </p>
            </div>
            <div className="rounded-[1.5rem] border border-[var(--line)] bg-[var(--panel)] p-5">
              <p className="text-xs font-semibold uppercase tracking-[0.18em] text-[var(--ink-muted)]">
                Step 3
              </p>
              <h2 className="mt-3 text-lg font-semibold text-[var(--ink-strong)]">
                ダッシュボード
              </h2>
              <p className="mt-2 text-sm leading-6 text-[var(--ink-soft)]">
                ステータス分布と最近の更新を一画面にまとめ、進行中の応募全体を俯瞰します。
              </p>
            </div>
          </div>
        </section>

        <aside className="rounded-[2rem] border border-[var(--line)] bg-[var(--card)] p-8 shadow-[0_24px_60px_rgba(31,45,61,0.12)] md:p-10">
          <div className="mb-8">
            <p className="text-sm font-semibold uppercase tracking-[0.18em] text-[var(--ink-muted)]">
              Sign in
            </p>
            <h2 className="mt-3 text-3xl font-semibold tracking-[-0.03em] text-[var(--ink-strong)]">
              アカウントに接続
            </h2>
            <p className="mt-3 text-sm leading-7 text-[var(--ink-soft)]">
              現在は <code>{API_BASE_URL}</code> の API に接続します。
            </p>
          </div>

          <LoginForm />

          <div className="mt-8 rounded-[1.5rem] border border-dashed border-[var(--line)] bg-white px-5 py-4">
            <p className="text-xs font-semibold uppercase tracking-[0.18em] text-[var(--ink-muted)]">
              Next work
            </p>
            <p className="mt-2 text-sm leading-6 text-[var(--ink-soft)]">
              次はログイン後の応募一覧、応募詳細、応募作成画面を順に接続します。
            </p>
          </div>
        </aside>
      </main>
    </div>
  );
}
