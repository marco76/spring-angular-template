#!/usr/bin/env bash

cd "$(dirname "${BASH_SOURCE[0]}")/../frontend"

if [[ ! -d node_modules ]]; then
  echo "Frontend dependencies are missing. Run: cd frontend && npm install"
  exit 1
fi

npm run format:check
format_status=$?
if [[ "${format_status}" -ne 0 ]]; then
  exit "${format_status}"
fi

npm run lint
lint_status=$?
if [[ "${lint_status}" -ne 0 ]]; then
  exit "${lint_status}"
fi

npm test -- --no-watch --browsers=ChromeHeadless
test_status=$?
if [[ "${test_status}" -ne 0 ]]; then
  exit "${test_status}"
fi
