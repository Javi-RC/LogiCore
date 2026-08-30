# LogiCore — Plan de Implementación

## Visión General

LogiCore es una plataforma distribuida de gestión logística construida para demostrar conocimientos de Ingeniería de Software en el ecosistema Java/Spring.

---

## Fase 1: Foundation (Semanas 1-2)

### 1.1 Estructura Maven Multi-Module

```
logicore/
├── pom.xml (parent)
├── api-gateway/
├── user-service/
├── product-service/
├── order-service/
├── inventory-service/
├── shipping-service/
├── notification-service/
├── shared/
│   └── common-dto/ (solo DTOs de eventos, NO entidades)
├── docker-compose.yml
└── docs/
```

### 1.2 Estructura Hexagonal Base

Cada microservicio seguirá:

```
service-name/
├── src/main/java/com/logicore/servicename/
│   ├── domain/
│   │   ├── model/
│   │   ├── service/
│   │   ├── exception/
│   │   └── port/
│   │       ├── in/
│   │       └── out/
│   ├── application/
│   │   └── service/
│   ├── adapter/
│   │   ├── in/
│   │   │   ├── web/
│   │   │   └── messaging/
│   │   └── out/
│   │       ├── persistence/
│   │       └── messaging/
│   └── config/
├── src/main/resources/
│   ├── application.yml
│   └── db/migration/
└── src/test/java/
```

### 1.3 Infraestructura Docker Compose

Services:
- PostgreSQL (multi-db via init scripts)
- Kafka + Zookeeper
- Kafka UI

### 1.4 Shared Common (Event Contracts)

- EventDTO base con eventId, eventType, occurredAt, correlationId
- Eventos: OrderCreated, OrderConfirmed, etc.

---

## Fase 2: User Service + Product Service (Semana 2-3)

### 2.1 User Service

Modelo:
- UserId (Value Object)
- User
- UserRole (enum: CUSTOMER, ADMIN)

API:
- POST /api/auth/register
- POST /api/auth/login
- GET /api/users/{id}

### 2.2 Product Service

Modelo:
- ProductId (Value Object)
- Sku (Value Object)
- Money (Value Object - BigDecimal)
- Product

API:
- GET /api/products
- GET /api/products/{id}
- POST /api/products (ADMIN)
- PUT /api/products/{id} (ADMIN)
- DELETE /api/products/{id} (ADMIN)

---

## Fase 3: Order Service (Semana 3-4)

### 3.1 Modelo de Dominio

Value Objects: OrderId, CustomerId, OrderItem

Aggregate Root: Order
- id: OrderId
- customerId: CustomerId
- items: List<OrderItem>
- total: Money
- status: OrderStatus (PENDING, CONFIRMED, CANCELLED, FAILED)
- createdAt: LocalDateTime
- updatedAt: LocalDateTime

### 3.2 API

- POST /api/orders
- GET /api/orders/{id}
- GET /api/orders
- POST /api/orders/{id}/cancel

---

## Fase 4: Inventory Service (Semana 4-5)

### 4.1 Modelo

InventoryItem:
- id: UUID
- productId: UUID
- availableQuantity: int
- reservedQuantity: int
- version: Long (@Version - Optimistic Locking)

### 4.2 Operaciones

- reserveStock(productId, quantity)
- releaseStock(productId, quantity)
- confirmReservation(productId, quantity)

---

## Fase 5: Kafka + Event-Driven (Semana 5-6)

### 5.1 Topics

- order-events
- inventory-events
- shipment-events
- notification-events

### 5.2 Flujo OrderCreated → StockReserved/Failed

```
Order Service → OrderCreated → Kafka → Inventory Service
                                       ├── StockReserved
                                       └── StockReservationFailed
                                   ↓
                               Order Service
                               ├── StockReserved → CONFIRMED
                               └── StockReservationFailed → FAILED
```

---

## Fase 6: Shipping Service (Semana 6)

### 6.1 Modelo

Shipment:
- id: UUID
- orderId: UUID
- status: ShipmentStatus
- createdAt: LocalDateTime
- updatedAt: LocalDateTime

### 6.2 API

- GET /api/shipments/{id}

---

## Fase 7: Notification Service (Semana 7)

Consume todos los eventos y registra notificaciones.

---

## Fase 8: Security (Semana 7-8)

### 8.1 JWT Implementation

- User Service genera JWT
- API Gateway valida JWT
- Propaga userId y roles

### 8.2 Roles

- CUSTOMER: crear pedidos, consultar
- ADMIN: gestionar productos, inventario

---

## Fase 9: Quality + Testing (Semana 8-9)

### 9.1 Unit Tests

- Domain logic (sin Spring)
- Use cases con Mockito
- Value objects

### 9.2 Integration Tests

Testcontainers para PostgreSQL y Kafka

### 9.3 Coverage Target

- Domain: >90%
- Application: >80%
- Adapters: >70%

---

## Fase 10: DevOps + Observabilidad (Semana 9-10)

### 10.1 GitHub Actions CI

```yaml
- Checkout
- Setup Java
- Maven verify
- SonarQube (optional)
```

### 10.2 Observabilidad

- Spring Boot Actuator
- Health checks
- Correlation ID
- Logs estructurado (JSON)
- OpenTelemetry (evolución)

---

## Fase 11: Documentation (Semana 10)

- README principal
- ADRs
- OpenAPI

---

## Decisiones Técnicas

| Decisión | Elección | Razón |
|----------|----------|-------|
| Java Version | Java 17 | LTS, amplio soporte |
| Spring Boot | 3.2.x | Estable, LTS |
| API Gateway | Spring Cloud Gateway | Integración nativa con Spring |
| UUID | UUID nativo | Mejor rendimiento que String |
| API Versioning | URL (/v1/) | Más explícito |
| Database | PostgreSQL 16 | robustez, extensions |
| Message Broker | Kafka | estándar industria |

---

## Estimación de Tiempo

| Fase | Semanas |
|------|---------|
| 1. Foundation | 1-2 |
| 2. User + Product | 2-3 |
| 3. Order Service | 3-4 |
| 4. Inventory Service | 4-5 |
| 5. Kafka | 5-6 |
| 6. Shipping Service | 6 |
| 7. Notification Service | 7 |
| 8. Security | 7-8 |
| 9. Testing | 8-9 |
| 10. DevOps | 9-10 |
| 11. Documentation | 10 |

**Total: ~10 semanas (2.5 meses)**

---

## Criterios de Aceptación

### Arquitectura
- [ ] Microservicios desacoplados
- [ ] Cada servicio con propia persistencia
- [ ] Arquitectura Hexagonal en cada servicio
- [ ] Dominio sin dependencias de Spring/Hibernate/Kafka

### Hibernate
- [ ] Separación dominio/persistencia
- [ ] JPA/Hibernate correctamente utilizado
- [ ] LAZY loading apropiado
- [ ] Caso N+1 documentado y resuelto
- [ ] Optimistic locking en Inventory

### Microservicios
- [ ] REST cuando procede
- [ ] Kafka para eventos
- [ ] Consistencia eventual

### Quality
- [ ] Unit tests
- [ ] Integration tests
- [ ] Testcontainers
- [ ] Tests de concurrencia

### Security
- [ ] Contraseñas hasheadas
- [ ] JWT funcional
- [ ] Roles implementados

### DevOps
- [ ] Docker Compose funcional
- [ ] Maven compila todo
- [ ] CI ejecuta tests

### Documentation
- [ ] README profesional
- [ ] Diagrama arquitectónico
- [ ] ADRs
- [ ] OpenAPI
