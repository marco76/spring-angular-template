#!/usr/bin/env bash
set -uo pipefail

cd "$(dirname "${BASH_SOURCE[0]}")/.."

usage() {
  cat <<USAGE
Usage: ./scripts/dev.sh [docker|h2|postgres]

Without an argument, asks which local development mode to start.

Modes:
  docker    Start the full Docker stack: PostgreSQL, backend, frontend
  h2        Start backend with H2 and Angular dev server
  postgres  Start backend against a local PostgreSQL and Angular dev server

Local PostgreSQL environment defaults:
  POSTGRES_DB        app
  POSTGRES_USER      app
  POSTGRES_PASSWORD  app
  POSTGRES_PORT      5432

Port overrides:
  BACKEND_PORT       Spring Boot port. Default: 8080
  FRONTEND_PORT      Angular dev server port. Default: 4200

Backend:  http://localhost:${BACKEND_PORT:-8080}  (health: /api/health)
Frontend: http://localhost:${FRONTEND_PORT:-4200}
USAGE
}

if [[ "${1:-}" == "--help" || "${1:-}" == "-h" ]]; then
  usage
  exit 0
fi

choose_mode() {
  cat <<MENU
Choose a development mode:

  1) Docker PostgreSQL stack (recommended)
     -> PostgreSQL in a Docker container (docker-compose.yml)
  2) H2 backend + Angular dev server (fastest)
     -> In-memory H2, reset on every restart, no setup required
  3) Local PostgreSQL backend + Angular dev server
     -> PostgreSQL on the host at ${POSTGRES_DB:-app}@localhost:${POSTGRES_PORT:-5432} (override via POSTGRES_DB/POSTGRES_USER/POSTGRES_PASSWORD/POSTGRES_PORT)

MENU

  read -r -p "Mode [1]: " choice
  case "${choice:-1}" in
    1|docker) echo "docker" ;;
    2|h2) echo "h2" ;;
    3|postgres|local-postgres) echo "postgres" ;;
    *)
      echo "Unknown choice: ${choice}" >&2
      exit 1
      ;;
  esac
}

MODE="${1:-}"
if [[ -z "${MODE}" ]]; then
  MODE="$(choose_mode)"
fi

BACKEND_PORT="${BACKEND_PORT:-8080}"
FRONTEND_PORT="${FRONTEND_PORT:-4200}"

require_frontend_dependencies() {
  if [[ ! -d frontend/node_modules ]]; then
    echo "Frontend dependencies are missing. Run: cd frontend && npm install"
    exit 1
  fi
}

create_proxy_config() {
  local proxy_config

  proxy_config="$(mktemp "${TMPDIR:-/tmp}/example-app-proxy.XXXXXX.json")"
  cat >"${proxy_config}" <<PROXY
{
  "/api": {
    "target": "http://localhost:${BACKEND_PORT}",
    "secure": false,
    "changeOrigin": true
  },
  "/actuator": {
    "target": "http://localhost:${BACKEND_PORT}",
    "secure": false,
    "changeOrigin": true
  },
  "/swagger-ui": {
    "target": "http://localhost:${BACKEND_PORT}",
    "secure": false,
    "changeOrigin": true
  },
  "/v3/api-docs": {
    "target": "http://localhost:${BACKEND_PORT}",
    "secure": false,
    "changeOrigin": true
  }
}
PROXY
  echo "${proxy_config}"
}

start_frontend() {
  local proxy_config="$1"

  echo "Starting frontend..."
  (cd frontend && npm start -- --port "${FRONTEND_PORT}" --proxy-config "${proxy_config}") &
}

wait_for_backend() {
  local backend_pid="$1"

  echo "Waiting for backend health..."
  backend_ready="false"
  for _ in $(seq 1 60); do
    if [[ "$(curl -sS -o /dev/null -w '%{http_code}' "http://localhost:${BACKEND_PORT}/actuator/health" 2>/dev/null)" == "200" ]]; then
      backend_ready="true"
      break
    fi
    if ! kill -0 "${backend_pid}" 2>/dev/null; then
      echo "Backend exited before becoming healthy."
      exit 1
    fi
    sleep 1
  done

  if [[ "${backend_ready}" != "true" ]]; then
    echo "Backend did not become healthy within 60s."
    exit 1
  fi
  echo "Backend is up."
}

run_process_pair() {
  require_frontend_dependencies
  proxy_config="$(create_proxy_config)"

  # Kills everything this script started (backend, frontend, and their child
  # processes) on Ctrl+C or exit, since they share this script's process group.
  trap 'rm -f "${proxy_config}"; kill 0' EXIT INT TERM

  "$@" &
  backend_pid=$!

  wait_for_backend "${backend_pid}"
  start_frontend "${proxy_config}"

  echo
  echo "Backend:  http://localhost:${BACKEND_PORT}  (health: /api/health)"
  echo "Frontend: http://localhost:${FRONTEND_PORT}"
  echo "Press Ctrl+C to stop both."

  wait
}

case "${MODE}" in
  docker)
    echo "Starting Docker stack with PostgreSQL (container)..."
    ./scripts/deploy-local.sh up
    ;;
  h2)
    echo "Starting backend with H2 (local profile)..."
    run_process_pair mvn -q -f backend/pom.xml spring-boot:run -Dspring-boot.run.profiles=local "-Dspring-boot.run.arguments=--server.port=${BACKEND_PORT}"
    ;;
  postgres|local-postgres)
    POSTGRES_DB="${POSTGRES_DB:-app}"
    POSTGRES_USER="${POSTGRES_USER:-app}"
    POSTGRES_PASSWORD="${POSTGRES_PASSWORD:-app}"
    POSTGRES_PORT="${POSTGRES_PORT:-5432}"
    SPRING_DATASOURCE_URL="${SPRING_DATASOURCE_URL:-jdbc:postgresql://localhost:${POSTGRES_PORT}/${POSTGRES_DB}}"
    SPRING_DATASOURCE_USERNAME="${SPRING_DATASOURCE_USERNAME:-${POSTGRES_USER}}"
    SPRING_DATASOURCE_PASSWORD="${SPRING_DATASOURCE_PASSWORD:-${POSTGRES_PASSWORD}}"

    export SPRING_DATASOURCE_URL
    export SPRING_DATASOURCE_USERNAME
    export SPRING_DATASOURCE_PASSWORD

    echo "Starting backend with local PostgreSQL (${SPRING_DATASOURCE_URL})..."
    run_process_pair mvn -q -f backend/pom.xml spring-boot:run -Dspring-boot.run.profiles=prod "-Dspring-boot.run.arguments=--server.port=${BACKEND_PORT}"
    ;;
  *)
    echo "Unknown mode: ${MODE}"
    usage
    exit 1
    ;;
esac
