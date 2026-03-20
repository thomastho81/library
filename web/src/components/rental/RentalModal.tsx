"use client";

import Image from "next/image";
import { useCallback, useEffect, useState } from "react";
import type { Book } from "@/lib/types/book";

const BOOK_PLACEHOLDER_SRC = "/images/book-placeholder.svg";

interface RentalModalProps {
  book: Book | null;
  onClose: () => void;
  onConfirm?: (bookId: string, quantity: number) => void;
}

export default function RentalModal({
  book,
  onClose,
  onConfirm,
}: RentalModalProps) {
  const available = book?.availableCopies ?? 0;
  const maxQty = Math.max(0, available);
  const [quantity, setQuantity] = useState(() => Math.min(1, maxQty));

  useEffect(() => {
    setQuantity(maxQty >= 1 ? 1 : 0);
  }, [book?.id, maxQty]);

  const handleConfirm = useCallback(() => {
    if (!book) return;
    const qty = Math.min(quantity, maxQty);
    if (qty < 1) return;
    onConfirm?.(book.id, qty);
    onClose();
  }, [book, quantity, maxQty, onConfirm, onClose]);

  const canReserve = maxQty >= 1 && quantity >= 1 && quantity <= maxQty;

  if (!book) return null;

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4"
      role="dialog"
      aria-modal="true"
      aria-labelledby="rental-modal-title"
    >
      <div
        className="w-full max-w-md rounded-2xl bg-white p-6 shadow-xl"
        onClick={(e) => e.stopPropagation()}
      >
        <h2 id="rental-modal-title" className="mb-4 text-lg font-semibold text-gray-900">
          Alugar livro
        </h2>

        {/* Resumo do livro (imagem ilustrativa para simulação) */}
        <div className="mb-6 flex gap-4 rounded-xl border border-gray-100 bg-gray-50/50 p-4">
          <div className="relative h-24 w-16 flex-shrink-0 overflow-hidden rounded-lg bg-gray-200">
            <Image
              src={BOOK_PLACEHOLDER_SRC}
              alt=""
              width={64}
              height={96}
              className="object-cover object-top"
              sizes="64px"
            />
          </div>
          <div className="min-w-0 flex-1">
            <p className="font-medium text-gray-900">{book.title}</p>
            <p className="text-sm text-gray-600">{book.author}</p>
            {book.publishedYear != null && (
              <p className="text-xs text-gray-500">Ano: {book.publishedYear}</p>
            )}
            <p className="mt-1 line-clamp-2 text-xs text-gray-600">
              {book.description || "Sem descrição."}
            </p>
          </div>
        </div>

        {/* Quantidade de cópias */}
        <div className="mb-6">
          <label className="mb-2 block text-sm font-medium text-gray-700">
            Quantidade de cópias
          </label>
          <div className="flex items-center gap-3">
            <button
              type="button"
              onClick={() => setQuantity((q) => Math.max(1, q - 1))}
              className="flex h-10 w-10 items-center justify-center rounded-xl border border-gray-300 bg-white text-gray-700 hover:bg-gray-50 disabled:opacity-50"
              disabled={quantity <= 1}
              aria-label="Diminuir quantidade"
            >
              −
            </button>
            <span className="min-w-[2rem] text-center font-medium">{quantity}</span>
            <button
              type="button"
              onClick={() => setQuantity((q) => Math.min(maxQty, q + 1))}
              className="flex h-10 w-10 items-center justify-center rounded-xl border border-gray-300 bg-white text-gray-700 hover:bg-gray-50 disabled:opacity-50"
              disabled={quantity >= maxQty}
              aria-label="Aumentar quantidade"
            >
              +
            </button>
          </div>
          {book.availableCopies != null && (
            <p className="mt-1 text-xs text-gray-500">
              Máximo: {book.availableCopies} disponíve(is)
            </p>
          )}
        </div>

        <div className="flex gap-3">
          <button
            type="button"
            onClick={onClose}
            className="flex-1 rounded-xl border border-gray-300 bg-white py-2.5 text-sm font-medium text-gray-700 hover:bg-gray-50"
          >
            Cancelar
          </button>
          <button
            type="button"
            onClick={handleConfirm}
            disabled={!canReserve}
            className="flex-1 rounded-xl bg-[#2563eb] py-2.5 text-sm font-medium text-white hover:bg-[#1d4ed8] disabled:cursor-not-allowed disabled:opacity-50"
          >
            {maxQty < 1 ? "Indisponível" : "Alugar"}
          </button>
        </div>
      </div>
    </div>
  );
}
