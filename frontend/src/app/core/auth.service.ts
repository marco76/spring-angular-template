import { HttpClient, HttpContext } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { catchError, map, Observable, of, tap, throwError } from 'rxjs';
import { ApiConfiguration } from './api/api-configuration';
import { currentUser } from './api/fn/auth-controller/current-user';
import { CurrentUserResponse } from './api/models/current-user-response';
import { AUTHORIZATION_HEADER } from './auth-credentials.interceptor';
import { AuthCredentialsService } from './auth-credentials.service';

export type AuthStatus = 'loading' | 'authenticated' | 'anonymous';

/**
 * The backend uses HTTP Basic (see SecurityConfig). The header login stores
 * the Basic authorization header in memory for this browser tab and sends it
 * through authCredentialsInterceptor. There is still no server-side session;
 * see docs/KNOWN_LIMITATIONS.md.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly apiConfig = inject(ApiConfiguration);
  private readonly credentials = inject(AuthCredentialsService);

  private readonly user = signal<CurrentUserResponse | null>(null);
  private readonly status = signal<AuthStatus>('loading');

  readonly currentUser = this.user.asReadonly();
  readonly authStatus = this.status.asReadonly();
  readonly isAdmin = computed(() => this.user()?.roles?.includes('ROLE_ADMIN') ?? false);

  readonly initials = computed(() => {
    const username = this.user()?.username;
    return username ? username.slice(0, 2).toUpperCase() : '';
  });

  constructor() {
    this.refresh();
  }

  refresh(): void {
    this.status.set('loading');
    currentUser(this.http, this.apiConfig.rootUrl)
      .pipe(
        catchError(() => {
          this.user.set(null);
          this.status.set('anonymous');
          return of(null);
        })
      )
      .subscribe((response) => {
        if (response) {
          this.user.set(response.body);
          this.status.set('authenticated');
        }
      });
  }

  signIn(username: string, password: string): Observable<CurrentUserResponse> {
    const authorizationHeader = `Basic ${btoa(`${username}:${password}`)}`;
    const context = new HttpContext().set(AUTHORIZATION_HEADER, authorizationHeader);

    this.status.set('loading');
    return currentUser(this.http, this.apiConfig.rootUrl, undefined, context).pipe(
      map((response) => response.body),
      tap((user) => {
        this.credentials.rememberAuthorizationHeader(authorizationHeader);
        this.user.set(user);
        this.status.set('authenticated');
      }),
      catchError((error: unknown) => {
        this.credentials.clear();
        this.user.set(null);
        this.status.set('anonymous');
        return throwError(() => error);
      })
    );
  }

  signOut(): void {
    this.credentials.clear();
    this.user.set(null);
    this.status.set('anonymous');
  }
}
