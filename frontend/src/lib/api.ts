const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

export type LoginPayload = {
  email: string;
  password: string;
};

export type AuthResponse = {
  userId: number;
  email: string;
  name: string;
  accessToken?: string;
  tokenType?: string;
  expiresIn?: number;
};

export type ApiErrorResponse = {
  status: number;
  error: string;
  code: string;
  message: string;
  timestamp: string;
  errors?: Record<string, string>;
};

export type PageResponse<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
};

export type CompanyResponse = {
  id: number;
  name: string;
  industry: string | null;
  websiteUrl: string | null;
  memo: string | null;
  createdAt: string | null;
  updatedAt: string | null;
};

export type ApplicationStatus =
  | "NOT_STARTED"
  | "APPLICATION"
  | "INFO_SESSION"
  | "DOCUMENT_SCREENING"
  | "TEST"
  | "CASUAL_MEETING"
  | "INTERVIEW"
  | "OFFERED"
  | "REJECTED";

export type ApplicationResponse = {
  id: number;
  companyId: number;
  jobTitle: string;
  applicationRoute: string | null;
  status: ApplicationStatus;
  appliedAt: string | null;
  resultDate: string | null;
  offerDeadline: string | null;
  priority: number;
  isArchived: boolean;
  createdAt: string | null;
  updatedAt: string | null;
};

export type GetApplicationsParams = {
  status?: ApplicationStatus;
  isArchived?: boolean;
  keyword?: string;
  page?: number;
  size?: number;
};

export type StageStatus =
  | "PENDING"
  | "SCHEDULED"
  | "COMPLETED"
  | "PASSED"
  | "FAILED";

export type StageType =
  | "INFO_SESSION"
  | "DOCUMENT_SCREENING"
  | "CODING_TEST"
  | "APTITUDE_TEST"
  | "FIRST_CASUAL_MEETING"
  | "SECOND_CASUAL_MEETING"
  | "THIRD_CASUAL_MEETING"
  | "FIRST_INTERVIEW"
  | "SECOND_INTERVIEW"
  | "THIRD_INTERVIEW"
  | "FOURTH_INTERVIEW"
  | "FINAL_INTERVIEW"
  | "OTHER";

export type StageResponse = {
  id: number;
  applicationId: number;
  stageOrder: number;
  stageType: StageType;
  stageName: string;
  status: StageStatus;
  scheduledAt: string | null;
  completedAt: string | null;
  resultDate: string | null;
  memo: string | null;
  createdAt: string | null;
  updatedAt: string | null;
};

export type ScheduleType = "DEADLINE" | "EVENT" | "RESULT_ANNOUNCEMENT" | "OTHER";

export type ScheduleResponse = {
  id: number;
  applicationId: number;
  stageId: number | null;
  scheduleType: ScheduleType;
  title: string;
  description: string | null;
  startAt: string;
  endAt: string | null;
  location: string | null;
  isAllDay: boolean;
  createdAt: string | null;
  updatedAt: string | null;
};

export type NoteType =
  | "UNSPECIFIED"
  | "PREPARATION"
  | "ACTUAL_CONTENT"
  | "REVIEW"
  | "OTHER";

export type NoteResponse = {
  id: number;
  applicationId: number;
  title: string | null;
  noteType: NoteType;
  content: string;
  createdAt: string | null;
  updatedAt: string | null;
};

export type CreateApplicationPayload = {
  companyId: number;
  jobTitle: string;
  applicationRoute?: string | null;
  status?: ApplicationStatus;
  appliedAt?: string | null;
  resultDate?: string | null;
  offerDeadline?: string | null;
  priority?: number;
  isArchived?: boolean;
};

export type CreateCompanyPayload = {
  name: string;
  industry?: string | null;
  websiteUrl?: string | null;
  memo?: string | null;
};

export type CreateStagePayload = {
  stageOrder: number;
  stageType: StageType;
  stageName: string;
  status?: StageStatus;
  scheduledAt?: string | null;
  completedAt?: string | null;
  resultDate?: string | null;
  memo?: string | null;
};

export type CreateSchedulePayload = {
  stageId?: number | null;
  scheduleType: ScheduleType;
  title: string;
  description?: string | null;
  startAt: string;
  endAt?: string | null;
  location?: string | null;
  isAllDay?: boolean;
};

export type CreateNotePayload = {
  title?: string | null;
  noteType?: NoteType;
  content: string;
};

export type UpdateStagePayload = {
  stageOrder?: number;
  stageType?: StageType;
  stageName?: string;
  status?: StageStatus;
  scheduledAt?: string | null;
  completedAt?: string | null;
  resultDate?: string | null;
  memo?: string | null;
};

export type UpdateSchedulePayload = {
  stageId?: number | null;
  scheduleType?: ScheduleType;
  title?: string;
  description?: string | null;
  startAt?: string | null;
  endAt?: string | null;
  location?: string | null;
  isAllDay?: boolean;
};

export type UpdateNotePayload = {
  title?: string | null;
  noteType?: NoteType;
  content?: string;
};

export class ApiError extends Error {
  status: number;
  code: string;
  errors?: Record<string, string>;

  constructor(response: ApiErrorResponse) {
    super(response.message);
    this.name = "ApiError";
    this.status = response.status;
    this.code = response.code;
    this.errors = response.errors;
  }
}

async function parseJsonSafely<T>(response: Response): Promise<T | null> {
  const text = await response.text();

  if (!text) {
    return null;
  }

  return JSON.parse(text) as T;
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...init,
    headers: {
      "Content-Type": "application/json",
      ...(init?.headers ?? {}),
    },
  });

  if (!response.ok) {
    const errorResponse = await parseJsonSafely<ApiErrorResponse>(response);

    throw new ApiError(
      errorResponse ?? {
        status: response.status,
        error: response.statusText,
        code: "UNKNOWN_ERROR",
        message: "API request failed.",
        timestamp: new Date().toISOString(),
      }
    );
  }

  const data = await parseJsonSafely<T>(response);
  return data as T;
}

function buildAuthorizationHeader(accessToken: string, tokenType = "Bearer") {
  return `${tokenType} ${accessToken}`;
}

export async function login(payload: LoginPayload): Promise<AuthResponse> {
  return request<AuthResponse>("/auth/login", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export async function getCompanies(
  accessToken: string,
  tokenType = "Bearer"
): Promise<CompanyResponse[]> {
  return request<CompanyResponse[]>("/companies", {
    headers: {
      Authorization: buildAuthorizationHeader(accessToken, tokenType),
    },
  });
}

export async function createCompany(
  payload: CreateCompanyPayload,
  accessToken: string,
  tokenType = "Bearer"
): Promise<CompanyResponse> {
  return request<CompanyResponse>("/companies", {
    method: "POST",
    headers: {
      Authorization: buildAuthorizationHeader(accessToken, tokenType),
    },
    body: JSON.stringify(payload),
  });
}

export async function getApplications(
  accessToken: string,
  tokenType = "Bearer",
  params: GetApplicationsParams = {}
): Promise<PageResponse<ApplicationResponse>> {
  const searchParams = new URLSearchParams();

  if (params.status) {
    searchParams.set("status", params.status);
  }

  if (params.isArchived !== undefined) {
    searchParams.set("isArchived", String(params.isArchived));
  }

  if (params.keyword?.trim()) {
    searchParams.set("keyword", params.keyword.trim());
  }

  searchParams.set("page", String(params.page ?? 0));
  searchParams.set("size", String(params.size ?? 20));

  const queryString = searchParams.toString();

  return request<PageResponse<ApplicationResponse>>(`/applications?${queryString}`, {
    headers: {
      Authorization: buildAuthorizationHeader(accessToken, tokenType),
    },
  });
}

export async function getApplication(
  id: number,
  accessToken: string,
  tokenType = "Bearer"
): Promise<ApplicationResponse> {
  return request<ApplicationResponse>(`/applications/${id}`, {
    headers: {
      Authorization: buildAuthorizationHeader(accessToken, tokenType),
    },
  });
}

export async function createApplication(
  payload: CreateApplicationPayload,
  accessToken: string,
  tokenType = "Bearer"
): Promise<ApplicationResponse> {
  return request<ApplicationResponse>("/applications", {
    method: "POST",
    headers: {
      Authorization: buildAuthorizationHeader(accessToken, tokenType),
    },
    body: JSON.stringify(payload),
  });
}

export async function getStages(
  applicationId: number,
  accessToken: string,
  tokenType = "Bearer"
): Promise<StageResponse[]> {
  return request<StageResponse[]>(`/applications/${applicationId}/stages`, {
    headers: {
      Authorization: buildAuthorizationHeader(accessToken, tokenType),
    },
  });
}

export async function getSchedules(
  applicationId: number,
  accessToken: string,
  tokenType = "Bearer"
): Promise<ScheduleResponse[]> {
  return request<ScheduleResponse[]>(`/applications/${applicationId}/schedules`, {
    headers: {
      Authorization: buildAuthorizationHeader(accessToken, tokenType),
    },
  });
}

export async function getNotes(
  applicationId: number,
  accessToken: string,
  tokenType = "Bearer"
): Promise<NoteResponse[]> {
  return request<NoteResponse[]>(`/applications/${applicationId}/notes`, {
    headers: {
      Authorization: buildAuthorizationHeader(accessToken, tokenType),
    },
  });
}

export async function createStage(
  applicationId: number,
  payload: CreateStagePayload,
  accessToken: string,
  tokenType = "Bearer"
): Promise<StageResponse> {
  return request<StageResponse>(`/applications/${applicationId}/stages`, {
    method: "POST",
    headers: {
      Authorization: buildAuthorizationHeader(accessToken, tokenType),
    },
    body: JSON.stringify(payload),
  });
}

export async function createSchedule(
  applicationId: number,
  payload: CreateSchedulePayload,
  accessToken: string,
  tokenType = "Bearer"
): Promise<ScheduleResponse> {
  return request<ScheduleResponse>(`/applications/${applicationId}/schedules`, {
    method: "POST",
    headers: {
      Authorization: buildAuthorizationHeader(accessToken, tokenType),
    },
    body: JSON.stringify(payload),
  });
}

export async function createNote(
  applicationId: number,
  payload: CreateNotePayload,
  accessToken: string,
  tokenType = "Bearer"
): Promise<NoteResponse> {
  return request<NoteResponse>(`/applications/${applicationId}/notes`, {
    method: "POST",
    headers: {
      Authorization: buildAuthorizationHeader(accessToken, tokenType),
    },
    body: JSON.stringify(payload),
  });
}

export async function updateStage(
  id: number,
  payload: UpdateStagePayload,
  accessToken: string,
  tokenType = "Bearer"
): Promise<StageResponse> {
  return request<StageResponse>(`/stages/${id}`, {
    method: "PATCH",
    headers: {
      Authorization: buildAuthorizationHeader(accessToken, tokenType),
    },
    body: JSON.stringify(payload),
  });
}

export async function deleteStage(
  id: number,
  accessToken: string,
  tokenType = "Bearer"
): Promise<void> {
  return request<void>(`/stages/${id}`, {
    method: "DELETE",
    headers: {
      Authorization: buildAuthorizationHeader(accessToken, tokenType),
    },
  });
}

export async function updateSchedule(
  id: number,
  payload: UpdateSchedulePayload,
  accessToken: string,
  tokenType = "Bearer"
): Promise<ScheduleResponse> {
  return request<ScheduleResponse>(`/schedules/${id}`, {
    method: "PATCH",
    headers: {
      Authorization: buildAuthorizationHeader(accessToken, tokenType),
    },
    body: JSON.stringify(payload),
  });
}

export async function deleteSchedule(
  id: number,
  accessToken: string,
  tokenType = "Bearer"
): Promise<void> {
  return request<void>(`/schedules/${id}`, {
    method: "DELETE",
    headers: {
      Authorization: buildAuthorizationHeader(accessToken, tokenType),
    },
  });
}

export async function updateNote(
  id: number,
  payload: UpdateNotePayload,
  accessToken: string,
  tokenType = "Bearer"
): Promise<NoteResponse> {
  return request<NoteResponse>(`/notes/${id}`, {
    method: "PATCH",
    headers: {
      Authorization: buildAuthorizationHeader(accessToken, tokenType),
    },
    body: JSON.stringify(payload),
  });
}

export async function deleteNote(
  id: number,
  accessToken: string,
  tokenType = "Bearer"
): Promise<void> {
  return request<void>(`/notes/${id}`, {
    method: "DELETE",
    headers: {
      Authorization: buildAuthorizationHeader(accessToken, tokenType),
    },
  });
}

export { API_BASE_URL };
