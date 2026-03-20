import type { PagedUserResponse, UserResponse } from "@/lib/types/users";
import { throwApiError } from "./errors";

const BASE = process.env.NEXT_PUBLIC_RENTAL_API_URL || "";

/**
 * Lista clientes (usuários que não são gestores) - GET /api/users.
 * Uso: tela Clientes do perfil gestor.
 */
export async function listClients(params: {
  page?: number;
  size?: number;
}): Promise<PagedUserResponse> {
  const { page = 0, size = 25 } = params;
  const url = `${BASE}/api/users?page=${page}&size=${size}`;
  const res = await fetch(url);
  if (!res.ok) await throwApiError(res);
  const data = await res.json();
  return {
    content: Array.isArray(data.content) ? data.content : [],
    page: typeof data.page === "number" ? data.page : 0,
    size: typeof data.size === "number" ? data.size : 0,
    totalElements: typeof data.totalElements === "number" ? data.totalElements : 0,
    totalPages: typeof data.totalPages === "number" ? data.totalPages : 0,
    first: data.first === true,
    last: data.last === true,
  };
}
