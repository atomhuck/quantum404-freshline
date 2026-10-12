# Lead — Java API, интеграция и release

## Старт

```text
Сверь docs/lanes/lead.md, docs/progress/lead.md и docs/handoffs/lead.md с remote. При HANDOFF обнови progress и добавь реальную запись в журнал одним status-коммитом.
Реализуй FreshLine в этом репозитории как lead backend и интегратор Quantum404. Сначала прочитай AGENTS.md, README.md, constitution, spec/plan/tasks в specs/001-restaurant-inventory/, contracts/openapi.yaml, docs/DOMAIN_RULES.md, docs/TEAM_WORKFLOW.md, docs/ROADMAP.md, docs/HUMAN_REQUIRED.md и docs/progress/lead.md. Проверь git remote/status/log и фактическое состояние кода; не доверяй истории чата.

Твоя ветка codex/lead. Сначала безопасный claim по TEAM_WORKFLOW.md. Владеешь backend/app, корневым Compose, CI, deploy, контрактом и ежедневной интеграцией. Не переписывай backend/domain, backend/persistence, frontend без согласования владельца. Заморозь G0 OpenAPI/Java ports вместе с DE/orders через коммит; затем выполняй ближайшие T задачи Lead по зависимостям, продолжая после первого результата. Java 21/Spring Boot, один transactional API с PostgreSQL, без Python data service. Бизнес-инварианты из DOMAIN_RULES неизменны без ADR.

Сделай реальную вертикаль как можно раньше: delivery → PG → stock → order FEFO → stop-list → browser. Изолированный mock не считается готовностью. Не задерживай первую интеграцию до конца всех веток. Контрактные ошибки, idempotency, csrf/session, SSE и release gates должны иметь проверки. Каждый малый проверяемый шаг: code commit, status commit с точными результатами, push. Не объявляй DONE/HTTPS/QA без доказательства. Секреты вне Git; если нужен человек, добавь точную H запись и продолжи независимую работу.

В финале сообщи SHA опубликованного checkpoint, выполненные проверки, состояние gate, next_step и только конкретное действие человека, если оно действительно нужно.
```

## Продолжение на другом ноутбуке

```text
Сверь docs/lanes/lead.md, docs/progress/lead.md и docs/handoffs/lead.md с remote. При HANDOFF обнови progress и добавь реальную запись в журнал одним status-коммитом.
Продолжи полосу codex/lead из Git, а не из старого чата. Прочитай AGENTS.md, docs/TEAM_WORKFLOW.md, docs/progress/lead.md, tasks.md, последние remote commits и CI. Fetch/pull --ff-only, проверь владельца/status, сделай claim при FREE/HANDOFF. Если ACTIVE у другого, дождись подтверждённой остановки или handoff; не force push. Проверь recorded SHA, восстанови точный current_substep, выполни ближайшую Lead-задачу и маленькие тестируемые checkpoints. Обнови status и push после каждого. Интегрируй открытые data/orders/web PR минимум дважды в день. Не объявляй готовой функцию только по mock/health.
```

## Передача

```text
Сверь docs/lanes/lead.md, docs/progress/lead.md и docs/handoffs/lead.md с remote. При HANDOFF обнови progress и добавь реальную запись в журнал одним status-коммитом.
Подготовь HANDOFF полосы codex/lead по docs/TEAM_WORKFLOW.md. Закончи один малый шаг или пометь WIP. Проверь staged diff на секреты, опубликуй code commit, отдельный status commit с verified SHA, командами/результатами/ошибками/точным next_step и push. Сверь remote SHA. Не начинай новую крупную задачу. Сообщи ветку, SHA, gate и единственное действие следующего ноутбука.
```
