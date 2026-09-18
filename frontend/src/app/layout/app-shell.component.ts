import { BreakpointObserver } from '@angular/cdk/layout';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatListModule } from '@angular/material/list';
import { MatSidenav, MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { map } from 'rxjs';
import { AuthService } from '../core/auth.service';
import { ThemePreferenceService } from '../core/theme-preference.service';
import { LoginDialogComponent } from './login-dialog.component';

// Matches the .app-sidenav breakpoint in styles.css: below this width the
// sidenav becomes a dismissible overlay instead of pushing page content.
const MOBILE_QUERY = '(max-width: 720px)';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    MatButtonModule,
    MatDialogModule,
    MatListModule,
    MatSidenavModule,
    MatToolbarModule,
    RouterLink,
    RouterLinkActive,
    RouterOutlet
  ],
  template: `
    <mat-toolbar color="primary" class="app-header">
      <button
        mat-icon-button
        type="button"
        aria-label="Toggle navigation"
        class="menu-button"
        (click)="navigation.toggle()"
      >
        <span aria-hidden="true" class="menu-lines"></span>
      </button>
      <a routerLink="/" class="brand">
        <span class="brand-mark" aria-hidden="true">
          <svg viewBox="0 0 24 24" fill="none">
            <rect x="4" y="4" width="7" height="7" rx="2" fill="currentColor" />
            <rect x="13" y="4" width="7" height="7" rx="2" fill="currentColor" opacity="0.55" />
            <rect x="4" y="13" width="7" height="7" rx="2" fill="currentColor" opacity="0.55" />
            <rect x="13" y="13" width="7" height="7" rx="2" fill="currentColor" />
          </svg>
        </span>
        <span class="app-title">Example App</span>
      </a>

      <span class="toolbar-spacer"></span>

      <div class="account">
        @switch (authStatus()) {
          @case ('loading') {
            <span class="account-status">Checking account...</span>
          }
          @case ('authenticated') {
            <span class="account-avatar" aria-hidden="true">{{ initials() }}</span>
            <span class="account-name">{{ currentUser()?.username }}</span>
            <button mat-button type="button" class="account-sign-out" (click)="signOut()">
              Sign out
            </button>
          }
          @case ('anonymous') {
            <button mat-flat-button color="primary" type="button" (click)="openSignInDialog()">
              Sign in
            </button>
          }
        }
      </div>
    </mat-toolbar>

    <mat-sidenav-container class="app-container">
      <mat-sidenav
        #navigation
        class="app-sidenav"
        [mode]="isMobile() ? 'over' : 'side'"
        [opened]="!isMobile()"
      >
        <mat-nav-list>
          <div class="nav-category">
            <p class="nav-section-label">Workspace</p>
            <a
              mat-list-item
              routerLink="/"
              routerLinkActive="active-link"
              [routerLinkActiveOptions]="{ exact: true }"
              class="nav-link"
              (click)="closeOnMobile(navigation)"
            >
              <span class="nav-icon" aria-hidden="true">
                <svg
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="1.75"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                >
                  <path d="M3 10.5 12 3l9 7.5" />
                  <path d="M5 9.5V21h14V9.5" />
                </svg>
              </span>
              Dashboard
            </a>
          </div>

          @if (isAdmin()) {
            <div class="nav-category">
              <p class="nav-section-label">Administration</p>
              <a
                mat-list-item
                routerLink="/admin"
                routerLinkActive="active-link"
                class="nav-link"
                (click)="closeOnMobile(navigation)"
              >
                <span class="nav-icon" aria-hidden="true">
                  <svg
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="1.75"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  >
                    <path d="M12 3l7 3v6c0 4.5-3 7.5-7 9-4-1.5-7-4.5-7-9V6l7-3Z" />
                    <path d="m9.5 12 1.8 1.8L14.5 10" />
                  </svg>
                </span>
                Admin
              </a>
            </div>
          }
        </mat-nav-list>
      </mat-sidenav>

      <mat-sidenav-content class="app-content">
        <router-outlet />
      </mat-sidenav-content>
    </mat-sidenav-container>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AppShellComponent {
  private readonly breakpointObserver = inject(BreakpointObserver);
  private readonly authService = inject(AuthService);
  private readonly dialog = inject(MatDialog);

  protected readonly isMobile = toSignal(
    this.breakpointObserver.observe(MOBILE_QUERY).pipe(map((state) => state.matches)),
    { initialValue: false }
  );

  protected readonly authStatus = this.authService.authStatus;
  protected readonly currentUser = this.authService.currentUser;
  protected readonly isAdmin = this.authService.isAdmin;
  protected readonly initials = this.authService.initials;

  protected closeOnMobile(sidenav: MatSidenav): void {
    if (this.isMobile()) {
      sidenav.close();
    }
  }

  protected openSignInDialog(): void {
    this.dialog.open(LoginDialogComponent, {
      autoFocus: 'first-tabbable',
      width: '420px'
    });
  }

  protected signOut(): void {
    this.authService.signOut();
  }

  constructor() {
    inject(ThemePreferenceService);
  }
}
