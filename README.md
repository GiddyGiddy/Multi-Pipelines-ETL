# ETL Dispatcher Service

A Spring Boot microservice that acts as an **ETL (Extract, Transform, Load) dispatcher**. It reads entity data from a relational database in pages, transforms it into transfer payloads, and publishes the results to a message queue — either **AWS SQS** or **Apache Kafka**, selectable at runtime via configuration.


---

## Table of Contents

- [Architecture Overview](#architecture-overview)
- [Key Features](#key-features)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [API Endpoints](#api-endpoints)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [Development Setup](#development-setup)
- [Extending the Application](#extending-the-application)
- [Documentation Coverage and Next Topics](#documentation-coverage-and-next-topics)

---

## Architecture Overview

```
┌──────────────────────────────────────────────────────────────────┐
│                        REST API (Basic Auth)                     │
│              POST /api/pipelines/start   GET /api/pipelines/status│
└──────────────────────┬───────────────────────────────────────────┘
                       │
              PipelineExecutionService
                       │
          ┌────────────┴─────────────┐
          │                          │
   PipelineJob 01              PipelineJob 02
   (Pipeline01EntityProvider)  (Pipeline02EntityProvider)
          │                          │
   ┌──────┼──────┐            ┌──────┼──────┐
   │      │      │            │      │      │
Extract  Transform  Load    Extract Transform Load
 (DB)   (Mapper)  (Queue)   (DB)   (Mapper) (Queue)
```

Each pipeline follows the standard ETL pattern:

1. **Extract** – reads paginated records from the database via a Spring Data JPA repository.
2. **Transform** – maps domain entities to transfer DTOs using MapStruct.
3. **Load** – serialises the DTO batch and publishes it to the configured message queue.

Pipelines can be executed **sequentially** or **in parallel** (async via `CompletableFuture`).

---

## Key Features

- **Two configurable pipelines** (`Pipeline01` / `Pipeline02`) registered in `PipelineJobRegistry`.
- **Pluggable message transport** — switch between SQS and Kafka with a single property (`etl.messaging.transport`).
- **Paginated processing** — configurable page size to control memory usage during large data exports.
- **Configurable max message size** — oversized batches are automatically split before publishing.
- **Cancellable execution** — a running job can be cancelled via the `/stop` endpoint.
- **Live status endpoint** — track total items, items processed, start time, and cancel time per pipeline.
- **Basic Auth security** — all `/api/**` routes require credentials configured via properties.
- **Spring Profile support** — `dev` profile wires a local SQS endpoint (e.g. LocalStack) instead of the real AWS client.

---

## Technology Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.3.2 |
| Persistence | Spring Data JPA |
| Messaging (SQS) | AWS SDK v2 (`software.amazon.awssdk:sqs`) |
| Messaging (Kafka) | Spring Cloud Stream + Kafka Binder |
| Mapping | MapStruct 1.5.5 |
| Boilerplate reduction | Lombok |
| Security | Spring Security (HTTP Basic) |
| Build | Maven |

---

## Project Structure

```
src/main/java/com/ideaproject/etl/
├── ETLApplication.java                  # Entry point
├── business/
│   ├── dataprovider/
│   │   ├── DataProvider.java            # Abstract base: pagination, batching, publishing
│   │   ├── Extractor.java               # Interface: extract a page from the source
│   │   ├── Transformer.java             # Interface: convert source type to transfer DTO
│   │   ├── Loader.java                  # Interface: publish a payload to the queue
│   │   ├── DataItemPostProcessor.java   # Optional post-processing hook
│   │   ├── Pipeline01EntityProvider.java# Concrete ETL provider for Entity1
│   │   └── Pipeline02EntityProvider.java# Concrete ETL provider for Entity2
│   ├── dto/
│   │   ├── PipelineStatusDto.java       # Status per pipeline (id, total, current, state)
│   │   ├── PipelineStatusResponseDto.java
│   │   └── event/                       # Transfer event DTOs (e.g. Pipeline01EntityTransfer)
│   ├── errors/                          # Custom exceptions
│   ├── mapper/                          # MapStruct mappers (entity → DTO)
│   ├── pipeline/
│   │   ├── PipelineJobDefinition.java   # Immutable binding of an ID to a DataProvider
│   │   ├── PipelineJobId.java           # Constant pipeline ID strings
│   │   └── PipelineJobRegistry.java     # Spring component holding all registered jobs
│   └── service/
│       └── PipelineExecutionService.java# Orchestrates execution, tracks status
├── config/
│   ├── DataSourceConfiguration.java     # JPA DataSource bean (disabled in dev profile)
│   ├── SecurityConfiguration.java       # HTTP Basic Auth, BCrypt password encoding
│   └── SqsConfiguration.java           # SqsClient bean (real vs. local dev endpoint)
├── controller/
│   └── Routes.java                      # Route constants (/api/pipelines/start|stop|status)
├── domain/
│   ├── entities/
│   │   ├── PipelineExecutionStatus.java # Runtime status holder (Spring component / singleton)
│   │   ├── ProceedStatus.java           # Per-pipeline progress (total, current)
│   │   ├── entity1/                     # JPA entity for Pipeline01
│   │   └── Entity2/                     # JPA entity for Pipeline02
├── messaging/
│   ├── MessagePublisher.java            # Abstraction: publish(bindingName, message)
│   ├── MessagePublisherSqsClient.java   # SQS implementation (active when transport=sqs)
│   └── MessagePublisherStreamBridge.java# Kafka implementation (active when transport=kafka)
└── persistence/
    └── repository/
        ├── Pipeline01EntityRepository.java
        └── Pipeline02EntityRepository.java
```

---

## API Endpoints

All endpoints are under `/api/pipelines` and require **HTTP Basic Auth**.

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/pipelines/start?parallel=false` | Start all pipeline jobs. Pass `parallel=true` to run pipelines concurrently. |
| `POST` | `/api/pipelines/stop` | Cancel the currently running execution. |
| `GET` | `/api/pipelines/status` | Return current execution status (running flag, start/cancel times, per-pipeline progress). |

---

## Configuration

All properties live in `src/main/resources/application.properties`. Sensitive values should be supplied as environment variables.

### Core dispatcher settings

| Property | Default | Description |
|---|---|---|
| `dispatcher.pagination.size` | `50` | Number of records fetched per page from the database |
| `dispatcher.max.message.size` | `160000` | Maximum byte size of a single queue message; batches are split automatically |
| `dispatcher.api.username` | *(required)* | Username for HTTP Basic Auth |
| `dispatcher.api.password` | *(required)* | Password for HTTP Basic Auth |

### Messaging transport

| Property | Default | Description |
|---|---|---|
| `etl.messaging.transport` | `sqs` | Set to `kafka` to use Kafka instead of SQS |

#### SQS (default)

| Property | Default / Env var |
|---|---|
| `messaging.entity1.binding` | `${ENTITY1_SQS_QUEUE_URL}` |
| `messaging.entity2.binding` | `${ENTITY2_SQS_QUEUE_URL}` |

The SQS client uses the AWS SDK default credential chain (environment variables, `~/.aws/credentials`, IAM role, etc.).

#### Kafka

Set `etl.messaging.transport=kafka` and configure the bindings to the Spring Cloud Stream output channel names:

```properties
etl.messaging.transport=kafka
messaging.entity1.binding=entity1-out-0
messaging.entity2.binding=entity2-out-0
spring.cloud.stream.bindings.entity1-out-0.destination=entity1-topic
spring.cloud.stream.bindings.entity2-out-0.destination=entity2-topic
spring.cloud.stream.kafka.binder.brokers=${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
```

### Database

Standard Spring Boot datasource properties (applied when **not** using the `dev` profile):

```properties
spring.datasource.url=jdbc:postgresql://host:5432/mydb
spring.datasource.username=...
spring.datasource.password=...
spring.jpa.hibernate.ddl-auto=validate
```

---

## Running the Application

### Prerequisites

- Java 21+
- Maven 3.8+
- A running relational database accessible via the datasource URL
- Either AWS credentials (for SQS) or a running Kafka broker

### Build

```bash
mvn clean package
```

### Run (production)

```bash
java -jar target/dispatcher-1.0.0-SNAPSHOT.jar \
  --dispatcher.api.username=admin \
  --dispatcher.api.password=secret \
  --spring.datasource.url=jdbc:postgresql://localhost:5432/mydb \
  --spring.datasource.username=dbuser \
  --spring.datasource.password=dbpass
```

### Run (development profile with LocalStack)

The `dev` Spring profile activates:
- A local SQS endpoint (configure `AWS_ENDPOINT_URL` or the relevant LocalStack URL in `SqsConfiguration`).
- No real `DataSource` bean — wire an H2 or Testcontainers DB instead.

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

---

## Development Setup

1. **Clone the repository**

   ```bash
   git clone <repo-url>
   cd ETLApp
   ```

2. **Import into your IDE** as an existing Maven project (IntelliJ IDEA or Eclipse).

3. **Configure local secrets** — create `src/main/resources/application-local.properties` (git-ignored) and override:

   ```properties
   dispatcher.api.username=admin
   dispatcher.api.password=secret
   spring.datasource.url=jdbc:h2:mem:testdb
   ```

4. **Run tests**

   ```bash
   mvn test
   ```

5. **Build without tests** (for a quick iteration)

   ```bash
   mvn package -DskipTests
   ```

---

## Extending the Application

### Adding a new pipeline

1. **Create the JPA entity** under `domain/entities/entity3/`.
2. **Create the Spring Data repository** under `persistence/repository/`.
3. **Create the transfer DTO** under `business/dto/event/`.
4. **Create the MapStruct mapper** under `business/mapper/`.
5. **Create a new `DataProvider` subclass** under `business/dataprovider/` (follow `Pipeline01EntityProvider` as a template).
6. **Register a new `PipelineJobId` constant** in `business/pipeline/PipelineJobId.java`.
7. **Add the job to `PipelineJobRegistry`** — inject the new provider and add it to `jobsInOrder`.
8. **Add a queue binding property** in `application.properties`:
   ```properties
   messaging.entity3.binding=${ENTITY3_SQS_QUEUE_URL:...}
   ```

### Switching the message transport

Change `etl.messaging.transport` to `kafka` and provide the Kafka broker address. No code changes are needed — the `MessagePublisherStreamBridge` bean activates automatically.

---

## Documentation Coverage and Next Topics

This README currently covers the service architecture, pipeline flow, technology stack, project layout, API routes, configuration, local setup, and how to add a pipeline or switch transports.

The following topics would make the repository documentation more complete:

- **Data contract:** source database tables and fields, entity relationships, and entity-to-event mapping rules.
- **Message examples:** representative SQS/Kafka payloads, serialization format, and downstream consumer expectations.
- **Operational setup:** required AWS queues and permissions, Kafka topics, database prerequisites, and environment-specific profiles.
- **Failure handling:** behavior for database or broker errors, retries, partial pipeline failures, and safe restart/replay procedures.
- **Testing:** test types, how to run them, and how to provide local database and messaging dependencies.
- **Deployment and operations:** supported deployment approach, health/monitoring signals, logs, and troubleshooting steps.
- **Security guidance:** credential provisioning, rotation, and recommended production safeguards for the API and infrastructure.
