/** Livro retornado pela API de busca (search-service). */
export interface BookSearchResponse {
  id: string;
  title: string;
  author: string;
  category: string | null;
  genre: string | null;
  description: string | null;
  isbn: string | null;
  publishedYear: number | null;
  active: boolean;
  createdAt: string;
  updatedAt: string;
  totalCopies: number | null;
  availableCopies: number | null;
  inventoryUpdatedAt: string | null;
}

/** Resposta paginada da API de busca. */
export interface PagedBookSearchResponse {
  content: BookSearchResponse[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
  numberOfElements: number;
}

export type Book = BookSearchResponse;
