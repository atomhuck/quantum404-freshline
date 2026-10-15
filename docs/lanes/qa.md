# План полосы QA — кейсы, доказательства, приёмка

**Ветка:** `codex/qa`. **Таблица всех задач:** [tracking/qa](../tracking/qa.md). **Статус:** [progress/qa](../progress/qa.md). **Передачи:** [handoffs/qa](../handoffs/qa.md). **Промпт:** [qa](../../prompts/qa.md). **Базовый список сценариев:** [QA.md](../QA.md). QA-инженеру не нужно писать тесты вручную: его агент создаёт cases/автоматизацию, инженер сверяет expected/actual и приоритет.

## Очередь без ожидания готового продукта

| Когда | Задачи | Выход и доказательство | Кому передать |
|---|---|---|---|
| Сразу, до кода | T004, T008 | `qa/traceability.md` FR→QA-01…18, синтетические fixtures, подробные положительные/отрицательные/граничные cases; все `NOT RUN` | lead получает неоднозначности с примером; DE/Orders — edge cases |
| После receipt/stock | T024 | на **реальном PostgreSQL** поставка с двумя партиями, дубль и expired/usable; версия API/DB и команды в отчёте | lead/DE — дефекты с воспроизведением |
| После order/stop-list | T032, T039 | полный и отклонённый multi-dish order, FEFO/tie, idempotency, местная полночь, stop-list после заказа и поставки | lead/orders/data — P0/P1 defects |
| После offers/demo | T044, T051 | рейтинг и исключения, start/pause simulator, локальный 5-минутный судейский путь в браузере, screenshots/URL/SHA | web/lead — UX и demo проблемы |
| G3 | T052, T056 | полная матрица PASS/FAIL/NOT RUN/BLOCKED, 100 contenders, restart/outage/recovery, security/negative cases; P0=0 | lead решает выпуск по фактам |
| После внешнего deploy | T060 | повторить путь и security smoke на публичном HTTPS URL **после** T057; release SHA, браузер, seed, дата, verdict | lead заполняет README/tag/подачу |

## Правило результатов

Mock/contract, локальный Compose+PostgreSQL и публичный VDS — три разных окружения в отчёте. PASS ставится только для выполненного шага с SHA, окружением, expected/actual и evidence; незапущенное — `NOT RUN`. Дефект: severity, точные шаги, seed/timezone, request ID, expected/actual, screenshot/response без секретов. QA не ослабляет доменные правила ради зелёного теста. Для P0 (oversell, двойное списание, просроченная партия, частичный заказ, сломанный публичный путь, утечка) выпуск закрыт до исправления и повторного теста.

На каждом блоке публиковать cases/отчёт, [progress/qa](../progress/qa.md) и push. [Журнал передачи](../handoffs/qa.md) должен назвать проверенный SHA, URL/seed/браузер, поимённые PASS/FAIL/NOT RUN, открытые P0 и **конкретный следующий сценарий**. Финальный verdict — отдельно от слов разработчика «готово».
