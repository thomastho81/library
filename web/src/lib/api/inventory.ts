import { throwApiError } from "./errors";

const BASE = process.env.NEXT_PUBLIC_INVENTORY_API_URL || "";

/** Resposta de PUT increase/decrease (alinhada ao inventory-service). */
export interface InventoryApiResponse {
  id?: number;
  bookId?: number;
  totalCopies?: number;
  availableCopies?: number;
  reservedCopies?: number;
  available?: boolean;
  active?: boolean;
}

/**
 * Aumenta a quantidade de exemplares (PUT /api/inventory/:bookId/increase).
 */
export async function increaseInventory(
  bookId: number | string,
  amount: number
): Promise<InventoryApiResponse> {
  const url = `${BASE}/api/inventory/${encodeURIComponent(String(bookId))}/increase`;
  const res = await fetch(url, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ amount }),
  });
  if (!res.ok) await throwApiError(res);
  return res.json() as Promise<InventoryApiResponse>;
}

/**
 * Reduz a quantidade de exemplares (PUT /api/inventory/:bookId/decrease).
 */
export async function decreaseInventory(
  bookId: number | string,
  amount: number
): Promise<InventoryApiResponse> {
  const url = `${BASE}/api/inventory/${encodeURIComponent(String(bookId))}/decrease`;
  const res = await fetch(url, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ amount }),
  });
  if (!res.ok) await throwApiError(res);
  return res.json() as Promise<InventoryApiResponse>;
}
