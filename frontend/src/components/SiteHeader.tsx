"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useAuth } from "@/context/AuthContext";

export function SiteHeader() {
  const { email, isAuthenticated, logout } = useAuth();
  const router = useRouter();

  async function handleLogout() {
    await logout();
    router.push("/");
  }

  return (
    <header className="flex items-center justify-between border-b p-4">
      <Link href="/" className="font-semibold">
        EcommercePlatform
      </Link>
      <nav className="flex items-center gap-4 text-sm">
        <Link href="/products">Products</Link>
        <Link href="/cart">Cart</Link>
        {isAuthenticated ? (
          <>
            <Link href="/orders">Orders</Link>
            <span className="text-gray-600">{email}</span>
            <button onClick={handleLogout} className="underline">
              Log out
            </button>
          </>
        ) : (
          <Link href="/login">Log in</Link>
        )}
      </nav>
    </header>
  );
}
