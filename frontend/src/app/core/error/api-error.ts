/**
 * Mirrors the backend's error.model.ErrorResponse shape. Anything the
 * backend's GlobalExceptionHandler or SecurityConfig error handlers produce
 * lands here unchanged; network failures and non-JSON errors are normalized
 * into the same shape by http-error.interceptor.ts.
 */
export interface ApiError {
  status: number;
  error: string;
  message: string;
  path?: string;
  timestamp?: string;
  fieldErrors?: Record<string, string>;
}
