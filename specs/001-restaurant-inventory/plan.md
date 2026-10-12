# Implementation Plan: FreshLine

**Branch**: `main` | **Date**: 2026-10-12 | **Spec**: [spec.md](spec.md)

## Summary

Построить одну вертикаль браузер → Java API → PostgreSQL для партий, заказов и стоп-листа, затем добавить объяснимые предложения и живую демонстрацию. Схема и SQL принадлежат дата-инженеру внутри общего Java-репозитория. Отдельный Python-контейнер создаёт только синтетические HTTP-события. Архитектура — модульный монолит: одна транзакция покрывает проверку, FEFO, списание, журнал и идемпотентный результат; это устраняет сетевую гонку между «проверил» и «списал».

## Technical Context

**Language/Version**: Java 21 LTS, Spring Boot 3.5.16; frontend React 19 + TypeScript; demo producer Python 3.12 без доменной логики.
**Primary Dependencies**: Spring Web, Validation, Security, Actuator, Flyway, jOOQ/JDBC; frontend Vite, Chakra UI 3, TanStack Query, Recharts. Точные npm и контейнерные версии/хеши закрепляются lock-файлами при scaffold.
**Storage**: PostgreSQL 17, одна БД; `NUMERIC(18,3)` в базовых единицах; UTC timestamps и ресторанная календарная дата.
**Testing**: JUnit 5, Testcontainers PostgreSQL, MockMvc/RestAssured, конкурентные интеграционные тесты; Vitest, Playwright; OpenAPI validation; QA manual matrix.
**Target Platform**: Linux VDS 2 vCPU / 4 GB RAM / 40 GB disk минимум для demo, Docker Compose, Caddy TLS; только web.
**Project Type**: веб-приложение и отдельный контейнер симулятора.
**Performance Goals**: 20 событий/мин demo; p95 API GET <500 ms и POST <1 s на описанном VDS; UI update ≤3 s. Это цели до замера.
**Constraints**: deadline 17.10, релиз freeze 16.10; все основные сценарии без платного сервиса; публичный demo только с синтетическими данными и ограниченным доступом к записи.
**Scale/Scope**: один ресторан, десятки ингредиентов/блюд/партий, сотни заказов для demo; не заявлять multi-tenant/POS integration.

## Constitution Check

- [x] Все остатки/движения/ключ результата — одна транзакция; блокировки в устойчивом порядке.
- [x] `contracts/openapi.yaml`, `data-model.md`, `docs/DOMAIN_RULES.md` замораживаются до работы в независимых ветках.
- [x] Тесты на реальном PostgreSQL запланированы отдельно от mock; честный QA gate.
- [x] Коммиты, handoff, секреты и критерии релиза описаны в `docs/TEAM_WORKFLOW.md`/`DEPLOYMENT.md`.

После Phase 1 эти пункты сверены с контрактом и моделью. Отступлений нет.

## Project Structure

### Documentation

```text
specs/001-restaurant-inventory/{spec,plan,research,data-model,quickstart,tasks}.md
contracts/openapi.yaml              # единственный HTTP-контракт
docs/{BRIEF_TRACEABILITY,DOMAIN_RULES,DECISIONS,TEAM_WORKFLOW,ROADMAP,QA,FRONTEND,DEPLOYMENT}.md
docs/diagrams/{architecture,erd,cjm}.md
docs/progress/{lead,orders,data,web,qa}.md
prompts/*.md
```

### Target source code (создаётся исполнителями задач)

```text
backend/
  pom.xml
  domain/src/main/java/...        # чистые модели, policies, порты
  persistence/src/main/java/...   # jOOQ/JDBC, SQL, реализации портов
  persistence/src/main/resources/db/migration/
  app/src/main/java/...           # транзакционные use cases, REST, security, SSE
  app/src/test/java/...           # unit + Testcontainers integration
frontend/{package.json,package-lock.json,src/,tests/}
simulator/{Dockerfile,requirements.lock,src/,scenarios/}
deploy/{Caddyfile,compose.prod.yaml}
compose.yaml
.github/workflows/
scripts/{verify,smoke,backup,restore}.*
```

**Structure Decision**: разделение по модулям Java позволяет дата-инженеру работать с PostgreSQL и интерфейсами портов без отдельного Python data-API. Lead владеет API/security/интеграцией, BE-orders владеет доменными правилами, fullstack владеет web и витринами. Не создавать распределённые транзакции ради разделения людей.

## Execution and gates

| Gate | Результат | Владелец | Крайний срок, МСК |
|---|---|---|---|
| G0 | Контракт v1, правила дат/единиц, архитектура, seed-сценарий, ветки и доступы | Lead + DE + QA | 13.10 12:00 |
| G1 | Реальная вертикаль поставка → партии/остатки → заказ FEFO → стоп-лист | Lead, BE-orders, DE, web | 14.10 23:00 |
| G2 | Hot offers, закупка, SSE, simulator, адаптивный UI; все P0 сценарии интегрированы | Все разработчики | 15.10 23:00 |
| G3 | Конкуренция, idempotency, expiry boundary, restart, QA, HTTPS; P0 blockers = 0 | QA + Lead | 16.10 18:00 |
| RELEASE | чистый deploy, demo script, README с фактическим URL/SHA, tag, резервная копия | Lead | 16.10 23:00 |
| Buffer | проверка подачи до точного срока 17.10; только исправления блокеров | Lead | 17.10 |

За просрочку gate не скрывать функцию и не объявлять mock готовым. Сначала исправлять вертикальный P0, потом polish. Граф и задачи — в `tasks.md`, расписание ролей — `docs/ROADMAP.md`.

## Critical implementation decisions

1. **Receipt**: delivery+batch+movement+idempotency result в одной транзакции; уникальность `(supplier, delivery_number)` и `(scope, key)`. Дата партии — либо manufactured_on + shelf_life_days, либо подтверждённый supplier expiry; источник хранится.
2. **Order**: агрегировать потребность всех блюд по ingredient; сортировать ingredient IDs; `SELECT ... FOR UPDATE` на строках ингредиентов, затем на пригодных партиях FEFO; повторно считать доступность внутри той же транзакции; записать accepted/rejected order, consumption, movements и response. Проверку дефицита нельзя выносить в браузер или асинхронный worker.
3. **Expiry**: пригодно через локальную дату `expires_on >= today`. В начале транзакции получить `today` из управляемого `Clock` + timezone; в UI обновить при полуночи и при командах. Журнал фактических списаний не удалять.
4. **Stop-list**: вычислять из пригодных остатков и актуальной версии рецептуры; snapshot/cache допустим только при гарантированной инвалидации. На первом MVP читать запросом из БД.
5. **Hot offers**: детерминированный score на ближайших партиях, взвешенный расходом ингредиента в порции, с перечислением причин; только если блюдо доступно. При 0 пригодных порций не ранжировать.
6. **Live update**: транзакционный outbox с monotonic event ID; SSE с `Last-Event-ID` и повторной загрузкой снапшота при разрыве. Если срок поджимает, polling 3 s сохраняет функцию, но G2 не отмечать полным до SSE.
7. **Security**: публичное чтение только синтетического demo; запись — через выдаваемую сервером demo-сессию после доступа по секретному коду из окружения, HttpOnly/Secure/SameSite cookie + CSRF. Rate limit на вход/команды. Prod не должен стартовать с demo credentials.
8. **Operations**: PostgreSQL наружу не публиковать, gateway только 80/443, миграция перед readiness, healthchecks, named volume, ежедневный backup и проверенное восстановление.

## Complexity Tracking

Сверх одного Java API добавлен только симулятор как демонстрационный клиент. Его отключение не меняет доменные правила. SSE/outbox добавляются после вертикального G1; Kafka/Redis/Kubernetes/ML исключены как ненужные для одного ресторана и пяти дней.
