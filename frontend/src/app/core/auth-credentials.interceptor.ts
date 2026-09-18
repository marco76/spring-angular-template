import { HttpContextToken, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthCredentialsService } from './auth-credentials.service';

export const AUTHORIZATION_HEADER = new HttpContextToken<string | null>(() => null);

export const authCredentialsInterceptor: HttpInterceptorFn = (request, next) => {
  const credentials = inject(AuthCredentialsService);
  const authorizationHeader =
    request.context.get(AUTHORIZATION_HEADER) ?? credentials.currentAuthorizationHeader();

  if (!authorizationHeader || !isApiRequest(request.url)) {
    return next(request);
  }

  return next(request.clone({ setHeaders: { Authorization: authorizationHeader } }));
};

function isApiRequest(url: string): boolean {
  return url.startsWith('/api/') || url.includes('/api/');
}
