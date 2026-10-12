# План полосы web — интерфейс, UX и демо-клиент

**Ветка:** `codex/web`. **Владелец:** backend fullstack под UX-направлением lead. **Зона:** `frontend/`, `simulator/`. **Статус:** [progress/web](../progress/web.md). **Передачи:** [handoffs/web](../handoffs/web.md). **Промпт:** [backend-fullstack](../../prompts/backend-fullstack.md).

## Два независимых потока одной полосы

**Web:** контрактные типы, получение данных, формы и доступный интерфейс. **Simulator:** отдельный HTTP-клиент с синтетикой, который можно выключить без потери пользовательских функций. Симулятор не знает SQL, FEFO или бизнес-решений.

| Порядок и вход | Задачи | Проверяемый выход | Стыковка |
|---|---|---|---|
| Сразу по OpenAPI | T002, T007, T016 | Vite/React/TS/Chakra, lockfile, дизайн-токены, desktop/mobile shell, typed client, обработка ошибок; typecheck/build | lead замораживает контракт; mock имеет явный badge и не считается G1 |
| После delivery/stock API | T020, T023 | форма поставки с полями партий/дат и валидацией, таблица и карточки остатков; реальные browser requests, ошибки и loading/empty | QA выполняет поставку; DE сверяет числа/единицы |
| После order/availability API | T030, T036 | composer multi-dish заказа, объяснение дефицита, стоп-лист/риск; idempotency key для retry и подтверждённый ответ перед success | QA проверяет G1, lead исправляет контрактные расхождения |
| После insight API | T043, T049 | объяснимые hot offers, dashboard, адаптивные 390/1440px экраны, keyboard/focus/aria; скриншоты и Playwright | QA 5-минутный путь, lead визуальный review |
| Параллельно после стабильного POST API | T046 | Docker-ready producer: seed, pace, pause, повторяемый сценарий, корректные ключи; тест start/stop без потери ручного API | lead подключает demo profile, QA QA-15 |
| После SSE endpoint | T048, T059 | reconnect/Last-Event-ID/refetch, event timeline, честный fallback, polish контраста и мобильных таблиц; browser smoke | QA проверяет публичный URL после deploy |

## Качество и способ интеграции

- Визуальные правила, тексты, состояния, responsive и доступность — в [FRONTEND](../FRONTEND.md); маршрут пользователя — [CJM](../diagrams/cjm.md). Figma допустима для согласования, но не блокирует код.
- Первую вертикаль UI соединить с настоящим API сразу после T019/T022; mock использовать только пока нет endpoint и явно показывать режим. Не имитировать подтверждение заказа или серверный остаток на клиенте.
- На каждой форме проверить success, 4xx/409, сеть недоступна, loading, повтор и клавиатуру. Для mobile проверить отсутствие горизонтальной потери данных, для desktop — читаемую таблицу.
- Генерируемый клиент обновлять при смене OpenAPI SHA. Если контракт расходится, фиксировать конкретный diff и ждать владельца контракта, не создавать локальный альтернативный DTO.

После малого шага — typecheck/build и нужный UI-тест, code commit, [progress/web](../progress/web.md), push. При передаче остановить **только локальный** producer, указать OpenAPI SHA, UI/API mode, последние скриншоты, тесты и следующий экран/файл в [журнале](../handoffs/web.md).
