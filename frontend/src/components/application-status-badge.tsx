import type { ApplicationStatus } from "@/lib/api";

const statusLabels: Record<ApplicationStatus, string> = {
  NOT_STARTED: "未着手",
  APPLICATION: "応募済み",
  INFO_SESSION: "説明会",
  DOCUMENT_SCREENING: "書類選考",
  TEST: "テスト",
  CASUAL_MEETING: "カジュアル面談",
  INTERVIEW: "面接",
  OFFERED: "内定",
  REJECTED: "不採用",
};

const statusClassNames: Record<ApplicationStatus, string> = {
  NOT_STARTED: "bg-stone-100 text-stone-700",
  APPLICATION: "bg-blue-100 text-blue-700",
  INFO_SESSION: "bg-cyan-100 text-cyan-700",
  DOCUMENT_SCREENING: "bg-indigo-100 text-indigo-700",
  TEST: "bg-amber-100 text-amber-700",
  CASUAL_MEETING: "bg-emerald-100 text-emerald-700",
  INTERVIEW: "bg-orange-100 text-orange-700",
  OFFERED: "bg-green-100 text-green-700",
  REJECTED: "bg-rose-100 text-rose-700",
};

export function ApplicationStatusBadge({ status }: { status: ApplicationStatus }) {
  return (
    <span
      className={`inline-flex rounded-full px-3 py-1 text-xs font-semibold ${statusClassNames[status]}`}
    >
      {statusLabels[status]}
    </span>
  );
}
