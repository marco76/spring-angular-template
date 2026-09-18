# Getting Started

This guide turns the template into a fresh Angular + Spring Boot project and gets it running locally.

## 1. Create A Project

From the template directory:

```bash
./create-project.sh invoice-hub com.acme.invoice ../invoice-hub
```

This also runs `git init` with an initial commit and `npm install` in the generated frontend, and, if you don't pass `--ui` explicitly, asks interactively whether to create a basic Angular Material UI. Choose `yes` for a classic app shell with a header, left menu, and dashboard route. Choose `no` for a minimal routed Angular app.

For non-interactive generation, or to change the defaults, pass options explicitly:

```bash
./create-project.sh invoice-hub com.acme.invoice ../invoice-hub --ui=basic
./create-project.sh invoice-hub com.acme.invoice ../invoice-hub --ui=minimal
./create-project.sh invoice-hub com.acme.invoice ../invoice-hub --theme=ft
./create-project.sh invoice-hub com.acme.invoice ../invoice-hub --theme=gl
./create-project.sh invoice-hub com.acme.invoice ../invoice-hub --mcp
./create-project.sh invoice-hub com.acme.invoice ../invoice-hub --skip-git --skip-install
```

Arguments:

- `invoice-hub`: application name used in Docker images, Angular config, and Spring application name
- `com.acme.invoice`: Java package name
- `../invoice-hub`: target directory for the generated project

Options:

- `--ui=basic|minimal`: UI starter choice; prompted interactively if omitted
- `--theme=default|ft|gl`: initial visual theme choice; generated apps keep all themes, can switch later from the Admin page, and save the app-wide choice in the database
- `--mcp`: enable the optional Spring AI MCP server dependency (disabled by default — read `docs/MCP_SETUP.md` before exposing it, it must sit behind authentication)
- `--skip-git`: don't run `git init` / the initial commit
- `--skip-install`: don't run `npm install` in the generated frontend

Then enter the project:

```bash
cd ../invoice-hub
```

Verify the rename went cleanly before doing anything else:

```bash
./scripts/doctor.sh
```

It checks for required tooling, leftover `com.example`/`example-app` placeholders, and Flyway migration naming. Warnings about `node_modules`, `.env`, or the stack not running yet are expected at this point.

## 2. Start The Docker Stack

This starts PostgreSQL, the Spring Boot backend, and the Angular frontend served through Nginx:

```bash
./scripts/deploy-local.sh up
```

Optional local overrides are documented in `.env.example`. Copy it to `.env` before starting the stack if you need different ports or database credentials:

```bash
cp .env.example .env
```

Open:

- Frontend: `http://localhost:4200`
- Admin page: `http://localhost:4200/admin`
- Backend health: `http://localhost:8080/api/health`
- Actuator health: `http://localhost:8080/actuator/health`

Starter HTTP Basic users:

```text
admin / admin
user / user
```

The default admin page and `GET /api/admin` require the `admin` account. Replace these demo users before using the template for anything beyond local development.

You can also open the frontend from the script:

```bash
./scripts/deploy-local.sh open
```

Or open it automatically after startup:

```bash
OPEN_BROWSER=true ./scripts/deploy-local.sh up
```

## 3. Choose A Local Development Mode

Install frontend dependencies once first:

```bash
cd frontend && npm install && cd ..
```

Start the interactive launcher:

```bash
./scripts/dev.sh
```

It asks whether to start:

- Docker PostgreSQL stack
- H2 backend + Angular dev server
- Local PostgreSQL backend + Angular dev server

For non-interactive use:

```bash
./scripts/dev.sh docker
./scripts/dev.sh h2
./scripts/dev.sh postgres
```

If the default ports are already occupied by a manually started instance, leave
that instance running and use alternate ports for temporary testing:

```bash
BACKEND_PORT=18080 FRONTEND_PORT=14200 ./scripts/dev.sh h2
```

The H2 mode runs the backend on the `local` profile, waits for it to become healthy, then starts the Angular dev server. Ctrl+C stops both. Backend: `http://localhost:8080` (health at `/api/health`, H2 console at `/h2-console` — JDBC URL `jdbc:h2:mem:example-app`, user `sa`, empty password). Frontend: `http://localhost:4200`.

The local PostgreSQL mode uses these defaults unless you override them in the environment:

```text
POSTGRES_DB=app
POSTGRES_USER=app
POSTGRES_PASSWORD=app
POSTGRES_PORT=5432
```

These match `docker-compose.yml`'s defaults on purpose: `./scripts/dev.sh postgres` runs the backend natively (`mvn spring-boot:run`, so Spring Boot DevTools auto-restarts it on every rebuild) but does not start a database itself — it just opens a JDBC connection to `localhost:5432` and expects something already listening there with a matching database/user/password. That something can be *either* Docker or a natively installed PostgreSQL — the backend can't tell the difference and doesn't care. The simplest option, using this template's own compose file:

```bash
docker compose up -d postgres   # database only, no backend/frontend build
./scripts/dev.sh postgres       # native backend with devtools + ng serve
```

but a Postgres already running on your machine some other way (Homebrew/apt install, another project's Docker container already bound to `5432`, …) works exactly as well, as long as it accepts the `POSTGRES_DB`/`POSTGRES_USER`/`POSTGRES_PASSWORD` credentials above (override the env vars, or the database's own credentials, so they match).

This is the fastest inner loop that still runs against real PostgreSQL instead of H2: edit backend code, DevTools restarts the JVM in place, and the database and its data are untouched by the restart — they're a separate process either way. It's a different code path from `./scripts/dev.sh docker` / `./scripts/deploy-local.sh up`, where the backend is baked into an image at `docker build` time and does not auto-restart on code changes at all — there, re-running `up --build` is what picks up a change, and it likewise leaves an already-running `postgres` container and its data alone since that service's image and config never change between runs.

H2 is configured in PostgreSQL compatibility mode. Still, PostgreSQL remains the source of truth for production behavior.

### Running Each Side Separately

Useful when the backend runs from an IDE run configuration instead of the CLI (in IntelliJ, the Angular dev-server proxy is declared in `angular.json`, not only as an `npm start` flag, so it applies no matter how `ng serve` is launched):

IntelliJ users can also use the committed `Application (local)` run configuration, which starts Spring Boot with the `local` profile. A committed template (`.run/Spring Boot.run.xml`) also defaults every *new* Spring Boot run configuration in this project — including the one IntelliJ auto-generates if you click the gutter ▶ run icon on `Application.main()` directly — to the `local` profile, since `application.yml` declares no datasource outside the `local`/`prod` profiles and an unprofiled run fails on startup.

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

```bash
cd frontend
npm start
```

## 4. Add Database Changes

Flyway owns the schema. Add new SQL migrations under:

```text
backend/src/main/resources/db/migration
```

Use names like:

```text
V2__create_customer_table.sql
V3__add_customer_email_index.sql
```

Do not edit migrations after they have been shared or applied outside your machine.

Use these folders for non-schema data:

```text
backend/src/main/resources/db/dev-data
backend/src/test/resources/db/test-data
```

Use `dev-data` for local Docker/PostgreSQL sample data and `test-data` for automated test fixtures.

## 5. Build And Test

Install frontend dependencies once before running checks:

```bash
cd frontend
npm install
cd ..
```

```bash
./scripts/check.sh
./scripts/build-deploy.sh test
./scripts/build-deploy.sh build
./scripts/build-deploy.sh docker
```

If the local Docker stack is running, run:

```bash
./scripts/smoke.sh
./scripts/doctor.sh
```

`./scripts/check.sh` runs backend tests, frontend formatting checks, frontend linting, and frontend unit tests. `./scripts/doctor.sh` also checks reachability of the running stack; pass `--full` to additionally compile the backend and build the frontend.

If a sandboxed environment can run Maven but cannot expose Docker to Testcontainers, tag Testcontainers-backed tests with `@Tag("docker")` and run:

```bash
SKIP_DOCKER_TESTS=true ./scripts/check-backend.sh
```

To verify the production frontend build:

```bash
cd frontend
npm run build
cd ..
```

To apply frontend formatting before committing:

```bash
cd frontend && npm run format
```

## 6. Deploy Images

Set a registry and push both images:

```bash
REGISTRY=ghcr.io/acme APP_NAME=invoice-hub TAG=0.1.0 ./scripts/deploy-prod.sh
```

The script builds and pushes:

```text
ghcr.io/acme/invoice-hub-backend:0.1.0
ghcr.io/acme/invoice-hub-frontend:0.1.0
```

## 7. Configure MCP Developer Tools

After the project runs locally, add the recommended MCP servers for Spring, MDN, and Angular:

```text
MCP_SETUP.md
```

## 8. Agent Workflow

Before asking an AI agent to make larger changes, point it at:

```text
AGENTS.md
SKILLS.md
docs/ARCHITECTURE.md
docs/CONVENTIONS.md
docs/specs/
.agent/tasks/
```

Future agents should update:

```text
docs/DECISIONS.md
docs/PROJECT_STATE.md
docs/NEXT_STEPS.md
docs/KNOWN_LIMITATIONS.md
```

only when there is durable context worth carrying forward.
