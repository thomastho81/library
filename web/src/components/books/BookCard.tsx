"use client";

import Image from "next/image";
import type { Book } from "@/lib/types/book";

/** Imagem ilustrativa de livro para simulação (sem capas reais no backend). */
const BOOK_PLACEHOLDER_SRC = "/images/book-placeholder.svg";

interface BookCardProps {
  book: Book;
  onSelect: (book: Book) => void;
  selected?: boolean;
}

export default function BookCard({ book, onSelect, selected }: BookCardProps) {
  const available = book.availableCopies != null ? book.availableCopies > 0 : false;

  return (
    <button
      type="button"
      onClick={() => onSelect(book)}
      className={`flex w-36 flex-shrink-0 flex-col overflow-hidden rounded-2xl bg-white text-left shadow-sm transition-all hover:shadow-md focus:outline-none focus:ring-2 focus:ring-[#2563eb]/30 ${
        selected ? "ring-2 ring-[#2563eb]" : ""
      }`}
    >
      <div className="relative h-44 w-full overflow-hidden bg-gray-100">
        <Image
          src={BOOK_PLACEHOLDER_SRC}
          alt=""
          width={140}
          height={220}
          className="object-cover object-top"
          sizes="140px"
        />
      </div>
      <div className="flex flex-col gap-0.5 p-3">
        <span className="line-clamp-2 text-sm font-medium text-gray-900">
          {book.title}
        </span>
        <span className="line-clamp-1 text-xs text-gray-500">{book.author}</span>
        {book.availableCopies != null && (
          <span
            className={`mt-1 text-xs font-medium ${
              available ? "text-green-600" : "text-red-600"
            }`}
          >
            {available ? "Disponível" : "Sem cópias para aluguel"}
          </span>
        )}
      </div>
    </button>
  );
}
