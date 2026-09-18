# Conventions

## General

- Keep changes small and easy to review.
- Prefer explicit names over clever abstractions.
- Read relevant specs under `docs/specs/` before changing behavior, APIs, database schema, UI flows, or business rules.
- Add tests or explain why a change is too small to need them.
- Update docs when behavior, workflow, or architecture changes.
- Organize code by feature, not by technical layer (`feature/FooController.java`, `features/foo/foo.component.ts`), on both backend and frontend.
- Keep classes and files at or under 300 lines. Split into smaller collaborators before exceeding it.

## Backend

Structural rules below marked **(ArchUnit)** are enforced by the rule classes under `backend/src/test/java/com/example/app/architecture/` (one class per concern: `ControllerRulesTest`, `PersistenceRulesTest`, `InjectionRulesTest`, `TransactionRulesTest`, `StructureRulesTest`, sharing predicates from `ArchPredicates`) and fail `mvn test` on violation, not just this doc.

- Java 25.
- Spring Boot 4.
- Lombok is available for reducing boilerplate in backend implementation classes.
- Constructor injection only **(ArchUnit)**.
- Controllers call services, not repositories **(ArchUnit)**.
- Controllers return request/response models, not JPA entities **(ArchUnit)**.
- Repositories and JPA entities live under `feature/db/` (for example `feature/db/FeatureRepository.java`, `feature/db/FeatureEntity.java`) **(ArchUnit)**.
- Enums live under `feature/types/` (for example `feature/types/FeatureStatus.java`) **(ArchUnit)**.
- Service-layer transactions; controllers and repositories should not declare their own `@Transactional` **(ArchUnit)**.
- Flyway migrations only; do not use Hibernate schema updates.
- Keep `spring.jpa.hibernate.ddl-auto=validate`.
- Prefer records for immutable request/response types.
- Use SLF4J logging, not `System.out.println`.
- Prefer keeping filter logic in the database over the application layer.
- Handle exceptions in `error.GlobalExceptionHandler` (`@RestControllerAdvice`), not with try/catch in controllers or services. Add a new `@ExceptionHandler` method there for a new exception type instead of formatting an error response locally.
- Do not log secrets, tokens, or personal data.
- No nested/inner classes, records, or enums; each type gets its own top-level file (request/response models under `feature/model/`, enums under `feature/types/`, repositories/entities under `feature/db/`) **(ArchUnit)**.
- Method-level authorization is available through `@PreAuthorize` and similar annotations. Keep authorization checks on the backend even when the frontend hides UI.
- If a feature accepts file uploads, set `spring.servlet.multipart.*` limits explicitly for that feature's expected file sizes; do not rely on Spring Boot defaults.
- Store binary file content in PostgreSQL as `bytea` and map it as a plain `byte[]` field. Do not add `@Lob` for ordinary uploaded files, because Hibernate maps `@Lob byte[]` to PostgreSQL large-object `oid`.
- Local-only sample accounts or seed data should live behind `@Profile("local")`, usually in an idempotent `ApplicationRunner`. Never let local seeders run in production profiles.
- Validate request bodies with Bean Validation (`@NotNull`, `@Size`, `@Email`, etc. on request models, `@Valid` on the controller parameter). `GlobalExceptionHandler` already handles `MethodArgumentNotValidException`.
- Paginate list endpoints (`Page<T>`/`Pageable`), not `List<T>`. Spring Data's paging support is already auto-configured.
- Use correct HTTP semantics for writes: `201 Created` with a `Location` header from creates, `204 No Content` from deletes, `PUT` for a full replace, `PATCH` for a partial update.
- Mark read-only service methods `@Transactional(readOnly = true)`.
- Prefer DTO projections for read-only repository requests, especially list and summary screens that do not need hydrated JPA entities.
- Never return `null` collections from a service or repository; return an empty collection.
- Keep service classes focused on transactions and business decisions — don't let unrelated helper logic accumulate there as private methods. Extract mapping (entity ↔ request/response), formatting, or other supporting logic into its own top-level class under `feature/` (for example `FeatureMapper`, constructor-injected like any other collaborator) once any of these is true: it needs an injected dependency, it's reused by more than one service, or it's grown past trivial one-to-one field assignment. Below that bar, a private method on the service is fine.

### Turning On Session Authentication

The scaffold disables CSRF while only public starter endpoints exist. If the app adds browser session or cookie-based login, turn CSRF back on before protecting mutating endpoints.

Use Angular's default `XSRF-TOKEN` cookie and `X-XSRF-TOKEN` header names, and make sure Spring resolves the token on each request so the cookie is written for the frontend. A typical Spring Security shape is:

```java
.csrf(csrf -> csrf
    .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
```

Add a small filter after `BasicAuthenticationFilter` that reads the `CsrfToken` request attribute; this forces token creation for REST requests.

## Frontend

- Angular standalone components.
- Strict TypeScript.
- `inject()` for dependencies.
- Signals for local state where they make code clearer.
- Native Angular control flow.
- Route-level lazy loading for features.
- Keep API access in services under `src/app/core`.
- Prefer Angular Material for base UI controls.
- No duplicated backend models when generated API models exist.
- Components stay thin: bind data and handle template events only. Business logic (calculations, validation, data shaping) lives in a service or a pure function, not in the component class.
- HTTP errors are normalized once in `core/error/http-error.interceptor.ts` (an `HttpInterceptorFn` registered in `main.ts`), not parsed ad hoc by each service or component. It rethrows a typed `ApiError` (see `core/error/api-error.ts`) so callers still render their own loading/error state — it does not swallow errors or decide UI behavior.
- Prefer Reactive Forms over template-driven forms for anything beyond a trivial input.
- Use `takeUntilDestroyed()` for any manual `.subscribe()`; prefer the `async` pipe when possible so there's nothing to unsubscribe.
- Prefer the signal-based `input()`/`output()` functions over the `@Input()`/`@Output()` decorators.
- Keep components `ChangeDetectionStrategy.OnPush` (the schematics default in `angular.json`) — do not override it per component without a specific reason.
- Do not use barrel files (`index.ts` re-export files); import directly from the source file.

## Feature Flags

- Frontend flags hide UI.
- Backend flags enforce behavior.
- Do not keep stale flags after rollout.
- Do not expose internal provider keys or rollout rules to Angular.

## Documentation

When making a meaningful architectural choice, add an entry to `docs/DECISIONS.md`.

When leaving incomplete work, update `docs/NEXT_STEPS.md` or `docs/KNOWN_LIMITATIONS.md`.
