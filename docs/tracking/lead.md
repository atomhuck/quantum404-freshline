# Lead — задачи и статус

[Все направления](README.md) · [План полосы](../lanes/lead.md) · [Оперативный статус и доказательства](../progress/lead.md) · [Полный список задач](../../specs/001-restaurant-inventory/tasks.md)

**6/20 DONE = 30%; осталось 14 пунктов.** T019 — WIP, T010/G0 — NOT RUN, ещё 12 — TODO. Это счёт по пунктам плана на опубликованной `codex/lead` по состоянию на 15.10.2026; G0–G3/RELEASE не пройдены.

| ID | Этап | Пункт плана и критерий | Статус | Доказательство или ближайшая зависимость |
|---|---|---|---|---|
| T001 | L0 | Java 21 Maven multi-module scaffold; `mvn verify` | DONE | [Сборка и code SHA](../progress/lead.md) |
| T005 | L0 | Проверить и заморозить OpenAPI v1, ошибки/единицы/даты/примеры | DONE | [Lint и contract SHA](../progress/lead.md) |
| T006 | L0 | Корневой Compose, env example, Caddy; без секрета и публичного PG | DONE | [Config, API+PG smoke](../progress/lead.md); полный стек ещё не проверен |
| T009 | L0 | CI и скрипты проверки Maven/frontend/YAML/Compose | DONE | [CI и локальные проверки](../progress/lead.md); full-stack job красный без frontend |
| T010 | L1 | Первый вертикальный CI/Compose checkpoint; G0 только после T001–T009 | NOT RUN | Нужны T002/T003/T004/T007/T008 и совместные Java ports |
| T013 | L1 | API Problem, request ID, validation, session/CSRF и безопасные ошибки | DONE | [HTTP и backend checks](../progress/lead.md) |
| T019 | L1 | Транзакционная поставка: POST, replay, hash conflict, отказ целого payload | WIP | Проверен только preflight Idempotency-Key; нужны T003/T011/T014/T017/T018 |
| T022 | L1 | GET `/stock`, контрактные decimal/date тесты | TODO | Нужен T021, затем реальная поставка и PostgreSQL |
| T028 | L2 | Команда заказа в одной транзакции; отказ без списания | TODO | Нужны T015/T025–T027 и PG |
| T029 | L2 | POST/GET `/orders`, replay и hash mismatch | TODO | После T028; контрактные и отрицательные проверки |
| T035 | L2 | GET `/stop-list`, `/ingredients`, `/dishes` с причинами дефицита | TODO | После order и availability query T033 |
| T038 | L3 | GET `/replenishment` с объяснимой рекомендацией | TODO | После T037 |
| T042 | L3 | GET `/hot-offers`, детерминированный порядок и причины | TODO | После T040/T041 |
| T047 | L3 | Outbox в транзакции и SSE replay/reconnect | TODO | После delivery/order транзакций |
| T050 | L3 | Полный Compose, simulator, healthchecks, Caddy; запуск/остановка | TODO | Нужны Web/simulator контексты и рабочий API |
| T054 | L4 | Production security/config guard и redaction | DONE | [Guard и no-public-PG проверки](../progress/lead.md) |
| T055 | L4 | Smoke delivery → order → stop-list → offer + повтор ключа | TODO | После G1/G2 API |
| T057 | L4 | VDS, Caddy HTTPS, внешний URL с проверкой из другой сети | TODO | Нужны параметры VDS и интегрированный продукт |
| T058 | L4 | Проверенный runbook старта, rollback, backup/restore | TODO | После deploy и Data T053 |
| T061 | L4 | README с фактическим URL, QA verdict, release SHA/tag | TODO | После T057/T060 и release gate |

Зависимости и точные критерии остаются в [плане Lead](../lanes/lead.md) и [tasks.md](../../specs/001-restaurant-inventory/tasks.md). `DONE` подтверждён опубликованным code SHA и проверками в progress; строка не означает прохождение следующего gate.
