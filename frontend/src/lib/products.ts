const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

export type Product = {
  id: string;
  name: string;
  description: string | null;
  price: number;
  stockQuantity: number;
  categoryId: string | null;
  categoryName: string | null;
  createdAt: string;
};

export type Category = {
  id: string;
  name: string;
};

type Page<T> = {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
};

export async function fetchProducts(params?: {
  categoryId?: string;
  name?: string;
}): Promise<Page<Product>> {
  const qs = new URLSearchParams();
  if (params?.categoryId) qs.set("categoryId", params.categoryId);
  if (params?.name) qs.set("name", params.name);

  const res = await fetch(`${API_BASE_URL}/api/products?${qs.toString()}`, {
    cache: "no-store",
  });
  if (!res.ok) throw new Error("Failed to load products");
  return res.json();
}

export async function fetchProduct(id: string): Promise<Product> {
  const res = await fetch(`${API_BASE_URL}/api/products/${id}`, {
    cache: "no-store",
  });
  if (!res.ok) throw new Error("Failed to load product");
  return res.json();
}

export async function fetchCategories(): Promise<Category[]> {
  const res = await fetch(`${API_BASE_URL}/api/categories`, {
    cache: "no-store",
  });
  if (!res.ok) throw new Error("Failed to load categories");
  return res.json();
}
