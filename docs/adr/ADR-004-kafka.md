# ADR-004: Kafka for Asynchronous Communication

## Status: Accepted

## Context
Some workflows (e.g., order creation → inventory reservation → shipping → notification) are naturally asynchronous and involve multiple services. Using synchronous REST for all of them would couple services tightly, create long call chains, and limit resilience. We need decoupled, eventually-consistent processes.

## Decision
Use **Apache Kafka** as the message broker for asynchronous, event-driven communication between services. Each service publishes domain/integration events and consumes events from other services.

Explicit event contracts carry:
```json
{
  "eventId": "uuid",
  "eventType": "OrderCreated",
  "occurredAt": "ISO-8601",
  "correlationId": "uuid",
  "payload": { ... }
}
```

Key design points:
- Consumers are **idempotent** (dedupe by `eventId`).
- Kafka is used *only where* eventual consistency / decoupling is beneficial — not as an artificial mandate. Synchronous REST remains for operations needing immediate responses (e.g., fetching a product, creating an order).
- Topics are segregated per bounded context (`order-events`, `inventory-events`, `shipment-events`, `notification-events`).

## Alternatives
- **Synchronous REST for everything** — simpler but creates tight coupling and synchronous failure propagation.
- **RabbitMQ / ActiveMQ** — viable brokers, but Kafka's log-based model, replay, and at-least-once semantics are strong fits for event streaming and align with modern distributed-system practice.
- **Database-backed outbox without broker** — used later (ADR-008) rather than as a replacement for Kafka.

## Consequences
- **Positive:** Loose coupling, resilience (services can be down temporarily), scalability of consumers.
- **Negative:** Eventual consistency (no strong transactional guarantee across services), requires idempotency and messaging error handling (retries, dead-letter).
- **Deferred:** Transactional Outbox pattern (see [ADR-008]) to make Kafka publishing transactional with DB writes.

## Related
- [ADR-001: Microservices Architecture](./ADR-001-microservices.md)
- [ADR-007: Eventual Consistency](./ADR-007-eventual-consistency.md)
- [ADR-008: Transactional Outbox](./ADR-008-transactional-outbox.md)
