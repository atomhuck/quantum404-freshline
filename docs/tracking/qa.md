# QA — задачи и статус

[Все направления](README.md) · [План полосы](../lanes/qa.md) · [Оперативный статус](../progress/qa.md) · [Полный список задач](../../specs/001-restaurant-inventory/tasks.md)

**0/10 DONE = 0%.** Снимок опубликованной `codex/qa` на 15.10.2026: ветка на базовом коммите, progress `FREE`, QA artifacts не опубликованы. Все пункты `TODO`; кейсы продукта остаются `NOT RUN`.

| ID | Этап | Пункт плана и критерий | Статус | Ближайшая зависимость |
|---|---|---|---|---|
| T004 | G0 | FR→QA traceability, владельцы и честные PASS/FAIL/NOT RUN | TODO | Spec и contract |
| T008 | G0 | Cases поставки/заказа/expiry/idempotency и fixtures | TODO | T004 |
| T024 | G1 | Реальный PG: две партии, дубль, expired/usable stock | TODO | Delivery/stock API |
| T032 | G1 | Реальный order: FEFO, недостаток, повтор, отрицательные кейсы | TODO | Order API и PG |
| T039 | G1/G2 | Stop-list после receipt/order и переход локальной даты | TODO | Stop-list API |
| T044 | G2 | Hot-offer priority/exclusion и before/after evidence | TODO | Offer API |
| T051 | G2 | 5-минутный browser путь на локальном Compose, URL/SHA/screenshots | TODO | Полная вертикаль |
| T052 | G3 | Полная FR/security/negative/regression matrix и P0 repro | TODO | Интеграционный checkpoint |
| T056 | G3 | 100 contenders, restart/outage/recovery на реальном Compose | TODO | Order/concurrency и полный Compose |
| T060 | G3 | Повтор journey/security на внешнем HTTPS, release SHA/verdict | TODO | T057 и внешний URL |

`PASS` не возникает из наличия case: нужен exact SHA, среда, expected/actual и evidence в [progress/qa](../progress/qa.md).
