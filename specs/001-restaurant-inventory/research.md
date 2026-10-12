# Research and decisions

| Decision | Rationale | Alternative |
|---|---|---|
| Java 21 + Spring Boot 3.5.16 | зрелая стабильная линия, Actuator/Security/Flyway, Java совместима; patch закрепляется в pom | Boot 4.1.1 новее, но миграционный риск за 5 дней не даёт пользы |
| Java modular monolith | одна ACID-транзакция для проверки и списания, меньше сетевых отказов | Java → Python HTTP → DB полезно при независимом data-домене, здесь добавляет distributed consistency |
| PostgreSQL 17 + row locks | стабильные `FOR UPDATE`, CHECK, UNIQUE, NUMERIC; порядок lock снижает deadlock | Redis lock не заменяет транзакционную целостность |
| Flyway + SQL/jOOQ | прозрачные миграции и FEFO-запросы для дата-инженера | ORM-only скрывает сложный выбор партий |
| React/TS/Vite/Chakra | прямо соответствует рекомендуемому фронт-стеку презентации; быстрый дизайн с токенами | Next.js SSR не нужен для внутренней панели |
| Caddy + Compose | автоматический HTTPS и один сервер, воспроизводимость | Kubernetes создаёт лишнюю эксплуатационную нагрузку |

Проверенные первичные источники: [Spring Boot stable releases](https://docs.spring.io/spring-boot/), [PostgreSQL locking](https://www.postgresql.org/docs/current/explicit-locking.html), [Chakra installation](https://chakra-ui.com/docs/get-started/installation), [Spec Kit installation](https://github.com/github/spec-kit/blob/main/docs/installation.md). Выбор версии сам по себе не гарантирует совместимость jOOQ/springdoc: агент G0 проверяет BOM и сборку, записывает фактические номера и lock files.

Опыт `max-fleet` и доставленные экспорты чатов: Git синхронизирует файлы, не историю агента; данные/секреты Docker volumes не переезжают между ноутбуками; отдельная ветка и статус для DE/QA; mock и опубликованный HTTPS не равны проверенному пользовательскому сценарию. Конкретный прежний Go/Python/MAX-стек в новое решение не переносится. Подробности — `docs/LESSONS_FROM_MAX_FLEET.md`.
