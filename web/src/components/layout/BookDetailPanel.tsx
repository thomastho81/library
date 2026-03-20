"use client";

import Image from "next/image";
import type { Book } from "@/lib/types/book";

const BOOK_PLACEHOLDER_SRC = "/images/book-placeholder.svg";

interface BookDetailPanelProps {
  book: Book | null;
  onReserve?: (book: Book) => void;
  /** Quando false (ex.: perfil gestor), o botão de reservar não é exibido. */
  canReserve?: boolean;
}

export default function BookDetailPanel({ book, onReserve, canReserve = true }: BookDetailPanelProps) {
  if (!book) {
    return (
      <aside className="flex h-full min-h-0 w-80 flex-shrink-0 self-stretch flex-col overflow-y-auto rounded-l-2xl bg-[#1e3a8a] p-6">
        <div className="flex flex-1 flex-col items-center justify-center text-center text-white/70">
          <p className="text-sm">Selecione um livro para ver os detalhes</p>
        </div>
      </aside>
    );
  }

  const available = book.availableCopies != null ? book.availableCopies > 0 : false;

  return (
    <aside className="flex h-full min-h-0 w-80 flex-shrink-0 self-stretch flex-col overflow-y-auto rounded-l-2xl bg-[#1e3a8a] p-6 text-white shadow-lg">
      {/* Capa em destaque (imagem ilustrativa para simulação) */}
      <div className="-mt-2 mb-4 flex justify-center">
        <div className="relative h-40 w-28 overflow-hidden rounded-xl bg-white/10 shadow-lg">
          <Image
            src={BOOK_PLACEHOLDER_SRC}
            alt=""
            width={112}
            height={160}
            className="object-cover object-top"
            sizes="112px"
          />
        </div>
      </div>

      <h2 className="text-lg font-semibold leading-tight">{book.title}</h2>
      <p className="text-sm text-white/80">{book.author}</p>
      {(book.category != null && book.category !== "") && (
        <p className="text-xs text-white/80">Categoria: {book.category}</p>
      )}
      {(book.genre != null && book.genre !== "") && (
        <p className="text-xs text-white/80">Gênero: {book.genre}</p>
      )}
      {book.publishedYear != null && (
        <p className="text-xs text-white/70">Ano de publicação: {book.publishedYear}</p>
      )}
      <div className="mb-4" />

      {/* Rating placeholder (backend não retorna; exibimos disponibilidade) */}
      <div className="mb-4 flex items-center gap-2">
        <span className="text-amber-300">★★★★☆</span>
        <span className={`text-sm font-medium ${available ? "text-green-300" : "text-red-300"}`}>
          {available ? "Disponível" : "Sem cópias para aluguel"}
        </span>
      </div>

      {/* Estatísticas */}
      <div className="mb-4 grid grid-cols-3 gap-2">
        <div className="rounded-lg bg-white/10 px-2 py-2 text-center">
          <div className="text-xs text-white/70">Páginas</div>
          <div className="text-sm font-medium">—</div>
        </div>
        <div className="rounded-lg bg-white/10 px-2 py-2 text-center">
          <div className="text-xs text-white/70">Avaliações</div>
          <div className="text-sm font-medium">—</div>
        </div>
        <div className="rounded-lg bg-white/10 px-2 py-2 text-center">
          <div className="text-xs text-white/70">Resenhas</div>
          <div className="text-sm font-medium">—</div>
        </div>
      </div>

      {/* Descrição breve */}
      <p className="mb-6 line-clamp-4 text-sm leading-relaxed text-white/90">
        {book.description || "Sem descrição disponível."}
      </p>

      {/* Botão de aluguel (oculto para perfil gestor) */}
      {canReserve && (
        <button
          type="button"
          onClick={() => onReserve?.(book)}
          disabled={!available}
          className="flex w-full items-center justify-center gap-2 rounded-xl bg-[#2563eb] py-3 text-sm font-medium text-white transition-colors hover:bg-[#1d4ed8] disabled:cursor-not-allowed disabled:opacity-50"
        >
          <span>📖</span>
          {available ? "Reservar agora" : "Indisponível"}
        </button>
      )}
    </aside>
  );
}
