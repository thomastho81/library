import { Suspense } from "react";
import PesquisaPageClient from "./PesquisaPageClient";

/**
 * useSearchParams() exige boundary Suspense no App Router; sem isso o `next build` falha ao pré-renderizar.
 */
export default function PesquisaPage() {
  return (
    <Suspense
      fallback={
        <div className="flex min-h-[50vh] flex-1 items-center justify-center bg-gray-50/50 text-sm text-gray-500">
          Carregando pesquisa…
        </div>
      }
    >
      <PesquisaPageClient />
    </Suspense>
  );
}
