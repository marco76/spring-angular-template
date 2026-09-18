import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class AuthCredentialsService {
  private readonly authorizationHeader = signal<string | null>(null);

  currentAuthorizationHeader(): string | null {
    return this.authorizationHeader();
  }

  rememberAuthorizationHeader(header: string): void {
    this.authorizationHeader.set(header);
  }

  clear(): void {
    this.authorizationHeader.set(null);
  }
}
