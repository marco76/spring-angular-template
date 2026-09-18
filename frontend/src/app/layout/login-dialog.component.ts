import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { finalize } from 'rxjs';
import { AuthService } from '../core/auth.service';
import { ApiError } from '../core/error/api-error';

@Component({
  selector: 'app-login-dialog',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule
  ],
  template: `
    <h2 mat-dialog-title>Sign in</h2>

    <form [formGroup]="form" (ngSubmit)="signIn()">
      <mat-dialog-content class="login-dialog-fields">
        <mat-form-field appearance="outline">
          <mat-label>Username</mat-label>
          <input matInput type="text" autocomplete="username" formControlName="username" />
          @if (form.controls.username.hasError('required')) {
            <mat-error>Username is required.</mat-error>
          }
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Password</mat-label>
          <input
            matInput
            type="password"
            autocomplete="current-password"
            formControlName="password"
          />
          @if (form.controls.password.hasError('required')) {
            <mat-error>Password is required.</mat-error>
          }
        </mat-form-field>

        @if (loginError()) {
          <p class="login-dialog-error">{{ loginError() }}</p>
        }
      </mat-dialog-content>

      <mat-dialog-actions align="end">
        <button mat-button type="button" mat-dialog-close>Cancel</button>
        <button
          mat-flat-button
          color="primary"
          type="submit"
          [disabled]="form.invalid || isSigningIn()"
        >
          {{ isSigningIn() ? 'Signing in...' : 'Sign in' }}
        </button>
      </mat-dialog-actions>
    </form>
  `,
  styles: `
    .login-dialog-fields {
      display: grid;
      gap: 4px;
      min-width: min(360px, 72vw);
      padding-top: 4px;
    }

    mat-form-field {
      width: 100%;
    }

    .login-dialog-error {
      margin: 0;
      color: var(--app-danger);
      font-size: 13px;
      line-height: 1.4;
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class LoginDialogComponent {
  private readonly authService = inject(AuthService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly dialogRef = inject(MatDialogRef<LoginDialogComponent>);
  private readonly formBuilder = inject(FormBuilder);

  protected readonly form = this.formBuilder.nonNullable.group({
    username: ['', Validators.required],
    password: ['', Validators.required]
  });
  protected readonly isSigningIn = signal(false);
  protected readonly loginError = signal<string | null>(null);

  protected signIn(): void {
    if (this.form.invalid || this.isSigningIn()) {
      return;
    }

    const { username, password } = this.form.getRawValue();
    const trimmedUsername = username.trim();
    if (!trimmedUsername) {
      this.loginError.set('Enter a username.');
      return;
    }

    this.isSigningIn.set(true);
    this.loginError.set(null);
    this.authService
      .signIn(trimmedUsername, password)
      .pipe(
        finalize(() => this.isSigningIn.set(false)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: () => this.dialogRef.close(),
        error: (error: ApiError) => this.loginError.set(error.message)
      });
  }
}
