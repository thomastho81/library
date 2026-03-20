"use client";

import { useCallback, useEffect, useState, Fragment } from "react";
import { useRouter } from "next/navigation";
import { listClients } from "@/lib/api/users";
import { listRentalsByUser } from "@/lib/api/rental";
import { getBookById } from "@/lib/api/search";
import { ApiError } from "@/lib/api/errors";
import { useErrorModal } from "@/contexts/ErrorModalContext";
import { useProfile } from "@/contexts/ProfileContext";
import type { UserResponse } from "@/lib/types/users";
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

export default function ClientesPage() {
  const { profile } = useProfile();
  const router = useRouter();
  const { showError: showErrorModal } = useErrorModal();
  const [users, setUsers] = useState<UserResponse[]>([]);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);

  const [expandedUserId, setExpandedUserId] = useState<number | null>(null);
  const [clientRentals, setClientRentals] = useState<Record<number, RentalResponse[]>>({});
  const [rentalBookTitles, setRentalBookTitles] = useState<Record<number, string>>({});
  const [loadingRentalsFor, setLoadingRentalsFor] = useState<number | null>(null);

  const loadClients = useCallback(async () => {
    setLoading(true);
    try {
      const res = await listClients({ page, size: PAGE_SIZE });
      setUsers(res.content);
      setTotalPages(res.totalPages);
      setTotalElements(res.totalElements);
    } catch (e) {
      if (e instanceof ApiError) {
        if (e.isClientError) showErrorModal(e.message);
        else if (e.isServerError) alert(`Erro de servidor: ${e.message}`);
      }
      setUsers([]);
      setTotalPages(0);
      setTotalElements(0);
    } finally {
      setLoading(false);
    }
  }, [page, showErrorModal]);

  useEffect(() => {
    if (profile !== "gestor") {
      router.replace("/");
      return;
    }
    loadClients();
  }, [profile, router, loadClients]);

  const loadRentalsForUser = useCallback(
    async (userId: number) => {
      if (clientRentals[userId]) return;
      setLoadingRentalsFor(userId);
      try {
        const res = await listRentalsByUser({
          userId,
          page: 0,
          size: 100,
          sort: "reservedAt,desc",
        });
        setClientRentals((prev) => ({ ...prev, [userId]: res.content }));
        const bookIds = [...new Set(res.content.map((r) => r.bookId))];
        const results = await Promise.all(
          bookIds.map((id) =>
            getBookById(String(id))
              .then((b) => ({ id, title: b?.title ?? null }))
              .catch(() => ({ id, title: null }))
          )
        );
        setRentalBookTitles((prev) => {
          const next = { ...prev };
          results.forEach(({ id, title }) => {
            if (title) next[id] = title;
          });
          return next;
        });
      } catch (e) {
        if (e instanceof ApiError) {
          if (e.isClientError) showErrorModal(e.message);
          else if (e.isServerError) alert(`Erro de servidor: ${e.message}`);
        }
        setClientRentals((prev) => ({ ...prev, [userId]: [] }));
      } finally {
        setLoadingRentalsFor(null);
      }
    },
    [clientRentals, showErrorModal]
  );

  const handleRowClick = useCallback(
    (userId: number) => {
      const willExpand = expandedUserId !== userId;
      setExpandedUserId((prev) => (prev === userId ? null : userId));
      if (willExpand) loadRentalsForUser(userId);
    },
    [expandedUserId, loadRentalsForUser]
  );

  if (profile !== "gestor") return null;

  return (
    <div className="flex flex-1 flex-col overflow-hidden">
      <div className="flex-shrink-0 p-4">
        <header className="rounded-2xl bg-white px-6 py-4 shadow-sm">
          <h1 className="text-xl font-semibold text-gray-900">Clientes</h1>
          <p className="mt-1 text-sm text-gray-500">
            Usuários que não são gestores. Clique na linha para ver os aluguéis do cliente.
          </p>
        </header>
      </div>
      <main className="flex-1 overflow-y-auto px-4 pb-6">
        <div className="rounded-2xl bg-white shadow-sm overflow-hidden">
          {loading ? (
            <div className="space-y-3 p-6">
              {[1, 2, 3, 4, 5].map((i) => (
                <div key={i} className="h-14 animate-pulse rounded-xl bg-gray-100" />
              ))}
            </div>
          ) : users.length === 0 ? (
            <p className="py-8 text-center text-sm text-gray-500">Nenhum cliente encontrado.</p>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full min-w-[500px]">
                <thead>
                  <tr className="border-b border-gray-200 bg-gray-50/80">
                    <th className="w-10 px-2 py-3 text-center text-xs font-semibold text-gray-600 uppercase tracking-wider" aria-hidden />
                    <th className="px-4 py-3 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Nome</th>
                    <th className="px-4 py-3 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">Email</th>
                    <th className="px-4 py-3 text-center text-xs font-semibold text-gray-600 uppercase tracking-wider">Idade</th>
                    <th className="px-4 py-3 text-center text-xs font-semibold text-gray-600 uppercase tracking-wider">Status</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100">
                  {users.map((user) => (
                    <Fragment key={user.id}>
                      <tr
                        onClick={() => handleRowClick(user.id)}
                        className="cursor-pointer hover:bg-gray-50/80 transition-colors"
                      >
                        <td className="w-10 px-2 py-3 text-center text-gray-500">
                          <span className="inline-block text-sm" aria-hidden>
                            {expandedUserId === user.id ? "▼" : "▶"}
                          </span>
                        </td>
                        <td className="px-4 py-3 text-sm font-medium text-gray-900">{user.name}</td>
                        <td className="px-4 py-3 text-sm text-gray-600">{user.email}</td>
                        <td className="px-4 py-3 text-sm text-center text-gray-600">{user.age}</td>
                        <td className="px-4 py-3 text-center">
                          <span
                            className={`inline-flex rounded-full px-2.5 py-0.5 text-xs font-medium ${
                              user.active ? "bg-green-100 text-green-800" : "bg-red-100 text-red-800"
                            }`}
                          >
                            {user.active ? "Ativo" : "Inativo"}
                          </span>
                        </td>
                      </tr>
                      {expandedUserId === user.id && (
                        <tr className="bg-gray-50/80">
                          <td colSpan={5} className="px-4 py-3">
                            {loadingRentalsFor === user.id ? (
                              <p className="text-sm text-gray-500">Carregando aluguéis…</p>
                            ) : (
                              <div>
                                <p className="mb-2 text-xs font-semibold text-gray-600 uppercase tracking-wider">
                                  Aluguéis do cliente
                                </p>
                                {(clientRentals[user.id]?.length ?? 0) === 0 ? (
                                  <p className="text-sm text-gray-500">Nenhum aluguel.</p>
                                ) : (
                                  <table className="w-full text-sm">
                                    <thead>
                                      <tr className="border-b border-gray-200 text-left">
                                        <th className="py-2 pr-4 font-medium text-gray-600">Livro</th>
                                        <th className="py-2 pr-4 font-medium text-gray-600">Quantidade</th>
                                        <th className="py-2 pr-4 font-medium text-gray-600">Status</th>
                                        <th className="py-2 font-medium text-gray-600">Data reserva</th>
                                      </tr>
                                    </thead>
                                    <tbody className="divide-y divide-gray-100">
                                      {clientRentals[user.id]?.map((rental) => (
                                        <tr key={rental.id}>
                                          <td className="py-2 pr-4 text-gray-900">
                                            {rentalBookTitles[rental.bookId] ?? `Livro #${rental.bookId}`}
                                          </td>
                                          <td className="py-2 pr-4 text-gray-600">{rental.quantity}</td>
                                          <td className="py-2 pr-4">
                                            <span className="rounded-full bg-gray-100 px-2 py-0.5 text-xs text-gray-700">
                                              {RENTAL_STATUS_LABELS[rental.status as keyof typeof RENTAL_STATUS_LABELS] ?? rental.status}
                                            </span>
                                          </td>
                                          <td className="py-2 text-gray-600">{formatDate(rental.reservedAt)}</td>
                                        </tr>
                                      ))}
                                    </tbody>
                                  </table>
                                )}
                              </div>
                            )}
                          </td>
                        </tr>
                      )}
                    </Fragment>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          {totalPages > 1 && !loading && users.length > 0 && (
            <div className="flex items-center justify-between border-t border-gray-200 px-4 py-3">
              <div className="text-sm text-gray-600">
                Página {page + 1} de {totalPages} ({totalElements} itens)
              </div>
              <div className="flex gap-2">
                <button
                  type="button"
                  onClick={() => setPage((p) => Math.max(0, p - 1))}
                  disabled={page === 0}
                  className="rounded-lg border border-gray-300 bg-white px-3 py-1.5 text-sm disabled:opacity-50"
                >
                  Anterior
                </button>
                <button
                  type="button"
                  onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
                  disabled={page >= totalPages - 1}
                  className="rounded-lg border border-gray-300 bg-white px-3 py-1.5 text-sm disabled:opacity-50"
                >
                  Próxima
                </button>
              </div>
            </div>
          )}
        </div>
      </main>
    </div>
  );
}
