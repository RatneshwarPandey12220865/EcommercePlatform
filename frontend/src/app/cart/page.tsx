"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { useCart } from "@/context/CartContext";
import { useAuth } from "@/context/AuthContext";
import { createOrder } from "@/lib/orders";

export default function CartPage() {
  const { items, updateQuantity, removeItem, clear, total } = useCart();
  const { accessToken, isAuthenticated } = useAuth();
  const router = useRouter();
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function handleCheckout() {
    if (!isAuthenticated || !accessToken) {
      router.push("/login");
      return;
    }
    setError(null);
    setLoading(true);
    try {
      await createOrder(
        accessToken,
        items.map((i) => ({ productId: i.productId, quantity: i.quantity }))
      );
      clear();
      router.push("/orders");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Checkout failed");
    } finally {
      setLoading(false);
    }
  }

  if (items.length === 0) {
    return (
      <main className="mx-auto max-w-2xl p-8">
        <h1 className="text-2xl font-semibold">Your cart is empty</h1>
      </main>
    );
  }

  return (
    <main className="mx-auto max-w-2xl p-8">
      <h1 className="mb-6 text-2xl font-semibold">Cart</h1>
      <ul className="space-y-4">
        {items.map((item) => (
          <li key={item.productId} className="flex items-center justify-between rounded border p-4">
            <div>
              <p className="font-medium">{item.name}</p>
              <p className="text-sm text-gray-600">${item.price} each</p>
            </div>
            <div className="flex items-center gap-2">
              <input
                type="number"
                min={1}
                value={item.quantity}
                onChange={(e) => updateQuantity(item.productId, Number(e.target.value))}
                className="w-16 rounded border px-2 py-1"
              />
              <button onClick={() => removeItem(item.productId)} className="text-sm text-red-600 underline">
                Remove
              </button>
            </div>
          </li>
        ))}
      </ul>
      <div className="mt-6 flex items-center justify-between">
        <p className="text-lg font-semibold">Total: ${total.toFixed(2)}</p>
        <button
          onClick={handleCheckout}
          disabled={loading}
          className="rounded bg-black px-4 py-2 text-white disabled:opacity-50"
        >
          {loading ? "Placing order..." : "Checkout"}
        </button>
      </div>
      {error && <p className="mt-2 text-sm text-red-600">{error}</p>}
    </main>
  );
}
