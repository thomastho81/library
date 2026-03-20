"use client";

/**
 * Modal de erro para respostas 4xx: fundo branco à esquerda, faixa azul à direita,
 * bordas arredondadas (estilo da visualização em detalhe da home).
 */
interface ErrorModalProps {
  message: string;
  onClose: () => void;
}

export default function ErrorModal({ message, onClose }: ErrorModalProps) {
  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4"
      role="dialog"
      aria-modal="true"
      aria-labelledby="error-modal-title"
    >
      <div className="flex max-w-lg overflow-hidden rounded-2xl bg-white shadow-xl">
        {/* Parte branca: mensagem e botão */}
        <div className="flex flex-1 flex-col p-6">
          <h2 id="error-modal-title" className="mb-2 text-lg font-semibold text-gray-900">
            Erro
          </h2>
          <p className="mb-6 flex-1 text-sm text-gray-700">{message}</p>
          <button
            type="button"
            onClick={onClose}
            className="w-full rounded-xl bg-[#2563eb] py-2.5 text-sm font-medium text-white hover:bg-[#1d4ed8]"
          >
            Fechar
          </button>
        </div>
        {/* Faixa azul à direita (igual detalhe do livro) */}
        <div className="w-24 flex-shrink-0 rounded-r-2xl bg-[#1e3a8a]" />
      </div>
    </div>
  );
}
