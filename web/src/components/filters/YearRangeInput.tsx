"use client";

const MIN_YEAR = 1000;
const MAX_YEAR = 2100;

interface YearRangeInputProps {
  valueFrom: string;
  valueTo: string;
  onChangeFrom: (value: string) => void;
  onChangeTo: (value: string) => void;
  placeholderFrom?: string;
  placeholderTo?: string;
}

/**
 * Ano (de) e Ano (até) na mesma linha. O usuário pode digitar o ano ou usar
 * as setas do input numérico (rolar para cima/baixo no campo).
 */
export default function YearRangeInput({
  valueFrom,
  valueTo,
  onChangeFrom,
  onChangeTo,
  placeholderFrom = "Ex.: 2000",
  placeholderTo = "Ex.: 2024",
}: YearRangeInputProps) {
  return (
    <div className="flex flex-1 flex-wrap items-end gap-3">
      <div className="min-w-[100px] flex-1">
        <label className="mb-1 block text-xs font-medium text-gray-600">
          Ano (de)
        </label>
        <input
          type="number"
          min={MIN_YEAR}
          max={MAX_YEAR}
          step={1}
          value={valueFrom}
          onChange={(e) => onChangeFrom(e.target.value)}
          placeholder={placeholderFrom}
          className="w-full rounded-lg border border-gray-200 px-3 py-2 text-sm"
          aria-label="Ano mínimo de publicação"
        />
      </div>
      <div className="min-w-[100px] flex-1">
        <label className="mb-1 block text-xs font-medium text-gray-600">
          Ano (até)
        </label>
        <input
          type="number"
          min={MIN_YEAR}
          max={MAX_YEAR}
          step={1}
          value={valueTo}
          onChange={(e) => onChangeTo(e.target.value)}
          placeholder={placeholderTo}
          className="w-full rounded-lg border border-gray-200 px-3 py-2 text-sm"
          aria-label="Ano máximo de publicação"
        />
      </div>
    </div>
  );
}
