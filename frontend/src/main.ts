import { bootstrapApplication } from '@angular/platform-browser';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { Routes, provideRouter } from '@angular/router';
import { provideApiConfiguration } from './app/core/api/api-configuration';
import { authCredentialsInterceptor } from './app/core/auth-credentials.interceptor';
import { adminGuard } from './app/core/admin.guard';
import { httpErrorInterceptor } from './app/core/error/http-error.interceptor';
import { HealthService } from './app/core/health.service';
import { AppShellComponent } from './app/layout/app-shell.component';

const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./app/features/home.component').then((m) => m.HomeComponent)
  },
  {
    path: 'admin',
    canActivate: [adminGuard],
    loadComponent: () => import('./app/features/admin.component').then((m) => m.AdminComponent)
  },
  {
    path: 'admin/users',
    canActivate: [adminGuard],
    loadComponent: () =>
      import('./app/features/admin-users.component').then((m) => m.AdminUsersComponent)
  }
];

bootstrapApplication(AppShellComponent, {
  providers: [
    provideAnimationsAsync(),
    provideHttpClient(withInterceptors([authCredentialsInterceptor, httpErrorInterceptor])),
    provideRouter(routes),
    // Empty root URL: requests stay relative so the dev proxy (proxy.conf.json)
    // and the prod nginx /api/ location both route them to the backend.
    provideApiConfiguration(''),
    HealthService
  ]
}).catch((error: unknown) => console.error(error));
