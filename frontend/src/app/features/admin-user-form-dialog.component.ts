import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { UserResponse } from '../core/api/models/user-response';

export interface AdminUserFormDialogData {
  user: UserResponse | null;
}

export interface AdminUserFormDialogResult {
  username: string;
  password: string | null;
  role: 'ADMIN' | 'USER';
  enabled: boolean;
}

@Component({
  selector: 'app-admin-user-form-dialog',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatSlideToggleModule
  ],
  template: `
    <h2 mat-dialog-title>{{ isEdit ? 'Edit user' : 'Add user' }}</h2>

    <form [formGroup]="form" (ngSubmit)="submit()">
      <mat-dialog-content class="form-fields">
        <mat-form-field appearance="outline">
          <mat-label>Username</mat-label>
          <input matInput formControlName="username" autocomplete="off" />
          @if (form.controls.username.hasError('required')) {
            <mat-error>Username is required.</mat-error>
          }
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>{{ isEdit ? 'New password (optional)' : 'Password' }}</mat-label>
          <input matInput type="password" formControlName="password" autocomplete="new-password" />
          @if (form.controls.password.hasError('minlength')) {
            <mat-error>Password must be at least 8 characters.</mat-error>
          }
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Role</mat-label>
          <mat-select formControlName="role">
            <mat-option value="USER">User</mat-option>
            <mat-option value="ADMIN">Admin</mat-option>
          </mat-select>
        </mat-form-field>

        <mat-slide-toggle formControlName="enabled">Enabled</mat-slide-toggle>
      </mat-dialog-content>

      <mat-dialog-actions align="end">
        <button mat-button type="button" mat-dialog-close>Cancel</button>
        <button mat-flat-button color="primary" type="submit" [disabled]="form.invalid">
          {{ isEdit ? 'Save' : 'Create' }}
        </button>
      </mat-dialog-actions>
    </form>
  `,
  styles: `
    .form-fields {
      display: grid;
      gap: 4px;
      padding-top: 4px;
    }

    mat-form-field {
      width: 100%;
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AdminUserFormDialogComponent {
  private readonly dialogRef = inject(
    MatDialogRef<AdminUserFormDialogComponent, AdminUserFormDialogResult>
  );
  private readonly data = inject<AdminUserFormDialogData>(MAT_DIALOG_DATA);
  private readonly formBuilder = inject(FormBuilder);

  protected readonly isEdit = this.data.user !== null;

  protected readonly form = this.formBuilder.nonNullable.group({
    username: [
      { value: this.data.user?.username ?? '', disabled: this.isEdit },
      [Validators.required, Validators.maxLength(100)]
    ],
    password: [
      '',
      this.isEdit ? [Validators.minLength(8)] : [Validators.required, Validators.minLength(8)]
    ],
    role: [this.data.user?.role ?? 'USER', Validators.required],
    enabled: [this.data.user?.enabled ?? true]
  });

  protected submit(): void {
    if (this.form.invalid) {
      return;
    }
    const value = this.form.getRawValue();
    this.dialogRef.close({
      username: value.username,
      password: value.password ? value.password : null,
      role: value.role,
      enabled: value.enabled
    });
  }
}
