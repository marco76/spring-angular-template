import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { bootstrapApplication } from '@angular/platform-browser';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { RouterOutlet, Routes, provideRouter } from '@angular/router';
import { provideApiConfiguration } from './app/core/api/api-configuration';
import { adminGuard } from './app/core/admin.guard';
import { httpErrorInterceptor } from './app/core/error/http-error.interceptor';
import { HealthService } from './app/core/health.service';
import { ThemePreferenceService } from './app/core/theme-preference.service';

// Used in place of main.ts when create-project.sh generates a project with
// --ui=minimal (no header/sidenav shell). Swapped in as main.ts by
// create-project.sh; not part of this template's own build (see
// tsconfig.app.json's single-file "files" entry point), but still linted.

const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./app/features/home.component').then((m) => m.HomeComponent)
  },
  {
    path: 'admin',
    canActivate: [adminGuard],
    loadComponent: () => import('./app/features/admin.component').then((m) => m.AdminComponent)
  }
];

@Component({
  selector: 'app-root',
  standalone: true,
  template: '<main class="minimal-shell"><router-outlet /></main>',
  imports: [RouterOutlet],
  changeDetection: ChangeDetectionStrategy.OnPush
})
class AppComponent {
  constructor() {
    inject(ThemePreferenceService);
  }
}

bootstrapApplication(AppComponent, {
  providers: [
    provideAnimationsAsync(),
    provideHttpClient(withInterceptors([httpErrorInterceptor])),
    provideRouter(routes),
    // Empty root URL: requests stay relative so the dev proxy (proxy.conf.json)
    // and the prod nginx /api/ location both route them to the backend.
    provideApiConfiguration(''),
    HealthService
  ]
}).catch((error: unknown) => console.error(error));
