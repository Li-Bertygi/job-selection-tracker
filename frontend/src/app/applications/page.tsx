"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { AppShell } from "@/components/app-shell";
import { ApplicationStatusBadge } from "@/components/application-status-badge";
import { RequireAuth } from "@/components/require-auth";
import { ApiError, type ApplicationResponse, getApplications } from "@/lib/api";

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
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    async function loadApplications() {
      try {
        const response = await getApplications(accessToken, tokenType);
        setApplications(response);
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
  }, [accessToken, tokenType]);

  const activeApplications = applications.filter((application) => !application.isArchived);
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
      </section>
    </AppShell>
  );
}
