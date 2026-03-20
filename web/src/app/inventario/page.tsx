"use client";

import { useCallback, useEffect, useState, Fragment } from "react";
import { useRouter } from "next/navigation";
import { searchBooks } from "@/lib/api/search";
import { createBook, updateBook, deleteBook, type CatalogBookRequest } from "@/lib/api/catalog";
import {
  increaseInventory,
  decreaseInventory,
  type InventoryApiResponse,
} from "@/lib/api/inventory";
import { ApiError } from "@/lib/api/errors";
import { useErrorModal } from "@/contexts/ErrorModalContext";
import { useProfile } from "@/contexts/ProfileContext";
import ProfileSwitcher from "@/components/layout/ProfileSwitcher";
import type { BookSearchResponse } from "@/lib/types/book";

const PAGE_SIZE = 25;

/** Campo de filtro com botão limpar alinhado à direita e centralizado na vertical (evita deslocamento com input number). */
function InventoryFilterInput({
  placeholder,
  value,
  onValueChange,
  type = "text",
  clearAriaLabel,
}: {
  placeholder: string;
  value: string;
  onValueChange: (v: string) => void;
  type?: "text" | "number";
  clearAriaLabel: string;
}) {
  return (
    <div className="flex min-h-[2.5rem] items-stretch overflow-hidden rounded-lg border border-gray-200 bg-white shadow-sm focus-within:border-[#2563eb] focus-within:ring-1 focus-within:ring-[#2563eb]/25">
      <input
        type={type}
        placeholder={placeholder}
        value={value}
        onChange={(e) => onValueChange(e.target.value)}
        className="min-w-0 flex-1 border-0 bg-transparent px-3 py-2 text-sm outline-none focus:ring-0 [appearance:textfield] [&::-webkit-inner-spin-button]:appearance-none [&::-webkit-outer-spin-button]:appearance-none"
      />
      {value ? (
        <button
          type="button"
          onClick={() => onValueChange("")}
          className="flex w-9 shrink-0 items-center justify-center self-stretch border-l border-gray-100 text-gray-500 transition-colors hover:bg-gray-50 hover:text-gray-800"
          aria-label={clearAriaLabel}
        >
          <span className="block text-xl leading-none">×</span>
        </button>
      ) : null}
    </div>
  );
}

function formatCreationDate(iso: string | null | undefined): string {
  if (!iso) return "—";
  try {
    const d = new Date(iso);
    const day = String(d.getDate()).padStart(2, "0");
    const month = String(d.getMonth() + 1).padStart(2, "0");
    const year = d.getFullYear();
    return `${day}/${month}/${year}`;
  } catch {
    return iso;
  }
}

type ModalKind = "book" | "book-add" | "inventory" | null;

export default function InventarioPage() {
  const { profile } = useProfile();
  const router = useRouter();
  const { showError: showErrorModal } = useErrorModal();

  const [books, setBooks] = useState<BookSearchResponse[]>([]);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);

  const [titleFilter, setTitleFilter] = useState("");
  const [authorFilter, setAuthorFilter] = useState("");
  const [yearFromFilter, setYearFromFilter] = useState("");
  const [yearToFilter, setYearToFilter] = useState("");
  const [isbnFilter, setIsbnFilter] = useState("");
  const [availableOnlyFilter, setAvailableOnlyFilter] = useState(false);
  const [activeFilter, setActiveFilter] = useState<boolean | "all">("all");

  const [expandedId, setExpandedId] = useState<string | null>(null);
  const [modalKind, setModalKind] = useState<ModalKind>(null);
  const [modalBook, setModalBook] = useState<BookSearchResponse | null>(null);

  const loadBooks = useCallback(async () => {
    setLoading(true);
    try {
      const res = await searchBooks({
        all: activeFilter === "all",
        active: activeFilter === "all" ? undefined : activeFilter,
        title: titleFilter.trim() || undefined,
        author: authorFilter.trim() || undefined,
        publishedYearFrom: yearFromFilter ? parseInt(yearFromFilter, 10) : undefined,
        publishedYearTo: yearToFilter ? parseInt(yearToFilter, 10) : undefined,
        isbn: isbnFilter.trim() || undefined,
        availableOnly: availableOnlyFilter || undefined,
        page,
        size: PAGE_SIZE,
        sortBy: "title",
        sortDir: "asc",
      });
      setBooks(res.content);
      setTotalElements(res.totalElements);
      setTotalPages(res.totalPages);
    } catch (e) {
      if (e instanceof ApiError) {
        if (e.isClientError) showErrorModal(e.message);
        else if (e.isServerError) alert(`Erro de servidor: ${e.message}`);
      }
      setBooks([]);
      setTotalElements(0);
      setTotalPages(0);
    } finally {
      setLoading(false);
    }
  }, [
    page,
    titleFilter,
    authorFilter,
    yearFromFilter,
    yearToFilter,
    isbnFilter,
    availableOnlyFilter,
    activeFilter,
    showErrorModal,
  ]);

  useEffect(() => {
    if (profile !== "gestor") {
      router.replace("/");
      return;
    }
    loadBooks();
  }, [profile, router, loadBooks]);

  const handleRowClick = useCallback((id: string) => {
    setExpandedId((prev) => (prev === id ? null : id));
  }, []);

  const openBookModal = useCallback((book: BookSearchResponse) => {
    setModalBook(book);
    setModalKind("book");
  }, []);

  const openInventoryModal = useCallback((book: BookSearchResponse) => {
    setModalBook(book);
    setModalKind("inventory");
  }, []);

  const closeModal = useCallback(() => {
    setModalKind(null);
    setModalBook(null);
  }, []);

  /** Atualiza totais na tabela a partir da resposta do inventory (imediato); o search/ES pode atrasar. */
  const patchBookInventory = useCallback((bookId: string, data: InventoryApiResponse) => {
    const bid =
      data.bookId != null ? String(data.bookId) : bookId;
    setBooks((prev) =>
      prev.map((b) =>
        String(b.id) === String(bid)
          ? {
              ...b,
              totalCopies:
                data.totalCopies !== undefined ? data.totalCopies : b.totalCopies,
              availableCopies:
                data.availableCopies !== undefined
                  ? data.availableCopies
                  : b.availableCopies,
            }
          : b
      )
    );
  }, []);

  if (profile !== "gestor") return null;

  return (
    <div className="flex h-full min-h-0 min-w-0 flex-1 flex-col overflow-hidden">
      <div className="flex-shrink-0 p-4">
        <header className="flex flex-wrap items-center justify-between gap-4 rounded-2xl bg-white px-6 py-4 shadow-sm">
          <h1 className="text-xl font-semibold text-gray-900">Inventário</h1>
          <div className="flex items-center gap-3">
            <ProfileSwitcher />
            <button
              type="button"
              onClick={() => { setModalBook(null); setModalKind("book-add"); }}
              className="rounded-xl bg-emerald-600 px-4 py-2.5 text-sm font-medium text-white hover:bg-emerald-700 transition-colors flex items-center gap-2"
            >
              <span className="text-lg">+</span>
              Adicionar livro
            </button>
          </div>
        </header>
      </div>

      <main className="flex-1 overflow-y-auto px-4 pb-6">
        {/* Filtros */}
        <section className="mb-4 rounded-2xl bg-white p-4 shadow-sm">
          <div className="grid grid-cols-2 gap-3 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6">
            <InventoryFilterInput
              placeholder="Título"
              value={titleFilter}
              onValueChange={(v) => { setTitleFilter(v); setPage(0); }}
              clearAriaLabel="Limpar título"
            />
            <InventoryFilterInput
              placeholder="Autor"
              value={authorFilter}
              onValueChange={(v) => { setAuthorFilter(v); setPage(0); }}
              clearAriaLabel="Limpar autor"
            />
            <InventoryFilterInput
              placeholder="Ano de"
              value={yearFromFilter}
              type="number"
              onValueChange={(v) => { setYearFromFilter(v); setPage(0); }}
              clearAriaLabel="Limpar ano de"
            />
            <InventoryFilterInput
              placeholder="Ano até"
              value={yearToFilter}
              type="number"
              onValueChange={(v) => { setYearToFilter(v); setPage(0); }}
              clearAriaLabel="Limpar ano até"
            />
            <InventoryFilterInput
              placeholder="ISBN"
              value={isbnFilter}
              onValueChange={(v) => { setIsbnFilter(v); setPage(0); }}
              clearAriaLabel="Limpar ISBN"
            />
            <div className="flex flex-col gap-3 sm:col-span-2 md:col-span-2 lg:col-span-1 lg:flex-row lg:flex-wrap lg:items-center">
              <label className="flex items-center gap-2 text-sm whitespace-nowrap">
                <input
                  type="checkbox"
                  checked={availableOnlyFilter}
                  onChange={(e) => { setAvailableOnlyFilter(e.target.checked); setPage(0); }}
                  className="rounded border-gray-300"
                />
                Disponíveis
              </label>
              <div className="flex min-h-[2.5rem] min-w-0 flex-1 items-stretch overflow-hidden rounded-lg border border-gray-200 bg-white shadow-sm focus-within:border-[#2563eb] focus-within:ring-1 focus-within:ring-[#2563eb]/25">
                <select
                  value={activeFilter === "all" ? "all" : activeFilter ? "true" : "false"}
                  onChange={(e) => {
                    const v = e.target.value;
                    setActiveFilter(v === "all" ? "all" : v === "true");
                    setPage(0);
                  }}
                  className="min-w-0 flex-1 cursor-pointer border-0 bg-transparent px-3 py-2 text-sm outline-none focus:ring-0"
                >
                  <option value="all">Todos (ativos e inativos)</option>
                  <option value="true">Ativos</option>
                  <option value="false">Inativos</option>
                </select>
                {activeFilter !== "all" ? (
                  <button
                    type="button"
                    onClick={() => { setActiveFilter("all"); setPage(0); }}
                    className="flex w-9 shrink-0 items-center justify-center self-stretch border-l border-gray-100 text-gray-500 transition-colors hover:bg-gray-50 hover:text-gray-800"
                    aria-label="Limpar status"
                  >
                    <span className="block text-xl leading-none">×</span>
                  </button>
                ) : null}
              </div>
            </div>
          </div>
        </section>

        {/* Tabela */}
        <section className="rounded-2xl bg-white shadow-sm overflow-hidden">
          {loading ? (
            <div className="p-8 text-center text-sm text-gray-500">Carregando…</div>
          ) : books.length === 0 ? (
            <div className="p-8 text-center text-sm text-gray-500">Nenhum livro encontrado.</div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full min-w-[800px]">
                <thead>
                  <tr className="border-b border-gray-200 bg-gray-50/80">
                    <th className="w-10 px-2 py-3 text-center text-xs font-semibold text-gray-600 uppercase tracking-wider" aria-hidden />
                    <th className="px-4 py-3 text-center text-xs font-semibold text-gray-600 uppercase tracking-wider">Título</th>
                    <th className="px-4 py-3 text-center text-xs font-semibold text-gray-600 uppercase tracking-wider">Autor</th>
                    <th className="px-4 py-3 text-center text-xs font-semibold text-gray-600 uppercase tracking-wider">Ano de publicação</th>
                    <th className="px-4 py-3 text-center text-xs font-semibold text-gray-600 uppercase tracking-wider">Data de criação</th>
                    <th className="px-4 py-3 text-center text-xs font-semibold text-gray-600 uppercase tracking-wider">Total de cópias</th>
                    <th className="px-4 py-3 text-center text-xs font-semibold text-gray-600 uppercase tracking-wider">Cópias disponíveis</th>
                    <th className="px-4 py-3 text-center text-xs font-semibold text-gray-600 uppercase tracking-wider">Status</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100">
                  {books.map((book) => (
                    <Fragment key={book.id}>
                      <tr
                        key={book.id}
                        onClick={() => handleRowClick(book.id)}
                        className="cursor-pointer hover:bg-gray-50/80 transition-colors"
                      >
                        <td className="w-10 px-2 py-3 text-center text-gray-500">
                          <span className="inline-block text-sm" aria-hidden>
                            {expandedId === book.id ? "▼" : "▶"}
                          </span>
                        </td>
                        <td className="px-4 py-3 text-sm text-center text-gray-900">{book.title}</td>
                        <td className="px-4 py-3 text-sm text-center text-gray-700">{book.author}</td>
                        <td className="px-4 py-3 text-sm text-center text-gray-600">{book.publishedYear ?? "—"}</td>
                        <td className="px-4 py-3 text-sm text-center text-gray-600">{formatCreationDate(book.createdAt)}</td>
                        <td className="px-4 py-3 text-sm text-center text-gray-600">{book.totalCopies ?? "—"}</td>
                        <td className="px-4 py-3 text-sm text-center text-gray-600">{book.availableCopies ?? "—"}</td>
                        <td className="px-4 py-3 text-center">
                          <span
                            className={`inline-flex rounded-full px-2.5 py-0.5 text-xs font-medium ${
                              book.active
                                ? "bg-green-100 text-green-800"
                                : "bg-red-100 text-red-800"
                            }`}
                          >
                            {book.active ? "ATIVO" : "INATIVO"}
                          </span>
                        </td>
                      </tr>
                      {expandedId === book.id && (
                        <tr key={`${book.id}-expanded`} className="bg-gray-50/80">
                          <td colSpan={8} className="px-4 py-3">
                            <div className="flex gap-3">
                              <button
                                type="button"
                                disabled={!book.active}
                                onClick={(e) => { e.stopPropagation(); openBookModal(book); }}
                                className="rounded-xl bg-[#2563eb] px-4 py-2 text-sm font-medium text-white hover:bg-[#1d4ed8] disabled:opacity-50 disabled:cursor-not-allowed"
                              >
                                Alterar livro
                              </button>
                              <button
                                type="button"
                                disabled={!book.active}
                                onClick={(e) => { e.stopPropagation(); openInventoryModal(book); }}
                                className="rounded-xl border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                              >
                                Alterar inventário
                              </button>
                            </div>
                          </td>
                        </tr>
                      )}
                    </Fragment>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          {totalPages > 1 && (
            <div className="flex items-center justify-between border-t border-gray-200 px-4 py-3">
              <div className="text-sm text-gray-600">
                Mostrando {(page * PAGE_SIZE) + 1} a {Math.min((page + 1) * PAGE_SIZE, totalElements)} de {totalElements} itens
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
                <span className="flex items-center px-2 text-sm text-gray-600">
                  Página {page + 1} de {totalPages}
                </span>
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
        </section>
      </main>

      {(modalKind === "book" && modalBook) || modalKind === "book-add" ? (
        <BookEditModal
          book={modalKind === "book-add" ? null : modalBook}
          onClose={closeModal}
          onSuccess={loadBooks}
          showErrorModal={showErrorModal}
        />
      ) : null}
      {modalKind === "inventory" && modalBook && (
        <InventoryEditModal
          book={modalBook}
          onClose={closeModal}
          onPatchInventory={patchBookInventory}
          onSearchResync={() => {
            void loadBooks();
            window.setTimeout(() => void loadBooks(), 1200);
          }}
          showErrorModal={showErrorModal}
        />
      )}
    </div>
  );
}

function BookEditModal({
  book,
  onClose,
  onSuccess,
  showErrorModal,
}: {
  book: BookSearchResponse | null;
  onClose: () => void;
  onSuccess: () => void;
  showErrorModal: (msg: string) => void;
}) {
  const isAdd = book === null;
  const [title, setTitle] = useState(book?.title ?? "");
  const [author, setAuthor] = useState(book?.author ?? "");
  const [category, setCategory] = useState(book?.category ?? "");
  const [genre, setGenre] = useState(book?.genre ?? "");
  const [description, setDescription] = useState(book?.description ?? "");
  const [isbn, setIsbn] = useState(book?.isbn ?? "");
  const [publishedYear, setPublishedYear] = useState(book?.publishedYear?.toString() ?? "");
  const [removeFromCatalog, setRemoveFromCatalog] = useState(false);
  const [saving, setSaving] = useState(false);
  const [titleError, setTitleError] = useState(false);
  const [authorError, setAuthorError] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const titleValid = title.trim().length > 0;
    const authorValid = author.trim().length > 0;
    setTitleError(!titleValid);
    setAuthorError(!authorValid);
    if (isAdd) {
      if (!titleValid || !authorValid) return;
      setSaving(true);
      try {
        const payload: CatalogBookRequest = {
          title: title.trim(),
          author: author.trim(),
          category: category.trim() || null,
          genre: genre.trim() || null,
          description: description.trim() || null,
          isbn: isbn.trim() || null,
          publishedYear: publishedYear ? parseInt(publishedYear, 10) : null,
        };
        await createBook(payload);
        onSuccess();
        onClose();
      } catch (err) {
        if (err instanceof ApiError) {
          if (err.isClientError) showErrorModal(err.message);
          else if (err.isServerError) alert(`Erro de servidor: ${err.message}`);
        }
      } finally {
        setSaving(false);
      }
      return;
    }
    setSaving(true);
    try {
      if (removeFromCatalog && book) {
        await deleteBook(book.id);
      } else if (book) {
        const payload: CatalogBookRequest = {
          title: title.trim(),
          author: author.trim(),
          category: category.trim() || null,
          genre: genre.trim() || null,
          description: description.trim() || null,
          isbn: isbn.trim() || null,
          publishedYear: publishedYear ? parseInt(publishedYear, 10) : null,
        };
        await updateBook(book.id, payload);
      }
      onSuccess();
      onClose();
    } catch (err) {
      if (err instanceof ApiError) {
        if (err.isClientError) showErrorModal(err.message);
        else if (err.isServerError) alert(`Erro de servidor: ${err.message}`);
      }
    } finally {
      setSaving(false);
    }
  };

  const disabled = !isAdd && removeFromCatalog;
  const inputBorder = (isRequiredError: boolean) =>
    isRequiredError ? "border-red-500 focus:ring-red-500" : "border-gray-200";

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4" onClick={onClose}>
      <div className="max-h-[90vh] w-full max-w-2xl overflow-y-auto rounded-2xl bg-white shadow-xl" onClick={(e) => e.stopPropagation()}>
        <div className="border-b border-gray-200 px-6 py-4">
          <h2 className="text-lg font-semibold text-gray-900">{isAdd ? "Adicionar livro" : "Alterar livro"}</h2>
        </div>
        <form onSubmit={handleSubmit} className="p-6">
          <div className="grid grid-cols-4 gap-4">
            <div>
              <label className="mb-1 block text-xs font-medium text-gray-600">{isAdd ? "Título*" : "Título"}</label>
              <input
                type="text"
                value={title}
                onChange={(e) => { setTitle(e.target.value); if (isAdd) setTitleError(false); }}
                disabled={disabled}
                className={`w-full rounded-lg border px-3 py-2 text-sm disabled:bg-gray-100 ${inputBorder(isAdd && titleError)}`}
                required={!isAdd}
              />
            </div>
            <div>
              <label className="mb-1 block text-xs font-medium text-gray-600">{isAdd ? "Autor*" : "Autor"}</label>
              <input
                type="text"
                value={author}
                onChange={(e) => { setAuthor(e.target.value); if (isAdd) setAuthorError(false); }}
                disabled={disabled}
                className={`w-full rounded-lg border px-3 py-2 text-sm disabled:bg-gray-100 ${inputBorder(isAdd && authorError)}`}
                required={!isAdd}
              />
            </div>
            <div>
              <label className="mb-1 block text-xs font-medium text-gray-600">Categoria</label>
              <input type="text" value={category} onChange={(e) => setCategory(e.target.value)} disabled={disabled} className="w-full rounded-lg border border-gray-200 px-3 py-2 text-sm disabled:bg-gray-100" />
            </div>
            <div>
              <label className="mb-1 block text-xs font-medium text-gray-600">Gênero</label>
              <input type="text" value={genre} onChange={(e) => setGenre(e.target.value)} disabled={disabled} className="w-full rounded-lg border border-gray-200 px-3 py-2 text-sm disabled:bg-gray-100" />
            </div>
            <div className="col-span-4">
              <label className="mb-1 block text-xs font-medium text-gray-600">Descrição</label>
              <textarea value={description} onChange={(e) => setDescription(e.target.value)} disabled={disabled} rows={2} className="w-full rounded-lg border border-gray-200 px-3 py-2 text-sm disabled:bg-gray-100" />
            </div>
            <div>
              <label className="mb-1 block text-xs font-medium text-gray-600">ISBN</label>
              <input type="text" value={isbn} onChange={(e) => setIsbn(e.target.value)} disabled={disabled} className="w-full rounded-lg border border-gray-200 px-3 py-2 text-sm disabled:bg-gray-100" />
            </div>
            <div>
              <label className="mb-1 block text-xs font-medium text-gray-600">Ano de publicação</label>
              <input type="number" value={publishedYear} onChange={(e) => setPublishedYear(e.target.value)} disabled={disabled} className="w-full rounded-lg border border-gray-200 px-3 py-2 text-sm disabled:bg-gray-100" />
            </div>
            {!isAdd && (
              <div className="col-span-4">
                <label className="flex items-center gap-2 text-sm">
                  <input type="checkbox" checked={removeFromCatalog} onChange={(e) => setRemoveFromCatalog(e.target.checked)} className="rounded border-gray-300" />
                  Deseja remover esse livro do catálogo?
                </label>
              </div>
            )}
          </div>
          <div className="mt-6 flex justify-end">
            <button type="button" onClick={onClose} className="mr-3 rounded-xl border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50">
              Cancelar
            </button>
            <button type="submit" disabled={saving} className="rounded-xl bg-[#2563eb] px-4 py-2 text-sm font-medium text-white hover:bg-[#1d4ed8] disabled:opacity-50">
              {saving ? (isAdd ? "Adicionando…" : "Atualizando…") : isAdd ? "Adicionar" : "Atualizar"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

function InventoryEditModal({
  book,
  onClose,
  onPatchInventory,
  onSearchResync,
  showErrorModal,
}: {
  book: BookSearchResponse;
  onClose: () => void;
  onPatchInventory: (bookId: string, data: InventoryApiResponse) => void;
  /** Recarrega lista a partir do search (e repete após atraso para o ES indexar). */
  onSearchResync: () => void;
  showErrorModal: (msg: string) => void;
}) {
  const total = book.totalCopies ?? 0;
  const available = book.availableCopies ?? 0;
  const quantityRented = total - available;
  const [editingTotal, setEditingTotal] = useState(total);
  const [saving, setSaving] = useState(false);

  const minTotal = quantityRented;
  const delta = editingTotal - total;
  const isIncreased = delta > 0;
  const isDecreased = delta < 0;

  const handleUpdate = async () => {
    if (delta === 0) {
      onClose();
      return;
    }
    setSaving(true);
    try {
      const data: InventoryApiResponse = isIncreased
        ? await increaseInventory(book.id, delta)
        : await decreaseInventory(book.id, -delta);
      onPatchInventory(String(book.id), data);
      onSearchResync();
      onClose();
    } catch (err) {
      if (err instanceof ApiError) {
        if (err.isClientError) showErrorModal(err.message);
        else if (err.isServerError) alert(`Erro de servidor: ${err.message}`);
      }
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4" onClick={onClose}>
      <div className="w-full max-w-md rounded-2xl bg-white p-6 shadow-xl" onClick={(e) => e.stopPropagation()}>
        <h2 className="text-lg font-semibold text-gray-900 mb-4">Alterar inventário</h2>
        <div className="space-y-2 mb-4">
          <p className="text-sm text-gray-700">
            <span className="font-medium text-gray-600">Quantidade total:</span>{" "}
            <span className={`text-xl font-semibold ${isDecreased ? "text-red-600" : isIncreased ? "text-green-600" : "text-gray-900"}`}>
              {editingTotal}
            </span>
          </p>
          <p className="text-sm text-gray-700">
            <span className="font-medium text-gray-600">Quantidade alugada:</span>{" "}
            <span className="font-medium">{quantityRented}</span>
          </p>
        </div>
        <div className="flex items-center gap-4">
          <button
            type="button"
            onClick={() => setEditingTotal((t) => Math.max(minTotal, t - 1))}
            disabled={editingTotal <= minTotal}
            className="rounded-xl border-2 border-gray-300 bg-white px-4 py-2 text-lg font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-50"
          >
            −
          </button>
          <span
            className={`text-2xl font-bold min-w-[3rem] text-center ${
              isDecreased ? "text-red-600" : isIncreased ? "text-green-600" : "text-gray-900"
            }`}
          >
            {editingTotal}
          </span>
          <button
            type="button"
            onClick={() => setEditingTotal((t) => t + 1)}
            className="rounded-xl border-2 border-gray-300 bg-white px-4 py-2 text-lg font-medium text-gray-700 hover:bg-gray-50"
          >
            +
          </button>
        </div>
        <div className="mt-6 flex justify-end">
          <button type="button" onClick={onClose} className="mr-3 rounded-xl border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50">
            Cancelar
          </button>
          <button
            type="button"
            onClick={handleUpdate}
            disabled={saving || delta === 0}
            className="rounded-xl bg-[#2563eb] px-4 py-2 text-sm font-medium text-white hover:bg-[#1d4ed8] disabled:opacity-50"
          >
            {saving ? "Atualizando…" : "Atualizar"}
          </button>
        </div>
      </div>
    </div>
  );
}
