import type { PagedRentalResponse, RentalResponse } from "@/lib/types/rental";
import { throwApiError } from "./errors";

const BASE = process.env.NEXT_PUBLIC_RENTAL_API_URL || "";

export interface ReserveRequest {
  userId: number;
  bookId: number;
  quantity: number;
}

/**
 * Reserva um livro (POST /api/rentals/reserve).
 * Retorna 202 Accepted com o Rental em status PENDING.
 */
export async function reserveRental(
  payload: ReserveRequest
): Promise<RentalResponse> {
  const url = `${BASE}/api/rentals/reserve`;
  const res = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  if (!res.ok) await throwApiError(res);
  return res.json();
}

export interface ListRentalsParams {
  userId: number;
  status?: string;
  reservedAtFrom?: string;
  reservedAtTo?: string;
  page?: number;
  size?: number;
  sort?: string;
}

/**
 * Lista alugueis do usuário (GET /api/rentals?userId=...).
 * Filtros opcionais: status, reservedAtFrom, reservedAtTo (yyyy-MM-dd). Ordenação: sort=reservedAt,desc.
 */
export async function listRentalsByUser(
  params: ListRentalsParams
): Promise<PagedRentalResponse> {
  const { userId, status, reservedAtFrom, reservedAtTo, page = 0, size = 25, sort } = params;
  const search = new URLSearchParams();
  search.set("userId", String(userId));
  if (status && status !== "TODOS") search.set("status", status);
  if (reservedAtFrom) search.set("reservedAtFrom", reservedAtFrom);
  if (reservedAtTo) search.set("reservedAtTo", reservedAtTo);
  search.set("page", String(page));
  search.set("size", String(size));
  if (sort) search.set("sort", sort);
  const url = `${BASE}/api/rentals?${search.toString()}`;
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

/**
 * Solicitação de devolução pelo usuário (POST /api/rentals/:id/request-return).
 * Rental deve estar RESERVED e pertencer ao userId.
 */
export async function requestReturn(rentalId: number, userId: number): Promise<RentalResponse> {
  const url = `${BASE}/api/rentals/${rentalId}/request-return`;
  const res = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ userId }),
  });
  if (!res.ok) await throwApiError(res);
  return res.json();
}

/**
 * Confirmação de recebimento da devolução pelo gestor (POST /api/rentals/:id/confirm-return).
 */
export async function confirmReturn(rentalId: number, gestorUserId: number): Promise<RentalResponse> {
  const url = `${BASE}/api/rentals/${rentalId}/confirm-return`;
  const res = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ gestorUserId }),
  });
  if (!res.ok) await throwApiError(res);
  return res.json();
}

/**
 * Lista devoluções pendentes (GET /api/rentals/pending-returns). Apenas gestor.
 */
export async function listPendingReturns(params: {
  gestorUserId: number;
  page?: number;
  size?: number;
}): Promise<PagedRentalResponse> {
  const { gestorUserId, page = 0, size = 25 } = params;
  const url = `${BASE}/api/rentals/pending-returns?gestorUserId=${gestorUserId}&page=${page}&size=${size}`;
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
