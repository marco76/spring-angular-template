#!/usr/bin/env bash

cd "$(dirname "${BASH_SOURCE[0]}")/.."

./scripts/check-backend.sh
backend_status=$?
if [[ "${backend_status}" -ne 0 ]]; then
  exit "${backend_status}"
fi

./scripts/check-frontend.sh
frontend_status=$?
if [[ "${frontend_status}" -ne 0 ]]; then
  exit "${frontend_status}"
fi
