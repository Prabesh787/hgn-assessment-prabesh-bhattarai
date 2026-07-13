# HGN SOS Alert Intake & Dispatch

A backend service for taking in SOS signals from GPS/satellite trekking devices
and helping coordinators respond. This is an API-only project — no frontend, no
real satellite hardware, no authentication.

## Tech stack

- Java 21, Spring Boot 3.5
- PostgreSQL 16 (real Postgres, the project relies on Postgres row
  locking and partial indexes)
- Flyway for database migrations
- Spring Data JPA / Hibernate
- springdoc-openapi (Swagger UI)
- Testcontainers for tests

## Running it

### 1. Configure environment

Copy the example file and fill it in:

```bash
cp .env.example .env
```

A working `.env` for local development:

```dotenv
SERVER_PORT=8081
DB_URL=jdbc:postgresql://localhost:5432/hgh_assessement
DB_USERNAME=<YOUR_USERNAME>
DB_PASSWORD=<YOUR_PASSWORD>
JPA_DDL_AUTO=validate
```

| Variable                             | Meaning                                               | Default if unset |
|--------------------------------------|-------------------------------------------------------|------------------|
| `SERVER_PORT`                        | Port the app listens on                               | `8081`           |
| `DB_URL`                             | JDBC URL of the database                              | port **5432**    |
| `DB_USERNAME`                        | Database user                                         | — (required)     |
| `DB_PASSWORD`                        | Database password                                     | — (required)     |
| `JPA_DDL_AUTO`                       | Hibernate schema mode (Flyway owns the schema)        | `validate`       |
| `ALERT_DEDUP_WINDOW_MINUTES`         | A signal within this long of the last one is a retransmission | `5`      |
| `ALERT_ESCALATION_THRESHOLD_MINUTES` | Unclaimed for this long → `ESCALATED`                 | `15`             |
| `ALERT_ESCALATION_SWEEP_INTERVAL_MS` | How often the escalation sweep runs                   | `60000`          |
| `ALERT_ESCALATION_ENABLED`           | Set `false` to turn the sweep off entirely            | `true`           |

### 2. Run the whole stack in Docker** — app and database together, no local JDK needed:

```bash
docker compose up --build
```

### 4. Open Swagger

```
http://localhost:8081/swagger-ui.html
```

Every endpoint is callable from there.


## Response format

Every endpoint returns the same envelope:

```json
{
  "statusCode": 201,
  "message": "Device registered",
  "status": true,
  "data": { }
}
```

On an error, `status` is `false`, `data` is `null`, and `message` explains what
went wrong:

```json
{
  "statusCode": 409,
  "message": "Device serial SAT-001 is already registered",
  "status": false,
  "data": null
}
```

## Endpoints

**Setup — devices, orders, people**

| Method | Path                                    | What it does                                        |
|--------|-----------------------------------------|-----------------------------------------------------|
| POST   | `/devices`                              | Register a device                                   |
| POST   | `/orders`                               | Create an order (a booking)                         |
| GET    | `/orders/{orderId}`                     | The order with its full trekker list                |
| POST   | `/orders/{orderId}/trekkers`            | Add a person to an order                            |
| POST   | `/devices/{deviceId}/assignments`       | Assign a device to an order (closes any prior one)  |
| POST   | `/devices/{deviceId}/assignments/close` | Release the device — `COMPLETED` or `CANCELLED`     |
| GET    | `/devices/{deviceId}/assignments`       | The device's full assignment history                |

**Alerts**

| Method | Path                          | What it does                                                     |
|--------|-------------------------------|------------------------------------------------------------------|
| POST   | `/devices/{deviceId}/alerts`  | Take in an SOS signal (`201` if new, `200` if deduped)            |
| GET    | `/devices/{deviceId}/alerts`  | The device's alert history, newest first                          |
| GET    | `/alerts`                     | All alerts; `?status=OPEN\|CLAIMED\|ESCALATED\|RESOLVED` to filter |
| GET    | `/alerts/{alertId}`           | Alert detail — device, resolved order, whole trekking party        |
| GET    | `/alerts/{alertId}/signals`   | The signal trail (`RAISED` / `RETRANSMISSION`)                     |
| POST   | `/alerts/{alertId}/claim`     | A coordinator claims the alert (concurrency-safe: one winner)      |
| POST   | `/alerts/{alertId}/assign-order` | Manually attach the alert to an order                           |
| POST   | `/alerts/{alertId}/resolve`   | Close the alert out                                               |

**Coordinators**

| Method | Path            | What it does           |
|--------|-----------------|------------------------|
| POST   | `/coordinators` | Register a coordinator |
| GET    | `/coordinators` | List coordinators      |

## Example walkthrough

### Set the scene: a device, a group, and a coordinator

```bash
# Register a device
curl -X POST http://localhost:8081/devices \
  -H 'Content-Type: application/json' \
  -d '{"deviceSerial":"SAT-001"}'

# Create an order (a booking)
curl -X POST http://localhost:8081/orders \
  -H 'Content-Type: application/json' \
  -d '{"orderRef":"ORD-A","trekName":"Annapurna Circuit"}'

# Add two trekkers — they share the one device
curl -X POST http://localhost:8081/orders/1/trekkers \
  -H 'Content-Type: application/json' \
  -d '{"fullName":"Pemba Sherpa","phone":"+977-9800000001"}'

curl -X POST http://localhost:8081/orders/1/trekkers \
  -H 'Content-Type: application/json' \
  -d '{"fullName":"Mingma Dorje","phone":"+977-9800000002"}'

# Hand the device to the group
curl -X POST http://localhost:8081/devices/1/assignments \
  -H 'Content-Type: application/json' \
  -d '{"orderId":1}'

# Register a coordinator to answer alerts
curl -X POST http://localhost:8081/coordinators \
  -H 'Content-Type: application/json' \
  -d '{"name":"Sita Rai"}'
```

### The emergency: SOS, dedup, respond

```bash
# Someone presses SOS -> 201, a new alert is raised
curl -X POST http://localhost:8081/devices/1/alerts \
  -H 'Content-Type: application/json' \
  -d '{"latitude":28.7961,"longitude":83.9856}'

# The device retransmits seconds later -> 200, folded into the SAME alert.
# No second alert created; retransmissionCount goes to 1.
curl -X POST http://localhost:8081/devices/1/alerts \
  -H 'Content-Type: application/json' \
  -d '{"latitude":28.7962,"longitude":83.9855}'

# The signal trail shows one RAISED and one RETRANSMISSION
curl http://localhost:8081/alerts/1/signals

# Who is in trouble? Detail resolves the device -> order -> the whole party,
# because a shared device cannot say which member pressed it.
curl http://localhost:8081/alerts/1

# The coordinator dashboard: everything still open
curl 'http://localhost:8081/alerts?status=OPEN'

# Coordinator 1 claims it. Fire this twenty times at once and exactly one wins;
# the rest get 409.
curl -X POST http://localhost:8081/alerts/1/claim \
  -H 'Content-Type: application/json' \
  -d '{"coordinatorId":1}'

# Emergency over
curl -X POST http://localhost:8081/alerts/1/resolve
```

### Watch an alert escalate

With the default 2-minute threshold: raise an alert, **don't** claim it, wait
just over two minutes for the sweep, then look for it.

```bash
curl -X POST http://localhost:8081/devices/1/alerts \
  -H 'Content-Type: application/json' \
  -d '{"latitude":28.7961,"longitude":83.9856}'

# ...wait ~2 minutes. The app logs it at WARN, and:
curl 'http://localhost:8081/alerts?status=ESCALATED'

# It is still claimable — escalating never revokes a response.
curl -X POST http://localhost:8081/alerts/1/claim \
  -H 'Content-Type: application/json' \
  -d '{"coordinatorId":1}'
```

### One device, many treks over time

```bash
# The trek finishes — release the device
curl -X POST http://localhost:8081/devices/1/assignments/close \
  -H 'Content-Type: application/json' \
  -d '{"reason":"COMPLETED"}'

# A new booking, and the same physical device goes back out
curl -X POST http://localhost:8081/orders \
  -H 'Content-Type: application/json' \
  -d '{"orderRef":"ORD-B","trekName":"Everest Base Camp"}'

curl -X POST http://localhost:8081/devices/1/assignments \
  -H 'Content-Type: application/json' \
  -d '{"orderId":2}'

# The history: the first window is closed, the second is open. Nothing was
# overwritten, so ORD-A's old alerts still resolve to ORD-A.
curl http://localhost:8081/devices/1/assignments
```

## Project structure

```
src/main/java/.../
  controller/   REST endpoints
  service/      business logic (interfaces)
  service/impl/ implementations
  repository/   database access
  entity/       JPA entities (database tables)
  enums/        AlertStatus, SignalKind, AssignmentEndReason
  scheduler/    the escalation sweep
  dto/request/  incoming request bodies
  dto/response/ outgoing response bodies + the ApiResponse envelope
  exception/    custom exceptions + global error handler
  config/       Swagger/OpenAPI and scheduling config
src/main/resources/
  db/migration/ Flyway SQL migrations
  application.properties
src/test/java/...  Testcontainers-backed tests (concurrency)
```
