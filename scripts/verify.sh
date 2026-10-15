#!/usr/bin/env bash
set -euo pipefail

scope="${1:-full}"
if [[ "$scope" != "full" && "$scope" != "backend" ]]; then
  echo "Usage: $0 [full|backend]" >&2
  exit 2
fi

root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$root"

fail() {
  echo "FAIL: $*" >&2
  exit 1
}

[[ -f backend/pom.xml ]] || fail "backend/pom.xml is missing; Maven verification cannot run."
command -v mvn >/dev/null 2>&1 || fail "Maven is required."
echo "+ mvn -B -f backend/pom.xml verify"
mvn -B -f backend/pom.xml verify || fail "Maven verification failed."

[[ -f contracts/openapi.yaml && -f redocly.yaml ]] || fail "OpenAPI source or Redocly configuration is missing."
command -v npx >/dev/null 2>&1 || fail "npx is required for pinned OpenAPI lint."
echo "+ npx --yes @redocly/cli@2.63.0 lint --config=redocly.yaml contracts/openapi.yaml"
npx --yes @redocly/cli@2.63.0 lint --config=redocly.yaml contracts/openapi.yaml

command -v docker >/dev/null 2>&1 || fail "Docker CLI is required for Compose config validation."
echo "+ docker compose config --quiet (ephemeral validation values; no containers started)"
POSTGRES_PASSWORD=validation-only FRESHLINE_DEMO_ACCESS_CODE=validation-only docker compose config --quiet \
  || fail "Compose configuration is invalid."
command -v node >/dev/null 2>&1 || fail "Node.js is required to verify Compose database exposure."
echo "+ assert PostgreSQL has no published host ports"
compose_config="$(POSTGRES_PASSWORD=validation-only FRESHLINE_DEMO_ACCESS_CODE=validation-only docker compose config --format json)" \
  || fail "Compose JSON configuration could not be rendered."
printf '%s' "$compose_config" | node -e '
let input = "";
process.stdin.setEncoding("utf8");
process.stdin.on("data", chunk => input += chunk);
process.stdin.on("end", () => {
  const configuration = JSON.parse(input);
  const postgres = configuration.services && configuration.services.postgres;
  if (!postgres) {
    console.error("Compose service postgres is missing.");
    process.exitCode = 1;
    return;
  }
  if (Array.isArray(postgres.ports) && postgres.ports.length > 0) {
    console.error("PostgreSQL must not publish host ports.");
    process.exitCode = 1;
  }
});
' || fail "PostgreSQL must not publish host ports."
echo "PASS: PostgreSQL has no published host ports."

if [[ "$scope" == "full" ]]; then
  for path in frontend/package.json frontend/package-lock.json frontend/Dockerfile simulator/Dockerfile; do
    [[ -f "$path" ]] || fail "NOT RUN / FAIL: required full-stack file '$path' is missing; its owner has not published this Compose dependency."
  done
  echo "+ npm --prefix frontend ci"
  npm --prefix frontend ci
  echo "+ npm --prefix frontend run build"
  npm --prefix frontend run build
else
  echo "NOT RUN: frontend build excluded by Scope=backend; this is not a full-stack PASS."
fi

echo "PASS: requested '$scope' verification scope completed."
