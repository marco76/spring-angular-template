import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ApiError } from './api-error';
import { httpErrorInterceptor } from './http-error.interceptor';

describe('httpErrorInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([httpErrorInterceptor])),
        provideHttpClientTesting()
      ]
    });
    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('passes a backend ApiError body through unchanged', () => {
    const backendError: ApiError = {
      status: 403,
      error: 'Forbidden',
      message: 'Access is denied.',
      path: '/api/admin',
      timestamp: '2026-01-01T00:00:00Z'
    };
    let caught: ApiError | undefined;

    http.get('/api/admin').subscribe({ error: (error: ApiError) => (caught = error) });

    httpMock.expectOne('/api/admin').flush(backendError, { status: 403, statusText: 'Forbidden' });

    expect(caught).toEqual(backendError);
  });

  it('normalizes a network failure (status 0) into an ApiError shape', () => {
    let caught: ApiError | undefined;

    http.get('/api/health').subscribe({ error: (error: ApiError) => (caught = error) });

    httpMock
      .expectOne('/api/health')
      .error(new ProgressEvent('error'), { status: 0, statusText: 'Unknown Error' });

    expect(caught?.status).toBe(0);
    expect(caught?.message).toBe('Could not reach the server.');
  });
});
