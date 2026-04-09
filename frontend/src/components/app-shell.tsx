"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import type { ReactNode } from "react";
import { clearSession, getStoredSession } from "@/lib/auth";

type AppShellProps = {
  title: string;
  description: string;
  children: ReactNode;
};

const navItems = [
  { href: "/applications", label: "応募一覧" },
  { href: "/applications/new", label: "新規応募" },
  { href: "/companies/new", label: "会社登録" },
];

export function AppShell({ title, description, children }: AppShellProps) {
  const pathname = usePathname();
  const router = useRouter();
  const session = getStoredSession();

  function handleLogout() {
    clearSession();
    router.push("/");
  }

  return (
    <div className="min-h-screen bg-[var(--page-bg)] px-4 py-6 md:px-8 md:py-8">
      <div className="mx-auto flex max-w-6xl flex-col gap-6">
        <header className="rounded-[2rem] border border-white/60 bg-white/80 px-6 py-5 shadow-[0_24px_60px_rgba(31,45,61,0.1)] backdrop-blur">
          <div className="flex flex-col gap-5 md:flex-row md:items-end md:justify-between">
            <div>
              <p className="text-sm font-semibold uppercase tracking-[0.18em] text-[var(--ink-muted)]">
                Job Application Tracker
              </p>
              <h1 className="mt-2 text-3xl font-semibold tracking-[-0.03em] text-[var(--ink-strong)]">
                {title}
              </h1>
              <p className="mt-2 max-w-2xl text-sm leading-7 text-[var(--ink-soft)]">
                {description}
              </p>
            </div>

            <div className="flex items-center gap-3">
              <div className="rounded-full border border-[var(--line)] bg-[var(--panel)] px-4 py-2 text-sm text-[var(--ink-soft)]">
                {session ? `${session.userName} / ${session.userEmail}` : "未ログイン"}
              </div>
              <button
                type="button"
                onClick={handleLogout}
                className="inline-flex h-11 items-center justify-center rounded-full bg-[var(--ink-strong)] px-5 text-sm font-semibold text-white transition hover:bg-black"
              >
                ログアウト
              </button>
            </div>
          </div>

          <nav className="mt-5 flex flex-wrap gap-3">
            {navItems.map((item) => {
              const isActive = pathname === item.href;

              return (
                <Link
                  key={item.href}
                  href={item.href}
                  className={`inline-flex rounded-full px-4 py-2 text-sm font-medium transition ${
                    isActive
                      ? "bg-[var(--ink-strong)] text-white"
                      : "border border-[var(--line)] bg-white text-[var(--ink-soft)] hover:border-[var(--accent)] hover:text-[var(--ink-strong)]"
                  }`}
                >
                  {item.label}
                </Link>
              );
            })}
          </nav>
        </header>

        {children}
      </div>
    </div>
  );
}
