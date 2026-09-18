#!/usr/bin/env bash
set -euo pipefail

MODE="${1:-up}"
LOCAL_URL="${LOCAL_URL:-http://localhost:4200}"
OPEN_BROWSER="${OPEN_BROWSER:-false}"

usage() {
  cat <<USAGE
Usage: ./scripts/deploy-local.sh [up|down|logs|restart|open]

Commands:
  up       Build and start PostgreSQL, backend, and frontend
  down     Stop the local stack
  logs     Follow local stack logs
  restart  Rebuild and restart the local stack
  open     Open the local frontend in the default browser

Environment:
  LOCAL_URL      URL to open. Default: http://localhost:4200
  OPEN_BROWSER  Open LOCAL_URL after up/restart when true. Default: false
USAGE
}

open_local_url() {
  if command -v open >/dev/null 2>&1; then
    open "${LOCAL_URL}"
  elif command -v xdg-open >/dev/null 2>&1; then
    xdg-open "${LOCAL_URL}" >/dev/null 2>&1 &
  elif command -v cmd.exe >/dev/null 2>&1; then
    cmd.exe /c start "" "${LOCAL_URL}"
  else
    echo "Open ${LOCAL_URL}"
  fi
}

maybe_open_local_url() {
  if [[ "${OPEN_BROWSER}" == "true" ]]; then
    open_local_url
  fi
}

case "${MODE}" in
  --help|-h|help)
    usage
    ;;
  up)
    docker compose up --build -d
    docker compose ps
    maybe_open_local_url
    ;;
  down)
    docker compose down
    ;;
  logs)
    docker compose logs -f
    ;;
  restart)
    docker compose down
    docker compose up --build -d
    docker compose ps
    maybe_open_local_url
    ;;
  open)
    open_local_url
    ;;
  *)
    usage
    exit 1
    ;;
esac
