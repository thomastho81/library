"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useCallback, useEffect, useState } from "react";
import { reserveRental } from "@/lib/api/rental";
import { searchBooks } from "@/lib/api/search";
import { ApiError } from "@/lib/api/errors";
import { useErrorModal } from "@/contexts/ErrorModalContext";
import { useProfile } from "@/contexts/ProfileContext";
import type { Book } from "@/lib/types/book";
import BookCard from "@/components/books/BookCard";
import BookDetailPanel from "@/components/layout/BookDetailPanel";
import Header from "@/components/layout/Header";
import RentalModal from "@/components/rental/RentalModal";
import ReserveSuccessModal from "@/components/rental/ReserveSuccessModal";

/**
 * Categorias exibidas na seção Categorias da Home.
 * Estratégia: lista fixa das "principais" categorias; ao clicar, a API é chamada
 * com category e genre iguais ao label. Se não existir livro cadastrado para
 * essa categoria, o resultado vem vazio e mostramos "Nenhum livro nesta categoria."
 * Alternativa futura: buscar categorias reais via agregação no Elasticsearch
 * ou endpoint GET /api/search/books/categories (se existir).
 */
const CATEGORIES = [
  "Todos",
  "Literatura brasileira",
  "Tecnologia",
  "Ficção",
  "Não ficção",
  "Educação",
  "Negócios",
  "Romance",
  "Drama",
];

export default function Home() {
  const router = useRouter();
  const { showError: showErrorModal } = useErrorModal();
  const { userId, profile } = useProfile();
  const [searchQuery, setSearchQuery] = useState("");

  useEffect(() => {
    if (profile === "gestor") router.replace("/pesquisa");
  }, [profile, router]);
  const [recommended, setRecommended] = useState<Book[]>([]);
  const [categoryBooks, setCategoryBooks] = useState<Book[]>([]);
  const [selectedBook, setSelectedBook] = useState<Book | null>(null);
  /** Qual seção originou a seleção: só o card dessa seção fica destacado. */
  const [selectedFromSection, setSelectedFromSection] = useState<"recommended" | "categories">("recommended");
  const [activeCategory, setActiveCategory] = useState("Todos");
  const [loading, setLoading] = useState(true);
  const [searchLoading, setSearchLoading] = useState(false);
  const [rentalModalBook, setRentalModalBook] = useState<Book | null>(null);
  const [showReserveSuccessModal, setShowReserveSuccessModal] = useState(false);

  const loadRecommended = useCallback(async () => {
    setLoading(true);
    try {
      const res = await searchBooks({ page: 0, size: 8, sortBy: "updatedAt" });
      setRecommended(res.content);
    } catch (e) {
      console.error("[Home] loadRecommended falhou:", e);
      if (e instanceof Error && e.stack) console.error("[Home] stack:", e.stack);
      setRecommended([]);
      if (e instanceof ApiError) {
        if (e.isClientError) showErrorModal(e.message);
        else if (e.isServerError) alert(`Erro de servidor: ${e.message}`);
      }
    } finally {
      setLoading(false);
    }
  }, [showErrorModal]);

  const loadByCategory = useCallback(async () => {
    setSearchLoading(true);
    try {
      if (activeCategory === "Todos") {
        const res = await searchBooks({ page: 0, size: 5 });
        setCategoryBooks(res.content);
      } else {
        const res = await searchBooks({
          page: 0,
          size: 5,
          genre: activeCategory,
          category: activeCategory,
        });
        setCategoryBooks(res.content);
      }
    } catch (e) {
      console.error("[Home] loadByCategory falhou:", e);
      if (e instanceof Error && e.stack) console.error("[Home] stack:", e.stack);
      setCategoryBooks([]);
      if (e instanceof ApiError) {
        if (e.isClientError) showErrorModal(e.message);
        else if (e.isServerError) alert(`Erro de servidor: ${e.message}`);
      }
    } finally {
      setSearchLoading(false);
    }
  }, [activeCategory, showErrorModal]);

  const doSearch = useCallback(async () => {
    setSearchLoading(true);
    try {
      const recRes = await searchBooks({
        q: searchQuery || undefined,
        page: 0,
        size: 8,
        sortBy: "updatedAt",
      });
      setRecommended(recRes.content);
      const catRes = await searchBooks({
        q: searchQuery || undefined,
        page: 0,
        size: 5,
        genre: activeCategory !== "Todos" ? activeCategory : undefined,
        category: activeCategory !== "Todos" ? activeCategory : undefined,
      });
      setCategoryBooks(catRes.content);
      // Não seleciona automaticamente o primeiro livro após busca.
    } catch (e) {
      console.error("[Home] doSearch falhou:", e);
      if (e instanceof Error && e.stack) console.error("[Home] stack:", e.stack);
      if (e instanceof ApiError) {
        if (e.isClientError) showErrorModal(e.message);
        else if (e.isServerError) alert(`Erro de servidor: ${e.message}`);
      }
    } finally {
      setSearchLoading(false);
    }
  }, [searchQuery, activeCategory, showErrorModal]);

  useEffect(() => {
    loadRecommended();
  }, []);

  useEffect(() => {
    loadByCategory();
  }, [loadByCategory]);

  const handleReserveClick = useCallback((book: Book) => {
    setRentalModalBook(book);
  }, []);

  const handleRentalConfirm = useCallback(
    async (bookId: string, quantity: number) => {
      const bookIdNum = Number(bookId);
      if (!Number.isInteger(bookIdNum) || bookIdNum < 1) {
        console.error("[Home] bookId inválido:", bookId);
        return;
      }
      if (quantity < 1) return;
      try {
        await reserveRental({ userId, bookId: bookIdNum, quantity });
        setRentalModalBook(null);
        setShowReserveSuccessModal(true);
      } catch (e) {
        console.error("[Home] reserva falhou:", e);
        if (e instanceof ApiError) {
          if (e.isClientError) showErrorModal(e.message);
          else if (e.isServerError) alert(`Erro de servidor: ${e.message}`);
        } else if (e instanceof Error) alert(e.message);
      }
    },
    [showErrorModal, userId]
  );

  if (profile === "gestor") return null;

  return (
    <div className="flex h-full min-h-0 min-w-0 flex-1 gap-0 overflow-x-auto overflow-y-hidden md:overflow-hidden">
      {/* Área central: header + conteúdo */}
      <div className="flex min-h-0 min-w-0 flex-1 flex-col overflow-hidden bg-gray-50/50">
        <div className="flex-shrink-0 p-4">
          <Header
            searchQuery={searchQuery}
            onSearchChange={setSearchQuery}
            onSearchSubmit={() => {
              const q = searchQuery?.trim();
              router.push(q ? `/pesquisa?q=${encodeURIComponent(q)}` : "/pesquisa");
            }}
          />
        </div>

        <main className="flex-1 overflow-y-auto px-4 pb-6">
          {/* Recomendados */}
          <section className="mb-8">
            <div className="mb-4 flex items-center justify-between">
              <h2 className="text-xl font-semibold text-gray-900">
                Recomendados
              </h2>
              <Link
                href="/pesquisa?sortBy=updatedAt&sortDir=desc"
                className="inline-flex items-center gap-1 rounded-xl border-2 border-[#2563eb] px-4 py-2 text-sm font-semibold text-[#2563eb] transition-colors hover:bg-[#2563eb] hover:text-white"
              >
                Ver todos →
              </Link>
            </div>
            {loading ? (
              <div className="flex gap-4 overflow-hidden py-2">
                {[1, 2, 3, 4].map((i) => (
                  <div
                    key={i}
                    className="h-52 w-36 flex-shrink-0 animate-pulse rounded-2xl bg-gray-200"
                  />
                ))}
              </div>
            ) : (
              <div className="flex gap-4 overflow-x-auto pb-2">
                {recommended.map((book) => (
                  <BookCard
                    key={book.id}
                    book={book}
                    onSelect={(b) => {
                      setSelectedBook(b);
                      setSelectedFromSection("recommended");
                    }}
                    selected={selectedBook?.id === book.id && selectedFromSection === "recommended"}
                  />
                ))}
                {recommended.length === 0 && !loading && (
                  <p className="py-4 text-sm text-gray-500">
                    Nenhum livro encontrado. Verifique o search-service.
                  </p>
                )}
              </div>
            )}
          </section>

          {/* Categorias */}
          <section>
            <div className="mb-4 flex items-center justify-between">
              <h2 className="text-xl font-semibold text-gray-900">
                Categorias
              </h2>
              <Link
                href={
                  activeCategory === "Todos"
                    ? "/pesquisa"
                    : `/pesquisa?category=${encodeURIComponent(activeCategory)}`
                }
                className="inline-flex items-center gap-1 rounded-xl border-2 border-[#2563eb] px-4 py-2 text-sm font-semibold text-[#2563eb] transition-colors hover:bg-[#2563eb] hover:text-white"
              >
                {activeCategory === "Todos" ? "Ver todos" : "Ver tudo"} →
              </Link>
            </div>
            <div className="mb-4 flex flex-wrap gap-2">
              {CATEGORIES.map((cat) => (
                <button
                  key={cat}
                  type="button"
                  onClick={() => setActiveCategory(cat)}
                  className={`rounded-xl px-4 py-2 text-sm font-medium transition-colors ${
                    activeCategory === cat
                      ? "bg-[#2563eb] text-white"
                      : "bg-white text-gray-700 shadow-sm hover:bg-gray-50"
                  }`}
                >
                  {cat}
                </button>
              ))}
            </div>
            {searchLoading ? (
              <div className="flex gap-4 overflow-hidden py-2">
                {[1, 2, 3, 4].map((i) => (
                  <div
                    key={i}
                    className="h-52 w-36 flex-shrink-0 animate-pulse rounded-2xl bg-gray-200"
                  />
                ))}
              </div>
            ) : (
              <div className="flex gap-4 overflow-x-auto pb-2">
                {categoryBooks.map((book) => (
                  <BookCard
                    key={book.id}
                    book={book}
                    onSelect={(b) => {
                      setSelectedBook(b);
                      setSelectedFromSection("categories");
                    }}
                    selected={selectedBook?.id === book.id && selectedFromSection === "categories"}
                  />
                ))}
                {categoryBooks.length === 0 && !searchLoading && (
                  <p className="py-4 text-sm text-gray-500">
                    Nenhum livro nesta categoria.
                  </p>
                )}
              </div>
            )}
          </section>
        </main>
      </div>

      {/* Painel direito: detalhes do livro */}
      <BookDetailPanel
        book={selectedBook}
        onReserve={handleReserveClick}
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
