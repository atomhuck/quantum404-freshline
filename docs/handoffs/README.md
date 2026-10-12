# Журналы передачи

Здесь хранится **история** передачи каждой полосы. Оперативное состояние и `next_step` находятся в [docs/progress](../progress/lead.md), план задач — в [docs/lanes](../lanes/lead.md). До начала реализации записи о передаче отсутствуют: не создавать фиктивных `PASS` или SHA.

| Полоса | Журнал | Текущий статус | План |
|---|---|---|---|
| lead | [lead.md](lead.md) | [progress](../progress/lead.md) | [план](../lanes/lead.md) |
| orders | [orders.md](orders.md) | [progress](../progress/orders.md) | [план](../lanes/orders.md) |
| web | [web.md](web.md) | [progress](../progress/web.md) | [план](../lanes/web.md) |
| data | [data.md](data.md) | [progress](../progress/data.md) | [план](../lanes/data.md) |
| qa | [qa.md](qa.md) | [progress](../progress/qa.md) | [план](../lanes/qa.md) |

## Протокол одной записи

Добавлять запись при плановой передаче, аварийном takeover и отказе передачи из-за несошедшегося remote. Новую запись помещать **сверху**, под заголовком «Записи», не переписывая прежние. Вместе с ней обновить `state` в соответствующем `docs/progress/<lane>.md`; коммитить оба файла одним status-коммитом **после** code commit. В журнале указывать `last_verified_code_commit`; SHA самого status-коммита в его содержимое не вписывать. После push сверить remote HEAD и передать его следующему участнику в сообщении.

```text
### YYYY-MM-DD HH:MM UTC — HANDOFF | TAKEOVER | BLOCKED — Txxx
- От кого → кому: имя/агент → следующий владелец (или unassigned)
- Ветка и состояние: codex/<lane>, HANDOFF|ACTIVE|INTERRUPTED
- Последний проверенный code SHA и contract/port/migration SHA: ...
- Проверено: команда, exit code, окружение/seed, PASS|FAIL|NOT RUN
- Сделано: конкретные файлы/поведение; PR/issue, если есть
- Осталось/риски: WIP, ошибка, блокер, кто исправляет
- Следующий шаг: точный файл + действие + первая проверка
- Внешние зависимости: только имена переменных и ID из HUMAN_REQUIRED, без значений секретов
```

`HANDOFF` позволяет второму ноутбуку сделать claim без нового разрешения. При `ACTIVE` и внезапном обрыве второй ждёт подтверждения человека, что прежний агент остановлен; время heartbeat само по себе не даёт права захвата. Подробнее: [TEAM_WORKFLOW](../TEAM_WORKFLOW.md#внезапное-прерывание-и-takeover).
