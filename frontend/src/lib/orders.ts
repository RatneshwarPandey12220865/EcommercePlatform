const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

export type OrderItem = {
  productId: string;
  productName: string;
  unitPrice: number;
  quantity: number;
};

export type Order = {
  id: string;
  status: string;
  totalAmount: number;
  items: OrderItem[];
  createdAt: string;
};

export async function createOrder(items: { productId: string; quantity: number }[]): Promise<Order> {
  const res = await fetch(`${API_BASE_URL}/api/orders`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    credentials: "include",
    body: JSON.stringify({ items }),
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({ message: res.statusText }));
    throw new Error(err.message ?? "Failed to place order");
  }
  return res.json();
}

export async function fetchOrders(): Promise<{ content: Order[] }> {
  const res = await fetch(`${API_BASE_URL}/api/orders`, {
    credentials: "include",
    cache: "no-store",
  });
  if (!res.ok) throw new Error("Failed to load orders");
  return res.json();
}
