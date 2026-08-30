# LogiCore — UML Diagrams

Rendered natively by GitHub (Mermaid). These diagrams complement the architecture
decisions in [`DECISIONS.md`](DECISIONS.md) and [`IMPLEMENTATION-PLAN.md`](IMPLEMENTATION-PLAN.md).

1. [System architecture (component diagram)](#1-system-architecture)
2. [Hexagonal architecture (class diagram, inventory-service)](#2-hexagonal-architecture)
3. [Order saga — happy path (sequence diagram)](#3-order-saga--happy-path)
4. [Order saga — failure and compensation (sequence diagram)](#4-order-saga--failure-and-compensation)
5. [Authentication with JWT (sequence diagram)](#5-authentication-with-jwt)
6. [Order state machine](#6-order-state-machine)
7. [Shipment state machine](#7-shipment-state-machine)

---

## 1. System architecture

Component view of the whole platform: one entry point (API Gateway), six services with
one PostgreSQL database each, and a Kafka broker as the backbone of event-driven
conversation. The gateway is the only component that parses JWTs; every downstream
service trusts the identity headers it injects (ADR-008).

```mermaid
flowchart LR
    Client["Client or curl"]

    subgraph GW["API Gateway :8080"]
        JWT["JwtTokenParser + GatewaySecurityFilter<br/>validates JWT - injects X-User-Id / X-User-Email / X-User-Roles"]
    end

    subgraph SVC["Microservices"]
        USR["User Service :8100<br/>users, roles, JWT issuer"]
        PRD["Product Service :8101<br/>catalog (Money, Sku)"]
        INV["Inventory Service :8102<br/>stock register, reserve, release"]
        ORD["Order Service :8103<br/>order lifecycle"]
        SHP["Shipping Service :8104<br/>shipment lifecycle"]
        NTF["Notification Service :8105<br/>records business notifications"]
    end

    subgraph KF["Kafka 7.6 broker"]
        K["order-events<br/>inventory-events<br/>shipment-events"]
        KUI["Kafka UI :8081"]
    end

    subgraph DB["PostgreSQL 16 - database per service"]
        DBU["user_db"]
        DBP["product_db"]
        DBI["inventory_db"]
        DBO["order_db"]
        DBS["shipping_db"]
        DBN["notification_db"]
    end

    Client --> GW
    GW --> USR
    GW --> PRD
    GW --> INV
    GW --> ORD
    GW --> SHP
    GW --> NTF

    USR <--> DBU
    PRD <--> DBP
    INV <--> DBI
    ORD <--> DBO
    SHP <--> DBS
    NTF <--> DBN

    ORD <--> K
    INV <--> K
    SHP <--> K
    NTF <--> K
    K --> KUI
```

**What to look at.** All synchronous REST traffic goes through `:8080`; all async traffic
goes through Kafka. Services never talk to each other over HTTP — the only service-to-
service HTTP call is Order Service validating prices against Product Service. Each
service persists only in its own database (ADR-003).

---

## 2. Hexagonal architecture

Every service follows the same shape; this class diagram uses `inventory-service` as the
example. The **domain aggregate** (`InventoryItem`) is plain Java with no Spring imports.
The **application layer** declares inbound *ports* (`ReserveStockUseCase`) and outbound
*ports* (`InventoryRepository`, `InventoryEventPublisher`) and runs the use cases. The
**adapter layer** implements those ports: REST + Kafka consumers (inbound) and JPA +
Kafka producer (outbound).

```mermaid
classDiagram
    direction LR
    class ProductId {
        +value: UUID
    }
    class InventoryItem {
        -availableQuantity: int
        -reservedQuantity: int
        -version: Long
        +create(productId, quantity) InventoryItem
        +reserve(quantity) InventoryItem
        +release(quantity) InventoryItem
        +confirm(quantity) InventoryItem
    }
    class InsufficientStockException

    class ReserveStockUseCase {
        <<interface>>
        +reserve(command) InventoryItemResponse
    }
    class ReserveStockApplicationService {
        +reserve(command) InventoryItemResponse
    }
    class InventoryRepository {
        <<interface>>
        +save(item) InventoryItem
        +findByProductId(ProductId) Optional~InventoryItem~
    }
    class InventoryEventPublisher {
        <<interface>>
        +publish(DomainEvent)
    }
    class InventoryPersistenceAdapter {
        +save(item) InventoryItem
        +findByProductId(ProductId) Optional~InventoryItem~
    }
    class InventoryItemJpaEntity
    class InventoryItemJpaRepository {
        <<interface>>
    }
    class KafkaInventoryEventPublisher {
        +publish(DomainEvent)
    }
    class OrderEventKafkaConsumer {
        +onOrderEvent(DomainEvent)
    }
    class InventoryController {
        +POST registerStock
        +POST reserve(productId, qty)
        +POST release(productId, qty)
        +GET getStock(productId)
    }

    InventoryItem --> ProductId : has id
    InventoryItem ..> InsufficientStockException : throws
    ReserveStockUseCase <|.. ReserveStockApplicationService : implements
    ReserveStockApplicationService --> InventoryRepository : uses (port)
    ReserveStockApplicationService --> InventoryEventPublisher : publishes (port)
    InventoryPersistenceAdapter ..|> InventoryRepository : adapter
    InventoryPersistenceAdapter --> InventoryItemJpaRepository : Spring Data
    InventoryPersistenceAdapter --> InventoryItemJpaEntity : maps
    KafkaInventoryEventPublisher ..|> InventoryEventPublisher : adapter
    OrderEventKafkaConsumer --> ReserveStockApplicationService : calls
    InventoryController --> ReserveStockApplicationService : calls
```

**What to look at.** Arrows point "inward": the application service depends only on
interfaces, never on Spring, JPA, Kafka, or HTTP. Swapping PostgreSQL for MongoDB or REST
for gRPC touches only the adapter boxes.

---

## 3. Order saga — happy path

The core choreography. The order is persisted as `PENDING` and an `OrderCreated` event
starts the saga. Inventory reserves per line (optimistic lock), then the order is
confirmed, a shipment is created, and notifications are recorded at each step. No database
spans more than one service; correctness comes from events + idempotency (ADR-006).

```mermaid
sequenceDiagram
    autonumber
    actor Client
    participant GW as "API Gateway :8080"
    participant ORD as "Order Service :8103"
    participant PRD as "Product Service :8101"
    participant DBO as "order_db"
    participant K as "Kafka"
    participant INV as "Inventory Service :8102"
    participant SHP as "Shipping Service :8104"
    participant NTF as "Notification Service :8105"

    Client->>GW: POST /api/orders (Bearer JWT)
    GW->>ORD: POST /api/orders (X-User-Id)
    ORD->>PRD: GET /api/products/{id} (price check)
    PRD-->>ORD: product price
    ORD->>DBO: persist order (PENDING)
    ORD->>K: publish OrderCreated (order-events)
    K-->>INV: OrderCreated
    INV->>INV: reserve stock per line - optimistic lock
    INV->>K: publish StockReserved (inventory-events)
    K-->>ORD: StockReserved
    ORD->>DBO: order - CONFIRMED
    ORD->>K: publish OrderConfirmed (order-events)
    K-->>SHP: OrderConfirmed
    SHP->>SHP: create shipment (CREATED)
    SHP->>K: publish ShipmentCreated (shipment-events)
    K-->>NTF: order and shipment events
    NTF->>NTF: record notification for customer
    Note over Client: GET /api/orders/{id} - status PENDING, then CONFIRMED
```

**What to look at.** The gateway returns immediately after persisting the order; the rest
is asynchronous. Inventory and Shipping never know each other exist — they only react to
events, which is what makes the architecture decoupled and independently deployable.

---

## 4. Order saga — failure and compensation

Same saga, but one order line can trigger a failure. Example with two lines: line 1 is
reserved, line 2 cannot be fulfilled. In a distributed transaction this would be a rollback
problem; here it is solved with a **compensating action** (release line 1) and an explicit
`StockReservationFailed` event that moves the order to `FAILED`.

```mermaid
sequenceDiagram
    autonumber
    participant ORD as "Order Service :8103"
    participant K as "Kafka"
    participant INV as "Inventory Service :8102"
    participant NTF as "Notification Service :8105"

    ORD->>K: publish OrderCreated (order-events, two lines)
    K-->>INV: OrderCreated
    INV->>INV: line 1 - reserved OK
    INV->>INV: line 2 - insufficient stock
    INV->>INV: compensate - release line 1 (StockReleased)
    INV->>K: publish StockReservationFailed (inventory-events)
    K-->>ORD: StockReservationFailed
    ORD->>ORD: order - FAILED
    ORD->>K: publish OrderFailed (order-events)
    K-->>NTF: OrderFailed
    NTF->>NTF: record notification for customer
```

**What to look at.** There is no single "transaction": each service commits its own work and
emits an event; problems are repaired with compensating events instead of rollbacks
(ADR-005, ADR-006).

---

## 5. Authentication with JWT

User Service is the only component allowed to issue tokens; the gateway validates every
incoming JWT **offline** (shared `JWT_SECRET`, HS256) and forwards identity headers. This
keeps the protected services free of token logic.

```mermaid
sequenceDiagram
    autonumber
    actor Client
    participant GW as "API Gateway :8080"
    participant USR as "User Service :8100"
    participant SVC as "Any protected service"

    rect rgb(236, 244, 255)
        note over Client,USR: Public - register + login
        Client->>GW: POST /api/auth/register
        GW->>USR: POST /api/auth/register
        USR-->>GW: 201 Created (user)
        GW-->>Client: 201
        Client->>GW: POST /api/auth/login
        GW->>USR: POST /api/auth/login
        USR-->>GW: 200 (JWT, user)
        GW-->>Client: 200 (JWT)
    end

    rect rgb(250, 246, 235)
        note over Client,SVC: Protected - every other request
        Client->>GW: GET /api/orders (Authorization: Bearer JWT)
        GW->>GW: JwtTokenParser - verify signature and expiry
        Note right of GW: strips Authorization header<br/>injects X-User-Id, X-User-Email, X-User-Roles
        GW->>SVC: GET /api/orders (identity headers)
        SVC-->>GW: 200
        GW-->>Client: 200
    end
```

**What to look at.** Login is BCrypt-checked in User Service; the JWT carries
`sub`/`email`/`roles`. The gateway verifies it without calling any service, and protected
services trust the injected headers (trusted network, ADR-008).

---

## 6. Order state machine

`Order` (order-service) — `PENDING` is the only initial state; `CONFIRMED`/`FAILED`
arrive asynchronously from the inventory outcome.

```mermaid
stateDiagram-v2
    direction LR
    [*] --> PENDING : order created
    PENDING --> CONFIRMED : StockReserved consumed
    PENDING --> FAILED : StockReservationFailed consumed
    PENDING --> CANCELLED : user cancels
    CONFIRMED --> CANCELLED : user cancels (stock released)
    CONFIRMED --> [*]
    FAILED --> [*]
    CANCELLED --> [*]
```

---

## 7. Shipment state machine

`Shipment` (shipping-service) — created automatically when an order is confirmed, then
advanced by explicit operations.

```mermaid
stateDiagram-v2
    direction LR
    [*] --> CREATED : OrderConfirmed consumed
    CREATED --> SHIPPED : POST /api/shipments/{id}/ship
    SHIPPED --> DELIVERED : POST /api/shipments/{id}/deliver
    DELIVERED --> [*]
```