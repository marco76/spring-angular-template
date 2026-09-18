import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';
import { ApiError } from './api-error';

/**
 * Single place that turns every HTTP error into the app's one ApiError
 * shape and logs it once, instead of each service/component parsing
 * HttpErrorResponse itself. Mirrors the backend's GlobalExceptionHandler.
 *
 * This does not swallow the error or decide UI behavior: it rethrows the
 * normalized ApiError so callers still render their own loading/error
 * state (see AdminComponent), the same way they already handle errors
 * with catchError — they just get a typed, consistent shape to work with.
 */
export const httpErrorInterceptor: HttpInterceptorFn = (request, next) =>
  next(request).pipe(
    catchError((response: unknown) => {
      const apiError = toApiError(response, request.urlWithParams);
      console.error(`[${apiError.status}] ${request.method} ${apiError.path}: ${apiError.message}`);
      return throwError(() => apiError);
    })
  );

function toApiError(response: unknown, path: string): ApiError {
  if (!(response instanceof HttpErrorResponse)) {
    return { status: 0, error: 'Unknown Error', message: 'An unexpected error occurred.', path };
  }

  if (isApiErrorShape(response.error)) {
    return response.error;
  }

  if (response.status === 0) {
    return { status: 0, error: 'Network Error', message: 'Could not reach the server.', path };
  }

  return {
    status: response.status,
    error: response.statusText || 'Error',
    message: response.message,
    path
  };
}

function isApiErrorShape(value: unknown): value is ApiError {
  return (
    typeof value === 'object' &&
    value !== null &&
    typeof (value as ApiError).status === 'number' &&
    typeof (value as ApiError).message === 'string'
  );
}
