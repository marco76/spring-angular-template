# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

This is a reusable Angular + Spring Boot project template (not a product with business features yet). Keep changes small, typed, tested, and conventional.

Before changing code, read in this order:

1. `AGENTS.md` — project shape, rules, do-not-do list, verification commands.
2. `docs/ARCHITECTURE.md` and `docs/CONVENTIONS.md`.
3. Relevant specs under `docs/specs/` if the change touches behavior, APIs, schema, UI flows, or business rules.
4. The closest checklist under `.agent/tasks/`, and the matching checklist in `SKILLS.md`.

Before saying a change is done, run the narrowest relevant script under `scripts/check*.sh`, `scripts/smoke.sh`, or `scripts/doctor.sh`, and update `docs/DECISIONS.md`, `docs/PROJECT_STATE.md`, `docs/NEXT_STEPS.md`, or `docs/KNOWN_LIMITATIONS.md` only when there is durable context worth carrying forward.

All rules in `AGENTS.md` apply; this file only exists so they load automatically instead of needing to be discovered.

## Commands

Run the whole local stack:

```bash
./scripts/dev.sh docker      # Docker PostgreSQL stack
./scripts/dev.sh h2          # backend (H2) + Angular dev server, fastest
./scripts/dev.sh postgres    # backend against local PostgreSQL + Angular dev server
./scripts/deploy-local.sh up # same as dev.sh docker, plus open/logs/down subcommands
```

Backend (from `backend/`, or `-f backend/pom.xml` from root):

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local   # run with H2
mvn test                                                # all backend tests
mvn test -Dtest=ClassName                               # single test class
mvn test -Dtest=ClassName#methodName                    # single test method
SKIP_DOCKER_TESTS=true ./scripts/check-backend.sh       # skip Testcontainers/docker-tagged tests
```

Frontend (from `frontend/`):

```bash
npm start                                    # ng serve, dev server on :4200
npm run build                                # production build
npm test                                     # Karma/Jasmine, watch mode
npm test -- --no-watch --browsers=ChromeHeadless               # CI-style single run
npm test -- --no-watch --browsers=ChromeHeadless --include='**/foo.component.spec.ts'  # single spec
npm run lint
npm run format          # prettier --write
npm run format:check
npm run generate:api    # regenerate API client (see below)
```

Combined checks (what to run before calling something done):

```bash
./scripts/check-backend.sh    # mvn test (+ format/lint if added later)
./scripts/check-frontend.sh   # format:check + lint + headless tests
./scripts/check.sh            # both of the above
./scripts/smoke.sh            # curl frontend + backend /actuator/health on a running stack
./scripts/doctor.sh [--full]  # environment/template sanity check; --full also compiles/builds
```

Local instance safety:

Do not kill, restart, stop, or replace backend/frontend/database processes that
you did not start in the current task. If a developer already has the app
running on the default ports, keep it running and use alternate ports for
temporary checks instead:

```bash
BACKEND_PORT=18080 FRONTEND_PORT=14200 ./scripts/dev.sh h2
FRONTEND_URL=http://localhost:14200 BACKEND_HEALTH_URL=http://localhost:18080/actuator/health ./scripts/smoke.sh
```

Regenerating the frontend API client (backend must be running):

```bash
./scripts/generate-api.sh     # fetches /v3/api-docs, writes frontend/openapi.json, runs ng-openapi-gen
```

Scaffolding a new project from this template:

```bash
./create-project.sh <name> <java.package> <target-dir> [--ui=basic|minimal] [--theme=default|ft|gl] [--mcp] [--skip-git] [--skip-install]
```

## Architecture

Two-part app: Angular frontend talks to a Spring Boot backend over a REST API the backend owns, backed by PostgreSQL (Docker/prod) or H2 (local backend-only runs — see `docs/KNOWN_LIMITATIONS.md` for where the two diverge). Full diagrams and rationale are in `docs/ARCHITECTURE.md`; the essentials:

**Backend** is organized by feature package, not technical layer:

```text
feature/
  FeatureController.java   # request/response models only, calls services only
  FeatureService.java      # transactions, business decisions
  db/FeatureRepository.java, FeatureEntity.java   # persistence, isolated here
  model/FeatureResponse.java
  types/FeatureStatus.java
```

Controllers never see JPA entities or call repositories directly. Schema changes go only through Flyway (`backend/src/main/resources/db/migration`, `ddl-auto=validate`) — never Hibernate auto-update. Error handling is centralized but has two producers: `GlobalExceptionHandler` (`@RestControllerAdvice`) catches everything that reaches `DispatcherServlet`; `SecurityConfig`'s `AccessDeniedHandler`/`AuthenticationEntryPoint` handle denials/auth failures thrown by the security filter chain itself (before dispatch), writing the same `ErrorResponse` JSON shape by hand (no `ObjectMapper` injection there — see `docs/DECISIONS.md` for why).

**Frontend** is standalone Angular with lazy feature routes:

```text
src/app/
  core/       app-wide services, generated API client (core/api/), auth, error handling
  features/   route-level features
  layout/     shell/navigation
  shared/     reusable presentational components
```

All HTTP errors are normalized exactly once, in `core/error/http-error.interceptor.ts`, into a typed `ApiError` (`core/error/api-error.ts`); it rethrows rather than swallowing, so components still render their own loading/error state.

**Cross-cutting**: OpenAPI (backend `/v3/api-docs`) is the source of truth for the API contract; `./scripts/generate-api.sh` regenerates the committed Angular client. Full conventions (DI style, DTO rules, the 300-line file limit, no nested classes, component/service split, etc.) live in `docs/CONVENTIONS.md` — read it before writing code rather than relying on this summary.

**Deployment topology** — three ways to run locally, matched to what you're doing:

| Mode | Command | Database |
|---|---|---|
| Backend + frontend, no Docker | `./scripts/dev.sh` | H2 (in-memory) |
| Full stack, Docker | `./scripts/deploy-local.sh up` | PostgreSQL (container) |
| Production-like, Docker | `docker compose up --build` | PostgreSQL (container) |

## Do Not Do

- Do not bypass backend security with frontend feature flags or hidden UI.
- Do not mix local sample data into Flyway schema migrations unless it is required reference data.
- Do not put secrets, tokens, passwords, or real credentials in committed files.
- Do not add dependencies without a clear need and a docs update.
- Do not use H2-specific behavior for code that must run against PostgreSQL.
- Do not hand-edit the generated Angular API client when generation is enabled.
- Do not leave behavior/spec/API mismatches unresolved — stop and clarify instead.
</content>
