const TOKEN_STORAGE_KEY = "mentortrack_token";
const ROLE_STORAGE_KEY = "mentortrack_role";
const FORCE_PASSWORD_CHANGE_KEY = "mentortrack_force_password_change";

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_STORAGE_KEY);
}

export function getRole(): string | null {
  return localStorage.getItem(ROLE_STORAGE_KEY);
}

export function getForcePasswordChange(): boolean {
  return localStorage.getItem(FORCE_PASSWORD_CHANGE_KEY) === "true";
}

export function setForcePasswordChange(value: boolean) {
  localStorage.setItem(FORCE_PASSWORD_CHANGE_KEY, String(value));
}

export function setSession(token: string, role: string, forcePasswordChange = false) {
  localStorage.setItem(TOKEN_STORAGE_KEY, token);
  localStorage.setItem(ROLE_STORAGE_KEY, role);
  setForcePasswordChange(forcePasswordChange);
}

export function clearSession() {
  localStorage.removeItem(TOKEN_STORAGE_KEY);
  localStorage.removeItem(ROLE_STORAGE_KEY);
  localStorage.removeItem(FORCE_PASSWORD_CHANGE_KEY);
}

export class ApiError extends Error {
  status: number;
  constructor(status: number, message: string) {
    super(message);
    this.status = status;
  }
}

async function request<T>(
  path: string,
  options: RequestInit = {},
): Promise<T> {
  const token = getToken();
  const headers = new Headers(options.headers);
  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }
  if (
    options.body &&
    !(options.body instanceof FormData) &&
    !headers.has("Content-Type")
  ) {
    headers.set("Content-Type", "application/json");
  }

  const response = await fetch(`/api${path}`, { ...options, headers });

  if (!response.ok) {
    let message = response.statusText;
    try {
      const body = await response.json();
      message = body.message ?? message;
    } catch {
      // ignore parse errors, fall back to statusText
    }
    throw new ApiError(response.status, message);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  const contentType = response.headers.get("Content-Type") ?? "";
  if (contentType.includes("application/json")) {
    return (await response.json()) as T;
  }
  return undefined as T;
}

export const api = {
  get: <T>(path: string) => request<T>(path, { method: "GET" }),
  post: <T>(path: string, body?: unknown) =>
    request<T>(path, {
      method: "POST",
      body: body instanceof FormData ? body : body !== undefined ? JSON.stringify(body) : undefined,
    }),
  upload: <T>(path: string, file: File) => {
    const formData = new FormData();
    formData.append("file", file);
    return request<T>(path, { method: "POST", body: formData });
  },
  downloadBlob: async (path: string): Promise<Blob> => {
    const token = getToken();
    const headers = new Headers();
    if (token) {
      headers.set("Authorization", `Bearer ${token}`);
    }
    const response = await fetch(`/api${path}`, { headers });
    if (!response.ok) {
      throw new ApiError(response.status, response.statusText);
    }
    return response.blob();
  },
};
