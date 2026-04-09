"use client";

import type { AuthResponse } from "@/lib/api";

const ACCESS_TOKEN_KEY = "job-tracker.accessToken";
const TOKEN_TYPE_KEY = "job-tracker.tokenType";
const USER_NAME_KEY = "job-tracker.userName";
const USER_EMAIL_KEY = "job-tracker.userEmail";

export type StoredSession = {
  accessToken: string;
  tokenType: string;
  userName: string;
  userEmail: string;
};

export function saveSession(auth: AuthResponse) {
  if (!auth.accessToken) {
    throw new Error("Missing access token.");
  }

  window.localStorage.setItem(ACCESS_TOKEN_KEY, auth.accessToken);
  window.localStorage.setItem(TOKEN_TYPE_KEY, auth.tokenType ?? "Bearer");
  window.localStorage.setItem(USER_NAME_KEY, auth.name);
  window.localStorage.setItem(USER_EMAIL_KEY, auth.email);
}

export function getStoredSession(): StoredSession | null {
  if (typeof window === "undefined") {
    return null;
  }

  const accessToken = window.localStorage.getItem(ACCESS_TOKEN_KEY);
  const tokenType = window.localStorage.getItem(TOKEN_TYPE_KEY) ?? "Bearer";
  const userName = window.localStorage.getItem(USER_NAME_KEY) ?? "";
  const userEmail = window.localStorage.getItem(USER_EMAIL_KEY) ?? "";

  if (!accessToken) {
    return null;
  }

  return {
    accessToken,
    tokenType,
    userName,
    userEmail,
  };
}

export function clearSession() {
  if (typeof window === "undefined") {
    return;
  }

  window.localStorage.removeItem(ACCESS_TOKEN_KEY);
  window.localStorage.removeItem(TOKEN_TYPE_KEY);
  window.localStorage.removeItem(USER_NAME_KEY);
  window.localStorage.removeItem(USER_EMAIL_KEY);
}
