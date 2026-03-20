"use client";

import { useCallback, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { getBookById } from "@/lib/api/search";
import { listRentalsByUser, requestReturn } from "@/lib/api/rental";
import { ApiError } from "@/lib/api/errors";
import { useErrorModal } from "@/contexts/ErrorModalContext";
import { useProfile } from "@/contexts/ProfileContext";
import type { Book } from "@/lib/types/book";
import type { RentalResponse } from "@/lib/types/rental";
import { RENTAL_STATUS_LABELS } from "@/lib/types/rental";
import BookDetailPanel from "@/components/layout/BookDetailPanel";

const RENTAL_STATUS_FILTERS = [
  { value: "TODOS", label: "Todos" },
  { value: "PENDING", label: "Em processamento" },
  { value: "RESERVED", label: "Reservado" },
  { value: "RETURN_REQUESTED", label: "Devolução solicitada" },
  { value: "RETURNED", label: "Devolvido" },
  { value: "CANCELLED", label: "Cancelado" },
  { value: "RESERVE_FAILED", label: "Reserva com erro" },
];

const PAGE_SIZE = 25;

function formatDate(s: string | null): string {
  if (!s) return "—";
  try {
    const d = new Date(s);
    return d.toLocaleDateString("pt-BR", { day: "2-digit", month: "2-digit", year: "numeric" });
  } catch {
    return s;
  }
}

function toISODateOnly(value: string): string | undefined {
  if (!value?.trim()) return undefined;
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) return undefined;
  return d.toISOString().slice(0, 10);
}

export default function MeusAlugueisPage() {
  const router = useRouter();
  const { userId, profile } = useProfile();
  const { showError: showErrorModal } = useErrorModal();
  const [statusFilter, setStatusFilter] = useState("TODOS");

  useEffect(() => {
    if (profile === "gestor") router.replace("/");
  }, [profile, router]);
  const [reservedAtFrom, setReservedAtFrom] = useState("");
  const [reservedAtTo, setReservedAtTo] = useState("");
  const [page, setPage] = useState(0);
  const [rentals, setRentals] = useState<RentalResponse[]>([]);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);
  const [selectedRental, setSelectedRental] = useState<RentalResponse | null>(null);
  const [selectedBook, setSelectedBook] = useState<Book | null>(null);
  const [loadingBook, setLoadingBook] = useState(false);
  const [requestingReturn, setRequestingReturn] = useState(false);
  const [requestingReturnId, setRequestingReturnId] = useState<number | null>(null);
  const [bookTitles, setBookTitles] = useState<Record<number, string>>({});

  const loadRentals = useCallback(async () => {
    setLoading(true);
    try {
      const res = await listRentalsByUser({
        userId,
        status: statusFilter === "TODOS" ? undefined : statusFilter,
        reservedAtFrom: toISODateOnly(reservedAtFrom),
        reservedAtTo: toISODateOnly(reservedAtTo),
        page,
        size: PAGE_SIZE,
        sort: "reservedAt,desc",
      });
      setRentals(res.content);
      setTotalPages(res.totalPages);
      setTotalElements(res.totalElements);
    } catch (e) {
      console.error("[MeusAlugueis] loadRentals falhou:", e);
      setRentals([]);
      setTotalPages(0);
      setTotalElements(0);
      if (e instanceof ApiError) {
        if (e.isClientError) showErrorModal(e.message);
        else if (e.isServerError) alert(`Erro de servidor: ${e.message}`);
      }
    } finally {
      setLoading(false);
    }
  }, [userId, statusFilter, reservedAtFrom, reservedAtTo, page, showErrorModal]);

  useEffect(() => {
    loadRentals();
  }, [loadRentals]);

  // Busca títulos dos livros da página atual para exibir nos cards (rental-service não retorna título).
  useEffect(() => {
    if (rentals.length === 0) {
      setBookTitles({});
      return;
    }
    const bookIds = [...new Set(rentals.map((r) => r.bookId))];
    let cancelled = false;
    (async () => {
      const results = await Promise.all(
        bookIds.map((id) => getBookById(String(id)).then((b) => ({ id, title: b?.title ?? null })).catch(() => ({ id, title: null })))
      );
      if (cancelled) return;
      setBookTitles((prev) => {
        const next = { ...prev };
        results.forEach(({ id, title }) => {
          if (title) next[id] = title;
        });
        return next;
      });
    })();
    return () => { cancelled = true; };
  }, [rentals]);

  const handleSelectRental = useCallback(async (rental: RentalResponse) => {
    setSelectedRental(rental);
    setSelectedBook(null);
    setLoadingBook(true);
    try {
      const book = await getBookById(String(rental.bookId));
      setSelectedBook(book ?? null);
    } catch (e) {
      setSelectedBook(null);
      if (e instanceof ApiError) {
        if (e.isClientError) showErrorModal(e.message);
        else if (e.isServerError) alert(`Erro de servidor: ${e.message}`);
      }
    } finally {
      setLoadingBook(false);
    }
  }, [showErrorModal]);

  const handleRequestReturn = useCallback(async () => {
    if (!selectedRental || selectedRental.status !== "RESERVED") return;
    setRequestingReturn(true);
    try {
      await requestReturn(selectedRental.id, userId);
      await loadRentals();
      setSelectedRental((r) => (r ? { ...r, status: "RETURN_REQUESTED" } : null));
    } catch (e) {
      if (e instanceof ApiError) {
        if (e.isClientError) showErrorModal(e.message);
        else if (e.isServerError) alert(`Erro de servidor: ${e.message}`);
      }
    } finally {
      setRequestingReturn(false);
    }
  }, [selectedRental, userId, loadRentals, showErrorModal]);

  const handleRequestReturnFromCard = useCallback(
    async (e: React.MouseEvent, rental: RentalResponse) => {
      e.stopPropagation();
      if (rental.status !== "RESERVED") return;
      setRequestingReturnId(rental.id);
      try {
        await requestReturn(rental.id, userId);
        await loadRentals();
        if (selectedRental?.id === rental.id) {
          setSelectedRental((r) => (r?.id === rental.id ? { ...r, status: "RETURN_REQUESTED" as const } : r));
        }
      } catch (err) {
        if (err instanceof ApiError) {
          if (err.isClientError) showErrorModal(err.message);
          else if (err.isServerError) alert(`Erro de servidor: ${err.message}`);
        }
      } finally {
        setRequestingReturnId(null);
      }
    },
    [userId, loadRentals, showErrorModal, selectedRental]
  );

  if (profile === "gestor") return null;

  return (
    <div className="flex flex-1 gap-0 overflow-hidden">
      <div className="flex flex-1 flex-col overflow-hidden bg-gray-50/50">
        <div className="flex-shrink-0 p-4">
          <header className="rounded-2xl bg-white px-6 py-4 shadow-sm">
            <h1 className="text-xl font-semibold text-gray-900">Meus alugueis</h1>
            <p className="mt-1 text-sm text-gray-500">
              Lista paginada dos seus empréstimos (25 por página).
            </p>
          </header>
        </div>

        <main className="flex-1 overflow-y-auto px-4 pb-6">
          {/* Filtros de aluguel */}
          <section className="mb-6 rounded-2xl bg-white p-4 shadow-sm">
            <h3 className="mb-3 text-sm font-semibold text-gray-900">Filtros de aluguel</h3>
            <div className="flex flex-wrap gap-2">
              {RENTAL_STATUS_FILTERS.map(({ value, label }) => (
                <button
                  key={value}
                  type="button"
                  onClick={() => {
                    setStatusFilter(value);
                    setPage(0);
                  }}
                  className={`rounded-xl px-4 py-2 text-sm font-medium transition-colors ${
                    statusFilter === value
                      ? "bg-[#2563eb] text-white"
                      : "bg-gray-100 text-gray-700 hover:bg-gray-200"
                  }`}
                >
                  {label}
                </button>
              ))}
            </div>
            <div className="mt-4 flex flex-wrap items-end gap-4">
              <div>
                <label className="mb-1 block text-xs font-medium text-gray-600">Reservado de</label>
                <input
                  type="date"
                  value={reservedAtFrom}
                  onChange={(e) => {
                    setReservedAtFrom(e.target.value);
                    setPage(0);
                  }}
                  className="rounded-lg border border-gray-200 px-3 py-2 text-sm"
                />
              </div>
              <div>
                <label className="mb-1 block text-xs font-medium text-gray-600">Reservado até</label>
                <input
                  type="date"
                  value={reservedAtTo}
                  onChange={(e) => {
                    setReservedAtTo(e.target.value);
                    setPage(0);
                  }}
                  className="rounded-lg border border-gray-200 px-3 py-2 text-sm"
                />
              </div>
            </div>
          </section>

          {/* Resultados */}
          <section>
            <h3 className="mb-3 text-sm font-semibold text-gray-900">Resultados</h3>
            {loading ? (
              <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5">
                {[1, 2, 3, 4, 5].map((i) => (
                  <div
                    key={i}
                    className="h-32 animate-pulse rounded-2xl bg-gray-200"
                  />
                ))}
              </div>
            ) : rentals.length === 0 ? (
              <p className="py-8 text-center text-sm text-gray-500">
                Nenhum aluguel encontrado.
              </p>
            ) : (
              <>
                <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5">
                  {rentals.map((rental) => (
                    <div
                      key={rental.id}
                      role="button"
                      tabIndex={0}
                      onClick={() => handleSelectRental(rental)}
                      onKeyDown={(e) => e.key === "Enter" && handleSelectRental(rental)}
                      className={`flex flex-col overflow-hidden rounded-2xl bg-white p-3 text-left shadow-sm transition-all hover:shadow-md cursor-pointer ${
                        selectedRental?.id === rental.id ? "ring-2 ring-[#2563eb]" : ""
                      }`}
                    >
                      <div className="flex h-12 items-center justify-center rounded-lg bg-gray-100 text-2xl text-gray-400">
                        📖
                      </div>
                      <p className="mt-2 truncate text-xs text-gray-500">
                        {bookTitles[rental.bookId] ?? `Livro #${rental.bookId}`}
                      </p>
                      <p className="mt-0.5 text-sm font-medium text-gray-900">
                        {rental.quantity} {rental.quantity === 1 ? "cópia" : "cópias"}
                      </p>
                      <span className="mt-2 inline-block rounded-lg bg-gray-100 px-2 py-0.5 text-xs text-gray-700">
                        {RENTAL_STATUS_LABELS[rental.status as keyof typeof RENTAL_STATUS_LABELS] ?? rental.status}
                      </span>
                      <p className="mt-1 text-xs text-gray-500">
                        {formatDate(rental.reservedAt)}
                      </p>
                      {rental.status === "RESERVED" ? (
                        <button
                          type="button"
                          onClick={(e) => handleRequestReturnFromCard(e, rental)}
                          disabled={requestingReturnId === rental.id}
                          className="mt-2 w-full rounded-xl bg-[#2563eb] py-1.5 text-xs font-medium text-white hover:bg-[#1d4ed8] disabled:opacity-50"
                        >
                          {requestingReturnId === rental.id ? "…" : "Fazer devolução"}
                        </button>
                      ) : (
                        <button
                          type="button"
                          disabled
                          className="mt-2 w-full rounded-xl bg-gray-200 py-1.5 text-xs font-medium text-gray-500 cursor-not-allowed"
                        >
                          Fazer devolução
                        </button>
                      )}
                    </div>
                  ))}
                </div>
                {/* Paginação */}
                {totalPages > 1 && (
                  <div className="mt-4 flex items-center justify-center gap-2">
                    <button
                      type="button"
                      onClick={() => setPage((p) => Math.max(0, p - 1))}
                      disabled={page === 0}
                      className="rounded-xl border border-gray-300 bg-white px-4 py-2 text-sm disabled:opacity-50"
                    >
                      Anterior
                    </button>
                    <span className="text-sm text-gray-600">
                      Página {page + 1} de {totalPages} ({totalElements} itens)
                    </span>
                    <button
                      type="button"
                      onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
                      disabled={page >= totalPages - 1}
                      className="rounded-xl border border-gray-300 bg-white px-4 py-2 text-sm disabled:opacity-50"
                    >
                      Próxima
                    </button>
                  </div>
                )}
              </>
            )}
          </section>
        </main>
      </div>

      {/* Painel: detalhe do livro e ações do aluguel */}
      {loadingBook ? (
        <aside className="flex w-80 flex-shrink-0 flex-col items-center justify-center rounded-l-2xl bg-[#1e3a8a] p-6 text-white/70">
          <p className="text-sm">Carregando livro…</p>
        </aside>
      ) : (
        <aside className="flex w-80 flex-shrink-0 flex-col overflow-y-auto rounded-l-2xl bg-[#1e3a8a]">
          <BookDetailPanel book={selectedBook} />
          {selectedRental && (
            <div className="border-t border-white/20 p-4">
              <p className="mb-2 text-xs text-white/70">Status do aluguel</p>
              <p className="text-sm font-medium text-white">
                {RENTAL_STATUS_LABELS[selectedRental.status as keyof typeof RENTAL_STATUS_LABELS] ?? selectedRental.status}
              </p>
              {selectedRental.status === "RESERVED" && (
                <button
                  type="button"
                  disabled={requestingReturn}
                  onClick={handleRequestReturn}
                  className="mt-3 w-full rounded-xl bg-[#2563eb] py-2.5 text-sm font-medium text-white hover:bg-[#1d4ed8] disabled:opacity-50"
                >
                  {requestingReturn ? "Fazendo devolução…" : "Fazer devolução"}
                </button>
              )}
              {selectedRental.status === "RETURN_REQUESTED" && (
                <p className="mt-3 text-sm text-amber-200">
                  Aguardando confirmação do gestor.
                </p>
              )}
            </div>
          )}
        </aside>
      )}
    </div>
  );
}
