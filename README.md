# Angular + Spring Boot Starter

Reusable empty-project template for an Angular frontend and Spring Boot backend.

## Stack

- Angular 22, Angular Material, standalone components, strict TypeScript
- Spring Boot 4.1, Java 25 LTS
- Lombok for backend boilerplate reduction
- Spring Boot DevTools for auto-restart on backend rebuild
- PostgreSQL for Docker-based local development, staging, and production
- H2 for optional lightweight backend-only runs
- Flyway for database migrations
- Spring Security, Validation, Actuator, OpenAPI
- Generated Angular API client from the backend's OpenAPI spec (`ng-openapi-gen`)
- Angular ESLint and Prettier checks
- Optional Spring AI / MCP module

## First-Time Setup

Create a new project from this template:

```bash
./create-project.sh invoice-hub com.acme.invoice ../invoice-hub
```

This also runs `git init` with an initial commit and `npm install` in the generated frontend. The generator asks whether to include the basic Angular Material UI shell if you don't pass `--ui` explicitly. Options:

```bash
./create-project.sh invoice-hub com.acme.invoice ../invoice-hub --ui=basic
./create-project.sh invoice-hub com.acme.invoice ../invoice-hub --ui=minimal
./create-project.sh invoice-hub com.acme.invoice ../invoice-hub --theme=ft
./create-project.sh invoice-hub com.acme.invoice ../invoice-hub --theme=gl
./create-project.sh invoice-hub com.acme.invoice ../invoice-hub --mcp
./create-project.sh invoice-hub com.acme.invoice ../invoice-hub --skip-git --skip-install
```

Generated apps include the default, FT-inspired, and GL-inspired themes.
`--theme=ft` or `--theme=gl` only sets the initial theme; admins can switch
later from the Admin page, and the app-wide choice is saved in the database.

Then run:

```bash
./scripts/deploy-local.sh up
```

Manual setup is also fine: copy this directory, then replace `com.example.app` and `example-app`.

For the full first-run checklist, see [Getting Started](docs/GETTING_STARTED.md).

## Local Modes

Run the normal local development stack with PostgreSQL:

```bash
./scripts/deploy-local.sh up
```

Copy `.env.example` to `.env` when you need to override local Docker ports or database credentials.

Choose a local development mode interactively:

```bash
./scripts/dev.sh
```

Non-interactive options:

```bash
./scripts/dev.sh docker    # Docker PostgreSQL stack
./scripts/dev.sh h2        # backend with H2 + Angular dev server
./scripts/dev.sh postgres  # backend with local PostgreSQL + Angular dev server
```

If a developer already has a local instance running, do not stop it for
testing. Start temporary native checks on alternate ports instead:

```bash
BACKEND_PORT=18080 FRONTEND_PORT=14200 ./scripts/dev.sh h2
```

Run just the backend with H2:

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Run a production-like local stack with PostgreSQL:

```bash
docker compose up --build
```

Starter HTTP Basic users are included for local development:

```text
admin / admin
user / user
```

The generated app includes a default admin page at `/admin`; replace these demo credentials before using the scaffold for real authentication.

## Database Rules

Flyway owns the schema. Do not use Hibernate `ddl-auto=update` and do not mix schema creation through `schema.sql`.

Add migrations under:

```text
backend/src/main/resources/db/migration
```

Use names like:

```text
V1__initial_schema.sql
V2__add_user_table.sql
```

Keep local development seed data under:

```text
backend/src/main/resources/db/dev-data
```

Keep automated test fixtures under:

```text
backend/src/test/resources/db/test-data
```

## Useful Commands

```bash
./scripts/deploy-local.sh up
./scripts/deploy-local.sh open
./scripts/deploy-local.sh logs
./scripts/deploy-local.sh down
./scripts/dev.sh
./scripts/check.sh
./scripts/smoke.sh
./scripts/doctor.sh
./scripts/generate-api.sh
./scripts/build-deploy.sh test
./scripts/build-deploy.sh build
./scripts/build-deploy.sh docker
```

Formatting and linting:

```bash
cd frontend && npm run format
```

Production image publishing:

```bash
REGISTRY=ghcr.io/acme APP_NAME=invoice-hub TAG=0.1.0 ./scripts/deploy-prod.sh
```

## Optional MCP

Use MCP in two layers:

- Development tools: Angular MCP, Spring MCP, database MCP, GitHub MCP.
- Runtime app capability: Spring AI MCP server/client, enabled only behind a secure profile.

Do not expose an HTTP MCP endpoint without authentication.

For setup steps, see [MCP Setup](docs/MCP_SETUP.md).

## Agent-Friendly Workflow

This starter includes docs and task recipes for AI-agent development:

- [Architecture](docs/ARCHITECTURE.md)
- [Conventions](docs/CONVENTIONS.md)
- [Specifications](docs/specs/README.md)
- [Decisions](docs/DECISIONS.md)
- [API Contract](docs/API_CONTRACT.md)
- [Local Workflow](docs/LOCAL_WORKFLOW.md)
- [Project State](docs/PROJECT_STATE.md)
- `.agent/tasks/*.md`

Put written product, technical, and workflow specifications in `docs/specs/` so agents can read the intended contract before changing code.
