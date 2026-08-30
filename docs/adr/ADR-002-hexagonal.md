# ADR-002: Hexagonal Architecture (Ports & Adapters)

## Status: Accepted

## Context
Each microservice must keep its domain independent of infrastructure (Spring, Hibernate, Kafka, HTTP). The domain should be testable without booting Spring. We must avoid coupling business rules to frameworks so the core logic can be evolved and tested in isolation.

## Decision
Every service follows **Hexagonal Architecture** (Ports & Adapters) with dependencies pointing inward:

```
Adapters → Application → Domain
```

- **Domain layer:** represents business facts and invariants in infrastructure-free Java (model, services, exceptions, ports that are truly domain-level).
- **Application layer:** use cases orchestrate domain behavior; defines *inbound ports* (use case interfaces) and *outbound ports* (interfaces for persistence, messaging, external calls).
- **Adapters layer:**
  - *inbound adapters* — HTTP controllers, Kafka consumers
  - *outbound adapters* — JPA persistence, Kafka producers

The controller depends on an input port (use case interface), never on business logic. Persistence uses JPA entities (`*JpaEntity`) mapped from/into domain objects by a mapper, keeping Hibernate out of the domain.

## Alternatives
- **Classic Layered (Controller → Service → Repository)** — couples the core to frameworks and often leaks persistence entities into the API layer.
- **Clean Architecture** — very similar goals; hexagonal chosen for its simpler terminology and adapter emphasis.
- **Feature-based with no ports** — simpler but makes dependency inversion harder to demonstrate.

## Consequences
- **Positive:** Domain is framework-free and unit-testable without Spring; infrastructure is swappable; explicit boundaries.
- **Negative:** More classes (entities + mappers + ports + use cases), some boilerplate; mapping overhead between layers.
- **Decision:** Keep mappings in adapters only; do not let ports or domain leak framework types.

## Related
- [ADR-001: Microservices Architecture](./ADR-001-microservices.md)
- [Skill: Hexagonal Architecture](../skills-hexagonal.md)
