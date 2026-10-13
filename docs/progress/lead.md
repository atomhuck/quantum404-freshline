---
state: ACTIVE
owner: Codex desktop lead
session_id: 42241754-8332-477d-af91-c46606b51966
branch: codex/lead
current_task: T019
substep: "T013 session, CSRF and safe API errors implemented; delivery integration awaits Data/Orders artifacts"
last_verified_code_commit: bb4a32fbfd439df00903ddea64170e2654d96fed
checkpoint: WIP
next_step: "Wire T019 delivery transaction to PostgreSQL after T003/T011/T014/T018 are published; verify replay and payload-hash conflict, then expose stock; rerun full Compose/CI when Web/simulator contexts arrive"
heartbeat_utc: 2026-10-13T16:53:55Z
human_required: [H-02]
---

# Lead progress

[План работы](../lanes/lead.md) · [Журнал передачи](../handoffs/lead.md) · [Общий календарь](../ROADMAP.md). Это оперативное состояние полосы; при смене компьютера читать также последнюю запись журнала.

| Task | State | Evidence |
|---|---|---|
| T001 | DONE | `mvn -B -q -f backend/pom.xml clean verify` exit=0; Java 23 compiler targeting release 21; code `6badce9d97d5a6b0d47525b5b37d651d122b7e34` |
| T005 | DONE | Redocly CLI 2.63.0 lint exit=0; contract v1.0.0 blob `8009e62976d780e1d56426b75f435c57ec10b031`; code `862949fc902dd441bd24a9e6010917cc60f18b71` |
| T006 | WIP | Compose config parses; Docker 29.8.0 built and started API with PostgreSQL 17.11. PG health, API startup/DB connection, actuator 200, and cookie/CSRF HTTP smoke pass. Full Web/simulator stack is NOT RUN because `frontend/` and `simulator/` contexts are absent |
| T009 | DONE | CI + PowerShell/Bash verify scripts in `6a257f9f996a786bf72422196d0f213363049b06`; Action runtime upgrade in `eaabe229504b3ac6e51aff16d597770c234eeeaa`; backend checks pass; Actions run `38426125849`: backend job success, full-stack job failed at missing `frontend/package.json` |
| T013 | DONE | API Problem mapping, request IDs, input validation, demo session, HttpOnly `FRESHLINE_SESSION`, CSRF, five-attempt rate limit; `scripts/verify.ps1 -Scope backend` exit=0 (7 tests + OpenAPI lint + Compose config); Docker HTTP smoke confirms session and CSRF behavior; code `bb4a32fbfd439df00903ddea64170e2654d96fed` |
| T014 | TODO | Cross-lane Java port signatures not agreed or created; Orders/Data were `FREE` at last remote inspection |
| T010 / G0 | NOT RUN | G0 requires all T001–T009, shared contract/ports, and integration evidence |

T001 result: `mvn -B -q -f backend/pom.xml clean verify` → exit 0 (2026-10-13; local JDK 23, `maven.compiler.release=21`).

T005 result: `npx --yes @redocly/cli@2.63.0 lint --config=redocly.yaml contracts/openapi.yaml` → exit 0. Added missing operation summaries, explicit public/session security declarations, tag descriptions, and delivery/stock/order/error examples. Contract freeze commit: `862949fc902dd441bd24a9e6010917cc60f18b71`; contract blob SHA: `8009e62976d780e1d56426b75f435c57ec10b031`; OpenAPI version `1.0.0`, freeze date `2026-10-13`.

T006/T009 checks: `docker compose config --quiet` with ephemeral validation-only env values → exit 0; YAML parse of `compose.yaml`, `redocly.yaml`, and `.github/workflows/ci.yaml` → exit 0; PowerShell and Git Bash backend verification pass; full PowerShell verify still exits 1 because `frontend/package.json` is absent. Actions run `38426125849` completed with backend job success and full-stack job failure at the missing frontend file; this is not a full-stack PASS. Docker follow-up on 2026-10-13: `docker info --format '{{.ServerVersion}}'` → `29.8.0`; `docker compose config --services` → postgres/web/api/caddy; `docker compose up -d --build api` built the API image and started API + PostgreSQL; PostgreSQL 17.11 became healthy and `pg_isready -U freshline -d freshline` accepted connections; API started on Java 21 and connected to PostgreSQL. HTTP smoke: `/actuator/health` → 200; session exchange issued `FRESHLINE_SESSION` plus CSRF token; protected write without CSRF → 401; with CSRF → reached the not-yet-implemented delivery route (404). No generated Spring password appeared after explicit security configuration. API and PostgreSQL were stopped after the smoke; the named volume was retained. Full Compose, Caddy and simulator runtime remain NOT RUN because frontend/simulator contexts are absent.

Remote lane audit on 2026-10-13 after Actions run `38425144284`: `git fetch origin --prune`; lead docs `lanes/lead.md`, `progress/lead.md`, and `handoffs/lead.md` matched `origin/codex/lead`. Orders, Data, Web, and QA progress remain `FREE` at baseline `43bbc7bcb6db43da03c556fd5c46b1b03c099280`; no migration, port agreement, frontend, or QA matrix has been published. `handoffs/lead.md` still correctly has no entries because no handoff occurred.

Integration attempt: creating a draft PR from `codex/lead` to `codex/integration` returned GitHub API HTTP 403, `Resource not accessible by integration`; no PR was created. `origin/codex/integration` remains at `43bbc7bcb6db43da03c556fd5c46b1b03c099280`. Retry the first integration after T003/T014 artifacts are available, through an enabled PR route or the repository's permitted merge workflow.

T009 follow-up code `eaabe229504b3ac6e51aff16d597770c234eeeaa`: upgraded `actions/checkout` to v7, `actions/setup-java` to v6, and `actions/setup-node` to v7 after hosted CI deprecation warnings; disabled setup-node's automatic package-manager cache. `scripts/verify.ps1 -Scope backend` → exit 0; PyYAML parse of `.github/workflows/ci.yaml` and `git diff --check` → exit 0. Hosted run `38426125849` confirms all three updated actions and the backend job succeed; the full-stack job still stops at missing `frontend/package.json`.

G0 snapshot: OpenAPI v1.0.0 is validated and frozen at the SHA above; no Java ports have been created. A fresh fetch before T013 commit confirmed remote Data/Orders/Web/QA branches remain at baseline `43bbc7bcb6db43da03c556fd5c46b1b03c099280`; T002/T003/T004 and shared T014 port agreement remain absent. No integration or release gate is `PASS`.

T013 runtime behavior checks now cover invalid/blank access codes, safe Problem responses, request IDs, rate limiting, session persistence, CSRF rejection/acceptance, and internal-error redaction (7 passing tests). Idempotency conflict/replay, delivery-to-stock integration, FEFO, SSE replay, and release smoke remain NOT RUN because their persistence/domain endpoints and shared port artifacts are absent. The delivery → PostgreSQL → stock → FEFO order → stop-list → browser vertical is not yet present; no mock counts as completion.

Claim: git fetch origin and remote progress/journal audit at 43bbc7b; branch claim pushed before product code. Session 42241754-8332-477d-af91-c46606b51966; 2026-10-13T15:27:33Z.
