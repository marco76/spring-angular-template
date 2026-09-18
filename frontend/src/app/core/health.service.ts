import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map } from 'rxjs/operators';
import { ApiConfiguration } from './api/api-configuration';
import { health } from './api/fn/health-controller/health';

@Injectable({ providedIn: 'root' })
export class HealthService {
  private readonly http = inject(HttpClient);
  private readonly apiConfig = inject(ApiConfiguration);

  load() {
    return health(this.http, this.apiConfig.rootUrl).pipe(map((response) => response.body));
  }
}
