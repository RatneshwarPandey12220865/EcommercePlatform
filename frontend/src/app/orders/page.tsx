"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { useAuth } from "@/context/AuthContext";
import { fetchOrders, type Order } from "@/lib/orders";

export default function OrdersPage() {
  const { isAuthenticated } = useAuth();
  const [orders, setOrders] = useState<Order[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!isAuthenticated) {
      // Synchronous exit for the "not logged in" case, not a reactive sync.
      // eslint-disable-next-line react-hooks/set-state-in-effect
      setLoading(false);
      return;
    }
    fetchOrders()
      .then((page) => setOrders(page.content))
      .catch((err) => setError(err instanceof Error ? err.message : "Failed to load orders"))
      .finally(() => setLoading(false));
  }, [isAuthenticated]);

  if (!isAuthenticated) {
    return (
      <main className="mx-auto max-w-2xl p-8">
        <p>
          <Link href="/login" className="underline">Log in</Link> to see your orders.
        </p>
      </main>
    );
  }

  return (
    <main className="mx-auto max-w-2xl p-8">
      <h1 className="mb-6 text-2xl font-semibold">Your orders</h1>
      {loading && <p>Loading...</p>}
      {error && <p className="text-red-600">{error}</p>}
      {!loading && orders.length === 0 && <p className="text-gray-600">No orders yet.</p>}
      <ul className="space-y-4">
        {orders.map((order) => (
          <li key={order.id} className="rounded border p-4">
            <div className="flex items-center justify-between">
              <span className="font-medium">{order.status}</span>
              <span className="font-semibold">${order.totalAmount}</span>
            </div>
            <ul className="mt-2 text-sm text-gray-600">
              {order.items.map((item) => (
                <li key={item.productId}>
                  {item.quantity} x {item.productName}
                </li>
              ))}
            </ul>
          </li>
        ))}
      </ul>
    </main>
  );
}
