# Web — задачи и статус

[Все направления](README.md) · [План полосы](../lanes/web.md) · [Оперативный статус](../progress/web.md) · [Полный список задач](../../specs/001-restaurant-inventory/tasks.md)

**0/12 DONE = 0%.** Снимок опубликованной `codex/web` на 15.10.2026: ветка на базовом коммите, progress `FREE`, frontend/simulator не опубликованы. Все пункты `TODO`.

| ID | Этап | Пункт плана и критерий | Статус | Ближайшая зависимость |
|---|---|---|---|---|
| T002 | G0 | Vite/React/TS/Chakra scaffold, lockfile, typecheck/build | TODO | OpenAPI v1 |
| T007 | G0 | Токены, адаптивный shell, навигация и состояния | TODO | T002 |
| T016 | G0/G1 | Типизированный OpenAPI client, Problem и badge API mode | TODO | T002/contract SHA |
| T020 | G1 | Доступная форма поставки: партии, даты, ошибки, pending | TODO | T016 и POST delivery |
| T023 | G1 | Таблица/карточка stock: риск, дата, единица, empty/error | TODO | T016 и GET stock |
| T030 | G1 | Multi-dish order composer, дефицит, retry/idempotency | TODO | POST/GET orders |
| T036 | G1/G2 | Stop-list и expiry cards, loading/empty/error | TODO | GET stop-list |
| T043 | G2 | Hot-offer cards с объяснением, без заявления прогноза | TODO | GET hot-offers |
| T049 | G2 | Dashboard stock/risk/stop-list/offers, responsive Playwright | TODO | Реальные API |
| T046 | G2 | Отдельный HTTP simulator: seed, pace/pause, idempotency | TODO | Стабильный POST API |
| T048 | G2 | SSE Last-Event-ID, reconnect/refetch и timeline | TODO | SSE endpoint T047 |
| T059 | G3 | Контраст/focus/mobile/demo labels и повтор build/Playwright | TODO | Интегрированный browser path |

UI mock с явным badge не закрывает реальный G1. `DONE` требует опубликованный SHA и проверки в [progress/web](../progress/web.md).
