"use client";

import { useCallback, useEffect, useRef, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { searchBooks } from "@/lib/api/search";
import { reserveRental } from "@/lib/api/rental";
import { ApiError } from "@/lib/api/errors";
import { useErrorModal } from "@/contexts/ErrorModalContext";
import { useProfile } from "@/contexts/ProfileContext";
import type { Book } from "@/lib/types/book";
import BookCard from "@/components/books/BookCard";
import BookDetailPanel from "@/components/layout/BookDetailPanel";
import Header from "@/components/layout/Header";
import RentalModal from "@/components/rental/RentalModal";
import ReserveSuccessModal from "@/components/rental/ReserveSuccessModal";
import YearRangeInput from "@/components/filters/YearRangeInput";

const PAGE_SIZE = 25;
const SORT_OPTIONS = [
  { value: "title", label: "Título" },
  { value: "author", label: "Autor" },
  { value: "publishedYear", label: "Ano" },
  { value: "updatedAt", label: "Atualização" },
  { value: "availableCopies", label: "Disponibilidade" },
];
const SORT_DIR_OPTIONS = [
  { value: "asc", label: "Ascendente (A→Z, 0→9)" },
  { value: "desc", label: "Descendente (Z→A, 9→0)" },
];

export default function PesquisaPageClient() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const { showError: showErrorModal } = useErrorModal();
  const { profile, userId } = useProfile();
  const [searchQuery, setSearchQuery] = useState("");
  const [category, setCategory] = useState("");
  const [genre, setGenre] = useState("");
  const [title, setTitle] = useState("");
  const [author, setAuthor] = useState("");
  const [isbn, setIsbn] = useState("");
  const [publishedYearFrom, setPublishedYearFrom] = useState<string>("");
  const [publishedYearTo, setPublishedYearTo] = useState<string>("");
  const [availableOnly, setAvailableOnly] = useState(false);
  const [sortBy, setSortBy] = useState("title");
  const [sortDir, setSortDir] = useState<"asc" | "desc">("asc");
  const [page, setPage] = useState(0);

  const [books, setBooks] = useState<Book[]>([]);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(false);
  const [loadingMore, setLoadingMore] = useState(false);
  const [selectedBook, setSelectedBook] = useState<Book | null>(null);
  const [rentalModalBook, setRentalModalBook] = useState<Book | null>(null);
  const [showReserveSuccessModal, setShowReserveSuccessModal] = useState(false);
  const sentinelRef = useRef<HTMLDivElement>(null);

  const hasMore = totalPages > 0 && page < totalPages - 1;

  const runSearch = useCallback(
    async (pageNum: number = 0, append: boolean = false) => {
      if (append) {
        setLoadingMore(true);
      } else {
        setLoading(true);
      }
      try {
        const res = await searchBooks({
          q: searchQuery?.trim() || undefined,
          category: category?.trim() || undefined,
          genre: genre?.trim() || undefined,
          title: title?.trim() || undefined,
          author: author?.trim() || undefined,
          isbn: isbn?.trim() || undefined,
          publishedYearFrom: publishedYearFrom ? parseInt(publishedYearFrom, 10) : undefined,
          publishedYearTo: publishedYearTo ? parseInt(publishedYearTo, 10) : undefined,
          availableOnly: availableOnly || undefined,
          sortBy: sortBy || undefined,
          sortDir: sortDir || undefined,
          page: pageNum,
          size: PAGE_SIZE,
        });
        if (append) {
          setBooks((prev) => [...prev, ...res.content]);
        } else {
          setBooks(res.content);
        }
        setTotalElements(res.totalElements);
        setTotalPages(res.totalPages);
        setPage(pageNum);
      } catch (e) {
        console.error("[Pesquisa] runSearch falhou:", e);
        if (!append) {
          setBooks([]);
          setTotalElements(0);
          setTotalPages(0);
        }
        if (e instanceof ApiError) {
          if (e.isClientError) showErrorModal(e.message);
          else if (e.isServerError) alert(`Erro de servidor: ${e.message}`);
        }
      } finally {
        setLoading(false);
        setLoadingMore(false);
      }
    },
    [
      searchQuery,
      category,
      genre,
      title,
      author,
      isbn,
      publishedYearFrom,
      publishedYearTo,
      availableOnly,
      sortBy,
      sortDir,
      showErrorModal,
    ]
  );

  const handleBuscar = useCallback(() => runSearch(0), [runSearch]);

  // Inicialização a partir da URL (ex.: /pesquisa?q=... ou ?category=... ou ?sortBy=updatedAt&sortDir=desc)
  useEffect(() => {
    const q = searchParams.get("q");
    const cat = searchParams.get("category");
    const urlSortBy = searchParams.get("sortBy");
    const urlSortDir = searchParams.get("sortDir");
    if (q != null) setSearchQuery(q);
    if (cat != null) setCategory(cat);
    if (urlSortBy != null && ["title", "author", "publishedYear", "updatedAt", "availableCopies"].includes(urlSortBy)) {
      setSortBy(urlSortBy);
    }
    if (urlSortDir === "asc" || urlSortDir === "desc") setSortDir(urlSortDir);
    const hasParams = q != null || cat != null || urlSortBy != null || urlSortDir != null;
    if (hasParams) {
      setLoading(true);
      searchBooks({
        q: q ?? undefined,
        category: cat ?? undefined,
        page: 0,
        size: PAGE_SIZE,
        sortBy: (urlSortBy != null && ["title", "author", "publishedYear", "updatedAt", "availableCopies"].includes(urlSortBy)) ? urlSortBy : "title",
        sortDir: urlSortDir === "asc" || urlSortDir === "desc" ? urlSortDir : "asc",
      })
        .then((res) => {
          setBooks(res.content);
          setTotalElements(res.totalElements);
          setTotalPages(res.totalPages);
          setPage(0);
        })
        .catch((e) => {
          setBooks([]);
          setTotalElements(0);
          setTotalPages(0);
          if (e instanceof ApiError) {
            if (e.isClientError) showErrorModal(e.message);
            else if (e.isServerError) alert(`Erro de servidor: ${e.message}`);
          }
        })
        .finally(() => setLoading(false));
    }
  }, [searchParams, showErrorModal]);

  // Infinite scroll: carregar mais 25 quando o sentinela entrar na viewport
  useEffect(() => {
    const sentinel = sentinelRef.current;
    if (!sentinel || !hasMore || loading || loadingMore) return;
    const observer = new IntersectionObserver(
      (entries) => {
        if (!entries[0]?.isIntersecting) return;
        runSearch(page + 1, true);
      },
      { rootMargin: "200px", threshold: 0 }
    );
    observer.observe(sentinel);
    return () => observer.disconnect();
  }, [hasMore, loading, loadingMore, page, runSearch]);

  const handleSearchSubmit = useCallback(() => {
    runSearch(0);
  }, [runSearch]);

  const handleRentalConfirm = useCallback(
    async (bookId: string, quantity: number) => {
      const bookIdNum = Number(bookId);
      if (!Number.isInteger(bookIdNum) || bookIdNum < 1) return;
      if (quantity < 1) return;
      try {
        await reserveRental({ userId, bookId: bookIdNum, quantity });
        setRentalModalBook(null);
        setShowReserveSuccessModal(true);
      } catch (e) {
        console.error("[Pesquisa] reserva falhou:", e);
        if (e instanceof ApiError) {
          if (e.isClientError) showErrorModal(e.message);
          else if (e.isServerError) alert(`Erro de servidor: ${e.message}`);
        } else if (e instanceof Error) alert(e.message);
      }
    },
    [showErrorModal, userId]
  );

  return (
    <div className="flex h-full min-h-0 min-w-0 flex-1 gap-0 overflow-x-auto overflow-y-hidden md:overflow-hidden">
      <div className="flex min-h-0 min-w-0 flex-1 flex-col overflow-hidden bg-gray-50/50">
        <div className="flex-shrink-0 p-4">
          <Header
            searchQuery={searchQuery}
            onSearchChange={setSearchQuery}
            onSearchSubmit={handleSearchSubmit}
          />
        </div>

        <main className="flex-1 overflow-y-auto px-4 pb-6">
          {searchQuery && (
            <p className="mb-4 text-sm text-gray-600">
              Resultados para: <strong>&quot;{searchQuery}&quot;</strong>
            </p>
          )}

          {/* Filtros */}
          <section className="mb-6 rounded-2xl bg-white p-4 shadow-sm">
            <h3 className="mb-3 text-sm font-semibold text-gray-900">Filtros</h3>
            <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
              <div>
                <label className="mb-1 block text-xs font-medium text-gray-600">Categoria</label>
                <input
                  type="text"
                  value={category}
                  onChange={(e) => setCategory(e.target.value)}
                  placeholder="Ex.: Ficção"
                  className="w-full rounded-lg border border-gray-200 px-3 py-2 text-sm"
                />
              </div>
              <div>
                <label className="mb-1 block text-xs font-medium text-gray-600">Gênero</label>
                <input
                  type="text"
                  value={genre}
                  onChange={(e) => setGenre(e.target.value)}
                  placeholder="Ex.: Romance"
                  className="w-full rounded-lg border border-gray-200 px-3 py-2 text-sm"
                />
              </div>
              <div>
                <label className="mb-1 block text-xs font-medium text-gray-600">Título</label>
                <input
                  type="text"
                  value={title}
                  onChange={(e) => setTitle(e.target.value)}
                  placeholder="Trecho do título"
                  className="w-full rounded-lg border border-gray-200 px-3 py-2 text-sm"
                />
              </div>
              <div>
                <label className="mb-1 block text-xs font-medium text-gray-600">Autor</label>
                <input
                  type="text"
                  value={author}
                  onChange={(e) => setAuthor(e.target.value)}
                  placeholder="Trecho do autor"
                  className="w-full rounded-lg border border-gray-200 px-3 py-2 text-sm"
                />
              </div>
              <div>
                <label className="mb-1 block text-xs font-medium text-gray-600">ISBN</label>
                <input
                  type="text"
                  value={isbn}
                  onChange={(e) => setIsbn(e.target.value)}
                  placeholder="ISBN exato"
                  className="w-full rounded-lg border border-gray-200 px-3 py-2 text-sm"
                />
              </div>
              <YearRangeInput
                valueFrom={publishedYearFrom}
                valueTo={publishedYearTo}
                onChangeFrom={setPublishedYearFrom}
                onChangeTo={setPublishedYearTo}
              />
              {/* Última linha: checkbox, ordenação com direção e botão Buscar */}
              <div className="flex flex-wrap items-end gap-4 sm:col-span-2 lg:col-span-3">
                <label className="flex cursor-pointer items-center gap-2">
                  <input
                    type="checkbox"
                    checked={availableOnly}
                    onChange={(e) => setAvailableOnly(e.target.checked)}
                    className="rounded border-gray-300"
                  />
                  <span className="text-sm text-gray-700">
                    Apenas com disponibilidade
                  </span>
                </label>
                <div className="min-w-[140px]">
                  <label className="mb-1 block text-xs font-medium text-gray-600">Ordenar por</label>
                  <select
                    value={sortBy}
                    onChange={(e) => setSortBy(e.target.value)}
                    className="w-full rounded-lg border border-gray-200 px-3 py-2 text-sm"
                  >
                    {SORT_OPTIONS.map((opt) => (
                      <option key={opt.value} value={opt.value}>
                        {opt.label}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="min-w-[180px]">
                  <label className="mb-1 block text-xs font-medium text-gray-600">Direção</label>
                  <select
                    value={sortDir}
                    onChange={(e) => setSortDir(e.target.value as "asc" | "desc")}
                    className="w-full rounded-lg border border-gray-200 px-3 py-2 text-sm"
                  >
                    {SORT_DIR_OPTIONS.map((opt) => (
                      <option key={opt.value} value={opt.value}>
                        {opt.label}
                      </option>
                    ))}
                  </select>
                </div>
                <button
                  type="button"
                  onClick={handleBuscar}
                  disabled={loading}
                  className="rounded-xl bg-[#2563eb] px-6 py-2.5 text-sm font-medium text-white hover:bg-[#1d4ed8] disabled:opacity-50"
                >
                  {loading ? "Buscando…" : "Buscar"}
                </button>
              </div>
            </div>
          </section>

          {/* Resultados: 5 livros por linha, 25 por página */}
          <section>
            <h3 className="mb-3 text-sm font-semibold text-gray-900">Resultados</h3>
            {loading && books.length === 0 ? (
              <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5">
                {Array.from({ length: 10 }).map((_, i) => (
                  <div key={i} className="h-52 animate-pulse rounded-2xl bg-gray-200" />
                ))}
              </div>
            ) : books.length === 0 ? (
              <p className="py-12 text-center text-gray-500">
                A busca não encontrou nenhum resultado.
              </p>
            ) : (
              <>
                <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5">
                  {books.map((book) => (
                    <BookCard
                      key={book.id}
                      book={book}
                      onSelect={setSelectedBook}
                      selected={selectedBook?.id === book.id}
                    />
                  ))}
                </div>
                {hasMore && (
                  <div ref={sentinelRef} className="mt-4 h-4 w-full" aria-hidden />
                )}
                {loadingMore && (
                  <div className="mt-4 flex justify-center">
                    <span className="text-sm text-gray-500">Carregando mais…</span>
                  </div>
                )}
                {!hasMore && books.length > 0 && totalElements > 0 && (
                  <p className="mt-4 text-center text-sm text-gray-500">
                    {totalElements} {totalElements === 1 ? "resultado" : "resultados"} no total.
                  </p>
                )}
              </>
            )}
          </section>
        </main>
      </div>

      <BookDetailPanel
        book={selectedBook}
        onReserve={setRentalModalBook}
        canReserve={profile === "user"}
      />

      {rentalModalBook && (
        <RentalModal
          book={rentalModalBook}
          onClose={() => setRentalModalBook(null)}
          onConfirm={handleRentalConfirm}
        />
      )}
      {showReserveSuccessModal && (
        <ReserveSuccessModal
          onVerAgora={() => {
            setShowReserveSuccessModal(false);
            router.push("/meus-alugueis");
          }}
          onFechar={() => setShowReserveSuccessModal(false)}
        />
      )}
    </div>
  );
}
