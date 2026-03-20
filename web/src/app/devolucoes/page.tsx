"use client";

import { useCallback, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { listPendingReturns, confirmReturn } from "@/lib/api/rental";
import { ApiError } from "@/lib/api/errors";
import { useErrorModal } from "@/contexts/ErrorModalContext";
import { useProfile } from "@/contexts/ProfileContext";
import type { RentalResponse } from "@/lib/types/rental";
import { RENTAL_STATUS_LABELS } from "@/lib/types/rental";

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

export default function DevolucoesPage() {
  const { profile, userId } = useProfile();
  const router = useRouter();
  const { showError: showErrorModal } = useErrorModal();
  const [rentals, setRentals] = useState<RentalResponse[]>([]);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [confirmingId, setConfirmingId] = useState<number | null>(null);

  const loadPending = useCallback(async () => {
    if (profile !== "gestor" || userId == null) return;
    setLoading(true);
    try {
      const res = await listPendingReturns({
        gestorUserId: userId,
        page,
        size: PAGE_SIZE,
      });
      setRentals(res.content);
      setTotalPages(res.totalPages);
      setTotalElements(res.totalElements);
    } catch (e) {
      if (e instanceof ApiError) {
        if (e.isClientError) showErrorModal(e.message);
        else if (e.isServerError) alert(`Erro de servidor: ${e.message}`);
      }
      setRentals([]);
      setTotalPages(0);
      setTotalElements(0);
    } finally {
      setLoading(false);
    }
  }, [profile, userId, page, showErrorModal]);

  useEffect(() => {
    if (profile !== "gestor") {
      router.replace("/");
      return;
    }
    loadPending();
  }, [profile, router, loadPending]);

  const handleConfirmReturn = useCallback(
    async (rentalId: number) => {
      if (userId == null) return;
      setConfirmingId(rentalId);
      try {
        await confirmReturn(rentalId, userId);
        await loadPending();
      } catch (e) {
        if (e instanceof ApiError) {
          if (e.isClientError) showErrorModal(e.message);
          else if (e.isServerError) alert(`Erro de servidor: ${e.message}`);
        }
      } finally {
        setConfirmingId(null);
      }
    },
    [userId, loadPending, showErrorModal]
  );

  if (profile !== "gestor") return null;

  return (
    <div className="flex flex-1 flex-col overflow-hidden">
      <div className="flex-shrink-0 p-4">
        <header className="rounded-2xl bg-white px-6 py-4 shadow-sm">
          <h1 className="text-xl font-semibold text-gray-900">Devoluções</h1>
          <p className="mt-1 text-sm text-gray-500">
            Pedidos de devolução aguardando confirmação. Confirme o recebimento dos livros.
          </p>
        </header>
      </div>
      <main className="flex-1 overflow-y-auto px-4 pb-6">
        <div className="rounded-2xl bg-white p-6 shadow-sm">
          {loading ? (
            <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
              {[1, 2, 3].map((i) => (
                <div key={i} className="h-28 animate-pulse rounded-xl bg-gray-100" />
              ))}
            </div>
          ) : rentals.length === 0 ? (
            <p className="py-8 text-center text-sm text-gray-500">
              Nenhum pedido de devolução pendente.
            </p>
          ) : (
            <>
              <ul className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
                {rentals.map((rental) => (
                  <li
                    key={rental.id}
                    className="flex flex-col rounded-xl border border-gray-200 p-4"
                  >
                    <div className="flex justify-between">
                      <span className="text-xs text-gray-500">Aluguel #{rental.id}</span>
                      <span className="rounded bg-amber-100 px-2 py-0.5 text-xs text-amber-800">
                        {RENTAL_STATUS_LABELS[rental.status as keyof typeof RENTAL_STATUS_LABELS] ?? rental.status}
                      </span>
                    </div>
                    <p className="mt-2 text-sm text-gray-700">
                      Livro #{rental.bookId} · {rental.quantity} {rental.quantity === 1 ? "cópia" : "cópias"}
                    </p>
                    <p className="text-xs text-gray-500">Cliente userId: {rental.userId}</p>
                    <p className="text-xs text-gray-500">Reservado em {formatDate(rental.reservedAt)}</p>
                    <button
                      type="button"
                      disabled={confirmingId === rental.id}
                      onClick={() => handleConfirmReturn(rental.id)}
                      className="mt-3 w-full rounded-xl bg-[#2563eb] py-2 text-sm font-medium text-white hover:bg-[#1d4ed8] disabled:opacity-50"
                    >
                      {confirmingId === rental.id ? "Confirmando…" : "Confirmar recebimento"}
                    </button>
                  </li>
                ))}
              </ul>
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
        </div>
      </main>
    </div>
  );
}
