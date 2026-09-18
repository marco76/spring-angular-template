# Decisions

Record durable architecture decisions here. Keep entries short and dated.

## Template

### Run Mockito As A Test JVM Agent

Date: 2026-09-15

Decision: Configure `maven-surefire-plugin` with `-javaagent:${settings.localRepository}/org/mockito/mockito-core/${mockito.version}/mockito-core-${mockito.version}.jar`.

Reason: Java 25 blocks Mockito's inline mock maker from reliably self-attaching in this environment, which caused every Mockito-based test to fail before assertions ran. Loading Mockito as the test JVM agent is the supported path and keeps `./scripts/check-backend.sh` meaningful.

### Add Optional Runtime Starter Themes

Date: 2026-09-15

Decision: Add `--theme=default|ft|gl` to `create-project.sh`. The default theme remains the existing neutral navy/amber palette, the FT theme provides a warm editorial palette, claret accents, teal links, sharper radii, and flatter shadows, and the GL theme provides a white corporate palette with serif headings, teal accents, and warm taupe actions. Generated apps keep all theme token files and persist the selected app-wide runtime theme in `app_setting` (`ui.theme`), with browser storage used only as a startup fallback before the backend responds.

Reason: The template should support a polished editorial starter style without duplicating the app shell. Keeping both palettes as token files lets the shared shell/page CSS stay generic, while Admin-page runtime switching lets teams try or change the visual direction after project initialization.

### Use Angular + Spring Boot

Date: initial template

Decision: Build as an Angular frontend and Spring Boot backend.

Reason: This keeps frontend and backend concerns clear while supporting typed API contracts, standard deployment, and strong testing boundaries.

### Use Flyway For Database Migrations

Date: initial template

Decision: Flyway owns database schema changes.

Reason: SQL migrations are explicit, reviewable, and easy for agents to reason about. Hibernate validates the schema but does not create or mutate it.

### Remove OpenFeature For Now

Date: 2026-09-14

Decision: Remove the OpenFeature backend config, `/api/features` controller, and the Angular OpenFeature provider/dependency.

Reason: No project built from this template had an active use for feature flags yet, and the in-memory provider was not wired to real infrastructure. Re-add a flagging library (OpenFeature or otherwise) when a project actually needs runtime flags. If reintroduced, keep the same rule: backend flags enforce behavior, frontend flags only hide UI.

### Generate The Angular API Client From OpenAPI

Date: 2026-09-14

Decision: Use `ng-openapi-gen` to generate a typed Angular client from the backend's `/v3/api-docs` spec, via `./scripts/generate-api.sh`. Commit the generated output under `frontend/src/app/core/api`; do not hand-write frontend models for backend request/response models.

Reason: The docs already stated "do not hand-write duplicated API models when OpenAPI generation is available" without anything implementing generation. Wiring it now, while the API surface is tiny, is far cheaper than retrofitting it after the contract grows. Controllers must declare an explicit `produces` content type or springdoc emits `*/*` and the generator falls back to an untyped `Blob` response.

### Add The H2 Console Module Explicitly

Date: 2026-09-14

Decision: Depend on `org.springframework.boot:spring-boot-h2console` (runtime scope) and permit `PathRequest.toH2Console()` with same-origin framing in `SecurityConfig`.

Reason: Spring Boot 4 split `H2ConsoleAutoConfiguration` out of `spring-boot-autoconfigure` into its own module (new package `org.springframework.boot.h2console.autoconfigure`). Without the explicit dependency, `spring.h2.console.enabled=true` (set in `application-local.yml`) silently registers nothing — `/h2-console` 404s instead of erroring, so the gap is easy to miss. The `PathRequest` helper Spring Security's own docs point to for this also moved packages, to `org.springframework.boot.security.autoconfigure.web.servlet.PathRequest`. This is scoped safely: `spring.h2.console.enabled` defaults to `false` and stays unset in `application-prod.yml`, so the console is inert in prod regardless of what's on the classpath.

### Declare The Dev Proxy In angular.json, Not Only As A CLI Flag

Date: 2026-09-14

Decision: Set `proxyConfig` in `angular.json`'s `serve` target options, and drop `--proxy-config proxy.conf.json` from `package.json`'s `start` script.

Reason: `main.ts` configures the generated API client with an empty root URL (`provideApiConfiguration('')`), so every `/api/...` call is relative and depends entirely on the dev-server proxy to reach the backend. When the proxy was only a flag on the `npm start` script, any other way of launching `ng serve` — an IDE's own run configuration (confirmed with IntelliJ), or typing `ng serve` directly — silently skipped it, and every API call 404'd against the dev server itself instead of the backend. Declaring it in `angular.json` makes proxying apply no matter how `ng serve` is launched.

### Add Spring Boot DevTools

Date: 2026-09-14

Decision: Depend on `org.springframework.boot:spring-boot-devtools` (runtime scope, optional).

Reason: Local backend runs (`mvn spring-boot:run` or an IDE run configuration) had no auto-restart on rebuild, which is a standard expectation for iterative local development.

### Centralize Exception Handling

Date: 2026-09-14

Decision: Add `error.GlobalExceptionHandler` (`@RestControllerAdvice`) as the one place that turns exceptions into the app's JSON error shape (`error.model.ErrorResponse`), plus a matching `AccessDeniedHandler`/`AuthenticationEntryPoint` pair in `SecurityConfig`.

Reason: Controllers and services should let exceptions propagate instead of catching and formatting errors ad hoc. `@RestControllerAdvice` only sees exceptions Spring MVC's dispatch can catch — it does not see exceptions the security filter chain throws for URL-level `authorizeHttpRequests` rules (those run before DispatcherServlet), so those need their own handlers wired directly into `SecurityConfig`, sharing the same response shape.

The `SecurityConfig` handlers write JSON by hand rather than through an injected `ObjectMapper`: this app currently has two incompatible Jackson major versions on the classpath at once — Jackson 3 (`tools.jackson.*`, what Spring Boot 4's own `JacksonAutoConfiguration` wires by default) and classic Jackson 2 (`com.fasterxml.jackson.*`, pulled in transitively by other dependencies, notably in the `@WebMvcTest` slice used by the corresponding tests). Requesting `com.fasterxml.jackson.databind.ObjectMapper` as a bean fails, non-obviously, because the framework's own bean is the Jackson 3 type. The security layer has no business depending on which one wins in a given context, so it doesn't depend on either.

### Centralize Frontend HTTP Error Handling In An Interceptor

Date: 2026-09-14

Decision: Add `core/error/http-error.interceptor.ts` (a functional `HttpInterceptorFn`, registered via `provideHttpClient(withInterceptors([httpErrorInterceptor]))` in `main.ts`) as the one place that normalizes every HTTP error into `ApiError` (`core/error/api-error.ts`, the same shape as the backend's `ErrorResponse`) and logs it.

Reason: Mirrors the backend decision above — services and components should not each parse `HttpErrorResponse` themselves. The interceptor rethrows the normalized error rather than swallowing it, so components still render their own loading/error state (see `AdminComponent`, which now shows the backend's actual message instead of a hardcoded guess) the same way they already did with `catchError`; centralizing only removes the parsing/logging duplication, not the UI decision.

### Move create-project.sh To Named Options; Init Git And Install By Default

Date: 2026-09-14

Decision: Replace the optional 4th positional `[basic|minimal]` argument with named flags (`--ui=`, `--mcp`, `--skip-git`, `--skip-install`), and default to running `git init` with an initial commit and `npm install` in the generated frontend, both skippable.

Reason: A generated project previously had zero version history by default — a real risk once real work starts landing on top of it, especially with more than one agent/session editing the same files. `ng new` and `spring init` both default to git-init/install-on-generation for the same reason; this template didn't, so it now does. Named flags replace the single positional UI argument because a 5th or 6th positional option would stop being readable; `--mcp` uncomments the dependency block `pom.xml` already carried (previously only actionable by hand). The `git commit`/`npm install` steps are wrapped so a failure (missing git identity, no network) prints a status line instead of aborting — the rest of generation already succeeded and shouldn't be reported as failed alongside it.

### Fix AppShellComponent's Selector Mismatch (basic UI mode produced a blank app)

Date: 2026-09-14

Decision: Change `AppShellComponent`'s selector from `app-shell` to `app-root`, matching `index.html`'s bootstrap element.

Reason: `main.ts` bootstraps `AppShellComponent`, but `index.html` only ever had `<app-root></app-root>` — Angular had no matching element to bootstrap into, so `basic`-mode apps (the default UI mode) rendered a completely blank page with a silent console error (`NG05104`). This was invisible to every check this session ran, because none of them rendered the app in an actual browser — `ng build` succeeding, `ng test` passing, and `curl` returning 200 all say nothing about whether Angular could find its root element and paint anything. Caught only by loading the real Docker-built production output in headless Chrome and looking at a screenshot, which is now the standard this template's own verification should have applied earlier: a build/test/curl passing is not evidence a frontend renders. `minimal` UI mode was unaffected — its inline root component's selector already matched `app-root`.

### Enforce Backend Structure Rules With ArchUnit

Date: 2026-09-14

Decision: Add `com.tngtech.archunit:archunit-junit5` (test scope) and one rule class per concern under `backend/src/test/java/com/example/app/architecture/` — `ControllerRulesTest`, `PersistenceRulesTest`, `InjectionRulesTest`, `TransactionRulesTest`, `StructureRulesTest` — sharing class-matching predicates from a plain (non-test) `ArchPredicates` helper. Together they check, as ordinary JUnit tests: controllers don't depend on repositories or entities; repositories/entities live under `..db..`; enums live under `..types..`; no field injection; no `@Transactional` on controllers or repositories; and no nested classes/records/enums.

Reason: `docs/CONVENTIONS.md` already stated all of these rules in prose, but nothing failed the build if a generated feature violated one — an agent (or a person) had to remember to re-read the doc and self-check. `mvn test` now fails immediately instead. One class per concern (rather than one `ArchitectureTest` holding all rules) keeps each file focused and lets each rule still report as its own independent test case — already true per-rule under ArchUnit's JUnit 5 runner regardless of file layout, but splitting keeps a file from becoming a dumping ground as more rules get added. `ArchPredicates` identifies controllers/repositories/entities by their actual Spring/JPA role (`@RestController`/`@Controller`, `assignableTo(Repository.class)`, `@Entity`), not by class name alone, so a class that breaks the naming convention but keeps the annotation (or vice versa) is still caught. Two rules (`*Repository`/`*Entity` under `..db..`, enums under `..types..`) use `allowEmptyShould(true)` because the template ships with no persistence layer yet; they still fire the moment the first matching class appears anywhere. Pin `archunit.version` to 1.5.0 or later — 1.3.0's bundled ASM cannot parse Java 25's class file version (major version 69, `IllegalArgumentException: Unsupported class file major version 69`) and silently imports zero classes, which makes every rule report a vacuous "failed to check any classes" error instead of a real pass/fail. `@AnalyzeClasses(packages = "com.example.app", ...)`'s package string is ordinary text to `create-project.sh`'s blind `com.example.app` → real-package substitution, so it renames correctly along with every other reference — verified by generating a project and running the suite against the renamed package.

### Replace The In-Memory User Store With A DB-Backed `app_user` Table And Admin CRUD API

Date: 2026-09-15

Decision: Add a `user` feature package (`UserEntity`/`UserRepository` under `user/db/`, `UserRole` under `user/types/`, `user/model/` request/response records, `UserService`, `UserController` at `/api/admin/users`, `hasRole('ADMIN')`-protected like the rest of `/api/admin/**`) backed by a new Flyway table (`V2__create_app_user.sql`). Replace `SecurityConfig`'s `InMemoryUserDetailsManager` with `AppUserDetailsService` (a normal `UserDetailsService` reading `UserRepository`) plus a `BCryptPasswordEncoder` bean. The first admin account is created by `UserBootstrapRunner` (an `ApplicationRunner`, not profile-restricted) from `app.bootstrap-admin.username`/`password` only when `app_user` is empty; `application-local.yml` sets these to the existing `admin`/`admin` demo value, and a separate `@Profile("local")` `LocalDemoUserSeeder` adds the non-admin `user`/`user` demo account, keeping local-only sample data behind the profile per `docs/CONVENTIONS.md`. `UserService` blocks self-deletion and removing/demoting the last enabled admin (via `ResponseStatusException`, so no new `GlobalExceptionHandler` wiring was needed).

Reason: `docs/KNOWN_LIMITATIONS.md` already flagged the in-memory `admin/admin` and `user/user` accounts as scaffold-only, and there was no way for an admin to add, update, or remove accounts. `ResponseEntity` could not be used in `UserController` — ArchUnit's `controllersDoNotDependOnEntities` rule matches JPA entities by `simpleNameEndingWith("Entity")` (see `ArchPredicates`), which also matches `org.springframework.http.ResponseEntity`; the create endpoint sets the `Location` header via `HttpServletResponse` instead. Seeding the demo admin only in `application-local.yml` (rather than unconditionally, as the old in-memory store did) also closes a real gap: previously `admin/admin` worked in every profile, including prod.

### Default IntelliJ Spring Boot Runs To The `local` Profile

Date: 2026-09-15

Decision: Add `.run/Spring Boot.run.xml`, a `default="true"` template configuration (not a named, runnable one) for IntelliJ's "Spring Boot" configuration type, setting `ACTIVE_PROFILES=local`.

Reason: The existing committed `.run/Application (local).xml` only covers the one specific, explicitly-selected run configuration. If someone instead clicks the gutter ▶ run icon on `Application.main()`, IntelliJ auto-generates a brand-new Spring Boot run configuration with no active profile — and `application.yml` declares no datasource outside the `local`/`prod` profiles, so that crashes on startup with a missing-datasource error. A `default="true"` configuration for a given type acts as IntelliJ's template for every configuration of that type created afterward in the project, including auto-generated ones, so this makes `local` the actual default without requiring anyone to remember to pick the named configuration first.

### Proxy /actuator, /swagger-ui, and /v3/api-docs, Not Only /api

Date: 2026-09-15

Decision: Add `/actuator`, `/swagger-ui`, and `/v3/api-docs` entries to `frontend/proxy.conf.json` (the `ng serve` dev proxy), and matching `location` blocks to `nginx.conf` (the Docker/prod-like path), alongside the existing `/api` proxying.

Reason: `HomeComponent` links to `/swagger-ui/index.html` and `/actuator/health`; `proxy.conf.json` and `nginx.conf` only proxied `/api`. Neither location fails loudly: the Angular dev server's SPA fallback (`try_files`-equivalent) serves its own `index.html` for any unmatched HTML request, returning `200` with the Angular app shell instead of Swagger UI or the actuator response, while logging `Request for HTML file "/swagger-ui/index.html" was received but no asset found. Asset may be missing from build.` to the console — easy to read as a build problem when it's a routing gap. `nginx.conf` had the same gap for `/swagger-ui` (already proxied `/actuator`, just not `/swagger-ui` or `/v3/api-docs`), via its own `try_files $uri $uri/ /index.html` fallback. Verified live: reproduced the wrong response body (Angular's own `index.html`, `<script src="/@vite/client">`) against a real running dev server before the fix, then confirmed real Swagger UI HTML, real actuator JSON, and a real OpenAPI document all come back correctly after adding the proxy entries, using a throwaway second `ng serve` instance so the running dev server wasn't disturbed. `/v3/api-docs`'s `nginx.conf` block also forwards `Host`/`X-Forwarded-Proto`, which springdoc uses to build the generated spec's `servers` entry — without them, Swagger UI's "Try it out" would target the backend's internal Docker address instead of the address the browser can actually reach.

### Extract create-project.sh's minimal-mode Files Out Of Heredocs

Date: 2026-09-14

Decision: Replace `create-project.sh`'s two large heredocs (`--ui=minimal`'s `main.ts` and `styles.css`, ~130 lines) with a real checked-in `frontend/src/main.minimal.ts` (swapped in for `main.ts` at generation time) plus one shared CSS rule (`.minimal-shell`) added directly to `frontend/src/styles.css`, inert in `--ui=basic`.

Reason: A bash heredoc holding TypeScript/CSS is invisible to every tool that would normally catch drift — not linted, not type-checked, not diffed clearly in review. This directly caused real problems twice this session: when the frontend HTTP-error interceptor was added, the heredoc had to be remembered and hand-updated separately from the real `main.ts`, and it's exactly the kind of copy nobody notices going stale. `main.minimal.ts` is now an ordinary source file — picked up by ESLint (`src/**/*.ts`), visible in a normal directory listing, easy to diff against `main.ts` when one changes. Cut `create-project.sh` from 333 to 204 lines with no behavior change (verified: rebuilt and screenshotted both UI modes, pixel-identical to before).

### Restyle The `--ui=basic` Shell Into A Professional Starting Point

Date: 2026-09-14

Decision: Add a small spacing/radius/shadow design-token layer at the top of `styles.css` (kept separate from `theme.css`'s Material color tokens), and rebuild the shell/dashboard/admin templates on top of it: a brand mark + nav icons in `AppShellComponent`, a multi-card `.card-grid` dashboard in `HomeComponent` (status, resource links, "getting started" tips — all generic template content, no invented product data) and a matching two-card layout in `AdminComponent`. No new npm dependencies; nav/brand icons are inline SVG rather than an icon font.

Reason: The original single centered card on an otherwise empty page read as a scaffold, not a starting point. Two real bugs surfaced only by rendering the result in a browser (per the `app-root` decision above, `ng build`/`ng test` prove nothing about visual correctness): (1) `mat-list-item`'s own generated CSS beats a plain single-class `.active-link { background: ... }` rule in the cascade, so the active nav item silently rendered with no highlight — fixed by raising specificity (`.nav-link.active-link`) plus `!important`, called out in a comment so it isn't "cleaned up" back to the broken version later. (2) `mat-sidenav` was hardcoded `mode="side" opened`, so at phone width it pushed page content off-screen instead of overlaying — pre-existing, not introduced by this change, but a professional starting point shouldn't break on a phone. Fixed with `BreakpointObserver` (already an `@angular/cdk` dependency) driving `[mode]`/`[opened]` via a signal (`toSignal`), matching Angular Material's own responsive-sidenav pattern; nav links call `sidenav.close()` on mobile after navigating. Verified by starting the real H2 stack and screenshotting both routes at desktop (1440px) and phone (390px) widths, sidenav open and closed.
