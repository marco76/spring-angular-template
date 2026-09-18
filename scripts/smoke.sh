#!/usr/bin/env bash
set -euo pipefail

FRONTEND_URL="${FRONTEND_URL:-http://localhost:4200}"
BACKEND_HEALTH_URL="${BACKEND_HEALTH_URL:-http://localhost:8080/actuator/health}"

require_status() {
  local url="$1"
  local expected_status="$2"
  local actual_status

  actual_status="$(curl -sS -o /dev/null -w "%{http_code}" "${url}")"

  if [[ "${actual_status}" != "${expected_status}" ]]; then
    echo "Expected ${url} to return ${expected_status}, got ${actual_status}"
    exit 1
  fi
}

require_status "${FRONTEND_URL}" "200"
require_status "${BACKEND_HEALTH_URL}" "200"

echo "Smoke checks passed:"
echo "  ${FRONTEND_URL}"
echo "  ${BACKEND_HEALTH_URL}"
