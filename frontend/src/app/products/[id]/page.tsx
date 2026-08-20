import { fetchProduct } from "@/lib/products";

export default async function ProductDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  const product = await fetchProduct(id);

  return (
    <main className="mx-auto max-w-2xl p-8">
      <h1 className="text-2xl font-semibold">{product.name}</h1>
      <p className="mt-1 text-sm text-gray-600">{product.categoryName ?? "Uncategorized"}</p>
      <p className="mt-4">{product.description}</p>
      <p className="mt-4 text-xl font-semibold">${product.price}</p>
      <p className="mt-1 text-sm text-gray-600">
        {product.stockQuantity > 0 ? `${product.stockQuantity} in stock` : "Out of stock"}
      </p>
    </main>
  );
}
