#!/usr/bin/env bash
set -euo pipefail

APP_NAME="${APP_NAME:-example-app}"
REGISTRY="${REGISTRY:-}"
TAG="${TAG:-$(git rev-parse --short HEAD 2>/dev/null || date +%Y%m%d%H%M%S)}"
PUSH_LATEST="${PUSH_LATEST:-false}"

usage() {
  cat <<USAGE
Usage: REGISTRY=<registry/namespace> ./scripts/deploy-prod.sh

Builds and pushes production Docker images:
  <REGISTRY>/<APP_NAME>-backend:<TAG>
  <REGISTRY>/<APP_NAME>-frontend:<TAG>

Environment:
  APP_NAME     Image name prefix. Default: example-app
  REGISTRY     Required registry host/namespace, e.g. ghcr.io/acme
  TAG          Image tag. Default: current git short SHA
  PUSH_LATEST  Also push :latest when true. Default: false
USAGE
}

if [[ "${1:-}" == "--help" || "${1:-}" == "-h" ]]; then
  usage
  exit 0
fi

if [[ -z "${REGISTRY}" ]]; then
  echo "REGISTRY is required, e.g. REGISTRY=ghcr.io/acme APP_NAME=${APP_NAME} ./scripts/deploy-prod.sh"
  exit 1
fi

BACKEND_LOCAL="${APP_NAME}-backend:${TAG}"
FRONTEND_LOCAL="${APP_NAME}-frontend:${TAG}"
BACKEND_REMOTE="${REGISTRY}/${APP_NAME}-backend:${TAG}"
FRONTEND_REMOTE="${REGISTRY}/${APP_NAME}-frontend:${TAG}"

docker build -f Dockerfile.backend -t "${BACKEND_LOCAL}" .
docker build -f Dockerfile.frontend -t "${FRONTEND_LOCAL}" .

docker tag "${BACKEND_LOCAL}" "${BACKEND_REMOTE}"
docker tag "${FRONTEND_LOCAL}" "${FRONTEND_REMOTE}"

docker push "${BACKEND_REMOTE}"
docker push "${FRONTEND_REMOTE}"

if [[ "${PUSH_LATEST}" == "true" ]]; then
  docker tag "${BACKEND_LOCAL}" "${REGISTRY}/${APP_NAME}-backend:latest"
  docker tag "${FRONTEND_LOCAL}" "${REGISTRY}/${APP_NAME}-frontend:latest"
  docker push "${REGISTRY}/${APP_NAME}-backend:latest"
  docker push "${REGISTRY}/${APP_NAME}-frontend:latest"
fi

echo "Published:"
echo "  ${BACKEND_REMOTE}"
echo "  ${FRONTEND_REMOTE}"

