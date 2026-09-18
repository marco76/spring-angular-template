import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map } from 'rxjs/operators';
import { ApiConfiguration } from './api/api-configuration';
import { overview } from './api/fn/admin-controller/overview';

@Injectable({ providedIn: 'root' })
export class AdminService {
  private readonly http = inject(HttpClient);
  private readonly apiConfig = inject(ApiConfiguration);

  loadOverview() {
    return overview(this.http, this.apiConfig.rootUrl).pipe(map((response) => response.body));
  }
}
