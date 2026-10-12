# Один промпт для каждого участника

Участник клонирует `https://github.com/atomhuck/quantum404-freshline`, открывает папку в Codex/AI-редакторе и вставляет **старт** из своей роли. При смене ноутбука вставляет **продолжение**; перед лимитом — **передача**. Агент читает [план роли](../README.md#прогресс-и-передача--прямые-ссылки), её `docs/progress/` и `docs/handoffs/`, выбирает задачу и ведёт Git. Никаких токенов в промпт.

| Роль | Промпты |
|---|---|
| Тимлид / Java API / интеграция / deploy | [backend-lead.md](backend-lead.md) |
| Java order engine / FEFO | [backend-orders.md](backend-orders.md) |
| Backend fullstack / web / simulator | [backend-fullstack.md](backend-fullstack.md) |
| Data engineer / SQL / migrations | [data-engineer.md](data-engineer.md) |
| QA engineer | [qa.md](qa.md) |

Все промпты используют [единый протокол передачи](../docs/TEAM_WORKFLOW.md). Если агент оборвался без handoff, новый ноутбук применяет раздел «Внезапное прерывание»; человек только подтверждает остановку предыдущего активного агента. Для каждого участника нужен GitHub write-доступ; он не переходит автоматически между аккаунтами Codex.
