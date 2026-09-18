#!/usr/bin/env bash
set -uo pipefail

cd "$(dirname "${BASH_SOURCE[0]}")/.."

FULL="false"
if [[ "${1:-}" == "--full" ]]; then
  FULL="true"
fi

usage() {
  cat <<USAGE
Usage: ./scripts/doctor.sh [--full]

Checks that a project generated from this template (via create-project.sh)
is set up correctly: required tooling, leftover template placeholders,
frontend/backend prerequisites, and reachability of a running local stack.

Options:
  --full  Also run mvn compile and npm run build (slower).
USAGE
}

if [[ "${1:-}" == "--help" || "${1:-}" == "-h" ]]; then
  usage
  exit 0
fi

PASS=0
WARN=0
FAIL=0

pass() { echo "  OK    $1"; PASS=$((PASS + 1)); }
warn() { echo "  WARN  $1"; WARN=$((WARN + 1)); }
fail() { echo "  FAIL  $1"; FAIL=$((FAIL + 1)); }
skip() { echo "  SKIP  $1"; }

section() { echo; echo "== $1 =="; }

env_or_default() {
  local key="$1"
  local default_value="$2"
  local value=""

  if [[ -n "${!key:-}" ]]; then
    printf '%s' "${!key}"
    return
  fi

  if [[ -f .env ]]; then
    value="$(grep -E "^${key}=" .env 2>/dev/null | tail -n 1 | cut -d '=' -f 2- || true)"
  fi

  if [[ -n "${value}" ]]; then
    value="${value%\"}"
    value="${value#\"}"
    value="${value%\'}"
    value="${value#\'}"
    printf '%s' "${value}"
  else
    printf '%s' "${default_value}"
  fi
}

section "Tooling"

if command -v docker >/dev/null 2>&1; then
  if docker info >/dev/null 2>&1; then
    pass "docker is installed and the daemon is running"
  else
    warn "docker is installed but the daemon does not appear to be running"
  fi
else
  fail "docker is not installed (required for ./scripts/deploy-local.sh)"
fi

if command -v node >/dev/null 2>&1; then
  node_version="$(node -v)"
  node_major="${node_version#v}"
  node_major="${node_major%%.*}"
  if [[ "${node_major}" -ge 20 ]]; then
    pass "node ${node_version} found"
  else
    warn "node ${node_version} found, but Angular 22 expects a newer major version"
  fi
else
  fail "node is not installed"
fi

if command -v npm >/dev/null 2>&1; then
  pass "npm $(npm -v) found"
else
  fail "npm is not installed"
fi

if command -v mvn >/dev/null 2>&1; then
  pass "mvn found"
else
  fail "mvn is not installed (required for backend build/test)"
fi

if command -v java >/dev/null 2>&1; then
  java_version="$(java -version 2>&1 | head -n 1)"
  if [[ "${java_version}" == *"25"* ]]; then
    pass "java 25 found (${java_version})"
  else
    warn "java found but does not report version 25: ${java_version}"
  fi
else
  fail "java is not installed"
fi

section "Template placeholders"

if [[ -d backend/src/main/java/com/example ]]; then
  fail "backend/src/main/java/com/example still exists; create-project.sh did not rename the Java package"
else
  pass "no leftover com/example Java package directory"
fi

# The escaped dots below are load-bearing: create-project.sh blindly text-substitutes
# the literal "com.example.app" (and "example-app") across every file it copies,
# INCLUDING this script. An unescaped copy of that literal here would get rewritten
# right along with the real code, silently defeating its own check post-fork. Do not
# "simplify" this pattern, and do not repeat the literal placeholder in messages either.
placeholder_hits="$(grep -rIl "com\.example\.app\|<groupId>com\.example</groupId>" backend/src backend/pom.xml 2>/dev/null || true)"
if [[ -n "${placeholder_hits}" ]]; then
  fail "found files still referencing the template's default Java package/groupId:"
  echo "${placeholder_hits}" | sed 's/^/        /'
else
  pass "no leftover default Java package/groupId references"
fi

# Checked against the current directory name, not a hardcoded placeholder string,
# for the same reason: a hardcoded literal here would get rewritten by create-project.sh
# along with the real data, making the comparison meaningless post-fork.
current_dir_name="$(basename "$(pwd)")"
package_name="$(grep -m1 '"name"' frontend/package.json 2>/dev/null | sed -E 's/.*"name": *"([^"]*)".*/\1/')"
if [[ -n "${package_name}" && "${package_name}" != "${current_dir_name}" ]]; then
  warn "frontend/package.json name (\"${package_name}\") does not match the project directory (\"${current_dir_name}\")"
else
  pass "frontend/package.json name matches the project directory"
fi

section "Frontend"

if [[ -f frontend/package.json ]]; then
  pass "frontend/package.json present"
else
  fail "frontend/package.json is missing"
fi

if [[ -d frontend/node_modules ]]; then
  pass "frontend/node_modules present"
else
  warn "frontend dependencies are not installed; run: cd frontend && npm install"
fi

if [[ "${FULL}" == "true" ]]; then
  if [[ -d frontend/node_modules ]]; then
    if (cd frontend && npm run build >/tmp/doctor-frontend-build.log 2>&1); then
      pass "frontend production build succeeded"
    else
      fail "frontend production build failed; see /tmp/doctor-frontend-build.log"
    fi
  else
    skip "frontend build (node_modules missing)"
  fi
fi

section "Backend"

if [[ -f backend/pom.xml ]]; then
  pass "backend/pom.xml present"
else
  fail "backend/pom.xml is missing"
fi

migration_dir="backend/src/main/resources/db/migration"
if [[ -d "${migration_dir}" ]] && compgen -G "${migration_dir}/V*__*.sql" >/dev/null; then
  migration_count="$(find "${migration_dir}" -name 'V*__*.sql' | wc -l | tr -d ' ')"
  pass "${migration_count} Flyway migration(s) found under ${migration_dir}"
  bad_names="$(find "${migration_dir}" -type f -name '*.sql' ! -name 'V[0-9]*__*.sql')"
  if [[ -n "${bad_names}" ]]; then
    warn "migration files not matching V<number>__description.sql:"
    echo "${bad_names}" | sed 's/^/        /'
  fi
else
  fail "no Flyway migrations found under ${migration_dir}"
fi

if [[ "${FULL}" == "true" ]]; then
  if (cd backend && mvn -q -o compile >/tmp/doctor-backend-compile.log 2>&1); then
    pass "backend compiles offline"
  else
    fail "backend compile failed; see /tmp/doctor-backend-compile.log"
  fi
fi

section "Environment"

if [[ -f .env.example && ! -f .env ]]; then
  warn ".env is not present; copy .env.example to .env if you need non-default ports or credentials"
elif [[ -f .env ]]; then
  pass ".env present"
fi

section "Running stack"

BACKEND_HEALTH_URL="${BACKEND_HEALTH_URL:-http://localhost:8080/actuator/health}"
FRONTEND_URL="${FRONTEND_URL:-http://localhost:4200}"
POSTGRES_DB_VALUE="$(env_or_default POSTGRES_DB app)"
POSTGRES_USER_VALUE="$(env_or_default POSTGRES_USER app)"

if curl -sS -o /dev/null --max-time 2 "${BACKEND_HEALTH_URL}"; then
  status_code="$(curl -sS -o /dev/null --max-time 2 -w '%{http_code}' "${BACKEND_HEALTH_URL}")"
  if [[ "${status_code}" == "200" ]]; then
    pass "backend health check reachable at ${BACKEND_HEALTH_URL}"
  else
    fail "backend health check at ${BACKEND_HEALTH_URL} returned ${status_code}"
  fi
else
  skip "backend not reachable at ${BACKEND_HEALTH_URL} (start it with ./scripts/deploy-local.sh up)"
fi

if curl -sS -o /dev/null --max-time 2 "${FRONTEND_URL}"; then
  status_code="$(curl -sS -o /dev/null --max-time 2 -w '%{http_code}' "${FRONTEND_URL}")"
  if [[ "${status_code}" == "200" ]]; then
    pass "frontend reachable at ${FRONTEND_URL}"
  else
    fail "frontend at ${FRONTEND_URL} returned ${status_code}"
  fi
else
  skip "frontend not reachable at ${FRONTEND_URL} (start it with ./scripts/deploy-local.sh up)"
fi

if command -v docker >/dev/null 2>&1 && docker info >/dev/null 2>&1; then
  postgres_container="$(docker compose ps -q postgres 2>/dev/null || true)"
  if [[ -z "${postgres_container}" ]]; then
    skip "postgres container is not running"
  else
    postgres_health="$(docker inspect -f '{{if .State.Health}}{{.State.Health.Status}}{{else}}none{{end}}' "${postgres_container}" 2>/dev/null || true)"
    if docker compose exec -T postgres psql -U "${POSTGRES_USER_VALUE}" -d "${POSTGRES_DB_VALUE}" -c '\du' >/dev/null 2>&1; then
      pass "postgres accepts configured database credentials"
    elif [[ "${postgres_health}" == "healthy" ]]; then
      warn "postgres is healthy but rejects POSTGRES_USER=${POSTGRES_USER_VALUE} / POSTGRES_DB=${POSTGRES_DB_VALUE}; possible stale Docker volume. Reset with: docker compose down -v && docker compose up --build"
    else
      skip "postgres container is present but not healthy yet"
    fi
  fi
else
  skip "postgres credential check (docker daemon unavailable)"
fi

section "Summary"
echo "  ${PASS} passed, ${WARN} warnings, ${FAIL} failed"

if [[ "${FAIL}" -gt 0 ]]; then
  exit 1
fi

exit 0
