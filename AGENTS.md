# Agent Guide

This repository is an empty Angular + Spring Boot project template. Keep changes small, typed, tested, and conventional.

## Project Shape

```text
backend/   Spring Boot 4, Java 25
frontend/  Angular 22
scripts/   Build and deployment helpers
docs/      Architecture, workflow, and decision memory
docs/specs Written product, technical, and workflow specifications
.agent/    Reusable task checklists for AI agents
```

## Agent Workflow

Before changing code:

- Read `docs/ARCHITECTURE.md`.
- Read `docs/CONVENTIONS.md`.
- Read relevant written specifications under `docs/specs/` before changing behavior, APIs, database schema, UI flows, or business rules.
- Pick the closest checklist under `.agent/tasks/`.

If implementation and specs disagree, stop and clarify instead of silently changing the contract.

Before saying done:

- Run the narrowest relevant check script.
- Update docs when behavior, architecture, workflow, flags, or API contracts change.
- Update `docs/PROJECT_STATE.md` only when future sessions need durable context.

## Specifications

Project-specific written specifications live under:

```text
docs/specs/
```

Use specs as the source of product and domain intent. Keep implementation details in architecture/API docs and durable product requirements in specs.

## Rules

Backend and frontend conventions (injection style, DTOs, Flyway, feature structure, the 300-line limit, no nested classes, component/service split, and more) live in `docs/CONVENTIONS.md`. Read it before changing code — this file does not repeat it.

Also add or update tests when changing behavior.

## Do Not Do

On top of `docs/CONVENTIONS.md`:

- Do not bypass backend security with frontend feature flags or hidden UI.
- Do not mix local sample data into Flyway schema migrations unless it is required reference data.
- Do not put secrets, tokens, passwords, or real credentials in committed files.
- Do not add dependencies without a clear need and a docs update.
- Do not use H2-specific behavior for code that must run against PostgreSQL.
- Do not leave behavior/spec/API mismatches unresolved.

## API Contract Rules

- Treat backend request/response models and OpenAPI as the contract.
- Do not hand-edit generated API clients when generation is enabled.
- Update `docs/API_CONTRACT.md` when endpoint meaning changes.

## Docker Rules

- Local Docker uses PostgreSQL, not H2.
- Backend image is built from `Dockerfile.backend`.
- Frontend image is built from `Dockerfile.frontend`.
- Do not put secrets in Dockerfiles or committed compose files.

## Verification

Use these before handing work back:

```bash
./scripts/check-backend.sh
./scripts/check-frontend.sh
./scripts/check.sh
./scripts/smoke.sh
./scripts/doctor.sh
```

## Local Instance Safety

When testing changes, do not kill, restart, stop, or replace a local backend,
frontend, Docker stack, or database process that you did not start in the
current task. A developer may have started it manually and may be using its
state.

If the default ports are already occupied, leave those processes running and
use alternate ports for temporary agent-owned checks instead:

```bash
BACKEND_PORT=18080 FRONTEND_PORT=14200 ./scripts/dev.sh h2
FRONTEND_URL=http://localhost:14200 BACKEND_HEALTH_URL=http://localhost:18080/actuator/health ./scripts/smoke.sh
```

Only stop processes that the current task started, and only when they are no
longer needed.

## Commit Style

Use Conventional Commits:

```text
feat: add account profile page
fix: validate duplicate usernames
chore: update angular dependencies
```
