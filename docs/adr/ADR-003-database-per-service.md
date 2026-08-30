# ADR-003: Database per Service

## Status: Accepted

## Context
Microservices must be autonomous. If they share a database, changes to schema ripple across services, and it becomes impossible to scale or deploy them independently.

## Decision
Each microservice owns its own PostgreSQL database and schema migrations (Flyway).

```
user_db          — User Service
product_db       — Product Service
order_db         — Order Service
inventory_db     — Inventory Service
shipping_db      — Shipping Service
notification_db  — Notification Service
```

There is **no shared database** between services. Services never access another service's tables directly; they communicate via REST or Kafka.

## Alternatives
- **Single shared database** — simpler but breaks service autonomy and couples schemas.
- **Event sourcing / CQRS** — powerful but adds complexity beyond current scope.

## Consequences
- **Positive:** Strong isolation, independent evolution and scaling, aligns with microservices goals.
- **Negative:** No cross-service SQL joins; consistency must be managed via REST/events (eventual consistency).
- **Tooling:** Each DB container in Docker Compose uses its own volume and init script.

## Related
- [ADR-001: Microservices Architecture](./ADR-001-microservices.md)
- [ADR-004: Kafka for Asynchronous Communication](./ADR-004-kafka.md)
