# API Contract

The backend API is owned by Spring Boot and documented through OpenAPI.

## Rules

- Backend request/response models define the API contract.
- Frontend code uses the generated API client under `frontend/src/app/core/api`; do not hand-write duplicate models or call `HttpClient` directly for backend endpoints.
- Controller methods must declare an explicit `produces` content type (for example `MediaType.APPLICATION_JSON_VALUE`). Without it, springdoc emits a `*/*` content type and the generator falls back to treating the response as an opaque `Blob` instead of parsed JSON.
- Treat nullability, enum values, date/time formats, and error response shapes as part of the contract.
- Every error response uses one shape: `{ status, error, message, path, timestamp, fieldErrors? }`, produced backend-side by `error.GlobalExceptionHandler` / `SecurityConfig`'s access-denied and authentication handlers, and consumed frontend-side as `ApiError` (`core/error/api-error.ts`) via `core/error/http-error.interceptor.ts`.

## Current Endpoints

```text
GET /api/admin
PUT /api/admin/settings/theme
GET /api/admin/users
POST /api/admin/users
PATCH /api/admin/users/{id}
DELETE /api/admin/users/{id}
GET /api/auth/me
GET /api/health
GET /api/settings/theme
GET /actuator/health
GET /v3/api-docs
GET /swagger-ui/index.html
```

`GET /api/auth/me` returns the authenticated principal's username and granted authorities (`{ username, roles }`); it requires authentication like any endpoint not listed under `permitAll()` in `SecurityConfig`, and is used by the frontend's `AuthService` to verify the header login form and drive the account area in the header (see `docs/KNOWN_LIMITATIONS.md` for what "signing in/out" means with HTTP Basic).

`/api/admin/users/**` is the admin user management API (`user.UserController`), `hasRole('ADMIN')`-protected like the rest of `/api/admin/**`:

- `GET /api/admin/users` — paginated list (`Page<UserResponse>`, standard Spring Data pagination query params).
- `POST /api/admin/users` — create (`CreateUserRequest`: `username`, `password` min 8 chars, `role`); `201` with a `Location` header, or `409` for a duplicate username.
- `PATCH /api/admin/users/{id}` — partial update (`UpdateUserRequest`: `role`, `enabled` always applied; `password` optional, left as-is when blank/omitted); `404` if the user does not exist, `409` if it would remove the last enabled admin.
- `DELETE /api/admin/users/{id}` — `204`; `409` for self-deletion or removing the last enabled admin, `404` if the user does not exist.

Users are persisted in `app_user` (Flyway `V2__create_app_user.sql`) and back `AppUserDetailsService`, which replaced the scaffold's `InMemoryUserDetailsManager`. The first admin account is created by `UserBootstrapRunner` from `app.bootstrap-admin.username`/`app.bootstrap-admin.password` when `app_user` is empty; `application-local.yml` sets these to the `admin`/`admin` demo values (see `docs/KNOWN_LIMITATIONS.md`), other environments must set them explicitly or create the first admin manually.

The runtime visual theme is persisted app-wide in `app_setting` under key `ui.theme`:

- `GET /api/settings/theme` — public read used during app startup; returns `{ theme }` with `DEFAULT`, `FT`, or `GL`.
- `PUT /api/admin/settings/theme` — admin-only update (`UpdateThemeRequest`: `theme` as `DEFAULT`, `FT`, or `GL`); returns the saved `{ theme }`.

## API Workflow

1. Change backend controller/model/service.
2. Run backend tests.
3. Start the backend locally (`cd backend && mvn spring-boot:run -Dspring-boot.run.profiles=local`).
4. Regenerate the frontend client: `./scripts/generate-api.sh`.
5. Update Angular code to use the generated types.
6. Run the frontend build.
7. Commit the regenerated files under `frontend/src/app/core/api` alongside the backend change.
8. Update this file if the contract meaning changed.

The generated client is committed to the repo so the frontend builds without a running backend; `frontend/openapi.json` (the raw fetched spec) is a local, gitignored intermediate file.
