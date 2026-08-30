# ADR-001: Microservices Architecture

## Status: Accepted

## Context
We need to build a portfolio project that demonstrates distributed systems knowledge in Java/Spring. The problem domain (logistics platform) involves distinct concerns: identity, catalog, ordering, inventory, shipping, and notification. A monolith would not demonstrate the engineering capabilities required (decoupling, eventual consistency, independent scalability).

## Decision
Decompose the system into autonomous microservices, one per bounded context:

- **API Gateway** — external entry point, routing, JWT validation, correlation ID
- **User Service** — identity and authentication
- **Product Service** — catalog management
- **Order Service** — order lifecycle (core flow)
- **Inventory Service** — stock management (optimistic locking)
- **Shipping Service** — shipment simulation
- **Notification Service** — event-driven notifications

Each service owns its own database, is independently deployable, and communicates via synchronous REST (where an immediate response is required) or asynchronous Kafka events (for disconnected, eventually-consistent processes).

## Alternatives
- **Monolith** — simpler, but does not demonstrate microservices capabilities.
- **Modular monolith** — better internal boundaries but avoids the distribution complexity we want to prove.
- **Serverless/Function-based** — not aligned with learning goals for Spring ecosystem.

## Consequences
- **Positive:** Independent testing/deployment, clear boundaries, demonstrates distributed systems concepts (consistency, decoupling, resilience).
- **Negative:** Operational complexity (multiple containers, orchestration), eventual consistency requires careful handling, more moving parts.
- **Trade-off:** Higher upfront complexity justified by the explicit portfolio goal of demonstrating these skills.

## Related
- [ADR-002: Hexagonal Architecture](./ADR-002-hexagonal.md)
- [ADR-003: Database per Service](./ADR-003-database-per-service.md)
