# LogiCore — Decision Log

This file records every technical decision made during implementation, including rationale, alternatives considered, and consequences.

## Format

Each entry:
- **Date:** When the decision was made
- **Context:** The situation/problem
- **Decision:** What was chosen
- **Alternatives:** What else was considered
- **Consequences:** Impact of the decision

---

## D-001: Java Version

- **Date:** 2026-08-30
- **Context:** Project requires a Java version. OpenCode's jdtls LSP server requires Java 21+, while the system only has Java 17 installed.
- **Decision:** Use **Java 17** for the project (LTS, broad ecosystem support, matches spec recommendation). Configure OpenCode to **skip LSP** (`jdtls`) and rely on **Maven compile** and tests for diagnostics instead.
- **Alternatives:**
  - Java 21 for the whole project (latest LTS)
  - Install JDK 21 separately just for LSP
- **Consequences:** No interactive diagnostics from IDE LSP in the agent loop; feedback comes from `mvn compile` / `mvn test`. Java 17 offers the widest JVM and dependency compatibility.

## D-002: Spring Boot Version

- **Date:** 2026-08-30
- **Context:** Need a stable Spring Boot version with Java 17 compatibility.
- **Decision:** Use **Spring Boot 3.2.x** (latest stable 3.2 release).
- **Alternatives:** Spring Boot 3.3/3.4/3.5, 2.7 (EOL)
- **Consequences:** LTS support, works with Java 17, mature ecosystem.

## D-003: API Gateway Technology

- **Date:** 2026-08-30
- **Context:** Need an entry point for external traffic, routing, JWT validation, correlation ID propagation.
- **Decision:** Use **Spring Cloud Gateway** (reactive, WebFlux-based).
- **Alternatives:** Kong, Traefik, Spring MVC proxy, a load balancer
- **Consequences:** Native Spring integration, programmatic route/filter configuration, but requires WebFlux (not Spring MVC) semantics.

## D-004: Skills Installed

- **Date:** 2026-08-30
- **Context:** User requested installation of relevant skills.
- **Decision:** Installed globally:
  - `github/awesome-copilot@java-springboot` (19.6K installs) — Spring Boot best practices
  - `affaan-m/ecc@hexagonal-architecture` (7.4K installs) — Ports & Adapters guidance
  - `giuseppe-trisciuoglio/developer-kit@spring-boot-test-patterns` (2.9K installs) — test patterns
- **Alternatives:** Various other skills with lower install counts / less relevance
- **Consequences:** Agent has curated guidance for Spring Boot, hexagonal architecture, and testing.

## D-005: Packaging structure

- **Date:** 2026-08-30
- **Context:** Multi-module Maven with hexagonal per-service layout.
- **Decision:** Root POM (packaging `pom`) with child modules per service. Each service organizes code as `domain` / `application` / `adapter` per hexagonal architecture.
- **Alternatives:** Single monolithic module, feature-based single package.
- **Consequences:** Clear boundaries, isolation, but each module needs its own Spring Boot app config.
