# ADR-007: Transactional Outbox Pattern

## Status: Proposed (planned for development after base flow is stable)

## Context
When a service both writes to a database and publishes a Kafka event (e.g., Order Service saves an Order then publishes OrderCreated), these two operations can diverge:

```
DB transaction:  save order           → OK
Kafka publish:   publish OrderCreated → FAIL
```

Result: the order exists in the database, but no event is emitted, so other services never react — the system is left inconsistent. Publishing inside the DB transaction, or after it, still risks partial failure or data loss on producer restart.

## Decision (planned)
Implement the **Transactional Outbox Pattern**:

```
Business Transaction
   │
   ├── persist Aggregate (e.g., Order)
   │
   └── persist OutboxEvent (same transaction)
                    │
                    ▼
              Outbox Publisher (relay)
                    │
                    ▼
                   Kafka
```

- Writing the aggregate and the outbox record happens in the **same local transaction** — this guarantees that "if the aggregate changed, its event exists".
- A separate **outbox relay** reads pending `OutboxEvent` rows, publishes them to Kafka, and marks them as published (with idempotent dedupe by `eventId`).
- Database credit/dedup `published` flag; periodic polling or CDC-based relay.

This removes the need to publish Kafka inside the DB transaction and provides at-least-once delivery guarantees at the business level.

## Alternatives
- **Publish inside DB transaction (pattern avoided)** — fragile, may publish events for rolled-back changes.
- **Publish after commit** — can lose events on crash between commit and publish.
- **CDC (Debezium) on outbox table** — robust but adds infrastructure; deferred.
- **Two-phase commit with broker** — Kafka's lack of full XA (without extra effort) makes this impractical.

## Consequences (when implemented)
- **Positive:** Stronger delivery guarantees; producers become resilient to broker outages; consumers get at-least-once delivery which, combined with idempotency, gives effective exactly-once business semantics.
- **Negative:** Extra table (outbox), relay component, and a small latency between commit and publish; more complexity.
- **Timing:** Explicitly deferred until the basic event-driven flow is proven stable (per project prioritization).

## Related
- [ADR-004: Kafka](./ADR-004-kafka.md)
- [ADR-006: Eventual Consistency](./ADR-006-eventual-consistency.md)
