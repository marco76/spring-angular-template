#!/usr/bin/env bash
set -euo pipefail

usage() {
  cat <<USAGE
Usage: ./create-project.sh <app-name> <java-package> <target-directory> [options]

Example:
  ./create-project.sh invoice-hub com.acme.invoice ../invoice-hub
  ./create-project.sh invoice-hub com.acme.invoice ../invoice-hub --ui=basic --theme=gl --mcp

Options:
  --ui=basic|minimal  basic: Material shell with header and left menu.
                      minimal: plain routed app without the shell.
                      Prompted interactively if omitted and a terminal is attached.
  --theme=default|ft|gl
                      default: neutral navy/amber starter palette.
                      ft: FT-inspired editorial palette with warm paper,
                      claret accents, teal links, and sharper surfaces.
                      gl: corporate white theme with serif headings,
                      teal icons, and warm taupe actions.
  --mcp               Enable the optional Spring AI MCP server dependency
                      (disabled by default; see docs/MCP_SETUP.md before
                      exposing it — it must sit behind authentication).
  --skip-git          Do not run 'git init' and create the initial commit.
  --skip-install      Do not run 'npm install' in the generated frontend.
  -h, --help          Show this help.
USAGE
}

UI_MODE=""
THEME_MODE="default"
ENABLE_MCP="false"
SKIP_GIT="false"
SKIP_INSTALL="false"
POSITIONAL=()

while [[ $# -gt 0 ]]; do
  case "$1" in
    --ui=*)
      UI_MODE="${1#--ui=}"
      shift
      ;;
    --theme=*)
      THEME_MODE="${1#--theme=}"
      shift
      ;;
    --mcp)
      ENABLE_MCP="true"
      shift
      ;;
    --skip-git)
      SKIP_GIT="true"
      shift
      ;;
    --skip-install)
      SKIP_INSTALL="true"
      shift
      ;;
    -h | --help)
      usage
      exit 0
      ;;
    --*)
      echo "Unknown option: $1"
      usage
      exit 1
      ;;
    *)
      POSITIONAL+=("$1")
      shift
      ;;
  esac
done

if [[ "${#POSITIONAL[@]}" -ne 3 ]]; then
  usage
  exit 1
fi

APP_NAME="${POSITIONAL[0]}"
JAVA_PACKAGE="${POSITIONAL[1]}"
TARGET_DIR="${POSITIONAL[2]}"
TEMPLATE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JAVA_PACKAGE_PATH="$(printf '%s' "${JAVA_PACKAGE}" | tr '.' '/')"
JAVA_GROUP_ID="${JAVA_PACKAGE%.*}"

if [[ ! "${APP_NAME}" =~ ^[a-z][a-z0-9-]*$ ]]; then
  echo "App name must be kebab-case, for example: invoice-hub"
  exit 1
fi

if [[ ! "${JAVA_PACKAGE}" =~ ^[a-z_][a-z0-9_]*(\.[a-z_][a-z0-9_]*)+$ ]]; then
  echo "Java package must be a dotted package name, for example: com.acme.invoice"
  exit 1
fi

if [[ -e "${TARGET_DIR}" ]]; then
  echo "Target directory already exists: ${TARGET_DIR}"
  exit 1
fi

if [[ -z "${UI_MODE}" ]]; then
  if [[ -t 0 ]]; then
    read -r -p "Create a basic UI with header and left menu? [Y/n] " UI_ANSWER
    case "${UI_ANSWER}" in
      '' | [Yy] | [Yy][Ee][Ss])
        UI_MODE="basic"
        ;;
      [Nn] | [Nn][Oo])
        UI_MODE="minimal"
        ;;
      *)
        echo "Please answer yes or no."
        exit 1
        ;;
    esac
  else
    UI_MODE="basic"
  fi
fi

if [[ "${UI_MODE}" != "basic" && "${UI_MODE}" != "minimal" ]]; then
  echo "UI option must be either 'basic' or 'minimal'."
  exit 1
fi

if [[ "${THEME_MODE}" != "default" && "${THEME_MODE}" != "ft" && "${THEME_MODE}" != "gl" ]]; then
  echo "Theme option must be either 'default', 'ft', or 'gl'."
  exit 1
fi

mkdir -p "${TARGET_DIR}"
tar \
  --exclude="./create-project.sh" \
  --exclude="./.git" \
  --exclude="./backend/target" \
  --exclude="./frontend/.angular" \
  --exclude="./frontend/dist" \
  --exclude="./frontend/node_modules" \
  --exclude="./frontend/openapi.json" \
  --exclude="./.idea" \
  --exclude="./.vscode" \
  --exclude="./.DS_Store" \
  --exclude="./**/.DS_Store" \
  -C "${TEMPLATE_DIR}" \
  -cf - . | tar -C "${TARGET_DIR}" -xf -

find "${TARGET_DIR}" -type f \
  ! -path "*/node_modules/*" \
  ! -path "*/target/*" \
  ! -path "*/dist/*" \
  ! -path "*/.angular/*" \
  -print0 | while IFS= read -r -d '' file; do
    APP_NAME="${APP_NAME}" \
    JAVA_GROUP_ID="${JAVA_GROUP_ID}" \
    JAVA_PACKAGE="${JAVA_PACKAGE}" \
    JAVA_PACKAGE_PATH="${JAVA_PACKAGE_PATH}" \
    perl -0pi \
      -e 's/example-app/$ENV{APP_NAME}/g;' \
      -e 's/Example App/$ENV{APP_NAME}/g;' \
      -e 's{<groupId>com\.example</groupId>}{<groupId>$ENV{JAVA_GROUP_ID}</groupId>}g;' \
      -e 's/com\.example\.app/$ENV{JAVA_PACKAGE}/g;' \
      -e 's{com/example/app}{$ENV{JAVA_PACKAGE_PATH}}g;' \
      "${file}"
  done

OLD_JAVA_DIR="${TARGET_DIR}/backend/src/main/java/com/example/app"
NEW_JAVA_DIR="${TARGET_DIR}/backend/src/main/java/${JAVA_PACKAGE_PATH}"
OLD_TEST_JAVA_DIR="${TARGET_DIR}/backend/src/test/java/com/example/app"
NEW_TEST_JAVA_DIR="${TARGET_DIR}/backend/src/test/java/${JAVA_PACKAGE_PATH}"

mkdir -p "$(dirname "${NEW_JAVA_DIR}")"
mv "${OLD_JAVA_DIR}" "${NEW_JAVA_DIR}"

if [[ -d "${OLD_TEST_JAVA_DIR}" ]]; then
  mkdir -p "$(dirname "${NEW_TEST_JAVA_DIR}")"
  mv "${OLD_TEST_JAVA_DIR}" "${NEW_TEST_JAVA_DIR}"
fi

find "${TARGET_DIR}/backend/src/main/java/com" -type d -empty -delete 2>/dev/null || true
find "${TARGET_DIR}/backend/src/test/java/com" -type d -empty -delete 2>/dev/null || true

if [[ "${UI_MODE}" == "minimal" ]]; then
  rm -rf "${TARGET_DIR}/frontend/src/app/layout"
  mv "${TARGET_DIR}/frontend/src/main.minimal.ts" "${TARGET_DIR}/frontend/src/main.ts"
else
  rm -f "${TARGET_DIR}/frontend/src/main.minimal.ts"
fi

if [[ "${THEME_MODE}" != "default" ]]; then
  perl -0pi -e "s/const INITIAL_THEME: AppTheme = 'default';/const INITIAL_THEME: AppTheme = '${THEME_MODE}';/" \
    "${TARGET_DIR}/frontend/src/app/core/theme-preference.service.ts"
fi

chmod +x "${TARGET_DIR}/scripts/"*.sh

if [[ "${ENABLE_MCP}" == "true" ]]; then
  # Note: XML comments forbid a literal "--" anywhere in their body, so the
  # replacement text below must not spell out the --mcp flag with its dashes.
  perl -0pi -e 's{<!-- Optional runtime MCP support\. Keep disabled unless the app needs it\. -->\n\s*<!--\n\s*<dependency>\n\s*<groupId>org\.springframework\.ai</groupId>\n\s*<artifactId>spring-ai-starter-mcp-server-webmvc</artifactId>\n\s*</dependency>\n\s*-->}{<!-- Spring AI MCP server, enabled at generation time. Secure this behind\n         authentication before exposing it; see docs/MCP_SETUP.md. -->\n    <dependency>\n      <groupId>org.springframework.ai</groupId>\n      <artifactId>spring-ai-starter-mcp-server-webmvc</artifactId>\n    </dependency>}' \
    "${TARGET_DIR}/backend/pom.xml"
fi

if [[ "${SKIP_GIT}" == "true" ]]; then
  GIT_STATUS="skipped (--skip-git)"
elif ! command -v git >/dev/null 2>&1; then
  GIT_STATUS="git not found on PATH; skipped"
elif (cd "${TARGET_DIR}" && git init -q && git add -A && git commit -q -m "chore: initial commit from spring-angular-template") >/dev/null 2>&1; then
  GIT_STATUS="initialized, initial commit created"
else
  GIT_STATUS="git init ran but the initial commit failed (check 'git config user.name'/'user.email'); finish it manually in ${TARGET_DIR}"
fi

if [[ "${SKIP_INSTALL}" == "true" ]]; then
  INSTALL_STATUS="skipped (--skip-install)"
elif ! command -v npm >/dev/null 2>&1; then
  INSTALL_STATUS="npm not found on PATH; skipped"
elif (cd "${TARGET_DIR}/frontend" && npm install) >/dev/null 2>&1; then
  INSTALL_STATUS="frontend dependencies installed"
else
  INSTALL_STATUS="npm install failed; run it manually in ${TARGET_DIR}/frontend"
fi

echo "Created ${APP_NAME} at ${TARGET_DIR}"
echo "UI: ${UI_MODE}"
echo "Theme: ${THEME_MODE}"
echo "MCP: $([[ "${ENABLE_MCP}" == "true" ]] && echo "enabled (see docs/MCP_SETUP.md before exposing it)" || echo "disabled")"
echo "Git: ${GIT_STATUS}"
echo "Frontend dependencies: ${INSTALL_STATUS}"
echo "Next: cd ${TARGET_DIR} && ./scripts/deploy-local.sh up"
