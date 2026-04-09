"use client";

import { FormEvent, useState } from "react";
import { useRouter } from "next/navigation";
import { ApiError, login } from "@/lib/api";
import { saveSession } from "@/lib/auth";

type LoginState = {
  email: string;
  password: string;
};

const initialState: LoginState = {
  email: "",
  password: "",
};

export function LoginForm() {
  const router = useRouter();
  const [form, setForm] = useState<LoginState>(initialState);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setIsSubmitting(true);
    setErrorMessage(null);
    setSuccessMessage(null);

    try {
      const response = await login(form);
      saveSession(response);
      setSuccessMessage(`${response.name}さんとしてログインしました。`);
      setForm((current) => ({ ...current, password: "" }));
      router.push("/applications");
    } catch (error) {
      if (error instanceof ApiError) {
        setErrorMessage(error.message);
      } else {
        setErrorMessage("ログイン中に予期しないエラーが発生しました。");
      }
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <form className="grid gap-5" onSubmit={handleSubmit}>
      <div className="grid gap-2">
        <label className="text-sm font-medium text-[var(--ink-strong)]" htmlFor="email">
          メールアドレス
        </label>
        <input
          id="email"
          type="email"
          autoComplete="email"
          required
          value={form.email}
          onChange={(event) =>
            setForm((current) => ({ ...current, email: event.target.value }))
          }
          className="h-12 rounded-2xl border border-[var(--line)] bg-white px-4 text-sm outline-none transition focus:border-[var(--accent)] focus:ring-2 focus:ring-[color:var(--accent-soft)]"
          placeholder="test@example.com"
        />
      </div>

      <div className="grid gap-2">
        <label className="text-sm font-medium text-[var(--ink-strong)]" htmlFor="password">
          パスワード
        </label>
        <input
          id="password"
          type="password"
          autoComplete="current-password"
          required
          value={form.password}
          onChange={(event) =>
            setForm((current) => ({ ...current, password: event.target.value }))
          }
          className="h-12 rounded-2xl border border-[var(--line)] bg-white px-4 text-sm outline-none transition focus:border-[var(--accent)] focus:ring-2 focus:ring-[color:var(--accent-soft)]"
          placeholder="password123"
        />
      </div>

      {errorMessage ? (
        <p className="rounded-2xl border border-[var(--danger-line)] bg-[var(--danger-bg)] px-4 py-3 text-sm text-[var(--danger-ink)]">
          {errorMessage}
        </p>
      ) : null}

      {successMessage ? (
        <p className="rounded-2xl border border-[var(--success-line)] bg-[var(--success-bg)] px-4 py-3 text-sm text-[var(--success-ink)]">
          {successMessage}
        </p>
      ) : null}

      <button
        type="submit"
        disabled={isSubmitting}
        className="inline-flex h-12 items-center justify-center rounded-full bg-[var(--ink-strong)] px-6 text-sm font-semibold text-white transition hover:bg-black disabled:cursor-not-allowed disabled:opacity-60"
      >
        {isSubmitting ? "ログイン中..." : "ログイン"}
      </button>
    </form>
  );
}
