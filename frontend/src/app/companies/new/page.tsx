"use client";

import { FormEvent, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { AppShell } from "@/components/app-shell";
import { RequireAuth } from "@/components/require-auth";
import { ApiError, createCompany } from "@/lib/api";

type FormState = {
  name: string;
  industry: string;
  websiteUrl: string;
  memo: string;
};

const initialState: FormState = {
  name: "",
  industry: "",
  websiteUrl: "",
  memo: "",
};

export default function NewCompanyPage() {
  return (
    <RequireAuth>
      {(session) => (
        <NewCompanyScreen accessToken={session.accessToken} tokenType={session.tokenType} />
      )}
    </RequireAuth>
  );
}

function NewCompanyScreen({
  accessToken,
  tokenType,
}: {
  accessToken: string;
  tokenType: string;
}) {
  const router = useRouter();
  const [form, setForm] = useState<FormState>(initialState);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setErrorMessage(null);
    setSuccessMessage(null);
    setIsSubmitting(true);

    try {
      const created = await createCompany(
        {
          name: form.name,
          industry: form.industry || null,
          websiteUrl: form.websiteUrl || null,
          memo: form.memo || null,
        },
        accessToken,
        tokenType
      );

      setSuccessMessage("会社を登録しました。応募作成画面へ移動します。");
      router.push(`/applications/new?companyId=${created.id}`);
    } catch (error) {
      if (error instanceof ApiError) {
        setErrorMessage(error.message);
      } else {
        setErrorMessage("会社登録中にエラーが発生しました。");
      }
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <AppShell
      title="会社登録"
      description="応募前に会社情報を登録しておくと、応募作成時の選択と管理が楽になります。"
    >
      <section className="rounded-[2rem] border border-[var(--line)] bg-[var(--card)] p-6 shadow-[0_24px_60px_rgba(31,45,61,0.08)]">
        <div className="flex items-center justify-between gap-4">
          <div>
            <p className="text-sm font-semibold uppercase tracking-[0.18em] text-[var(--ink-muted)]">
              Create
            </p>
            <h2 className="mt-2 text-2xl font-semibold tracking-[-0.03em] text-[var(--ink-strong)]">
              会社情報を追加
            </h2>
          </div>
          <Link
            href="/applications/new"
            className="inline-flex rounded-full border border-[var(--line)] bg-white px-4 py-2 text-sm font-medium text-[var(--ink-soft)] transition hover:border-[var(--accent)] hover:text-[var(--ink-strong)]"
          >
            応募作成へ
          </Link>
        </div>

        <form className="mt-6 grid gap-5" onSubmit={handleSubmit}>
          <div className="grid gap-5 md:grid-cols-2">
            <label className="grid gap-2 text-sm font-medium text-[var(--ink-strong)]">
              会社名
              <input
                required
                value={form.name}
                onChange={(event) =>
                  setForm((current) => ({ ...current, name: event.target.value }))
                }
                className="h-12 rounded-2xl border border-[var(--line)] bg-white px-4 text-sm outline-none focus:border-[var(--accent)]"
                placeholder="OpenAI"
              />
            </label>

            <label className="grid gap-2 text-sm font-medium text-[var(--ink-strong)]">
              業界
              <input
                value={form.industry}
                onChange={(event) =>
                  setForm((current) => ({ ...current, industry: event.target.value }))
                }
                className="h-12 rounded-2xl border border-[var(--line)] bg-white px-4 text-sm outline-none focus:border-[var(--accent)]"
                placeholder="AI"
              />
            </label>

            <label className="grid gap-2 text-sm font-medium text-[var(--ink-strong)] md:col-span-2">
              Webサイト
              <input
                value={form.websiteUrl}
                onChange={(event) =>
                  setForm((current) => ({ ...current, websiteUrl: event.target.value }))
                }
                className="h-12 rounded-2xl border border-[var(--line)] bg-white px-4 text-sm outline-none focus:border-[var(--accent)]"
                placeholder="https://openai.com"
              />
            </label>

            <label className="grid gap-2 text-sm font-medium text-[var(--ink-strong)] md:col-span-2">
              メモ
              <textarea
                value={form.memo}
                onChange={(event) =>
                  setForm((current) => ({ ...current, memo: event.target.value }))
                }
                className="min-h-32 rounded-[1.5rem] border border-[var(--line)] bg-white px-4 py-3 text-sm outline-none focus:border-[var(--accent)]"
                placeholder="志望度や企業研究メモ"
              />
            </label>
          </div>

          {errorMessage ? (
            <div className="rounded-[1.5rem] border border-[var(--danger-line)] bg-[var(--danger-bg)] px-5 py-4 text-sm text-[var(--danger-ink)]">
              {errorMessage}
            </div>
          ) : null}

          {successMessage ? (
            <div className="rounded-[1.5rem] border border-[var(--success-line)] bg-[var(--success-bg)] px-5 py-4 text-sm text-[var(--success-ink)]">
              {successMessage}
            </div>
          ) : null}

          <div className="flex justify-end">
            <button
              type="submit"
              disabled={isSubmitting}
              className="inline-flex h-12 items-center justify-center rounded-full bg-[var(--ink-strong)] px-6 text-sm font-semibold text-white transition hover:bg-black disabled:cursor-not-allowed disabled:opacity-60"
            >
              {isSubmitting ? "登録中..." : "会社を登録"}
            </button>
          </div>
        </form>
      </section>
    </AppShell>
  );
}
