# Data — задачи и статус

[Все направления](README.md) · [План полосы](../lanes/data.md) · [Оперативный статус](../progress/data.md) · [Полный список задач](../../specs/001-restaurant-inventory/tasks.md)

**0/13 DONE = 0%.** Снимок опубликованной `codex/data` на 15.10.2026: ветка на базовом коммите, progress `FREE`, миграции и repository-код не опубликованы. Все пункты `TODO`.

| ID | Этап | Пункт плана и критерий | Статус | Ближайшая зависимость |
|---|---|---|---|---|
| T003 | G0 | Flyway V1: таблицы, FK/CHECK/UNIQUE/индексы, миграция чистой PG | TODO | Согласованная модель данных |
| T012 | G0 | Testcontainers PG/Flyway: ограничения, empty migration, restart | TODO | T003 |
| T014 | G0 | Совместно с Orders: сигнатуры Java repository ports и fixture | TODO | Согласовать с Orders до реализации repositories |
| T015 | G1 | Sorted row locks и transaction helper, timeout | TODO | T003/T014 |
| T018 | G1 | Persistence delivery/batch/movement/idempotency, rollback/duplicate | TODO | T003/T014/T015 |
| T021 | G1 | Total/usable stock query с датой, expiry и партиями | TODO | T003/T018 |
| T026 | G1 | Recipe/order/consumption queries и ledger equality | TODO | T015 и policy Orders |
| T031 | G1 | Совместно с Orders: PG concurrency/retry, 100 contenders, deadlock/lost response | TODO | T026 и T025/T027/T028 |
| T033 | G1/G2 | Usable quantity/recipe shortage query, EXPLAIN и expiry test | TODO | Stock/order model |
| T037 | G2 | 7-day usage/reorder point, fallback без истории | TODO | Stock/order model |
| T041 | G2 | Candidate batches/possible servings; исключить expired/zero | TODO | Availability и policy T040 |
| T045 | G2 | Синтетический catalog/batches/recipes seed только под DEMO_MODE | TODO | Стабильная схема |
| T053 | G3 | Индексы/locks, backup и restore на отдельном volume | TODO | Рабочая БД и сценарии |

Для `DONE` нужны опубликованный SHA и проверки на реальном PostgreSQL в [progress/data](../progress/data.md).
