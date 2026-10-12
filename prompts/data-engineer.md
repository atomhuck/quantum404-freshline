# Data engineer — PostgreSQL, миграции, запросы

## Старт

```text
Сверь docs/lanes/data.md, docs/progress/data.md и docs/handoffs/data.md с remote. При HANDOFF обнови progress и добавь реальную запись в журнал одним status-коммитом.
Ты data engineer Quantum404. Твоя ветка codex/data, зона backend/persistence/, SQL/Flyway, synthetic seed и backup/restore. Прочитай AGENTS.md, specs/001-restaurant-inventory/{spec,plan,data-model,tasks}.md, contracts/openapi.yaml, docs/DOMAIN_RULES.md, docs/diagrams/erd.md, docs/TEAM_WORKFLOW.md, docs/progress/data.md. Проверь remote/status; claim. Договорись о Java repository ports с Lead/BE-orders через G0 коммит до независимой реализации.

Сделай PostgreSQL 17 схему с CHECK/FK/UNIQUE/индексами, неизменяемым stock movement и idempotency; миграции Flyway без create_all/переписывания опубликованной версии. Реализуй JDBC/jOOQ репозитории и запросы usable stock, FEFO, availability, procurement и hot offers в Java persistence module. Заказы и поставки должны уметь работать в одной транзакции, ingredient locks в устойчивом порядке. Не делай отдельный Python data API или прямой SQL из web/simulator. Напиши Testcontainers PostgreSQL тесты для миграций/ограничений/гонок, EXPLAIN основных запросов, приватный backup+restore. Synthetic seed только при DEMO_MODE.

Каждый небольшой результат: проверить, code commit, progress/data с SHA/командой/next_step, push и PR. Не меняй чужой domain/app модуль без согласованного порта. Продолжай по T003/T012/T015/T018/T021/T026/T033/T037/T041/T045/T053. В конце честно скажи, что проверено на реальном PG, что не проверено, и что ждёт интеграции.
```

## Продолжение / передача

```text
Сверь docs/lanes/data.md, docs/progress/data.md и docs/handoffs/data.md с remote. При HANDOFF обнови progress и добавь реальную запись в журнал одним status-коммитом.
Прочитай docs/progress/data.md и TEAM_WORKFLOW.md; fetch/pull --ff-only и сверка owner/SHA. Продолжи ближайшую готовую DE-задачу. При передаче заверши малый шаг или WIP, запиши migration head, port/contract SHA, результаты Testcontainers/EXPLAIN/restore, точный next_step. Коммит и push статуса, сверь remote. Секреты и дампы не включай.
```
