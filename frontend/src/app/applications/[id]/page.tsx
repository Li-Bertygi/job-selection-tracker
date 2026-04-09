"use client";

import Link from "next/link";
import { useParams } from "next/navigation";
import { FormEvent, useEffect, useState } from "react";
import { AppShell } from "@/components/app-shell";
import { ApplicationStatusBadge } from "@/components/application-status-badge";
import { RequireAuth } from "@/components/require-auth";
import {
  ApiError,
  type ApplicationResponse,
  createNote,
  createSchedule,
  createStage,
  deleteNote,
  deleteSchedule,
  deleteStage,
  getApplication,
  getNotes,
  getSchedules,
  getStages,
  type NoteResponse,
  type NoteType,
  type ScheduleResponse,
  type ScheduleType,
  type StageResponse,
  type StageStatus,
  type StageType,
  updateNote,
  updateSchedule,
  updateStage,
} from "@/lib/api";

const stageTypeLabels: Record<StageType, string> = {
  INFO_SESSION: "説明会",
  DOCUMENT_SCREENING: "書類選考",
  CODING_TEST: "コーディングテスト",
  APTITUDE_TEST: "適性検査",
  FIRST_CASUAL_MEETING: "一次カジュアル面談",
  SECOND_CASUAL_MEETING: "二次カジュアル面談",
  THIRD_CASUAL_MEETING: "三次カジュアル面談",
  FIRST_INTERVIEW: "一次面接",
  SECOND_INTERVIEW: "二次面接",
  THIRD_INTERVIEW: "三次面接",
  FOURTH_INTERVIEW: "四次面接",
  FINAL_INTERVIEW: "最終面接",
  OTHER: "その他",
};

const stageStatusLabels: Record<StageStatus, string> = {
  PENDING: "未着手",
  SCHEDULED: "予定済み",
  COMPLETED: "完了",
  PASSED: "通過",
  FAILED: "不通過",
};

const scheduleTypeLabels: Record<ScheduleType, string> = {
  DEADLINE: "締切",
  EVENT: "イベント",
  RESULT_ANNOUNCEMENT: "結果連絡",
  OTHER: "その他",
};

const noteTypeLabels: Record<NoteType, string> = {
  UNSPECIFIED: "未分類",
  PREPARATION: "準備",
  ACTUAL_CONTENT: "実施内容",
  REVIEW: "振り返り",
  OTHER: "その他",
};

function formatDate(value: string | null) {
  if (!value) return "-";
  return new Intl.DateTimeFormat("ja-JP").format(new Date(value));
}

function formatDateTime(value: string | null) {
  if (!value) return "-";
  return new Intl.DateTimeFormat("ja-JP", { dateStyle: "medium", timeStyle: "short" }).format(
    new Date(value)
  );
}

function Message({ message, tone }: { message: string | null; tone: "error" | "success" }) {
  if (!message) return null;
  const classes =
    tone === "error"
      ? "border-[var(--danger-line)] bg-[var(--danger-bg)] text-[var(--danger-ink)]"
      : "border-[var(--success-line)] bg-[var(--success-bg)] text-[var(--success-ink)]";
  return <div className={`rounded-2xl border px-4 py-3 text-sm ${classes}`}>{message}</div>;
}

export default function ApplicationDetailPage() {
  return (
    <RequireAuth>
      {(session) => (
        <ApplicationDetailScreen accessToken={session.accessToken} tokenType={session.tokenType} />
      )}
    </RequireAuth>
  );
}

function ApplicationDetailScreen({
  accessToken,
  tokenType,
}: {
  accessToken: string;
  tokenType: string;
}) {
  const params = useParams<{ id: string }>();
  const applicationId = Number(params.id);
  const [application, setApplication] = useState<ApplicationResponse | null>(null);
  const [stages, setStages] = useState<StageResponse[]>([]);
  const [schedules, setSchedules] = useState<ScheduleResponse[]>([]);
  const [notes, setNotes] = useState<NoteResponse[]>([]);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [sectionMessage, setSectionMessage] = useState<{ tone: "error" | "success"; text: string | null }>({
    tone: "success",
    text: null,
  });

  const [stageForm, setStageForm] = useState({
    stageOrder: "1",
    stageType: "FIRST_INTERVIEW" as StageType,
    stageName: "",
    status: "PENDING" as StageStatus,
  });
  const [scheduleForm, setScheduleForm] = useState({
    stageId: "",
    scheduleType: "EVENT" as ScheduleType,
    title: "",
    startAt: "",
  });
  const [noteForm, setNoteForm] = useState({
    title: "",
    noteType: "UNSPECIFIED" as NoteType,
    content: "",
  });

  useEffect(() => {
    async function load() {
      try {
        const [app, stageList, scheduleList, noteList] = await Promise.all([
          getApplication(applicationId, accessToken, tokenType),
          getStages(applicationId, accessToken, tokenType),
          getSchedules(applicationId, accessToken, tokenType),
          getNotes(applicationId, accessToken, tokenType),
        ]);
        setApplication(app);
        setStages(stageList);
        setSchedules(scheduleList);
        setNotes(noteList);
        setStageForm((current) => ({ ...current, stageOrder: String(stageList.length + 1) }));
      } catch (error) {
        setErrorMessage(error instanceof ApiError ? error.message : "応募詳細の取得に失敗しました。");
      }
    }

    if (!Number.isNaN(applicationId)) {
      void load();
    }
  }, [accessToken, applicationId, tokenType]);

  async function handleStageCreate(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    try {
      const created = await createStage(
        applicationId,
        {
          stageOrder: Number(stageForm.stageOrder),
          stageType: stageForm.stageType,
          stageName: stageForm.stageName,
          status: stageForm.status,
        },
        accessToken,
        tokenType
      );
      const next = [...stages, created].sort((a, b) => a.stageOrder - b.stageOrder);
      setStages(next);
      setStageForm({ stageOrder: String(next.length + 1), stageType: "FIRST_INTERVIEW", stageName: "", status: "PENDING" });
      setSectionMessage({ tone: "success", text: "ステージを追加しました。" });
    } catch (error) {
      setSectionMessage({ tone: "error", text: error instanceof ApiError ? error.message : "ステージ作成に失敗しました。" });
    }
  }

  async function handleScheduleCreate(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    try {
      const created = await createSchedule(
        applicationId,
        {
          stageId: scheduleForm.stageId ? Number(scheduleForm.stageId) : null,
          scheduleType: scheduleForm.scheduleType,
          title: scheduleForm.title,
          startAt: scheduleForm.startAt,
        },
        accessToken,
        tokenType
      );
      setSchedules([...schedules, created].sort((a, b) => new Date(a.startAt).getTime() - new Date(b.startAt).getTime()));
      setScheduleForm({ stageId: "", scheduleType: "EVENT", title: "", startAt: "" });
      setSectionMessage({ tone: "success", text: "日程を追加しました。" });
    } catch (error) {
      setSectionMessage({ tone: "error", text: error instanceof ApiError ? error.message : "日程作成に失敗しました。" });
    }
  }

  async function handleNoteCreate(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    try {
      const created = await createNote(
        applicationId,
        { title: noteForm.title || null, noteType: noteForm.noteType, content: noteForm.content },
        accessToken,
        tokenType
      );
      setNotes([created, ...notes]);
      setNoteForm({ title: "", noteType: "UNSPECIFIED", content: "" });
      setSectionMessage({ tone: "success", text: "メモを追加しました。" });
    } catch (error) {
      setSectionMessage({ tone: "error", text: error instanceof ApiError ? error.message : "メモ作成に失敗しました。" });
    }
  }

  async function editStage(item: StageResponse) {
    const nextName = window.prompt("ステージ名", item.stageName);
    if (nextName === null || !nextName.trim()) return;
    try {
      const updated = await updateStage(item.id, { stageName: nextName.trim() }, accessToken, tokenType);
      setStages(stages.map((stage) => (stage.id === item.id ? updated : stage)));
      setSectionMessage({ tone: "success", text: "ステージを更新しました。" });
    } catch (error) {
      setSectionMessage({ tone: "error", text: error instanceof ApiError ? error.message : "ステージ更新に失敗しました。" });
    }
  }

  async function removeStage(id: number) {
    if (!window.confirm("このステージを削除しますか。")) return;
    try {
      await deleteStage(id, accessToken, tokenType);
      const next = stages.filter((stage) => stage.id !== id);
      setStages(next);
      setStageForm((current) => ({ ...current, stageOrder: String(next.length + 1) }));
      setSectionMessage({ tone: "success", text: "ステージを削除しました。" });
    } catch (error) {
      setSectionMessage({ tone: "error", text: error instanceof ApiError ? error.message : "ステージ削除に失敗しました。" });
    }
  }

  async function editSchedule(item: ScheduleResponse) {
    const nextTitle = window.prompt("日程タイトル", item.title);
    if (nextTitle === null || !nextTitle.trim()) return;
    try {
      const updated = await updateSchedule(item.id, { title: nextTitle.trim() }, accessToken, tokenType);
      setSchedules(schedules.map((schedule) => (schedule.id === item.id ? updated : schedule)));
      setSectionMessage({ tone: "success", text: "日程を更新しました。" });
    } catch (error) {
      setSectionMessage({ tone: "error", text: error instanceof ApiError ? error.message : "日程更新に失敗しました。" });
    }
  }

  async function removeSchedule(id: number) {
    if (!window.confirm("この日程を削除しますか。")) return;
    try {
      await deleteSchedule(id, accessToken, tokenType);
      setSchedules(schedules.filter((schedule) => schedule.id !== id));
      setSectionMessage({ tone: "success", text: "日程を削除しました。" });
    } catch (error) {
      setSectionMessage({ tone: "error", text: error instanceof ApiError ? error.message : "日程削除に失敗しました。" });
    }
  }

  async function editNote(item: NoteResponse) {
    const nextContent = window.prompt("メモ内容", item.content);
    if (nextContent === null || !nextContent.trim()) return;
    try {
      const updated = await updateNote(item.id, { content: nextContent.trim() }, accessToken, tokenType);
      setNotes(notes.map((note) => (note.id === item.id ? updated : note)));
      setSectionMessage({ tone: "success", text: "メモを更新しました。" });
    } catch (error) {
      setSectionMessage({ tone: "error", text: error instanceof ApiError ? error.message : "メモ更新に失敗しました。" });
    }
  }

  async function removeNote(id: number) {
    if (!window.confirm("このメモを削除しますか。")) return;
    try {
      await deleteNote(id, accessToken, tokenType);
      setNotes(notes.filter((note) => note.id !== id));
      setSectionMessage({ tone: "success", text: "メモを削除しました。" });
    } catch (error) {
      setSectionMessage({ tone: "error", text: error instanceof ApiError ? error.message : "メモ削除に失敗しました。" });
    }
  }

  return (
    <AppShell title="応募詳細" description="ステージ、日程、メモの追加に加えて、個別の修正と削除もここで行えます。">
      <section className="rounded-[2rem] border border-[var(--line)] bg-[var(--card)] p-6">
        <div className="flex items-center justify-between">
          <h2 className="text-2xl font-semibold text-[var(--ink-strong)]">応募の現在地</h2>
          <Link href="/applications" className="rounded-full border border-[var(--line)] bg-white px-4 py-2 text-sm text-[var(--ink-soft)]">一覧へ戻る</Link>
        </div>
        {errorMessage ? <div className="mt-4"><Message tone="error" message={errorMessage} /></div> : null}
        {sectionMessage.text ? <div className="mt-4"><Message tone={sectionMessage.tone} message={sectionMessage.text} /></div> : null}
        {application ? (
          <div className="mt-6 grid gap-6 lg:grid-cols-[1.1fr_0.9fr]">
            <div className="rounded-[1.5rem] border border-[var(--line)] bg-white p-6">
              <div className="flex items-start justify-between gap-4">
                <div>
                  <p className="text-xs font-semibold uppercase tracking-[0.18em] text-[var(--ink-muted)]">Application #{application.id}</p>
                  <h3 className="mt-2 text-3xl font-semibold text-[var(--ink-strong)]">{application.jobTitle}</h3>
                  <p className="mt-2 text-sm text-[var(--ink-soft)]">会社 ID: {application.companyId} · 応募経路: {application.applicationRoute || "-"}</p>
                </div>
                <ApplicationStatusBadge status={application.status} />
              </div>
            </div>
            <div className="rounded-[1.5rem] border border-[var(--line)] bg-white p-6 text-sm text-[var(--ink-soft)]">
              <p>応募日: {formatDate(application.appliedAt)}</p>
              <p className="mt-2">結果予定日: {formatDate(application.resultDate)}</p>
              <p className="mt-2">オファー期限: {formatDate(application.offerDeadline)}</p>
              <p className="mt-2">優先度: {application.priority}</p>
            </div>
          </div>
        ) : null}
      </section>

      <section className="grid gap-6 xl:grid-cols-3">
        <div className="rounded-[2rem] border border-[var(--line)] bg-[var(--card)] p-6">
          <h3 className="text-xl font-semibold text-[var(--ink-strong)]">選考ステージ</h3>
          <form className="mt-4 grid gap-3" onSubmit={handleStageCreate}>
            <div className="grid gap-3 md:grid-cols-2">
              <input type="number" min="1" value={stageForm.stageOrder} onChange={(e) => setStageForm((c) => ({ ...c, stageOrder: e.target.value }))} className="h-11 rounded-2xl border border-[var(--line)] bg-white px-4 text-sm" />
              <select value={stageForm.stageType} onChange={(e) => setStageForm((c) => ({ ...c, stageType: e.target.value as StageType }))} className="h-11 rounded-2xl border border-[var(--line)] bg-white px-4 text-sm">
                {Object.entries(stageTypeLabels).map(([value, label]) => <option key={value} value={value}>{label}</option>)}
              </select>
            </div>
            <input value={stageForm.stageName} onChange={(e) => setStageForm((c) => ({ ...c, stageName: e.target.value }))} className="h-11 rounded-2xl border border-[var(--line)] bg-white px-4 text-sm" placeholder="ステージ名" required />
            <select value={stageForm.status} onChange={(e) => setStageForm((c) => ({ ...c, status: e.target.value as StageStatus }))} className="h-11 rounded-2xl border border-[var(--line)] bg-white px-4 text-sm">
              {Object.entries(stageStatusLabels).map(([value, label]) => <option key={value} value={value}>{label}</option>)}
            </select>
            <button type="submit" className="h-11 rounded-full bg-[var(--ink-strong)] text-sm font-semibold text-white">ステージを追加</button>
          </form>
          <div className="mt-5 grid gap-4">
            {stages.map((item) => (
              <article key={item.id} className="rounded-[1.5rem] border border-[var(--line)] bg-white p-5">
                <div className="flex items-start justify-between gap-4">
                  <div>
                    <p className="text-xs uppercase tracking-[0.18em] text-[var(--ink-muted)]">Stage {item.stageOrder}</p>
                    <h4 className="mt-2 text-lg font-semibold text-[var(--ink-strong)]">{item.stageName}</h4>
                    <p className="mt-2 text-sm text-[var(--ink-soft)]">{stageTypeLabels[item.stageType]} · {stageStatusLabels[item.status]}</p>
                  </div>
                  <div className="flex gap-2">
                    <button type="button" onClick={() => editStage(item)} className="rounded-full border border-[var(--line)] px-3 py-1 text-xs">修正</button>
                    <button type="button" onClick={() => removeStage(item.id)} className="rounded-full border border-[var(--danger-line)] px-3 py-1 text-xs text-[var(--danger-ink)]">削除</button>
                  </div>
                </div>
              </article>
            ))}
          </div>
        </div>

        <div className="rounded-[2rem] border border-[var(--line)] bg-[var(--card)] p-6">
          <h3 className="text-xl font-semibold text-[var(--ink-strong)]">日程</h3>
          <form className="mt-4 grid gap-3" onSubmit={handleScheduleCreate}>
            <select value={scheduleForm.stageId} onChange={(e) => setScheduleForm((c) => ({ ...c, stageId: e.target.value }))} className="h-11 rounded-2xl border border-[var(--line)] bg-white px-4 text-sm">
              <option value="">関連ステージなし</option>
              {stages.map((item) => <option key={item.id} value={item.id}>{`Stage ${item.stageOrder} · ${item.stageName}`}</option>)}
            </select>
            <div className="grid gap-3 md:grid-cols-2">
              <select value={scheduleForm.scheduleType} onChange={(e) => setScheduleForm((c) => ({ ...c, scheduleType: e.target.value as ScheduleType }))} className="h-11 rounded-2xl border border-[var(--line)] bg-white px-4 text-sm">
                {Object.entries(scheduleTypeLabels).map(([value, label]) => <option key={value} value={value}>{label}</option>)}
              </select>
              <input value={scheduleForm.title} onChange={(e) => setScheduleForm((c) => ({ ...c, title: e.target.value }))} className="h-11 rounded-2xl border border-[var(--line)] bg-white px-4 text-sm" placeholder="タイトル" required />
            </div>
            <input type="datetime-local" value={scheduleForm.startAt} onChange={(e) => setScheduleForm((c) => ({ ...c, startAt: e.target.value }))} className="h-11 rounded-2xl border border-[var(--line)] bg-white px-4 text-sm" required />
            <button type="submit" className="h-11 rounded-full bg-[var(--ink-strong)] text-sm font-semibold text-white">日程を追加</button>
          </form>
          <div className="mt-5 grid gap-4">
            {schedules.map((item) => (
              <article key={item.id} className="rounded-[1.5rem] border border-[var(--line)] bg-white p-5">
                <div className="flex items-start justify-between gap-4">
                  <div>
                    <p className="text-xs uppercase tracking-[0.18em] text-[var(--ink-muted)]">{scheduleTypeLabels[item.scheduleType]}</p>
                    <h4 className="mt-2 text-lg font-semibold text-[var(--ink-strong)]">{item.title}</h4>
                    <p className="mt-2 text-sm text-[var(--ink-soft)]">{formatDateTime(item.startAt)}</p>
                  </div>
                  <div className="flex gap-2">
                    <button type="button" onClick={() => editSchedule(item)} className="rounded-full border border-[var(--line)] px-3 py-1 text-xs">修正</button>
                    <button type="button" onClick={() => removeSchedule(item.id)} className="rounded-full border border-[var(--danger-line)] px-3 py-1 text-xs text-[var(--danger-ink)]">削除</button>
                  </div>
                </div>
              </article>
            ))}
          </div>
        </div>

        <div className="rounded-[2rem] border border-[var(--line)] bg-[var(--card)] p-6">
          <h3 className="text-xl font-semibold text-[var(--ink-strong)]">メモ</h3>
          <form className="mt-4 grid gap-3" onSubmit={handleNoteCreate}>
            <div className="grid gap-3 md:grid-cols-2">
              <input value={noteForm.title} onChange={(e) => setNoteForm((c) => ({ ...c, title: e.target.value }))} className="h-11 rounded-2xl border border-[var(--line)] bg-white px-4 text-sm" placeholder="タイトル" />
              <select value={noteForm.noteType} onChange={(e) => setNoteForm((c) => ({ ...c, noteType: e.target.value as NoteType }))} className="h-11 rounded-2xl border border-[var(--line)] bg-white px-4 text-sm">
                {Object.entries(noteTypeLabels).map(([value, label]) => <option key={value} value={value}>{label}</option>)}
              </select>
            </div>
            <textarea value={noteForm.content} onChange={(e) => setNoteForm((c) => ({ ...c, content: e.target.value }))} className="min-h-28 rounded-[1.25rem] border border-[var(--line)] bg-white px-4 py-3 text-sm" placeholder="内容" required />
            <button type="submit" className="h-11 rounded-full bg-[var(--ink-strong)] text-sm font-semibold text-white">メモを追加</button>
          </form>
          <div className="mt-5 grid gap-4">
            {notes.map((item) => (
              <article key={item.id} className="rounded-[1.5rem] border border-[var(--line)] bg-white p-5">
                <div className="flex items-start justify-between gap-4">
                  <div>
                    <p className="text-xs uppercase tracking-[0.18em] text-[var(--ink-muted)]">{noteTypeLabels[item.noteType]}</p>
                    <h4 className="mt-2 text-lg font-semibold text-[var(--ink-strong)]">{item.title || "無題メモ"}</h4>
                    <p className="mt-3 text-sm leading-7 text-[var(--ink-soft)]">{item.content}</p>
                  </div>
                  <div className="flex gap-2">
                    <button type="button" onClick={() => editNote(item)} className="rounded-full border border-[var(--line)] px-3 py-1 text-xs">修正</button>
                    <button type="button" onClick={() => removeNote(item.id)} className="rounded-full border border-[var(--danger-line)] px-3 py-1 text-xs text-[var(--danger-ink)]">削除</button>
                  </div>
                </div>
              </article>
            ))}
          </div>
        </div>
      </section>
    </AppShell>
  );
}
