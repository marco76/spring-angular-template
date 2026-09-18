#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "${BASH_SOURCE[0]}")/.."

BACKEND_URL="${BACKEND_URL:-http://localhost:8080}"
SPEC_FILE="frontend/openapi.json"

echo "Fetching OpenAPI spec from ${BACKEND_URL}/v3/api-docs ..."
if ! curl -sS -f "${BACKEND_URL}/v3/api-docs" -o "${SPEC_FILE}"; then
  echo "Could not reach ${BACKEND_URL}/v3/api-docs."
  echo "Start the backend first, for example:"
  echo "  cd backend && mvn spring-boot:run -Dspring-boot.run.profiles=local"
  exit 1
fi

cd frontend
npm run generate:api

echo
echo "Generated API client in frontend/src/app/core/api"
echo "Review the diff, then commit the generated client alongside your backend change."
