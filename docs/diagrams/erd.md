# ER-модель склада

```mermaid
erDiagram
    INGREDIENT ||--o{ BATCH : has
    DELIVERY ||--|{ BATCH : contains
    BATCH ||--o{ STOCK_MOVEMENT : logs
    DISH ||--|{ RECIPE_VERSION : versions
    RECIPE_VERSION ||--|{ RECIPE_ITEM : contains
    INGREDIENT ||--o{ RECIPE_ITEM : required
    CUSTOMER_ORDER ||--|{ ORDER_ITEM : contains
    DISH ||--o{ ORDER_ITEM : requested
    RECIPE_VERSION ||--o{ ORDER_ITEM : snapshot
    CUSTOMER_ORDER ||--o{ CONSUMPTION : makes
    BATCH ||--o{ CONSUMPTION : consumed_from
    INGREDIENT ||--o{ CONSUMPTION : ingredient
    CUSTOMER_ORDER ||--o{ OUTBOX_EVENT : publishes
    DELIVERY ||--o{ OUTBOX_EVENT : publishes

    INGREDIENT {
      uuid id PK
      string name UK
      string base_unit
      decimal reorder_point
    }
    DELIVERY {
      uuid id PK
      string supplier
      string delivery_number
      datetime received_at
    }
    BATCH {
      uuid id PK
      uuid delivery_id FK
      uuid ingredient_id FK
      decimal received_qty
      decimal remaining_qty
      date expires_on
      string expiry_source
    }
    STOCK_MOVEMENT {
      uuid id PK
      uuid batch_id FK
      decimal delta
      string kind
      datetime created_at
    }
    DISH {
      uuid id PK
      string name UK
      uuid current_recipe_version_id FK
    }
    RECIPE_VERSION {
      uuid id PK
      uuid dish_id FK
      int version
    }
    RECIPE_ITEM {
      uuid recipe_version_id FK
      uuid ingredient_id FK
      decimal qty_per_serving
    }
    CUSTOMER_ORDER {
      uuid id PK
      string source
      string external_id
      string status
      datetime created_at
    }
    ORDER_ITEM {
      uuid id PK
      uuid order_id FK
      uuid recipe_version_id FK
      int servings
    }
    CONSUMPTION {
      uuid id PK
      uuid order_id FK
      uuid batch_id FK
      decimal quantity
    }
    IDEMPOTENCY_RECORD {
      string scope PK
      string key PK
      string request_hash
      int status_code
    }
    OUTBOX_EVENT {
      bigint id PK
      string type
      datetime created_at
    }
```

`IDEMPOTENCY_RECORD` относится к команде по `(scope,key)` и намеренно не связан с заказом FK: один механизм обслуживает заказ и поставку. Это не отступление от модели. Поля и ограничения для миграции: [data-model](../../specs/001-restaurant-inventory/data-model.md).
