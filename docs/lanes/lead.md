# План полосы lead — Java API, интеграция, выпуск

**Ветка:** `codex/lead`. **Зона:** `backend/app/`, корневые Compose/CI/scripts/deploy, OpenAPI и выпуск. **Таблица всех задач:** [tracking/lead](../tracking/lead.md). **Текущий статус:** [progress/lead](../progress/lead.md). **История смены ноутбука:** [handoffs/lead](../handoffs/lead.md). **Промпт:** [backend-lead](../../prompts/backend-lead.md).

## Порядок работы

| Очередь и вход | Задачи | Проверяемый выход | Кому передать |
|---|---|---|---|
| L0 · сразу, без чужого кода | T001, T005, T006, T009 | Maven multi-module строится; OpenAPI v1 проверен; Compose config и CI опубликованы. В progress записан SHA контракта и дата freeze | Orders, Data и Web получают один contract SHA; QA — версии ошибок/сессии |
| L1 · после модели/миграции DE и policies Orders | T010, T013, T019, T022 | Сначала реальная поставка и stock в PostgreSQL, затем security/error mapping. Повтор ключа и неверный payload проверены; первый вертикальный smoke опубликован | Web подключает формы, QA выполняет QA-01…04 |
| L2 · после T015/T025–27 | T028, T029, T035 | Одна транзакция order: агрегировать спрос, lock, FEFO, ledger, response; GET/POST orders и стоп-лист. Тесты на реальном PG, не на mock | QA проверяет QA-05…11; Web подключает order/stop-list |
| L3 · после availability/offer SQL | T038, T042, T047, T050 | Объяснимые procurement/offer API, outbox/SSE, полный Compose с симулятором. Ручные и автопроверки не зависят от включённого producer | Web live dashboard; QA 5-минутный сценарий |
| L4 · после объединения G2 | T054, T055, T057, T058, T061 | Защита и redaction, сквозной smoke, HTTPS на VDS, проверка backup/rollback, URL+SHA+verdict в README | QA проверяет внешний URL, тимлид подаёт ссылки |

**Интеграция проходит раньше полной готовности полос:** первый PR после scaffold+миграции, второй после поставки, затем после заказа. Минимум два окна ежедневно: 12:00 и 20:00 МСК. Проверять миграцию чистой БД, `mvn verify`, сборку web, контракт, Compose и вертикальный smoke. При красном тесте вернуть конкретную проблему владельцу файла; не чинить молча чужой модуль.

## Критический путь и правила решения

1. Заморозить `contracts/openapi.yaml`, единицы, даты, ошибки, idempotency и Java ports до расхода веток. После freeze изменение — отдельный PR/ADR с влиянием на UI, DE и QA.
2. Как только DE даёт V1 и receipt repository, соединить POST delivery→PG→GET stock. Это первый реально проверяемый пользовательский путь.
3. Order endpoint соединять только с real row locks и общим transaction boundary. Нельзя считать stock в контроллере, принимать частичный заказ или считать успешным UI mock.
4. Публичный VDS начать готовить до последнего дня: ресурсы/порты/существующий proxy, секреты вне Git, HTTPS и доступ извне. Отсутствие доступа записать в [H-03](../HUMAN_REQUIRED.md), продолжать локальный G2.
5. Перед сдачей QA даёт verdict на exact release SHA. `main`, tag, README и URL должны ссылаться на одно состояние.

## Ритм и передача

После каждого 5–20-минутного проверяемого шага: точные пути в stage → проверка секрета/diff → code commit → `docs/progress/lead.md` с code SHA и next_step → push. При смене компьютера записать отдельную запись в [журнал](../handoffs/lead.md) вместе со статусом `HANDOFF`; новый агент начинает со сверки remote и последнего журнала. Подробная процедура — [TEAM_WORKFLOW](../TEAM_WORKFLOW.md).

**Готово для роли**, когда T001/005/006/009/010/013/019/022/028/029/035/038/042/047/050/054/055/057/058/061 подтверждены кодом и проверками, все интеграционные PR и QA verdict учтены, публичный URL проверен снаружи. До этого отмечать только фактические gate.
