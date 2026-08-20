import Link from "next/link";
import { fetchProducts } from "@/lib/products";

export default async function ProductsPage({
  searchParams,
}: {
  searchParams: Promise<{ categoryId?: string; name?: string }>;
}) {
  const params = await searchParams;
  const page = await fetchProducts(params);

  return (
    <main className="mx-auto max-w-5xl p-8">
      <h1 className="mb-6 text-2xl font-semibold">Products</h1>
      <ul className="grid grid-cols-1 gap-4 sm:grid-cols-2 md:grid-cols-3">
        {page.content.map((product) => (
          <li key={product.id} className="rounded border p-4">
            <Link href={`/products/${product.id}`} className="font-medium underline">
              {product.name}
            </Link>
            <p className="text-sm text-gray-600">{product.categoryName ?? "Uncategorized"}</p>
            <p className="mt-2 font-semibold">${product.price}</p>
          </li>
        ))}
      </ul>
      {page.content.length === 0 && <p className="text-gray-600">No products found.</p>}
    </main>
  );
}
