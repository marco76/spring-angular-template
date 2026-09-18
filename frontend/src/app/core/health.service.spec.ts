import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideApiConfiguration } from './api/api-configuration';
import { HealthResponse } from './api/models/health-response';
import { HealthService } from './health.service';

describe('HealthService', () => {
  let service: HealthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideApiConfiguration('')]
    });
    service = TestBed.inject(HealthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('loads health status from GET /api/health', () => {
    const expected: HealthResponse = { status: 'ok', timestamp: '2026-01-01T00:00:00Z' };
    let actual: HealthResponse | undefined;

    service.load().subscribe((response) => (actual = response));

    const req = httpMock.expectOne('/api/health');
    expect(req.request.method).toBe('GET');
    req.flush(expected);

    expect(actual).toEqual(expected);
  });
});
