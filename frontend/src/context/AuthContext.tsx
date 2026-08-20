"use client";

import { createContext, useContext, useEffect, useState, ReactNode } from "react";
import type { AuthResponse } from "@/lib/api";
import { logout as apiLogout } from "@/lib/api";

type AuthState = {
  email: string | null;
  role: string | null;
  isAuthenticated: boolean;
  setSession: (auth: AuthResponse) => void;
  logout: () => Promise<void>;
};

const AuthContext = createContext<AuthState | undefined>(undefined);

// Only UI-display state (email/role) lives here - the actual session lives in
// an httpOnly cookie the browser controls, this is never the source of truth
// for authorization.
const STORAGE_KEY = "ecommerce_auth_profile";

export function AuthProvider({ children }: { children: ReactNode }) {
  const [email, setEmail] = useState<string | null>(null);
  const [role, setRole] = useState<string | null>(null);

  useEffect(() => {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (raw) {
      const parsed = JSON.parse(raw) as AuthResponse;
      // One-time hydration from localStorage on mount - not a reactive sync.
      // eslint-disable-next-line react-hooks/set-state-in-effect
      setEmail(parsed.email);
      setRole(parsed.role);
    }
  }, []);

  function setSession(auth: AuthResponse) {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(auth));
    setEmail(auth.email);
    setRole(auth.role);
  }

  async function logout() {
    await apiLogout();
    localStorage.removeItem(STORAGE_KEY);
    setEmail(null);
    setRole(null);
  }

  return (
    <AuthContext.Provider value={{ email, role, isAuthenticated: !!email, setSession, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}
