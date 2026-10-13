# Tasks: FreshLine

**Input:** [spec](spec.md), [plan](plan.md), [model](data-model.md), [contract](../../contracts/openapi.yaml).
**Rule:** Checkbox means truly verified and pushed; assignee/lane is recorded in `docs/progress`. `[P]` means different files and no unfinished cross-dependency. Every ID is a small checkpoint with commit, relevant test, updated progress and push.

## Phase 1 — Setup (G0, 12–13 October)

- [x] T001 [P] Lead: create Maven multi-module `backend/pom.xml`, `backend/domain/pom.xml`, `backend/persistence/pom.xml`, `backend/app/pom.xml` with Java 21 and pinned Spring Boot BOM; `mvn verify` builds empty modules.
- [ ] T002 [P] Fullstack: create `frontend/package.json`, lockfile, Vite/React/TS/Chakra provider and `frontend/src/main.tsx`; typecheck/build pass.
- [ ] T003 [P] DE: create versioned baseline `backend/persistence/src/main/resources/db/migration/V1__init.sql` according to `data-model.md`, without sample data; migration works twice on Testcontainers PG.
- [ ] T004 [P] QA: create `qa/traceability.md` mapping FR-001…018 to case IDs and owners, with PASS/FAIL/NOT RUN columns; no fictional PASS.
- [x] T005 Lead: validate and freeze `contracts/openapi.yaml` (lint, duplicate paths, decimal/date examples), set version/contract SHA in `docs/progress/lead.md`; all lanes receive the same commit.
- [ ] T006 [P] Lead: add `.env.example`, `compose.yaml`, `deploy/Caddyfile` with API/web/PG/services and demo profile; no secrets or public DB port.
- [ ] T007 [P] Fullstack: define visual tokens, responsive shell, navigation and states in `frontend/src/theme.ts` and `frontend/src/App.tsx` per `docs/FRONTEND.md`.
- [ ] T008 [P] QA: write human-readable cases for delivery/order/expiry/idempotency in `qa/cases/core.md` before code appears.
- [x] T009 Lead: add `.github/workflows/ci.yaml` and `scripts/verify.ps1`/`scripts/verify.sh` for Maven, frontend, YAML, Compose; commands fail honestly when code missing.
- [ ] T010 Lead+DE: publish first vertical CI/Compose checkpoint in `docs/progress/lead.md`, contract version and exact next substep. G0 is done only after all of T001–T009 are verified.

## Phase 2 — Foundational domain/data gates

- [ ] T011 [P] BE-orders: define domain models and repository ports in `backend/domain/src/main/java/ru/quantum404/freshline/domain/`; include `Clock`, base units, positive decimal and expiry policy.
- [ ] T012 [P] DE: add Flyway-backed PostgreSQL/Testcontainers harness in `backend/persistence/src/test/java/` and assert FK/CHECK/UNIQUE plus migration-on-empty/restart.
- [x] T013 Lead: implement API error mapping, request IDs, validation, security/session/CSRF in `backend/app/src/main/java/ru/quantum404/freshline/app/`; test unauthorized write and safe errors.
- [ ] T014 DE+BE-orders: agree repository port signatures in `backend/domain/src/main/java/ru/quantum404/freshline/domain/port/` and one integration fixture; no same-file simultaneous edits.
- [ ] T015 [P] DE: implement row locking and transaction helper in `backend/persistence/src/main/java/ru/quantum404/freshline/persistence/` with sorted ingredient/batch lock order and bounded timeout.
- [ ] T016 [P] Fullstack: generate typed client from `contracts/openapi.yaml` into `frontend/src/api/`; handle `Problem` and API-mode badge.

## Phase 3 — User Story 1, поставка (P1; G1)

**Independent gate:** две партии одного продукта с разными сроками, сумма и дубль поставки проверены на реальном PG.

- [ ] T017 [US1] BE-orders: implement expiry input validation and batch creation policy in `backend/domain/src/main/java/ru/quantum404/freshline/domain/ReceiptPolicy.java`; unit test date source and inclusive boundary.
- [ ] T018 [US1] DE: implement delivery/batch/movement/idempotency persistence in `backend/persistence/src/main/java/ru/quantum404/freshline/persistence/DeliveryRepository.java`; Testcontainers checks rollback and duplicate supplier/number.
- [ ] T019 [US1] Lead: implement transactional delivery use case and POST `/deliveries` in `backend/app/src/main/java/ru/quantum404/freshline/app/delivery/`; replay same key, hash conflict and whole-payload rejection.
- [ ] T020 [P] [US1] Fullstack: create accessible receiving form in `frontend/src/features/deliveries/`; field errors, batch dates, duplicate feedback and pending state.
- [ ] T021 [US1] DE: implement aggregate/partion stock query in `backend/persistence/src/main/java/ru/quantum404/freshline/persistence/StockQuery.java`; test expired vs usable on fixed Clock.
- [ ] T022 [US1] Lead: expose GET `/stock` in `backend/app/src/main/java/ru/quantum404/freshline/app/stock/`; contract test serialized decimal/date fields.
- [ ] T023 [P] [US1] Fullstack: create stock table/detail in `frontend/src/features/stock/`; filter by risk and show exact date/unit.
- [ ] T024 [US1] QA: execute first real delivery/stock cases and record evidence in `docs/progress/qa.md`; mark unresolved cases NOT RUN.

## Phase 4 — User Story 2, заказ и FEFO (P1; G1)

**Independent gate:** all-or-nothing multi-dish order, FEFO, insufficient stock, 100 concurrent attempts, duplicate after lost response.

- [ ] T025 [US2] BE-orders: implement demand aggregation and immutable recipe version snapshot in `backend/domain/src/main/java/ru/quantum404/freshline/domain/OrderPolicy.java`; unit tests for repeated dish/ingredient.
- [ ] T026 [US2] DE: implement recipe/order/consumption/stock movement queries in `backend/persistence/src/main/java/ru/quantum404/freshline/persistence/OrderRepository.java`; verify exact ledger equality.
- [ ] T027 [US2] BE-orders: implement FEFO allocation in `backend/domain/src/main/java/ru/quantum404/freshline/domain/FefoAllocator.java`; same-expiry tie and expired exclusions.
- [ ] T028 [US2] Lead: wire single transaction order command in `backend/app/src/main/java/ru/quantum404/freshline/app/order/` using T015/T025–27; rejected outcome persisted with zero consumption.
- [ ] T029 [US2] Lead: expose POST/GET `/orders` in `backend/app/src/main/java/ru/quantum404/freshline/app/order/`; response replay and hash mismatch contract tests.
- [ ] T030 [P] [US2] Fullstack: order composer/result/error explanation in `frontend/src/features/orders/`, display exact ingredient shortage and prevent accidental repeat UI state.
- [ ] T031 [US2] BE-orders+DE: write Testcontainers concurrency/retry tests in `backend/app/src/test/java/ru/quantum404/freshline/app/OrderConcurrencyTest.java` for same/multi-ingredient races, deadlock retry, lost response.
- [ ] T032 [US2] QA: execute real order/FEFO/negative/duplicate cases in `qa/cases/core.md`, record SHA/results in `docs/progress/qa.md`.

## Phase 5 — User Story 3, риски и стоп-лист (P1; G1/G2)

**Independent gate:** exhaust ingredient → stopped dish; receipt → available; date rollover → expired without movement.

- [ ] T033 [P] [US3] DE: implement query for usable quantities and recipe shortages in `backend/persistence/src/main/java/ru/quantum404/freshline/persistence/AvailabilityQuery.java`; EXPLAIN and test expiry boundary.
- [ ] T034 [US3] BE-orders: implement status/threshold policy in `backend/domain/src/main/java/ru/quantum404/freshline/domain/StockRiskPolicy.java`; 0/1/3 day and timezone tests.
- [ ] T035 [US3] Lead: expose GET `/stop-list`, `/ingredients`, `/dishes` in `backend/app/src/main/java/ru/quantum404/freshline/app/catalog/`; response includes explicit shortages.
- [ ] T036 [P] [US3] Fullstack: stop-list and expiry cards in `frontend/src/features/availability/`; accessible labels and empty/loading/error states.
- [ ] T037 [US3] DE: implement simple 7-day usage/reorder-point query in `backend/persistence/src/main/java/ru/quantum404/freshline/persistence/ReplenishmentQuery.java`; insufficient-history fallback.
- [ ] T038 [US3] Lead: expose GET `/replenishment` in `backend/app/src/main/java/ru/quantum404/freshline/app/insight/`; do not claim ML forecast.
- [ ] T039 [US3] QA: confirm stock/stop-list accuracy across midnight and after receipt/order in `qa/cases/insight.md` and `docs/progress/qa.md`.

## Phase 6 — User Story 4, горячие предложения (P1; G2)

**Independent gate:** only makeable dishes; deterministic score, explanations and recomputation after order/receipt.

- [ ] T040 [US4] BE-orders: write explainable score formula and golden examples in `docs/DOMAIN_RULES.md` and `backend/domain/src/main/java/ru/quantum404/freshline/domain/HotOfferPolicy.java`.
- [ ] T041 [US4] DE: implement candidate-batch/possible-servings query in `backend/persistence/src/main/java/ru/quantum404/freshline/persistence/HotOfferQuery.java`; no expired/zero availability.
- [ ] T042 [US4] Lead: expose GET `/hot-offers` in `backend/app/src/main/java/ru/quantum404/freshline/app/insight/`; deterministic ordering and reasons in contract test.
- [ ] T043 [P] [US4] Fullstack: hot-offer cards and explanation in `frontend/src/features/offers/`; distinguish recommendation from fact/forecast.
- [ ] T044 [US4] QA: run priority, exclusion and change-after-event scenarios in `qa/cases/insight.md` with saved before/after evidence.

## Phase 7 — User Story 5, живое demo (P1; G2/G3)

**Independent gate:** deterministic simulator drives public web; stopping simulator does not stop app.

- [ ] T045 [P] [US5] DE: create synthetic fixed catalog, batches and recipes in `backend/persistence/src/main/resources/db/demo/seed.sql`; idempotent only under DEMO_MODE.
- [ ] T046 [P] [US5] BE-fullstack: create demo producer with deterministic JSON fixtures, configurable pace/pause/seed and correct idempotency in `simulator/src/`; it calls only published HTTP endpoints.
- [ ] T047 [US5] Lead: write outbox events in order/receipt transaction and SSE replay in `backend/app/src/main/java/ru/quantum404/freshline/app/events/`; test restart/reconnect.
- [ ] T048 [US5] Fullstack: subscribe with `Last-Event-ID` and refetch on reconnect in `frontend/src/api/events.ts`; event timeline in `frontend/src/features/timeline/`.
- [ ] T049 [US5] Fullstack: responsive dashboard with stock/risk/stop-list/offers and accessible status in `frontend/src/features/dashboard/`; desktop/tablet/mobile Playwright smoke.
- [ ] T050 [US5] Lead: run full Compose + simulator with healthchecks and Caddy in `compose.yaml`/`deploy/Caddyfile`; test clean startup and simulator stopped.
- [ ] T051 [US5] QA: perform 5-minute judge journey and browser evidence on local integrated Compose URL; record exact SHA/time in `docs/progress/qa.md`.

## Phase 8 — Hardening, deploy and release (G3)

- [ ] T052 [P] QA: complete FR matrix, security/negative/regression cases in `qa/traceability.md` and `qa/cases/`; report P0 defects with repro, commit, expected/actual.
- [ ] T053 [P] DE: measure indexes and transaction lock behaviour, run backup/restore on spare volumes; record actual outputs in `docs/progress/data.md`.
- [x] T054 Lead: enforce production security/config startup checks and secret redaction in `backend/app/src/main/java/ru/quantum404/freshline/app/security/`; no public DB, no default password.
- [ ] T055 Lead: create `scripts/smoke.ps1`/`.sh` for delivery→order→stop-list→offer + repeat key; fail on unexpected result.
- [ ] T056 QA+BE-orders: execute 100 contender tests, expiry rollover, API restart, DB outage and recovery on real Compose; save bounded evidence in `docs/progress/qa.md`.
- [ ] T057 Lead: deploy to VDS with `deploy/compose.prod.yaml`, Caddy HTTPS and DNS/sslip alias, then verify external URL from another network.
- [ ] T058 Lead: complete `docs/DEPLOYMENT.md` with exact start/rollback/backup/restore; test clean clone and non-destructive restart.
- [ ] T059 Fullstack: polish browser UX, contrast/focus/mobile table and demo labels in `frontend/src/`; rerun build and Playwright.
- [ ] T060 QA: repeat 5-minute journey and security smoke on external HTTPS URL after T057; record browser, URL, SHA and verdict in `docs/progress/qa.md`.
- [ ] T061 Lead: update `README.md` with factual public URL, demo access steps, QA matrix, limitations and full release SHA/tag; submit only verified link.

## Dependencies and parallel ownership

T001–004 can start in separate files. T005 is the freeze gate before lanes diverge. T011–016 form the shared base. US1 and US2 share data/invariants: T017–22 before T025–31; frontend shells can proceed in parallel on mock. US3 query starts after delivery/order models exist; US4 after US3 availability; US5 seed/simulator can start after API contract, but full vertical T050 waits for US1–4. T060 follows public deploy T057; final README/tag T061 follows QA verdict. Internal branches merge to integration at least twice daily, not only at the end.

## How to validate each story

`mvn verify` on Java modules, `npm run typecheck && npm run build` on frontend, contract validation, `docker compose config --quiet`, real Testcontainers PG, and the task-specific smoke. QA follows `docs/QA.md`. Mark `NOT RUN` when unavailable. No staged secrets or credentials; push status after code.

## Suggested MVP order

T001–32 is the smallest technically valuable slice (parties + atomic order). For the hackathon submission all P1 stories T001–51 and release gates T052–61 are targeted. Features with a star in `docs/ROADMAP.md` are outside release dependencies.
