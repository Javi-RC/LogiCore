# AGENTS.md — LogiCore

Guidance for AI agents working in this repository.

## Project Overview

LogiCore is a distributed logistics management platform (portfolio project) built with Java/Spring, microservices, hexagonal architecture, Kafka, PostgreSQL, and Docker.

## Build & Test Commands (run from repo root `C:\Users\javir\OneDrive\Documentos\logicore`)

Preferred way to get compiler/test feedback (LSP is intentionally disabled; see `docs/DECISIONS.md` D-001):

```bash
# Build all modules (compile + package, skip tests)
mvn -q clean install -DskipTests

# Compile all modules (fast feedback)
mvn -q compile

# Run all tests (unit + integration). Integration tests use Testcontainers -> needs Docker running.
mvn -q verify

# Run tests for a single module
mvn -q -pl user-service test
mvn -q -pl user-service verify

# Formatting/checkstyle (if configured)
mvn -q checkstyle:check
```

### Important notes
- **Integration tests require Docker** (Testcontainers spins up PostgreSQL/Kafka). If Docker is not running, run only unit tests:
  ```bash
  mvn -q test -DskipITs
  ```
- Before every commit / PR, run `mvn -q clean verify` and ensure it passes.
- Never modify already-applied Flyway migrations (`V*__*.sql`). Add new `V<N+1>__*.sql` files.

## Architecture Conventions

Each microservice follows **Hexagonal Architecture** (see `docs/adr/ADR-002-hexagonal.md`):

- `domain/` — framework-free business logic (models, value objects, services, exceptions).
  - **Must NOT import** Spring, Hibernate, Kafka, Web, HTTP classes.
- `application/` — inbound/outbound port interfaces (`port/in`, `port/out`) and use-case services.
- `adapter/in/` — HTTP controllers, Kafka consumers.
- `adapter/out/` — JPA persistence (`*JpaEntity`, mappers, repositories), Kafka producers.
- `config/` — Spring configuration/wiring (composition root).

Dependencies point inward: `Adapters → Application → Domain`. Domain is testable without Spring.

### Naming
- Persistence entities: `XxxJpaEntity`
- Persistence mappers: `XxxPersistenceMapper`
- Repositories (Spring Data): `XxxJpaRepository`
- Use-case interfaces (inbound ports): `XxxUseCase`
- Application services: `XxxApplicationService`
- Outbound port interfaces: `XxxRepository`, `XxxPublisher`, etc.
- REST DTOs: `XxxRequest`, `XxxResponse`
- Command objects: `XxxCommand`

## Tech Stack
- Java 17, Spring Boot 3.2.x, Maven (multi-module)
- Spring Web, Spring Data JPA / Hibernate, Bean Validation
- PostgreSQL, Flyway
- Spring Cloud Gateway (API Gateway)
- Kafka (Spring for Kafka), Testcontainers
- JUnit 5, Mockito, Spring Security + JWT
- Docker / Docker Compose

## Module Layout
```
logicore/
├── pom.xml                    (parent pom, dependency management)
├── api-gateway/
├── user-service/
├── product-service/
├── order-service/
├── inventory-service/
├── shipping-service/
├── notification-service/
├── shared/common-dto/         (event contracts only — no entities/domain)
├── docker-compose.yml
└── docs/
```

## Environment / Config
- Configuration via `application.yml`; DB/Kafka credentials via **environment variables** (`.env`, not committed).
- Never commit secrets. Use env vars.
- Services read config like `DB_URL`, `DB_USER`, `DB_PASSWORD`, `KAFKA_BOOTSTRAP_SERVERS`.

## Testing
- **Unit tests**: domain rules, use cases with Mockito, no Spring context.
- **Integration tests**: `@SpringBootTest` + Testcontainers (PostgreSQL, Kafka).
- **Concurrency tests**: inventory optimistic locking (two simultaneous reservations).
- Coverage targets: domain >90%, application >80%, adapters >70%.

## Git
- Commit messages: concise, imperative mood, matching repo style.
- Only commit when explicitly asked.
