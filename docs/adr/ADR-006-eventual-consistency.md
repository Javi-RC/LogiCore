# ADR-006: Eventual Consistency via Events

## Status: Accepted

## Context
Order creation spans multiple services (order, inventory, shipping, notification) holding data in separate databases. A distributed transaction (e.g., 2PC across PostgreSQL instances) is heavyweight, fragile, and unnecessary for this domain. We need a pattern where the system converges to a consistent state without strong coupling.

## Decision
Use **eventual consistency** driven by domain/integration events over Kafka. The order lifecycle converges through a saga-style flow:

```
1. POST /orders → Order Service persists Order (PENDING) and publishes OrderCreated
2. Inventory Service consumes OrderCreated:
     - if stock available → reserve → publish StockReserved
     - else → publish StockReservationFailed
3. Order Service consumes StockReserved → Order becomes CONFIRMED
        (or StockReservationFailed → Order becomes FAILED)
4. Shipping Service consumes OrderConfirmed → creates Shipment → ShipmentCreated
5. Notification Service consumes all events → records notifications
```

No distributed transaction spans services. Each service commits its own transaction and communicates state transitions by event. There is no `X-Transaction` spanning PostgreSQL instances.

## Alternatives
- **Distributed 2PC / XA** — strong consistency but fragile, slow, and rarely appropriate across services.
- **Synchronous orchestration (always REST)** — faster to build but couples services and fails the decoupling objective.
- **Choreography vs Orchestration:** we use a mix of choreography (consumers react to events) and light orchestration within Order Service's state machine.

## Consequences
- **Positive:** Loose coupling, resilience, matches the architecture goals.
- **Negative:** Intermediate states are visible (PENDING/FAILED); consumers must be idempotent; requires compensating actions (e.g., releasing reserved stock if the order ultimately fails).
- **Compensation:** If a reservation succeeds but the order cannot be confirmed, an event (`StockRelease`) is used to free the reserved stock.

## Related
- [ADR-004: Kafka](./ADR-004-kafka.md)
- [ADR-007: Eventual Consistency](./ADR-007-eventual-consistency.md)
