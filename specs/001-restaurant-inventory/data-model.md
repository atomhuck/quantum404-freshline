# Data model and invariants

Полная ER-схема: `docs/diagrams/erd.md`. Типы и уникальности уточняются миграциями; нижеследующие инварианты обязательны.

| Entity | Ключевые поля | Связь и ограничения |
|---|---|---|
| ingredient | id UUID, name, base_unit, reorder_point, active | name уникален в ресторане; `base_unit ∈ {g, ml, pcs}` |
| delivery | id UUID, supplier, delivery_number, received_at, created_at | unique `(supplier, delivery_number)` |
| batch | id UUID, delivery_id, ingredient_id, received_qty, remaining_qty, manufactured_on, shelf_life_days, expires_on, expiry_source | `0 ≤ remaining_qty ≤ received_qty`; одна строка поставки = одна партия |
| dish | id UUID, name, active, current_recipe_version_id | имя уникально; не удалять при истории |
| recipe_version | id UUID, dish_id, version, created_at | unique `(dish_id, version)`; после публикации неизменяема |
| recipe_item | recipe_version_id, ingredient_id, qty_per_serving | positive numeric, unique pair; единица унаследована от ingredient |
| customer_order | id UUID, source, external_id, status, created_at, rejection_reason, request_hash | unique `(source, external_id)`; status accepted/rejected |
| order_item | id UUID, order_id, dish_id, recipe_version_id, servings | positive servings; snapshot рецепта через version |
| consumption | id UUID, order_id, batch_id, ingredient_id, quantity | positive; сумма по ingredient = recipe demand принятого заказа |
| stock_movement | id UUID, batch_id, kind, delta, reference_type, reference_id, created_at | append-only; `received_qty + SUM(delta for non-receipt) = remaining_qty` |
| idempotency_record | scope, key, request_hash, status_code, response_json, created_at | unique `(scope,key)`; hash mismatch = 409 |
| outbox_event | id, type, aggregate_id, payload_json, created_at, published_at | сохраняется в той же транзакции, что команда |

Денежных величин нет. Количества `NUMERIC(18,3)` в базовой единице продукта; `pcs` разрешает только целые. Все FK индексируются. Срок `expires_on` — локальная дата ресторана, целый день включительно. Точное время команды в UTC. `Clock` внедряется для тестов. Любое изменение `remaining_qty` отражено в движении той же транзакции; исправление — отдельное компенсирующее движение, не UPDATE журнала.

Order state: `accepted` либо `rejected`; командный запрос после фиксации не меняет его. Для отклонённого заказа сохраняется результат/дефицит и 0 consumption. Stop-list/stock-risk — вычисляемые представления, а не источник истины. При одинаковом `expires_on` FEFO сортирует по batch ID. При двух заказах блокировать ingredient rows в порядке UUID, затем batch rows в порядке `(expires_on,id)`.
