import { AsyncPipe, DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { toObservable } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { RouterLink } from '@angular/router';
import { catchError, filter, map, of, startWith, switchMap } from 'rxjs';
import { AdminService } from '../core/admin.service';
import { AdminOverviewResponse } from '../core/api/models/admin-overview-response';
import { AuthService } from '../core/auth.service';
import { ApiError } from '../core/error/api-error';
import { ThemePreferenceService } from '../core/theme-preference.service';

type AdminPageState =
  | { status: 'loading' }
  | { status: 'ready'; overview: AdminOverviewResponse }
  | { status: 'error'; message: string };

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [
    AsyncPipe,
    DatePipe,
    MatButtonModule,
    MatButtonToggleModule,
    MatCardModule,
    MatChipsModule,
    MatProgressSpinnerModule,
    RouterLink
  ],
  template: `
    <section class="page">
      <header class="page-header">
        <div>
          <p class="eyebrow">Administration</p>
          <h1>Admin</h1>
          <p class="subtitle">Protected area — requires the admin role.</p>
        </div>
        <a mat-stroked-button href="/api/admin">Open endpoint</a>
      </header>

      <div class="card-grid">
        <mat-card appearance="outlined" class="dashboard-card">
          <mat-card-header>
            <mat-card-title>Protected admin area</mat-card-title>
            <mat-card-subtitle>Sign in with admin / admin</mat-card-subtitle>
          </mat-card-header>

          <mat-card-content>
            @if (state$ | async; as state) {
              @switch (state.status) {
                @case ('loading') {
                  <div class="status-row">
                    <mat-spinner diameter="24" />
                    <span>Checking admin access...</span>
                  </div>
                }
                @case ('ready') {
                  <div class="status-row">
                    <mat-chip-set aria-label="Admin status">
                      <mat-chip highlighted>{{ state.overview.status }}</mat-chip>
                    </mat-chip-set>
                    <span>{{ state.overview.message }}</span>
                  </div>
                  @if (state.overview.checkedAt) {
                    <p class="muted">Checked {{ state.overview.checkedAt | date: 'medium' }}</p>
                  }
                }
                @case ('error') {
                  <div class="status-row">
                    <mat-chip-set aria-label="Admin status">
                      <mat-chip>Needs admin</mat-chip>
                    </mat-chip-set>
                    <span>{{ state.message }}</span>
                  </div>
                }
              }
            }
          </mat-card-content>
        </mat-card>

        <mat-card appearance="outlined" class="dashboard-card">
          <mat-card-header>
            <mat-card-title>Users</mat-card-title>
            <mat-card-subtitle>Add, update, and remove accounts</mat-card-subtitle>
          </mat-card-header>
          <mat-card-content>
            <p>Manage who can sign in and which role each account has.</p>
          </mat-card-content>
          <mat-card-actions>
            <a mat-button routerLink="/admin/users">Manage users</a>
          </mat-card-actions>
        </mat-card>

        <mat-card appearance="outlined" class="dashboard-card">
          <mat-card-header>
            <mat-card-title>Appearance</mat-card-title>
            <mat-card-subtitle>Choose the app theme</mat-card-subtitle>
          </mat-card-header>
          <mat-card-content>
            <mat-button-toggle-group
              class="theme-switcher"
              aria-label="Theme"
              [value]="themePreference.theme()"
              (change)="themePreference.setTheme($event.value)"
            >
              <mat-button-toggle value="default">Default</mat-button-toggle>
              <mat-button-toggle value="ft">FT</mat-button-toggle>
              <mat-button-toggle value="gl">GL</mat-button-toggle>
            </mat-button-toggle-group>
          </mat-card-content>
        </mat-card>

        <mat-card appearance="outlined" class="dashboard-card notice-card">
          <mat-card-header>
            <mat-card-title>Before you ship</mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <p>
              The <code>admin / admin</code> credentials are for local development only. Replace
              them with real values before deploying (see <code>app.bootstrap-admin.*</code>).
            </p>
          </mat-card-content>
        </mat-card>
      </div>
    </section>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AdminComponent {
  private readonly adminService = inject(AdminService);
  private readonly authService = inject(AuthService);
  protected readonly themePreference = inject(ThemePreferenceService);

  protected readonly state$ = toObservable(this.authService.authStatus).pipe(
    filter((status) => status !== 'loading'),
    switchMap(() =>
      this.adminService.loadOverview().pipe(
        map((overview): AdminPageState => ({ status: 'ready', overview })),
        catchError((error: ApiError) =>
          of<AdminPageState>({
            status: 'error',
            message: error.message
          })
        ),
        startWith<AdminPageState>({ status: 'loading' })
      )
    ),
    startWith<AdminPageState>({ status: 'loading' })
  );
}
