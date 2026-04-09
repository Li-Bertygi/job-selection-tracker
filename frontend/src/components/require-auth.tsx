"use client";

import { useRouter } from "next/navigation";
import { useEffect, type ReactNode } from "react";
import { getStoredSession, type StoredSession } from "@/lib/auth";

type RequireAuthProps = {
  children: (session: StoredSession) => ReactNode;
};

export function RequireAuth({ children }: RequireAuthProps) {
  const router = useRouter();
  const session = getStoredSession();

  useEffect(() => {
    if (!session) {
      router.replace("/");
    }
  }, [router, session]);

  if (!session) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-[var(--page-bg)]">
        <div className="rounded-full border border-[var(--line)] bg-white px-5 py-3 text-sm text-[var(--ink-soft)] shadow-sm">
          セッションを確認しています...
        </div>
      </div>
    );
  }

  return <>{children(session)}</>;
}
