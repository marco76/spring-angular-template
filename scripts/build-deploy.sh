#!/usr/bin/env bash
set -euo pipefail

APP_NAME="${APP_NAME:-example-app}"
REGISTRY="${REGISTRY:-}"
TAG="${TAG:-$(git rev-parse --short HEAD 2>/dev/null || date +%Y%m%d%H%M%S)}"

usage() {
  cat <<USAGE
Usage: ./scripts/build-deploy.sh <command>

Commands:
  local    Build and start the local Docker stack
  test     Run backend and frontend tests
  build    Build backend jar and frontend assets
  docker   Build Docker images
  deploy   Build and push Docker images. Requires REGISTRY.

Environment:
  APP_NAME   Image name prefix. Default: example-app
  REGISTRY   Registry host/namespace, e.g. ghcr.io/acme
  TAG        Image tag. Default: current git short SHA
USAGE
}

run_test() {
  (cd backend && mvn test)
  (cd frontend && npm run format:check && npm run lint && npm test -- --watch=false --browsers=ChromeHeadless)
}

run_build() {
  (cd backend && mvn -DskipTests package)
  (cd frontend && npm ci && npm run build)
}

run_docker() {
  docker build -f Dockerfile.backend -t "${APP_NAME}-backend:${TAG}" .
  docker build -f Dockerfile.frontend -t "${APP_NAME}-frontend:${TAG}" .
}

run_deploy() {
  if [[ -z "${REGISTRY}" ]]; then
    echo "REGISTRY is required for deploy, e.g. REGISTRY=ghcr.io/acme ./scripts/build-deploy.sh deploy"
    exit 1
  fi

  run_docker
  docker tag "${APP_NAME}-backend:${TAG}" "${REGISTRY}/${APP_NAME}-backend:${TAG}"
  docker tag "${APP_NAME}-frontend:${TAG}" "${REGISTRY}/${APP_NAME}-frontend:${TAG}"
  docker push "${REGISTRY}/${APP_NAME}-backend:${TAG}"
  docker push "${REGISTRY}/${APP_NAME}-frontend:${TAG}"
}

case "${1:-}" in
  local)
    ./scripts/deploy-local.sh up
    ;;
  test)
    run_test
    ;;
  build)
    run_build
    ;;
  docker)
    run_docker
    ;;
  deploy)
    ./scripts/deploy-prod.sh
    ;;
  *)
    usage
    exit 1
    ;;
esac
