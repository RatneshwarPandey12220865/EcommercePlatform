"use client";

import { useState } from "react";
import { useCart } from "@/context/CartContext";

export function AddToCartButton({
  productId,
  name,
  price,
}: {
  productId: string;
  name: string;
  price: number;
}) {
  const { addItem } = useCart();
  const [added, setAdded] = useState(false);

  return (
    <button
      onClick={() => {
        addItem({ productId, name, price });
        setAdded(true);
        setTimeout(() => setAdded(false), 1500);
      }}
      className="mt-4 rounded bg-black px-4 py-2 text-white"
    >
      {added ? "Added!" : "Add to cart"}
    </button>
  );
}
