# Orders — задачи и статус

[Все направления](README.md) · [План полосы](../lanes/orders.md) · [Оперативный статус](../progress/orders.md) · [Полный список задач](../../specs/001-restaurant-inventory/tasks.md)

**0/8 DONE = 0%.** Снимок опубликованной `codex/orders` на 15.10.2026: ветка на базовом коммите, progress `FREE`, domain-код не опубликован. Все пункты `TODO`.

| ID | Этап | Пункт плана и критерий | Статус | Ближайшая зависимость |
|---|---|---|---|---|
| T014 | G0 | Совместно с Data: Java repository ports, Clock/quantity/date и fixture | TODO | Согласование с Data и Lead после contract freeze |
| T011 | G0/G1 | Domain models и ports, положительные базовые величины и Clock | TODO | Общий контракт и модель |
| T017 | G1 | Receipt/expiry policy, включительная дата, фиксированный Clock | TODO | T011 |
| T025 | G1 | Агрегация спроса и immutable recipe snapshot | TODO | T011 и модель заказов |
| T027 | G1 | FEFO allocator: `expires_on, id`, исключение просрочки | TODO | T025 |
| T031 | G1 | Совместно с Data: 100 PG contenders, retry, lock order, ledger | TODO | T026/T028 и реальный PG |
| T034 | G1/G2 | Risk policy: 0/1/3 дня и local midnight | TODO | Availability/expiry модель |
| T040 | G2 | Объяснимая hot-offer score formula и golden examples | TODO | T034/availability |

`DONE` требует опубликованный SHA и доказательства в [progress/orders](../progress/orders.md); чистые unit tests не заменяют PostgreSQL concurrency gate.
