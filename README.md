# microservices-demo

A small Java/Spring Boot microservices playground: two services talking to
each other over REST, gRPC, and Kafka, backed by Postgres and Redis, all
wired together with Docker Compose.

## Architecture

```
                 REST                    gRPC (sync)
  client  ─────────────────▶  order-service  ─────────────────▶  inventory-service
                                   │                                    │
                                   │ Kafka: order-created               │
                                   ▼                                    ▼
                              (topic, for async                   Postgres (stock_items)
                               consumers/audit)                   Redis   (stock cache)
                                   │
                                   ▼
                            inventory-service
                            (Kafka consumer → order_audit table)

  order-service   → Postgres (orders_db)
  inventory-service → Postgres (inventory_db) + Redis (cache-aside on stock reads)
```

**order-service**
- `POST /orders` — validates the request, calls inventory-service's
  `ReserveStock` gRPC RPC *synchronously* to check and decrement stock, then
  persists the order (`CREATED` or `REJECTED`) and, on success, publishes an
  `order-created` Kafka event.
- `GET /orders/{id}`, `GET /orders` — read orders back from Postgres.

**inventory-service**
- Exposes `InventoryService` over gRPC (`ReserveStock`, `GetStock`).
  `ReserveStock` is the authoritative, transactional stock decrement
  (optimistic locking via `@Version` guards concurrent reservations).
- `GetStock` reads through a Redis cache (Spring Cache abstraction,
  cache-aside, 30s TTL) backed by Postgres.
- Also consumes `order-created` from Kafka and writes an idempotent audit
  row (`order_audit`, keyed by orderId). This is deliberately *not* where
  stock gets decremented — that already happened synchronously over gRPC —
  it exists to demonstrate the event-consumption side of the architecture
  (e.g. what a reporting/notifications service would hook into) without
  double-counting stock.

The gRPC contract lives in `inventory.proto`, copied into both modules'
`src/main/proto/` (no shared parent module, to keep this demo simple —
keep the two copies in sync by hand).

## Stack

Java 21 · Spring Boot 4 · Spring Data JPA · Spring gRPC · Spring for Apache
Kafka · Spring Data Redis · PostgreSQL · Redis · Kafka (KRaft, no
Zookeeper) · Docker / Docker Compose

## Running it

Requires Docker (and Docker Compose v2, bundled with modern Docker
Desktop). No local Java/Maven install needed — the build happens inside
Docker.

```bash
docker compose up --build
```

This starts Postgres (with `orders_db` and `inventory_db`, one per
service), Redis, a single-node Kafka broker, and both Spring Boot services.

- order-service: http://localhost:8080
- inventory-service actuator: http://localhost:8081/actuator/health
- inventory-service gRPC: localhost:9091

### Try it

```bash
# Place an order for a SKU with stock (seeded: WIDGET-001 has 100 units)
curl -X POST localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{"sku":"WIDGET-001","quantity":5}'

# Fetch it back
curl localhost:8080/orders

# Try a SKU with no stock — GIZMO-003 is seeded with 0 units, expect 409
curl -i -X POST localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{"sku":"GIZMO-003","quantity":1}'
```

Seeded SKUs (see `inventory-service/src/main/resources/data.sql`):
`WIDGET-001` (100), `GADGET-002` (50), `GIZMO-003` (0).

## Local (non-Docker) development

Each service is a standard Maven project with the wrapper included:

```bash
cd inventory-service && ./mvnw spring-boot:run
cd order-service && ./mvnw spring-boot:run
```

You'll need Postgres, Redis, and Kafka reachable at the hosts/ports in each
service's `application.yml` (defaults assume `localhost`) — easiest way is
`docker compose up postgres redis kafka` and run the two services locally
against those.

## Project layout

```
order-service/       REST API, gRPC client, Kafka producer, orders_db
inventory-service/   gRPC server, Kafka consumer, Redis cache, inventory_db
postgres-init/       creates the two per-service databases/users on first boot
docker-compose.yml   wires everything together
```
