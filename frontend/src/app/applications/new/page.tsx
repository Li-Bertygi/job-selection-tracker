"use client";

import { FormEvent, useEffect, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { AppShell } from "@/components/app-shell";
import { RequireAuth } from "@/components/require-auth";
import {
  ApiError,
  createApplication,
  getCompanies,
  type ApplicationStatus,
  type CompanyResponse,
} from "@/lib/api";

const statuses: Array<{ value: ApplicationStatus; label: string }> = [
  { value: "NOT_STARTED", label: "未着手" },
  { value: "APPLICATION", label: "応募済み" },
  { value: "INFO_SESSION", label: "説明会" },
  { value: "DOCUMENT_SCREENING", label: "書類選考" },
  { value: "TEST", label: "テスト" },
  { value: "CASUAL_MEETING", label: "カジュアル面談" },
  { value: "INTERVIEW", label: "面接" },
  { value: "OFFERED", label: "内定" },
  { value: "REJECTED", label: "不採用" },
];

type FormState = {
  companyId: string;
  jobTitle: string;
  applicationRoute: string;
  status: ApplicationStatus;
  appliedAt: string;
  resultDate: string;
  offerDeadline: string;
  priority: string;
};

const initialState: FormState = {
  companyId: "",
  jobTitle: "",
  applicationRoute: "",
  status: "APPLICATION",
  appliedAt: "",
  resultDate: "",
  offerDeadline: "",
  priority: "0",
};

export default function NewApplicationPage() {
  return (
    <RequireAuth>
      {(session) => (
        <NewApplicationScreen accessToken={session.accessToken} tokenType={session.tokenType} />
      )}
    </RequireAuth>
  );
}

function NewApplicationScreen({
  accessToken,
  tokenType,
}: {
  accessToken: string;
  tokenType: string;
}) {
  const router = useRouter();
  const searchParams = useSearchParams();
  const preselectedCompanyId = searchParams.get("companyId") ?? "";

  const [companies, setCompanies] = useState<CompanyResponse[]>([]);
  const [form, setForm] = useState<FormState>({
    ...initialState,
    companyId: preselectedCompanyId,
  });
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isLoadingCompanies, setIsLoadingCompanies] = useState(true);

  useEffect(() => {
    async function loadCompanies() {
      try {
        const response = await getCompanies(accessToken, tokenType);
        setCompanies(response);
      } catch (error) {
        if (error instanceof ApiError) {
          setErrorMessage(error.message);
          return;
        }

        setErrorMessage("会社一覧の取得中にエラーが発生しました。");
      } finally {
        setIsLoadingCompanies(false);
      }
    }

    void loadCompanies();
  }, [accessToken, tokenType]);

  useEffect(() => {
    if (preselectedCompanyId) {
      setForm((current) => ({ ...current, companyId: preselectedCompanyId }));
    }
  }, [preselectedCompanyId]);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setErrorMessage(null);
    setSuccessMessage(null);
    setIsSubmitting(true);

    try {
      const created = await createApplication(
        {
          companyId: Number(form.companyId),
          jobTitle: form.jobTitle,
          applicationRoute: form.applicationRoute || null,
          status: form.status,
          appliedAt: form.appliedAt || null,
          resultDate: form.resultDate || null,
          offerDeadline: form.offerDeadline || null,
          priority: Number(form.priority),
          isArchived: false,
        },
        accessToken,
        tokenType
      );

      setSuccessMessage("応募を作成しました。詳細画面へ移動します。");
      router.push(`/applications/${created.id}`);
    } catch (error) {
      if (error instanceof ApiError) {
        setErrorMessage(error.message);
      } else {
        setErrorMessage("応募作成中にエラーが発生しました。");
      }
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <AppShell
      title="新規応募"
      description="会社を選択し、現在の進行状況や優先度を入力して応募管理を開始します。"
    >
      <section className="rounded-[2rem] border border-[var(--line)] bg-[var(--card)] p-6 shadow-[0_24px_60px_rgba(31,45,61,0.08)]">
        <div className="flex items-center justify-between gap-4">
          <div>
            <p className="text-sm font-semibold uppercase tracking-[0.18em] text-[var(--ink-muted)]">
              Create
            </p>
            <h2 className="mt-2 text-2xl font-semibold tracking-[-0.03em] text-[var(--ink-strong)]">
              応募情報を追加
            </h2>
          </div>
        </div>

        {isLoadingCompanies ? (
          <div className="mt-6 rounded-[1.5rem] border border-dashed border-[var(--line)] bg-white px-5 py-10 text-center text-sm text-[var(--ink-soft)]">
            会社一覧を読み込んでいます...
          </div>
        ) : null}

        {!isLoadingCompanies && companies.length === 0 ? (
          <div className="mt-6 rounded-[1.5rem] border border-dashed border-[var(--line)] bg-white px-5 py-10 text-center">
            <p className="text-lg font-semibold text-[var(--ink-strong)]">
              登録済みの会社がありません。
            </p>
            <p className="mt-2 text-sm leading-7 text-[var(--ink-soft)]">
              先に会社登録画面から会社を追加してください。
            </p>
          </div>
        ) : null}

        {!isLoadingCompanies && companies.length > 0 ? (
          <form className="mt-6 grid gap-5" onSubmit={handleSubmit}>
            <div className="grid gap-5 md:grid-cols-2">
              <label className="grid gap-2 text-sm font-medium text-[var(--ink-strong)]">
                会社
                <select
                  required
                  value={form.companyId}
                  onChange={(event) =>
                    setForm((current) => ({ ...current, companyId: event.target.value }))
                  }
                  className="h-12 rounded-2xl border border-[var(--line)] bg-white px-4 text-sm outline-none focus:border-[var(--accent)]"
                >
                  <option value="">会社を選択してください</option>
                  {companies.map((company) => (
                    <option key={company.id} value={company.id}>
                      {company.name}
                    </option>
                  ))}
                </select>
              </label>

              <label className="grid gap-2 text-sm font-medium text-[var(--ink-strong)]">
                職種名
                <input
                  required
                  value={form.jobTitle}
                  onChange={(event) =>
                    setForm((current) => ({ ...current, jobTitle: event.target.value }))
                  }
                  className="h-12 rounded-2xl border border-[var(--line)] bg-white px-4 text-sm outline-none focus:border-[var(--accent)]"
                  placeholder="Backend Engineer"
                />
              </label>

              <label className="grid gap-2 text-sm font-medium text-[var(--ink-strong)]">
                応募経路
                <input
                  value={form.applicationRoute}
                  onChange={(event) =>
                    setForm((current) => ({ ...current, applicationRoute: event.target.value }))
                  }
                  className="h-12 rounded-2xl border border-[var(--line)] bg-white px-4 text-sm outline-none focus:border-[var(--accent)]"
                  placeholder="Wantedly"
                />
              </label>

              <label className="grid gap-2 text-sm font-medium text-[var(--ink-strong)]">
                ステータス
                <select
                  value={form.status}
                  onChange={(event) =>
                    setForm((current) => ({
                      ...current,
                      status: event.target.value as ApplicationStatus,
                    }))
                  }
                  className="h-12 rounded-2xl border border-[var(--line)] bg-white px-4 text-sm outline-none focus:border-[var(--accent)]"
                >
                  {statuses.map((status) => (
                    <option key={status.value} value={status.value}>
                      {status.label}
                    </option>
                  ))}
                </select>
              </label>

              <label className="grid gap-2 text-sm font-medium text-[var(--ink-strong)]">
                応募日
                <input
                  type="date"
                  value={form.appliedAt}
                  onChange={(event) =>
                    setForm((current) => ({ ...current, appliedAt: event.target.value }))
                  }
                  className="h-12 rounded-2xl border border-[var(--line)] bg-white px-4 text-sm outline-none focus:border-[var(--accent)]"
                />
              </label>

              <label className="grid gap-2 text-sm font-medium text-[var(--ink-strong)]">
                結果予定日
                <input
                  type="date"
                  value={form.resultDate}
                  onChange={(event) =>
                    setForm((current) => ({ ...current, resultDate: event.target.value }))
                  }
                  className="h-12 rounded-2xl border border-[var(--line)] bg-white px-4 text-sm outline-none focus:border-[var(--accent)]"
                />
              </label>

              <label className="grid gap-2 text-sm font-medium text-[var(--ink-strong)]">
                オファー期限
                <input
                  type="date"
                  value={form.offerDeadline}
                  onChange={(event) =>
                    setForm((current) => ({ ...current, offerDeadline: event.target.value }))
                  }
                  className="h-12 rounded-2xl border border-[var(--line)] bg-white px-4 text-sm outline-none focus:border-[var(--accent)]"
                />
              </label>

              <label className="grid gap-2 text-sm font-medium text-[var(--ink-strong)]">
                優先度
                <input
                  type="number"
                  min="0"
                  max="10"
                  value={form.priority}
                  onChange={(event) =>
                    setForm((current) => ({ ...current, priority: event.target.value }))
                  }
                  className="h-12 rounded-2xl border border-[var(--line)] bg-white px-4 text-sm outline-none focus:border-[var(--accent)]"
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
                {isSubmitting ? "作成中..." : "応募を作成"}
              </button>
            </div>
          </form>
        ) : null}
      </section>
    </AppShell>
  );
}
