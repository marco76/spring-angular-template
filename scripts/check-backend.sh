#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "${BASH_SOURCE[0]}")/.."

MAVEN_ARGS=(test)

if [[ "${SKIP_DOCKER_TESTS:-false}" == "true" ]]; then
  MAVEN_ARGS+=("-DexcludedGroups=docker")
fi

mvn -f backend/pom.xml "${MAVEN_ARGS[@]}"
