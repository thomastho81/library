import type { BookSearchResponse, PagedBookSearchResponse } from "@/lib/types/book";
import { throwApiError } from "./errors";

const BASE = process.env.NEXT_PUBLIC_SEARCH_API_URL || "";

const LOG_PREFIX = "[search-api]";

function log(...args: unknown[]) {
  console.log(LOG_PREFIX, ...args);
}

function logError(message: string, err: unknown) {
  console.error(LOG_PREFIX, message, err);
  if (err instanceof Error && err.stack) {
    console.error(LOG_PREFIX, "stack:", err.stack);
  }
}

export interface SearchParams {
  q?: string;
  category?: string;
  genre?: string;
  publishedYearFrom?: number;
  publishedYearTo?: number;
  title?: string;
  author?: string;
  isbn?: string;
  active?: boolean;
  all?: boolean;
  availableOnly?: boolean;
  sortBy?: string;
  sortDir?: "asc" | "desc";
  page?: number;
  size?: number;
}

function buildQuery(params: SearchParams): string {
  const search = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== "" && value !== null) {
      search.set(key, String(value));
    }
  });
  const qs = search.toString();
  return qs ? `?${qs}` : "";
}

/**
 * Normaliza a resposta do backend para garantir content sempre array.
 * Contrato: PagedBookSearchResponse (content, page, size, totalElements, totalPages, first, last, numberOfElements).
 */
function normalizePagedResponse(data: unknown): PagedBookSearchResponse {
  const raw = data as Record<string, unknown>;
  const content = Array.isArray(raw.content) ? raw.content : [];
  return {
    content: content as BookSearchResponse[],
    page: typeof raw.page === "number" ? raw.page : 0,
    size: typeof raw.size === "number" ? raw.size : 0,
    totalElements: typeof raw.totalElements === "number" ? raw.totalElements : 0,
    totalPages: typeof raw.totalPages === "number" ? raw.totalPages : 0,
    first: raw.first === true,
    last: raw.last === true,
    numberOfElements: typeof raw.numberOfElements === "number" ? raw.numberOfElements : content.length,
  };
}

/** Busca parametrizada de livros (GET /api/search/books). */
export async function searchBooks(
  params: SearchParams = {}
): Promise<PagedBookSearchResponse> {
  if (!BASE) {
    console.warn(LOG_PREFIX, "NEXT_PUBLIC_SEARCH_API_URL não definida; chamada pode falhar.");
  }
  const url = `${BASE}/api/search/books${buildQuery(params)}`;
  log("GET", url);

  try {
    const res = await fetch(url);
    log("response status:", res.status, res.statusText);

    const contentType = res.headers.get("content-type") ?? "";
    if (!res.ok) {
      console.error(LOG_PREFIX, "erro HTTP", res.status);
      await throwApiError(res);
    }

    if (!contentType.includes("application/json")) {
      const bodyText = await res.text();
      console.error(LOG_PREFIX, "resposta não é JSON. content-type:", contentType, "body:", bodyText?.slice(0, 300));
      throw new Error(`Resposta não é JSON (content-type: ${contentType})`);
    }

    const data = await res.json();
    const normalized = normalizePagedResponse(data);
    log("ok, content.length:", normalized.content.length, "totalElements:", normalized.totalElements);
    return normalized;
  } catch (err) {
    logError("searchBooks falhou", err);
    if (err instanceof TypeError && err.message === "Failed to fetch") {
      console.error(
        LOG_PREFIX,
        "Dica: 'Failed to fetch' costuma ser CORS ou backend inacessível. Verifique: (1) search-service está rodando (ex.:",
        BASE || "http://localhost:8083",
        "); (2) CORS no search-service permite a origem do front (ex.: http://localhost:3000)."
      );
    }
    throw err;
  }
}

/** Busca um livro por id (GET /api/search/books/:id). */
export async function getBookById(id: string): Promise<BookSearchResponse | null> {
  if (!BASE) {
    console.warn(LOG_PREFIX, "NEXT_PUBLIC_SEARCH_API_URL não definida.");
  }
  const url = `${BASE}/api/search/books/${encodeURIComponent(id)}`;
  log("GET", url);

  try {
    const res = await fetch(url);
    log("response status:", res.status);

    if (res.status === 404) {
      log("livro não encontrado, id:", id);
      return null;
    }

    if (!res.ok) {
      console.error(LOG_PREFIX, "erro HTTP", res.status);
      await throwApiError(res);
    }

    const contentType = res.headers.get("content-type") ?? "";
    if (!contentType.includes("application/json")) {
      const bodyText = await res.text();
      console.error(LOG_PREFIX, "resposta não é JSON. content-type:", contentType);
      throw new Error(`Resposta não é JSON (content-type: ${contentType})`);
    }

    const data = await res.json();
    log("ok, book id:", (data as Record<string, unknown>)?.id);
    return data as BookSearchResponse;
  } catch (err) {
    logError("getBookById falhou", err);
    if (err instanceof TypeError && err.message === "Failed to fetch") {
      console.error(
        LOG_PREFIX,
        "Dica: 'Failed to fetch' — verifique se o search-service está rodando e se CORS está habilitado (origem: http://localhost:3000)."
      );
    }
    throw err;
  }
}
