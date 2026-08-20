const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

export type AuthResponse = {
  email: string;
  role: string;
};

async function request<T>(path: string, body: unknown): Promise<T> {
  const res = await fetch(`${API_BASE_URL}${path}`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    // The access/refresh tokens live in httpOnly cookies the browser can't
    // read - this just tells fetch to send/accept them.
    credentials: "include",
    body: JSON.stringify(body),
  });

  if (!res.ok) {
    const err = await res.json().catch(() => ({ message: res.statusText }));
    throw new Error(err.message ?? "Request failed");
  }

  return res.json() as Promise<T>;
}

export function register(email: string, password: string) {
  return request<AuthResponse>("/api/auth/register", { email, password });
}

export function login(email: string, password: string) {
  return request<AuthResponse>("/api/auth/login", { email, password });
}

export async function logout(): Promise<void> {
  await fetch(`${API_BASE_URL}/api/auth/logout`, {
    method: "POST",
    credentials: "include",
  });
}
