# Backend orders — Java domain, FEFO и конкуренция

## Старт

```text
Сверь docs/lanes/orders.md, docs/progress/orders.md и docs/handoffs/orders.md с remote. При HANDOFF обнови progress и добавь реальную запись в журнал одним status-коммитом.
Ты backend-разработчик Quantum404, владелец codex/orders и backend/domain. Прочитай AGENTS.md, README.md, spec/plan/tasks, contracts/openapi.yaml, docs/DOMAIN_RULES.md, docs/diagrams/erd.md, docs/TEAM_WORKFLOW.md, docs/progress/orders.md. Проверь remote и текущий код; claim свою полосу перед правками.

После G0 создай доменные модели/ports и OrderPolicy/FefoAllocator/HotOfferPolicy по T011, T017, T025, T027, T034, T040. Критично: все блюда агрегируются до проверки, дата истечения включительна, FEFO по expires_on/id, не расходовать просрочку, точный BigDecimal и snapshot рецепта. Для конкурентных испытаний работай с DE/Lead через порт/PR, не редактируй чужой модуль одновременно. Зафиксируй golden fixtures, unit и реальный PG test через app/Testcontainers для 100 contenders и duplicate key. Ошибка/красный тест = WIP, не DONE.

Работай малыми проверяемыми шагами; после каждого commit → progress/orders → push. Если нужен новый инвариант или изменение DTO, сначала ADR/контрактный PR. Продолжай следующие готовые задачи без ожидания человека. В конце назови SHA, тесты, риски, next_step.
```

## Продолжение на другом ноутбуке

```text
Сверь docs/lanes/orders.md, docs/progress/orders.md и docs/handoffs/orders.md с remote. При HANDOFF обнови progress и добавь реальную запись в журнал одним status-коммитом.
Продолжи codex/orders от remote. Прочитай docs/progress/orders.md, TEAM_WORKFLOW.md, spec/plan/tasks, последние commits/PR и contract SHA. Fetch/pull --ff-only, проверь CLEAN или сохрани WIP, захвати очередь только после HANDOFF/FREE или подтверждённой остановки прежнего агента. Возьми next_step, проверь тесты и выполняй дальнейшие маленькие T задачи. Не делай force push/reset --hard. Каждому шагу — code commit, status commit и push. Любую проблему конкуренции проверяй на PostgreSQL, не H2.
```

## Передача

```text
Сверь docs/lanes/orders.md, docs/progress/orders.md и docs/handoffs/orders.md с remote. При HANDOFF обнови progress и добавь реальную запись в журнал одним status-коммитом.
Сохрани handoff для codex/orders. Закрой малый шаг или WIP, укажи версии контрактов/портов, проверенный code SHA, тесты и точную оставшуюся проблему, затем поставь HANDOFF в docs/progress/orders.md. Отдельный commit и push, сверка remote SHA. Не передавай секреты или незапушенный код как будто он доступен.
```
