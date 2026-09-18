# Project Skills

Use these operating modes when working on this template or projects created from it.

## Spring Backend

Use for backend APIs, data model work, security, validation, scheduling, migrations, and tests.

Checklist:

- Confirm whether the change touches database schema.
- Add Flyway migrations for schema changes.
- Keep H2 and PostgreSQL compatibility in mind.
- Prefer PostgreSQL Testcontainers for persistence tests.
- Keep API contracts documented through OpenAPI.
- Prefer DTO projections for read-only queries and screens that do not need entities.

## Angular Frontend

Use for pages, components, routing, forms, API wiring, and UI state.

Checklist:

- Use standalone components.
- Keep components OnPush-compatible.
- Prefer typed services and generated API clients.
- Verify responsive behavior before calling UI work done.

## API Contract

Use whenever backend DTOs, endpoints, or frontend API calls change.

Checklist:

- Update OpenAPI output.
- Regenerate frontend client/models.
- Check nullability, enums, date formats, and error responses.

## Database Migration

Use for any schema, seed-data, index, or constraint change.

Checklist:

- Write a new Flyway migration.
- Do not edit old migrations after they are shared.
- Test against PostgreSQL.
- Keep H2 compatibility only where practical; production correctness wins.

## Release Readiness

Use before merging or deploying.

Checklist:

- Backend tests pass.
- Frontend build passes.
- Docker images build.
- Health endpoint responds.
- Migrations apply cleanly to an empty database.

## Agent Workflow

Use before starting medium or large changes.

Checklist:

- Read the matching `.agent/tasks/*.md` checklist.
- Check `docs/PROJECT_STATE.md` for current context.
- Record lasting architecture decisions in `docs/DECISIONS.md`.
- Update `docs/NEXT_STEPS.md` for deliberate follow-up work.
- Run the relevant script under `scripts/check*.sh`.

## MCP Integration

Use only when the app should expose or consume tools/resources/prompts.

Checklist:

- Keep MCP disabled by default.
- Secure HTTP MCP endpoints with Spring Security.
- Expose only intentionally safe tools.
- Do not expose database write operations as MCP tools without explicit authorization.
