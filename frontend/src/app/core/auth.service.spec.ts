import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideApiConfiguration } from './api/api-configuration';
import { CurrentUserResponse } from './api/models/current-user-response';
import { authCredentialsInterceptor } from './auth-credentials.interceptor';
import { AuthService } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;
  let http: HttpClient;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authCredentialsInterceptor])),
        provideHttpClientTesting(),
        provideApiConfiguration('')
      ]
    });
    service = TestBed.inject(AuthService);
    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('exposes the authenticated user and initials on a successful GET /api/auth/me', () => {
    const expected: CurrentUserResponse = { username: 'admin', roles: ['ROLE_ADMIN'] };

    httpMock.expectOne('/api/auth/me').flush(expected);

    expect(service.authStatus()).toBe('authenticated');
    expect(service.currentUser()).toEqual(expected);
    expect(service.isAdmin()).toBeTrue();
    expect(service.initials()).toBe('AD');
  });

  it('falls back to anonymous when the request is unauthorized', () => {
    httpMock
      .expectOne('/api/auth/me')
      .flush('Authentication is required.', { status: 401, statusText: 'Unauthorized' });

    expect(service.authStatus()).toBe('anonymous');
    expect(service.currentUser()).toBeNull();
    expect(service.isAdmin()).toBeFalse();
  });

  it('refresh() re-requests the current user', () => {
    httpMock
      .expectOne('/api/auth/me')
      .flush('Authentication is required.', { status: 401, statusText: 'Unauthorized' });

    service.refresh();

    const expected: CurrentUserResponse = { username: 'user', roles: ['ROLE_USER'] };
    httpMock.expectOne('/api/auth/me').flush(expected);

    expect(service.authStatus()).toBe('authenticated');
    expect(service.currentUser()).toEqual(expected);
    expect(service.isAdmin()).toBeFalse();
  });

  it('signIn() sends Basic credentials and stores them for later API calls', () => {
    httpMock
      .expectOne('/api/auth/me')
      .flush('Authentication is required.', { status: 401, statusText: 'Unauthorized' });

    const expected: CurrentUserResponse = { username: 'admin', roles: ['ROLE_ADMIN'] };
    const expectedHeader = `Basic ${btoa('admin:admin')}`;
    let actual: CurrentUserResponse | undefined;

    service.signIn('admin', 'admin').subscribe((user) => (actual = user));

    const signInRequest = httpMock.expectOne('/api/auth/me');
    expect(signInRequest.request.headers.get('Authorization')).toBe(expectedHeader);
    signInRequest.flush(expected);

    http.get('/api/admin').subscribe();

    const protectedRequest = httpMock.expectOne('/api/admin');
    expect(protectedRequest.request.headers.get('Authorization')).toBe(expectedHeader);
    protectedRequest.flush({});

    expect(actual).toEqual(expected);
    expect(service.authStatus()).toBe('authenticated');
    expect(service.currentUser()).toEqual(expected);
    expect(service.isAdmin()).toBeTrue();
  });

  it('signOut() clears stored credentials and authenticated state', () => {
    httpMock
      .expectOne('/api/auth/me')
      .flush('Authentication is required.', { status: 401, statusText: 'Unauthorized' });

    service.signIn('admin', 'admin').subscribe();
    httpMock.expectOne('/api/auth/me').flush({ username: 'admin', roles: ['ROLE_ADMIN'] });

    service.signOut();
    http.get('/api/admin').subscribe();

    const protectedRequest = httpMock.expectOne('/api/admin');
    expect(protectedRequest.request.headers.has('Authorization')).toBeFalse();
    protectedRequest.flush({});

    expect(service.authStatus()).toBe('anonymous');
    expect(service.currentUser()).toBeNull();
    expect(service.isAdmin()).toBeFalse();
  });
});
