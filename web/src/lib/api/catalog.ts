import { throwApiError } from "./errors";

const BASE = process.env.NEXT_PUBLIC_CATALOG_API_URL || "";

export interface CatalogBookRequest {
  title: string;
  author: string;
  category?: string | null;
  genre?: string | null;
  description?: string | null;
  isbn?: string | null;
  publishedYear?: number | null;
}

/**
 * Cria um livro (POST /api/books).
 */
export async function createBook(body: CatalogBookRequest): Promise<unknown> {
  const url = `${BASE}/api/books`;
  const res = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
  if (!res.ok) await throwApiError(res);
  return res.json();
}

/**
 * Atualiza um livro (PUT /api/books/:id).
 */
export async function updateBook(
  id: number | string,
  body: CatalogBookRequest
): Promise<unknown> {
  const url = `${BASE}/api/books/${encodeURIComponent(String(id))}`;
  const res = await fetch(url, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
  if (!res.ok) await throwApiError(res);
  return res.json();
}

/**
 * Remove um livro do catálogo (DELETE /api/books/:id).
 */
export async function deleteBook(id: number | string): Promise<void> {
  const url = `${BASE}/api/books/${encodeURIComponent(String(id))}`;
  const res = await fetch(url, { method: "DELETE" });
  if (!res.ok) await throwApiError(res);
}
