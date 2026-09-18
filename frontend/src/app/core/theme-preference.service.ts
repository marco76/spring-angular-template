import { DOCUMENT } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { EMPTY } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { ApiConfiguration } from './api/api-configuration';
import { getTheme } from './api/fn/settings-controller/get-theme';
import { updateTheme } from './api/fn/settings-controller/update-theme';
import { ThemeResponse } from './api/models/theme-response';
import { UpdateThemeRequest } from './api/models/update-theme-request';

export type AppTheme = 'default' | 'ft' | 'gl';
type ApiTheme = UpdateThemeRequest['theme'];

const THEME_STORAGE_KEY = 'example-app.theme';
const INITIAL_THEME: AppTheme = 'default';

@Injectable({
  providedIn: 'root'
})
export class ThemePreferenceService {
  private readonly http = inject(HttpClient);
  private readonly apiConfig = inject(ApiConfiguration);
  private readonly document = inject(DOCUMENT);

  readonly theme = signal<AppTheme>(this.readStoredTheme() ?? INITIAL_THEME);

  constructor() {
    this.applyTheme(this.theme());
    this.loadTheme();
  }

  setTheme(theme: AppTheme): void {
    this.applyTheme(theme);
    updateTheme(this.http, this.apiConfig.rootUrl, { body: { theme: this.toApiTheme(theme) } })
      .pipe(
        map((response) => response.body),
        catchError(() => EMPTY)
      )
      .subscribe((response) => this.applyTheme(this.fromApiTheme(response)));
  }

  private loadTheme(): void {
    getTheme(this.http, this.apiConfig.rootUrl)
      .pipe(
        map((response) => response.body),
        catchError(() => EMPTY)
      )
      .subscribe((response) => this.applyTheme(this.fromApiTheme(response)));
  }

  private applyTheme(theme: AppTheme): void {
    this.theme.set(theme);

    const root = this.document.documentElement;
    if (theme === 'default') {
      root.removeAttribute('data-theme');
    } else {
      root.dataset['theme'] = theme;
    }

    try {
      this.document.defaultView?.localStorage.setItem(THEME_STORAGE_KEY, theme);
    } catch {
      // Storage is optional; applying the current theme still works without it.
    }
  }

  private readStoredTheme(): AppTheme | null {
    try {
      const storedTheme = this.document.defaultView?.localStorage.getItem(THEME_STORAGE_KEY);
      return storedTheme === 'default' || storedTheme === 'ft' || storedTheme === 'gl'
        ? storedTheme
        : null;
    } catch {
      return null;
    }
  }

  private fromApiTheme(response: ThemeResponse): AppTheme {
    switch (response.theme) {
      case 'FT':
        return 'ft';
      case 'GL':
        return 'gl';
      default:
        return 'default';
    }
  }

  private toApiTheme(theme: AppTheme): ApiTheme {
    switch (theme) {
      case 'ft':
        return 'FT';
      case 'gl':
        return 'GL';
      default:
        return 'DEFAULT';
    }
  }
}
