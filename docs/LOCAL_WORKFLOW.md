# Local Workflow

## Start Everything

```bash
./scripts/deploy-local.sh up
```

The local stack uses PostgreSQL through Docker. Copy `.env.example` to `.env` if you need to override ports or database credentials.

Open the app:

```bash
./scripts/deploy-local.sh open
```

Or start and open:

```bash
OPEN_BROWSER=true ./scripts/deploy-local.sh up
```

## Choose A Development Mode

```bash
./scripts/dev.sh
```

Asks whether to start the Docker PostgreSQL stack, H2 backend + Angular dev server, or local PostgreSQL backend + Angular dev server.

Non-interactive options:

```bash
./scripts/dev.sh docker
./scripts/dev.sh h2
./scripts/dev.sh postgres
```

When another local instance is already running, leave it alone. For a temporary
agent-owned check, run the native dev stack on alternate ports:

```bash
BACKEND_PORT=18080 FRONTEND_PORT=14200 ./scripts/dev.sh h2
```

Then point smoke checks at the same ports:

```bash
FRONTEND_URL=http://localhost:14200 BACKEND_HEALTH_URL=http://localhost:18080/actuator/health ./scripts/smoke.sh
```

`docker` mode rebuilds and recreates the backend/frontend containers on every run but leaves an already-running `postgres` container (and its `postgres-data` volume) alone — its image and config don't change between runs. The backend there is baked into an image at build time, so DevTools' auto-restart does not apply; re-run `./scripts/dev.sh docker` to pick up a code change.

`postgres` mode runs the backend natively (`mvn spring-boot:run`, DevTools auto-restarts it on rebuild) against a real PostgreSQL at `localhost:5432`, but doesn't start that database itself — it just needs *something* listening there with matching credentials, Docker or a natively installed Postgres, it makes no difference. Using this template's own compose file:

```bash
docker compose up -d postgres
./scripts/dev.sh postgres
```

This is the fastest loop that still uses real PostgreSQL instead of H2.

## Stop Everything

```bash
./scripts/deploy-local.sh down
```

## Run Checks

Install frontend dependencies once first:

```bash
cd frontend
npm install
cd ..
```

```bash
./scripts/check.sh
```

This runs backend tests, frontend formatting checks, frontend linting, and frontend unit tests.

Frontend production build:

```bash
cd frontend
npm run build
cd ..
```

Backend only:

```bash
./scripts/check-backend.sh
```

Frontend only:

```bash
./scripts/check-frontend.sh
```

Smoke check a running local stack:

```bash
./scripts/smoke.sh
```

Full environment and setup check (tooling, template placeholders, migrations, and a running stack if one is up):

```bash
./scripts/doctor.sh
./scripts/doctor.sh --full  # also compiles backend and builds frontend
```

## Apply Frontend Formatting

```bash
cd frontend
npm run format
```

## Running Each Side Separately

Use this instead of `./scripts/dev.sh` when the backend runs from an IDE run configuration.

Backend with H2:

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Frontend dev server:

```bash
cd frontend
npm install
npm start
```
