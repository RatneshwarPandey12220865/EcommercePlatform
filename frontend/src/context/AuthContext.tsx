"use client";

import { createContext, useContext, useEffect, useState, ReactNode } from "react";
import type { AuthResponse } from "@/lib/api";

type AuthState = {
  email: string | null;
  role: string | null;
  accessToken: string | null;
  isAuthenticated: boolean;
  setSession: (auth: AuthResponse) => void;
  clearSession: () => void;
};

const AuthContext = createContext<AuthState | undefined>(undefined);

const STORAGE_KEY = "ecommerce_auth";

export function AuthProvider({ children }: { children: ReactNode }) {
  const [email, setEmail] = useState<string | null>(null);
  const [role, setRole] = useState<string | null>(null);
  const [accessToken, setAccessToken] = useState<string | null>(null);

  useEffect(() => {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (raw) {
      const parsed = JSON.parse(raw) as AuthResponse;
      setEmail(parsed.email);
      setRole(parsed.role);
      setAccessToken(parsed.accessToken);
    }
  }, []);

  function setSession(auth: AuthResponse) {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(auth));
    setEmail(auth.email);
    setRole(auth.role);
    setAccessToken(auth.accessToken);
  }

  function clearSession() {
    localStorage.removeItem(STORAGE_KEY);
    setEmail(null);
    setRole(null);
    setAccessToken(null);
  }

  return (
    <AuthContext.Provider
      value={{ email, role, accessToken, isAuthenticated: !!accessToken, setSession, clearSession }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}
