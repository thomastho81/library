"use client";

interface ReserveSuccessModalProps {
  onVerAgora: () => void;
  onFechar: () => void;
}

export default function ReserveSuccessModal({ onVerAgora, onFechar }: ReserveSuccessModalProps) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4" onClick={onFechar}>
      <div
        className="w-full max-w-md rounded-2xl bg-white p-6 shadow-xl"
        onClick={(e) => e.stopPropagation()}
      >
        <h3 className="text-lg font-semibold text-gray-900">Reserva em processamento</h3>
        <p className="mt-2 text-sm text-gray-600">
          Sua reserva está sendo processada e você poderá acompanhar no menu <strong>Meus aluguéis</strong>.
          Deseja ver agora?
        </p>
        <div className="mt-6 flex justify-end gap-3">
          <button
            type="button"
            onClick={onFechar}
            className="rounded-xl border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50"
          >
            Não
          </button>
          <button
            type="button"
            onClick={onVerAgora}
            className="rounded-xl bg-[#2563eb] px-4 py-2 text-sm font-medium text-white hover:bg-[#1d4ed8]"
          >
            Ver agora
          </button>
        </div>
      </div>
    </div>
  );
}
