/** Status do aluguel (espelho do backend RentalStatus). */
export type RentalStatus =
  | "PENDING"
  | "RESERVED"
  | "RETURNED"
  | "CANCELLED"
  | "RESERVE_FAILED"
  | "RETURN_REQUESTED";

/** Um aluguel retornado pela API (GET /api/rentals ou GET /api/rentals/{id}). */
export interface RentalResponse {
  id: number;
  userId: number;
  bookId: number;
  quantity: number;
  status: RentalStatus;
  reservedAt: string | null;
  returnedAt: string | null;
  _links?: Record<string, string>;
}

/** Resposta paginada da listagem "meus alugueis" (GET /api/rentals?userId=...). */
export interface PagedRentalResponse {
  content: RentalResponse[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export const RENTAL_STATUS_LABELS: Record<RentalStatus, string> = {
  PENDING: "Em processamento",
  RESERVED: "Reservado",
  RETURNED: "Devolvido",
  CANCELLED: "Cancelado",
  RESERVE_FAILED: "Reserva com erro",
  RETURN_REQUESTED: "Devolução solicitada (aguardando gestor)",
};
