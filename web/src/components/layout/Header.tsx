"use client";

import { useCallback, useState } from "react";
import ProfileSwitcher from "@/components/layout/ProfileSwitcher";

const DEFAULT_PLACEHOLDER = "Busque seus livros favoritos";
const FOCUS_PLACEHOLDER = "Pesquise por título, autor, gênero e categoria";

interface HeaderProps {
  searchQuery: string;
  onSearchChange: (value: string) => void;
  onSearchSubmit?: () => void;
}

export default function Header({
  searchQuery,
  onSearchChange,
  onSearchSubmit,
}: HeaderProps) {
  const [isFocused, setIsFocused] = useState(false);
  const placeholder = isFocused ? FOCUS_PLACEHOLDER : DEFAULT_PLACEHOLDER;

  const handleSubmit = useCallback(
    (e: React.FormEvent) => {
      e.preventDefault();
      onSearchSubmit?.();
    },
    [onSearchSubmit]
  );

  return (
    <header className="sticky top-0 z-10 flex flex-shrink-0 items-center gap-4 rounded-2xl bg-white px-6 py-4 shadow-sm">
      <form onSubmit={handleSubmit} className="flex-1">
        <div className="relative max-w-xl">
          <span className="pointer-events-none absolute left-4 top-1/2 -translate-y-1/2 text-gray-400">
            🔍
          </span>
          <input
            type="search"
            placeholder={placeholder}
            value={searchQuery}
            onChange={(e) => onSearchChange(e.target.value)}
            onFocus={() => setIsFocused(true)}
            onBlur={() => setIsFocused(false)}
            className="w-full rounded-xl border border-gray-200 bg-gray-50 py-2.5 pl-11 pr-4 text-sm text-gray-900 placeholder-gray-500 transition-colors focus:border-[#2563eb] focus:bg-white focus:outline-none focus:ring-2 focus:ring-[#2563eb]/20"
            aria-label="Buscar livros"
          />
        </div>
      </form>

      <div className="flex items-center gap-3">
        <button
          type="button"
          className="flex h-10 w-10 items-center justify-center rounded-xl text-gray-500 hover:bg-gray-100 hover:text-gray-700"
          aria-label="Notificações"
        >
          🔔
        </button>
        <ProfileSwitcher />
      </div>
    </header>
  );
}
