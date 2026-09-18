import { DatePipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  effect,
  inject,
  signal
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { AuthService } from '../core/auth.service';
import { ApiError } from '../core/error/api-error';
import { UserManagementService } from '../core/user-management.service';
import { UserResponse } from '../core/api/models/user-response';
import {
  AdminUserFormDialogComponent,
  AdminUserFormDialogResult
} from './admin-user-form-dialog.component';

type UsersPageState =
  | { status: 'loading' }
  | { status: 'ready'; users: UserResponse[] }
  | { status: 'error'; message: string };

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [
    DatePipe,
    MatButtonModule,
    MatChipsModule,
    MatDialogModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatTableModule
  ],
  template: `
    <section class="page">
      <header class="page-header">
        <div>
          <p class="eyebrow">Administration</p>
          <h1>Users</h1>
          <p class="subtitle">Add, update, and remove user accounts.</p>
        </div>
        <button mat-flat-button color="primary" type="button" (click)="openCreateDialog()">
          Add user
        </button>
      </header>

      @switch (state().status) {
        @case ('loading') {
          <div class="status-row">
            <mat-spinner diameter="24" />
            <span>Loading users...</span>
          </div>
        }
        @case ('error') {
          <p class="muted">{{ errorMessage() }}</p>
        }
        @case ('ready') {
          <table mat-table [dataSource]="users()" class="users-table">
            <ng-container matColumnDef="username">
              <th mat-header-cell *matHeaderCellDef>Username</th>
              <td mat-cell *matCellDef="let user">{{ user.username }}</td>
            </ng-container>

            <ng-container matColumnDef="role">
              <th mat-header-cell *matHeaderCellDef>Role</th>
              <td mat-cell *matCellDef="let user">{{ user.role }}</td>
            </ng-container>

            <ng-container matColumnDef="status">
              <th mat-header-cell *matHeaderCellDef>Status</th>
              <td mat-cell *matCellDef="let user">
                <mat-chip-set>
                  <mat-chip [highlighted]="user.enabled">{{
                    user.enabled ? 'Enabled' : 'Disabled'
                  }}</mat-chip>
                </mat-chip-set>
              </td>
            </ng-container>

            <ng-container matColumnDef="createdAt">
              <th mat-header-cell *matHeaderCellDef>Created</th>
              <td mat-cell *matCellDef="let user">{{ user.createdAt | date: 'medium' }}</td>
            </ng-container>

            <ng-container matColumnDef="actions">
              <th mat-header-cell *matHeaderCellDef></th>
              <td mat-cell *matCellDef="let user">
                <button
                  mat-icon-button
                  type="button"
                  aria-label="Edit user"
                  (click)="openEditDialog(user)"
                >
                  <mat-icon>edit</mat-icon>
                </button>
                <button
                  mat-icon-button
                  type="button"
                  aria-label="Delete user"
                  [disabled]="user.username === currentUsername()"
                  (click)="deleteUser(user)"
                >
                  <mat-icon>delete</mat-icon>
                </button>
              </td>
            </ng-container>

            <tr mat-header-row *matHeaderRowDef="columns"></tr>
            <tr mat-row *matRowDef="let row; columns: columns"></tr>
          </table>
        }
      }
    </section>
  `,
  styles: `
    .users-table {
      width: 100%;
      background: transparent;
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AdminUsersComponent {
  private readonly userManagementService = inject(UserManagementService);
  private readonly authService = inject(AuthService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly columns = ['username', 'role', 'status', 'createdAt', 'actions'];
  protected readonly currentUsername = () => this.authService.currentUser()?.username;

  protected readonly state = signal<UsersPageState>({ status: 'loading' });
  protected readonly users = () =>
    this.state().status === 'ready' ? (this.state() as { users: UserResponse[] }).users : [];
  protected readonly errorMessage = () =>
    this.state().status === 'error' ? (this.state() as { message: string }).message : '';

  constructor() {
    effect(() => {
      if (this.authService.authStatus() !== 'loading') {
        this.reload();
      }
    });
  }

  private reload(): void {
    this.state.set({ status: 'loading' });
    this.userManagementService
      .listUsers()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (page) => this.state.set({ status: 'ready', users: page?.content ?? [] }),
        error: (error: ApiError) => this.state.set({ status: 'error', message: error.message })
      });
  }

  protected openCreateDialog(): void {
    const dialogRef = this.dialog.open(AdminUserFormDialogComponent, {
      data: { user: null },
      width: '420px'
    });
    dialogRef
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((result?: AdminUserFormDialogResult) => {
        if (!result) {
          return;
        }
        this.userManagementService
          .createUser({
            username: result.username,
            password: result.password ?? '',
            role: result.role
          })
          .pipe(takeUntilDestroyed(this.destroyRef))
          .subscribe({
            next: () => {
              this.snackBar.open(`Created user "${result.username}".`, 'Dismiss', {
                duration: 4000
              });
              this.reload();
            },
            error: (error: ApiError) =>
              this.snackBar.open(error.message, 'Dismiss', { duration: 6000 })
          });
      });
  }

  protected openEditDialog(user: UserResponse): void {
    const dialogRef = this.dialog.open(AdminUserFormDialogComponent, {
      data: { user },
      width: '420px'
    });
    dialogRef
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((result?: AdminUserFormDialogResult) => {
        if (!result || user.id === undefined) {
          return;
        }
        this.userManagementService
          .updateUser(user.id, {
            role: result.role,
            enabled: result.enabled,
            password: result.password ?? undefined
          })
          .pipe(takeUntilDestroyed(this.destroyRef))
          .subscribe({
            next: () => {
              this.snackBar.open(`Updated user "${user.username}".`, 'Dismiss', { duration: 4000 });
              this.reload();
            },
            error: (error: ApiError) =>
              this.snackBar.open(error.message, 'Dismiss', { duration: 6000 })
          });
      });
  }

  protected deleteUser(user: UserResponse): void {
    if (user.id === undefined) {
      return;
    }
    if (!confirm(`Delete user "${user.username}"? This cannot be undone.`)) {
      return;
    }
    this.userManagementService
      .deleteUser(user.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.snackBar.open(`Deleted user "${user.username}".`, 'Dismiss', { duration: 4000 });
          this.reload();
        },
        error: (error: ApiError) => this.snackBar.open(error.message, 'Dismiss', { duration: 6000 })
      });
  }
}
