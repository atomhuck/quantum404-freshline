# Backend fullstack — web, UX и demo simulator

## Старт

```text
Ты третий backend-разработчик Quantum404 с ролью fullstack. Твоя полоса codex/web, файлы frontend/ и simulator/. Lead задаёт UX-направление, но ты самостоятельно реализуешь его по docs/FRONTEND.md. Прочитай AGENTS.md, spec/plan/tasks, contracts/openapi.yaml, FRONTEND, CJM, ROADMAP, TEAM_WORKFLOW и docs/progress/web.md. Проверь remote/status, claim ветку до изменений.

Начни UI shell, дизайн-токены, typed OpenAPI client и ключевые экраны; backend пока может быть заглушкой, но используй те же DTO/error codes. Реальная интеграция начинается сразу после G1. Сделай уверенный интерфейс ресторана: партии/сроки, заказ из нескольких блюд, объяснение стоп-листа, hot offers, responsive 390/1440, focus/empty/error. Не выдавай клиентский расчет за серверное подтверждение. Отдельный simulator шлёт только HTTP API synthetic deliveries/orders с ключами идемпотентности и настраиваемой скоростью; без SQL/доменных правил. SSE/reconnect или честный fallback.

После каждого небольшого результата typecheck/test/build и commit, status commit в progress/web, push. Скриншоты desktop/mobile после появления UI, без личных данных. Автоматизируй всё, что возможно; человеку только конкретный внешний доступ. В конце перечисли SHA, проверку, снимок, next_step.
```

## Продолжение на другом ноутбуке

```text
Продолжи codex/web по docs/TEAM_WORKFLOW.md и docs/progress/web.md. Fetch/pull --ff-only, проверь remote owner и contract SHA, захвати очередь при HANDOFF/FREE или после подтвержденной остановки старого агента. Восстанови текущую UI/симуляторную задачу по опубликованным commits; не по памяти старого чата. Работай по T002/T007/T016/T020/T023/T030/T036/T043/T046/T048/T049/T059 и зависимостям. После каждого проверяемого подшага commit+progress+push, не оставляй большой незакоммиченный блок.
```

## Передача

```text
Подготовь HANDOFF codex/web: останови только локальный demo producer, сохрани code commit, typecheck/build/Playwright status, контрактную версию, screenshots/evidence и точный next_step. Поставь HANDOFF в docs/progress/web.md, отдельный commit/push, сверь remote SHA. Секретный demo access code не записывай.
```
