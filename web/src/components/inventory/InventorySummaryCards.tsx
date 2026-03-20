import type { InventorySummaryResponse } from "@/lib/types/book";

const fmt = new Intl.NumberFormat("pt-BR");

function formatCount(n: number): string {
  return fmt.format(n);
}

function IconBooks({ className }: { className?: string }) {
  return (
    <svg className={className} width="20" height="20" viewBox="0 0 24 24" fill="none" aria-hidden>
      <path
        d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"
        stroke="currentColor"
        strokeWidth="2"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      <path
        d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"
        stroke="currentColor"
        strokeWidth="2"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      <path d="M8 7h8M8 11h6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
    </svg>
  );
}

function IconAvailable({ className }: { className?: string }) {
  return (
    <svg className={className} width="20" height="20" viewBox="0 0 24 24" fill="none" aria-hidden>
      <path
        d="M9 11l3 3L22 4"
        stroke="currentColor"
        strokeWidth="2"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      <path
        d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"
        stroke="currentColor"
        strokeWidth="2"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  );
}

function IconReserved({ className }: { className?: string }) {
  return (
    <svg className={className} width="20" height="20" viewBox="0 0 24 24" fill="none" aria-hidden>
      <path
        d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z"
        stroke="currentColor"
        strokeWidth="2"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  );
}

type CardConfig = {
  title: string;
  accentClass: string;
  Icon: typeof IconBooks;
  valueKey: keyof Pick<
    InventorySummaryResponse,
    "totalBooks" | "totalAvailableCopies" | "booksWithReservedCopies"
  >;
};

const CARDS: CardConfig[] = [
  {
    title: "Total de livros",
    accentClass: "bg-emerald-500",
    Icon: IconBooks,
    valueKey: "totalBooks",
  },
  {
    title: "Total de exemplares disponíveis",
    accentClass: "bg-sky-500",
    Icon: IconAvailable,
    valueKey: "totalAvailableCopies",
  },
  {
    title: "Total de livros reservados",
    accentClass: "bg-violet-500",
    Icon: IconReserved,
    valueKey: "booksWithReservedCopies",
  },
];

export default function InventorySummaryCards({
  summary,
  loading,
}: {
  summary: InventorySummaryResponse | null;
  loading: boolean;
}) {
  return (
    <section className="mb-4 grid grid-cols-1 gap-4 md:grid-cols-3" aria-label="Resumo do inventário">
      {CARDS.map(({ title, accentClass, Icon, valueKey }) => (
        <div
          key={valueKey}
          className="rounded-2xl border border-gray-100 bg-white p-5 shadow-sm"
        >
          <div className="flex items-start justify-between gap-2">
            <div className="flex min-w-0 flex-1 items-center gap-3">
              <div
                className={`flex h-10 w-10 shrink-0 items-center justify-center rounded-lg text-white ${accentClass}`}
              >
                <Icon className="h-5 w-5" />
              </div>
              <span className="text-sm font-medium leading-snug text-gray-600">{title}</span>
            </div>
            <span
              className="shrink-0 select-none rounded-lg p-1 text-gray-400"
              aria-hidden
            >
              <span className="block text-lg leading-none">⋯</span>
            </span>
          </div>
          <p className="mt-4 text-3xl font-bold tracking-tight text-gray-900 tabular-nums">
            {loading || !summary ? (
              <span className="text-gray-400">—</span>
            ) : (
              formatCount(summary[valueKey])
            )}
          </p>
          <p className="mt-2 text-xs text-gray-500">
            {loading ? "Carregando…" : "Com base nos filtros · índice Elasticsearch"}
          </p>
        </div>
      ))}
    </section>
  );
}
