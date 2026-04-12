"use client";

import Link from "next/link";
import { FormEvent, useEffect, useState } from "react";
import { AppShell } from "@/components/app-shell";
import { ApplicationStatusBadge } from "@/components/application-status-badge";
import { RequireAuth } from "@/components/require-auth";
import {
  ApiError,
  type ApplicationResponse,
  type ApplicationStatus,
  getApplications,
} from "@/lib/api";

const applicationStatuses: ApplicationStatus[] = [
  "NOT_STARTED",
  "APPLICATION",
  "INFO_SESSION",
  "DOCUMENT_SCREENING",
  "TEST",
  "CASUAL_MEETING",
  "INTERVIEW",
  "OFFERED",
  "REJECTED",
];

function formatDate(date: string | null) {
  if (!date) {
    return "-";
  }

  return new Intl.DateTimeFormat("ja-JP").format(new Date(date));
}

function DashboardCard({
  label,
  value,
  tone,
}: {
  label: string;
  value: string | number;
  tone: string;
}) {
  return (
    <div className={`rounded-[1.5rem] border p-5 ${tone}`}>
      <p className="text-xs font-semibold uppercase tracking-[0.18em]">{label}</p>
      <p className="mt-4 text-3xl font-semibold tracking-[-0.04em]">{value}</p>
    </div>
  );
}

export default function ApplicationsPage() {
  return (
    <RequireAuth>
      {(session) => <ApplicationsScreen accessToken={session.accessToken} tokenType={session.tokenType} />}
    </RequireAuth>
  );
}

function ApplicationsScreen({
  accessToken,
  tokenType,
}: {
  accessToken: string;
  tokenType: string;
}) {
  const [applications, setApplications] = useState<ApplicationResponse[]>([]);
  const [statusFilter, setStatusFilter] = useState<ApplicationStatus | "">("");
  const [archiveFilter, setArchiveFilter] = useState<"active" | "archived" | "all">("active");
  const [keywordInput, setKeywordInput] = useState("");
  const [keyword, setKeyword] = useState("");
  const [page, setPage] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    async function loadApplications() {
      try {
        setIsLoading(true);
        setErrorMessage(null);

        const response = await getApplications(accessToken, tokenType, {
          status: statusFilter || undefined,
          isArchived:
            archiveFilter === "all" ? undefined : archiveFilter === "archived",
          keyword,
          page,
          size: 10,
        });
        setApplications(response.content);
        setTotalElements(response.totalElements);
        setTotalPages(response.totalPages);
      } catch (error) {
        if (error instanceof ApiError) {
          setErrorMessage(error.message);
          return;
        }

        setErrorMessage("応募一覧の取得中にエラーが発生しました。");
      } finally {
        setIsLoading(false);
      }
    }

    void loadApplications();
  }, [accessToken, tokenType, statusFilter, archiveFilter, keyword, page]);

  function handleSearchSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setPage(0);
    setKeyword(keywordInput.trim());
  }

  function handleStatusChange(value: string) {
    setPage(0);
    setStatusFilter(value as ApplicationStatus | "");
  }

  function handleArchiveChange(value: "active" | "archived" | "all") {
    setPage(0);
    setArchiveFilter(value);
  }

  const activeApplications = applications.filter(
    (application) => !application.isArchived
  );
  const offeredCount = applications.filter((application) => application.status === "OFFERED").length;
  const interviewCount = applications.filter((application) => application.status === "INTERVIEW").length;

  return (
    <AppShell
      title="応募一覧"
      description="進行中の応募を一覧で確認し、優先度と更新状況を見ながら次のアクションを決めます。"
    >
      <section className="grid gap-4 md:grid-cols-3">
        <DashboardCard
          label="Active"
          value={activeApplications.length}
          tone="border-[var(--line)] bg-white text-[var(--ink-strong)]"
        />
        <DashboardCard
          label="Interview"
          value={interviewCount}
          tone="border-orange-200 bg-orange-50 text-orange-700"
        />
        <DashboardCard
          label="Offered"
          value={offeredCount}
          tone="border-green-200 bg-green-50 text-green-700"
        />
      </section>

      <section className="rounded-[2rem] border border-[var(--line)] bg-[var(--card)] p-6 shadow-[0_24px_60px_rgba(31,45,61,0.08)]">
        <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
          <div>
            <p className="text-sm font-semibold uppercase tracking-[0.18em] text-[var(--ink-muted)]">
              Applications
            </p>
            <h2 className="mt-2 text-2xl font-semibold tracking-[-0.03em] text-[var(--ink-strong)]">
              最新の応募状況
            </h2>
          </div>

          <Link
            href="/applications/new"
            className="inline-flex h-11 items-center justify-center rounded-full bg-[var(--accent)] px-5 text-sm font-semibold text-white transition hover:opacity-90"
          >
            応募を追加
          </Link>
        </div>

        <form
          onSubmit={handleSearchSubmit}
          className="mt-6 grid gap-3 md:grid-cols-[1fr_180px_160px_auto]"
        >
          <input
            value={keywordInput}
            onChange={(event) => setKeywordInput(event.target.value)}
            placeholder="会社名・職種で検索"
            className="h-11 rounded-lg border border-[var(--line)] bg-white px-4 text-sm text-[var(--ink-strong)] outline-none transition focus:border-[var(--accent)]"
          />
          <select
            value={statusFilter}
            onChange={(event) => handleStatusChange(event.target.value)}
            className="h-11 rounded-lg border border-[var(--line)] bg-white px-3 text-sm text-[var(--ink-strong)] outline-none transition focus:border-[var(--accent)]"
          >
            <option value="">すべてのステータス</option>
            {applicationStatuses.map((status) => (
              <option key={status} value={status}>
                {status}
              </option>
            ))}
          </select>
          <select
            value={archiveFilter}
            onChange={(event) =>
              handleArchiveChange(event.target.value as "active" | "archived" | "all")
            }
            className="h-11 rounded-lg border border-[var(--line)] bg-white px-3 text-sm text-[var(--ink-strong)] outline-none transition focus:border-[var(--accent)]"
          >
            <option value="active">アクティブ</option>
            <option value="archived">アーカイブ</option>
            <option value="all">すべて</option>
          </select>
          <button
            type="submit"
            className="h-11 rounded-lg bg-[var(--ink-strong)] px-5 text-sm font-semibold text-white transition hover:opacity-90"
          >
            検索
          </button>
        </form>

        {isLoading ? (
          <div className="mt-6 rounded-[1.5rem] border border-dashed border-[var(--line)] bg-white px-5 py-12 text-center text-sm text-[var(--ink-soft)]">
            応募一覧を読み込んでいます...
          </div>
        ) : null}

        {errorMessage ? (
          <div className="mt-6 rounded-[1.5rem] border border-[var(--danger-line)] bg-[var(--danger-bg)] px-5 py-4 text-sm text-[var(--danger-ink)]">
            {errorMessage}
          </div>
        ) : null}

        {!isLoading && !errorMessage && applications.length === 0 ? (
          <div className="mt-6 rounded-[1.5rem] border border-dashed border-[var(--line)] bg-white px-5 py-12 text-center">
            <p className="text-lg font-semibold text-[var(--ink-strong)]">
              まだ応募が登録されていません。
            </p>
            <p className="mt-2 text-sm leading-7 text-[var(--ink-soft)]">
              まずは会社を作成したうえで、応募を追加してください。
            </p>
          </div>
        ) : null}

        {!isLoading && !errorMessage && applications.length > 0 ? (
          <div className="mt-6 grid gap-4">
            {applications.map((application) => (
              <Link
                key={application.id}
                href={`/applications/${application.id}`}
                className="rounded-[1.5rem] border border-[var(--line)] bg-white p-5 transition hover:-translate-y-0.5 hover:border-[var(--accent)] hover:shadow-[0_16px_36px_rgba(31,45,61,0.08)]"
              >
                <div className="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
                  <div>
                    <p className="text-xs font-semibold uppercase tracking-[0.18em] text-[var(--ink-muted)]">
                      Application #{application.id}
                    </p>
                    <h3 className="mt-2 text-xl font-semibold text-[var(--ink-strong)]">
                      {application.jobTitle}
                    </h3>
                    <p className="mt-2 text-sm text-[var(--ink-soft)]">
                      会社 ID: {application.companyId}
                      {" · "}
                      応募経路: {application.applicationRoute || "-"}
                    </p>
                  </div>

                  <ApplicationStatusBadge status={application.status} />
                </div>

                <div className="mt-5 grid gap-3 text-sm text-[var(--ink-soft)] md:grid-cols-4">
                  <div>
                    <p className="text-xs uppercase tracking-[0.18em] text-[var(--ink-muted)]">
                      Applied
                    </p>
                    <p className="mt-1">{formatDate(application.appliedAt)}</p>
                  </div>
                  <div>
                    <p className="text-xs uppercase tracking-[0.18em] text-[var(--ink-muted)]">
                      Result
                    </p>
                    <p className="mt-1">{formatDate(application.resultDate)}</p>
                  </div>
                  <div>
                    <p className="text-xs uppercase tracking-[0.18em] text-[var(--ink-muted)]">
                      Priority
                    </p>
                    <p className="mt-1">{application.priority}</p>
                  </div>
                  <div>
                    <p className="text-xs uppercase tracking-[0.18em] text-[var(--ink-muted)]">
                      Updated
                    </p>
                    <p className="mt-1">{formatDate(application.updatedAt)}</p>
                  </div>
                </div>
              </Link>
            ))}
          </div>
        ) : null}

        {!isLoading && !errorMessage && totalElements > 0 ? (
          <div className="mt-6 flex flex-col gap-3 text-sm text-[var(--ink-soft)] md:flex-row md:items-center md:justify-between">
            <p>
              全{totalElements}件中 {page + 1} / {Math.max(totalPages, 1)} ページ
            </p>
            <div className="flex gap-2">
              <button
                type="button"
                disabled={page === 0}
                onClick={() => setPage((currentPage) => Math.max(currentPage - 1, 0))}
                className="h-10 rounded-lg border border-[var(--line)] bg-white px-4 font-semibold text-[var(--ink-strong)] disabled:cursor-not-allowed disabled:opacity-40"
              >
                前へ
              </button>
              <button
                type="button"
                disabled={page + 1 >= totalPages}
                onClick={() => setPage((currentPage) => currentPage + 1)}
                className="h-10 rounded-lg border border-[var(--line)] bg-white px-4 font-semibold text-[var(--ink-strong)] disabled:cursor-not-allowed disabled:opacity-40"
              >
                次へ
              </button>
            </div>
          </div>
        ) : null}
      </section>
    </AppShell>
  );
}
