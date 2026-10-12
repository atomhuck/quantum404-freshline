# Архитектура

```mermaid
flowchart LR
    J[Жюри / менеджер<br/>браузер] -->|HTTPS| C[Caddy<br/>TLS + routing]
    C --> W[React + TS<br/>Chakra web]
    C -->|/api/v1| A[Java 21 / Spring Boot<br/>API + use cases + security + SSE]
    S[Python demo simulator<br/>отдельный container] -->|HTTP + idempotency key| A
    A --> D[Domain module<br/>FEFO / availability / offer rules]
    A --> P[Persistence module<br/>jOOQ/JDBC + Flyway]
    P --> PG[(PostgreSQL 17<br/>batches / ledger / orders / outbox)]
    A -->|SSE committed events| C
    PG --> B[Backup to private VDS path]
```

Одна транзакционная граница находится внутри Java API и PostgreSQL. Демо-клиент не имеет DB credentials. Браузер не рассчитывает окончательную доступность. Наружу открыты только 80/443; PostgreSQL живёт в приватной сети Compose. В production simulator отключён, demo seed и код доступа запрещены.
