/**
 * Erro lançado quando uma chamada de API retorna status 4xx ou 5xx.
 * Inclui status HTTP e a mensagem do ErrorResponse do backend (quando disponível).
 */
export class ApiError extends Error {
  constructor(
    public readonly status: number,
    message: string
  ) {
    super(message);
    this.name = "ApiError";
    Object.setPrototypeOf(this, ApiError.prototype);
  }

  get isClientError(): boolean {
    return this.status >= 400 && this.status < 500;
  }

  get isServerError(): boolean {
    return this.status >= 500 && this.status < 600;
  }
}

/** Contrato do corpo de erro do backend (ex.: rental-service ErrorResponse). */
interface ErrorResponseBody {
  message?: string;
  error?: string;
  status?: number;
}

/**
 * Lê o corpo da resposta de erro e extrai a mensagem (ErrorResponse.message).
 * Se o backend retornar JSON com "message", usa esse valor; senão usa o texto ou fallback.
 */
export async function parseErrorMessage(res: Response): Promise<string> {
  const contentType = res.headers.get("content-type") ?? "";
  const text = await res.text();
  if (contentType.includes("application/json") && text) {
    try {
      const body = JSON.parse(text) as ErrorResponseBody;
      if (typeof body.message === "string" && body.message.trim()) {
        return body.message.trim();
      }
      if (typeof body.error === "string" && body.error.trim()) {
        return body.error.trim();
      }
    } catch {
      // ignore
    }
  }
  return text?.slice(0, 300) || `Erro ${res.status} ${res.statusText}`;
}

/**
 * Se a resposta não for ok, lê a mensagem do corpo e lança ApiError(status, message).
 * Uso: if (!res.ok) await throwApiError(res);
 */
export async function throwApiError(res: Response): Promise<never> {
  const message = await parseErrorMessage(res);
  throw new ApiError(res.status, message);
}

export function isApiError(e: unknown): e is ApiError {
  return e instanceof ApiError;
}
