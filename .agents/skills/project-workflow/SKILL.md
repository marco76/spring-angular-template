---
name: project-workflow
description: Use before making any non-trivial change in this repo (spring-angular-template) — a Spring Boot + Angular project template. Surfaces the required reading order, the matching SKILLS.md checklist and .agent/tasks/ procedure, and the verification/doc-update steps to complete before calling a change done.
---

# Project Workflow

This is a reusable Angular + Spring Boot template, not a product with business
features. Keep changes small, typed, tested, and conventional.

## 1. Read, in order, before changing code

1. `AGENTS.md` — project shape, rules, do-not-do list, verification commands.
2. `docs/ARCHITECTURE.md` and `docs/CONVENTIONS.md`.
3. Relevant specs under `docs/specs/` if the change touches behavior, APIs,
   schema, UI flows, or business rules.
4. The closest checklist under `.agent/tasks/` (e.g. `add-api-endpoint.md`,
   `add-db-migration.md`, `add-feature.md`, `add-full-stack-feature.md`,
   `add-page.md`, `release-check.md`, `review-security.md`), and the matching
   section in `SKILLS.md`:
   - Spring Backend
   - Angular Frontend
   - API Contract
   - Database Migration
   - Release Readiness
   - Agent Workflow
   - MCP Integration

## 2. Do-not-do list (from AGENTS.md)

- Do not bypass backend security with frontend feature flags or hidden UI.
- Do not mix local sample data into Flyway schema migrations unless it is
  required reference data.
- Do not put secrets, tokens, passwords, or real credentials in committed
  files.
- Do not add dependencies without a clear need and a docs update.
- Do not use H2-specific behavior for code that must run against PostgreSQL.
- Do not hand-edit the generated Angular API client when generation is
  enabled.
- Do not leave behavior/spec/API mismatches unresolved — stop and clarify
  instead.
- Do not kill, restart, stop, or replace local backend/frontend/database
  processes that were not started by the current task. If ports are occupied,
  run temporary checks on alternate ports (for example
  `BACKEND_PORT=18080 FRONTEND_PORT=14200 ./scripts/dev.sh h2`) and point
  smoke/API checks at those ports.

## 3. Before saying a change is done

- Run the narrowest relevant script:
  - `./scripts/check-backend.sh` (backend only)
  - `./scripts/check-frontend.sh` (frontend only)
  - `./scripts/check.sh` (both)
  - `./scripts/smoke.sh` (curl frontend + backend `/actuator/health` on a
    running stack)
  - `./scripts/doctor.sh [--full]` (environment/template sanity check)
- Update `docs/DECISIONS.md`, `docs/PROJECT_STATE.md`,
  `docs/NEXT_STEPS.md`, or `docs/KNOWN_LIMITATIONS.md` only when there is
  durable context worth carrying forward — not for every change.

## 4. Template propagation

This whole repo (including `.agents/skills/`) is copied by `create-project.sh`
into every project scaffolded from this template — keep this skill generic to
the template, not specific to any one downstream project.
