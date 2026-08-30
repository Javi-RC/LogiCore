# ADR-005: Optimistic Locking in Inventory

## Status: Accepted

## Context
The Inventory service must prevent over-reserving stock under concurrent requests. Two simultaneous reservation requests for the same product must not both succeed when only one unit is available.

## Decision
Use **optimistic locking** via a `@Version` column on the inventory entity:

```java
@Entity
@Table(name = "inventory_items")
public class InventoryItemJpaEntity {
    // ...
    @Version
    private Long version;
    // ...
}
```

The application reads the current version, performs the reservation, and attempts the update. If the version changed between read and write, Hibernate throws `OptimisticLockException`, which we catch and translate into an appropriate business error (e.g., `StockUnavailableException`). The operation can be retried or reported as a stock-conflict.

This approach is chosen over pessimistic locking because the conflict window is short and contention is expected to be low to moderate; it also demonstrates a clean, ID-level concurrency technique.

## Alternatives
- **Pessimistic locking (`SELECT ... FOR UPDATE`)** — guarantees serialized access but holds locks and can degrade throughput; more complex deadlock handling.
- **Single-writer / queue per product** — possible but adds infrastructure complexity.
- **LAST WRITE WINS (no locking)** — would allow double-reservation and corrupt stock.

## Consequences
- **Positive:** Simple, no long-held locks; conflicts are user-visible and catchable; demonstrates a widely-used distributed concurrency pattern.
- **Negative:** Under high contention, clients receive conflicts and may need retry logic; requires the UI/API to handle 409-style responses.
- **Testing:** Concurrency tests (with Testcontainers) must assert that two simultaneous reservations cannot both succeed for limited stock.

## Related
- [ADR-006: Events for Reservation Failure](./ADR-006-eventual-consistency.md) (outcome of failed reservation)
