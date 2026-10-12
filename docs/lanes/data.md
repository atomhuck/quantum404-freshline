# План полосы data — PostgreSQL и Java persistence

**Ветка:** `codex/data`. **Зона:** `backend/persistence/`, Flyway/SQL, synthetic seed и backup/restore. **Статус:** [progress/data](../progress/data.md). **Передачи:** [handoffs/data](../handoffs/data.md). **Промпт:** [data-engineer](../../prompts/data-engineer.md).

## Последовательность

| Вход | Задачи | Проверяемый выход и контроль | Кому нужен результат |
|---|---|---|---|
| G0: утверждены [ER](../diagrams/erd.md), единицы и даты; Java ports уточняются T014 | T003, T012, T014 | Flyway V1: FK/CHECK/UNIQUE/индексы/ledger/idempotency, migration-empty и restart Testcontainers; сигнатуры ports согласованы с orders/lead до repository-кода | lead делает receipt API; orders пишет policies |
| После V1 | T015, T018, T021 | transaction helper и сортированный lock order, delivery/batch/movement и usable/total stock; rollback/duplicate/expiry на реальном PG | lead T019/T022, web подключает склад |
| После order policy и FEFO | T026, T031 совместно с orders | order/recipe/consumption queries, ledger equality, 100 contender tests, отсутствие deadlock/oversell | lead связывает T028/029; QA проверяет G1 |
| После stock/order модели | T033, T037 | availability/recipe shortage и простой reorder point по 7 дням, fallback при отсутствии истории; EXPLAIN | lead stop-list/replenishment; orders risk policy |
| После hot-offer policy | T041, T045 | candidate/possible servings SQL, идемпотентный demo seed под DEMO_MODE; исключение просрочки и нулевого остатка | lead insight API; web demo producer |
| Перед G3 | T053 | измеренные индексы/locks, проверенный backup и restore в **отдельный** volume/экземпляр; запись фактических команд без секретов | lead deploy/runbook; QA recovery verdict |

## Границы ответственности

PostgreSQL — единственный источник истины; DE пишет реализацию Java repository ports, **не** отдельный Python/HTTP data-service. Не менять уже опубликованную Flyway-миграцию: новый `Vn__...sql`. `stock_balance` и неизменяемое движение согласованы в одной транзакции. Использовать `NUMERIC(18,3)` и базовые единицы; локальная ресторанная дата выводится из переданного Clock/timezone. Сохранять одинаковый порядок блокировок ингредиентов и партий. Секреты, реальные дампы и volume в Git не попадают.

Отдавать результат маленькими PR сразу после V1, receipt и order repository; lead не должен ждать всех аналитических запросов. Каждому code commit сопутствуют проверка на PostgreSQL, [progress/data](../progress/data.md) с migration head/портовым SHA и push. [Журнал](../handoffs/data.md) при передаче включает состояние миграций, набор фикстур, тестовый PG и следующий SQL/тест.
