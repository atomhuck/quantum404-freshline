# Развёртывание и сдача

Пока это исполнимый план, не отчёт о запущенном сервере. Владелец VDS и реальные SSH/hostname уточняются через `HUMAN_REQUIRED.md`. Рекомендуемый стенд: Linux, Docker Engine + Compose, минимум 2 vCPU/4 GB RAM/40 GB свободного диска; агент измеряет фактические ресурсы и не утверждает нагрузочную цель до замера.

## Контуры

- Local: `compose.yaml`, seed только synthetic, private DB volume, API/web/Caddy; demo simulator через профиль `demo` и управляемый start/stop.
- Public demo: тот же build, отдельный project name/volumes, synthetic seed и demo session; HTTPS 80/443, rate limit, непубличный PostgreSQL, резервная копия.
- Production pilot: `DEMO_MODE=false`, simulator/seed недоступны, реальные пользователи/политики требуют отдельной настройки; не заявляется на хакатоне.

## Агентский deploy runbook

1. `git fetch` и зафиксировать release candidate SHA; собрать на чистом checkout, проверить `docker compose config --quiet` и CI.
2. Проверить VDS OS, ресурсы, свободные порты, существующий proxy/стек и DNS. Не выключать чужие сервисы. Создать отдельный каталог и Compose project/volumes.
3. Положить секреты через приватный файл с правами владельца, без вывода в терминал/чат и вне Git; `.env.example` содержит только имена. Отказ запуска при пустых секретах или DEMO_MODE с production data.
4. Поднять PostgreSQL → Flyway → API → web/gateway; readiness только после миграции. `docker compose --profile demo up -d --build --wait` — целевая команда после реализации.
5. Настроить Caddy с HTTPS по hostname; проверить сертификат и внешний GET с другого сетевого пути, не только localhost. Не обходить TLS-warning.
6. Выполнить smoke: доставка, два срока, заказ FEFO, дубль, стоп-лист, hot offer, simulator/SSE. Проверить логи без секретов, состояние volumes и ручной браузерный сценарий.
7. `pg_dump` + проверка восстановления в отдельную пустую БД/volume, сверить counts и ledger invariant. Сохранить private backup на VDS вне web root; чистый скрипт/расписание после измерения.
8. Записать image digests, DB migration head, contract version, Git SHA, дату проверки, публичный URL и QA verdict в README. Создать tag сдачи и не подменять его после подачи.

## Откат

Сохранить backup до несовместимой миграции. При неудачном release вернуть прежние image digest/commit, не удаляя volume. Если миграция несовместима, восстановить backup в отдельный volume и переключить после сверки. Никогда не выполнять `down -v` на общем demo. Не обещать безопасный откат без реальной пробы восстановления.

## Проверка жюри

README содержит ссылку на GitHub и HTTPS-продукт, demo-доступ, 5-минутный сценарий, фактический статус тестов и известные ограничения. Нужна настоящая загрузка страницы и операция через браузер, а не только `/health`. Экспертная сессия не является зависимостью запуска.
