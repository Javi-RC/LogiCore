# ADR-008: JWT Authentication at the API Gateway

## Status: Accepted

## Context
Every service exposes HTTP endpoints (order, inventory, shipping, notification, product). Repeating token validation, secret management, and credential handling in each service is error-prone and bloats the codebase. We need a single trusted enforcement point for authentication, with services kept simple.

## Decision
Authentication is enforced **only at the API Gateway** using **JWT (HS256)**:

- **user-service** is the token issuer: `POST /api/auth/register` creates users (BCrypt-hashed), `POST /api/auth/login` verifies credentials and issues a signed JWT containing `sub` (user id), `email`, and `roles`.
- **api-gateway** validates every incoming JWT for all non-public routes (`/api/auth/**` and `/actuator/**` are public). A reactive `GlobalFilter` parses the token with the shared secret, strips the `Authorization` header, and injects verified identity headers before forwarding:
  - `X-User-Id`, `X-User-Email`, `X-User-Roles`
- **Downstream services trust the gateway** (trusted-network model): they read identity from the `X-User-*` headers and do not re-validate tokens. This keeps service code free of JWT/security concerns.

## Alternatives
- **Per-service Spring Security + filter chain** — redundant token parsing in every service; more to maintain and more attack surface for secret handling.
- **OAuth2 / OIDC with an external identity provider** — heavier setup; not justified for this project's scope.
- **Service-side role enforcement** — deferred; roles currently travel as claims/headers. If needed later, services can apply method security on the `X-User-Roles` header.

## Consequences
- **Positive:** One place to validate credentials; services stay framework-light; CORS centralized at the gateway; JWT is stateless (no session store).
- **Negative:** The shared HS256 secret is a single point of failure — it must be managed via the `JWT_SECRET` environment variable and is deployed identically to issuer and gateway.
- **Negative:** If a service is reachable outside the gateway, it has no inherent protection. Acceptable for this architecture; documented as trusted-network.

## Related
- [ADR-002: Hexagonal Architecture](./ADR-002-hexagonal.md)
- [ADR-006: Eventual Consistency](./ADR-006-eventual-consistency.md)