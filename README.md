# Paperless: Document Management System (Paperless DMS)

A lightweight Document Management System developed as part of the **Software Engineering 3 (SWEN3)** course at FH Technikum Wien.

## 1. Implemented Sprints

### Sprint 1: Project Setup, REST API & Data Access Layer
1. **Document Ingestion & Metadata Persistence**: Multipart PDF file upload (`POST /api/v1/documents/upload`) with title and original filename extraction, persisting metadata in PostgreSQL 16.
2. **Document Management & Full CRUD**: Document retrieval (`GET /api/v1/documents`, `GET /api/v1/documents/{id}`), search by title (`GET /api/v1/documents/search?query=...`), and idempotent deletion (`DELETE /api/v1/documents/{id}`) returning HTTP 204 No Content.
3. **4th Custom Use-Case (Document Lifecycle State & Status History Audit Trail)**:
   - Tracks document lifecycle states (`RECEIVED`, `PROCESSING`, `REVIEW_PENDING`, `COMPLETED`, `REJECTED`).
   - Strict domain invariant enforcement via Aggregate Root (`DocumentModel.transitionTo`).
   - Immutable, append-only status transition audit trail (`DocumentStatusHistoryEntity`, `1:n` relation).
   - Concept documentation: see [`docs/custom-usecase-concept.md`](docs/custom-usecase-concept.md).

### Sprint 2: Web-UI Integration, Validation & Domain-Driven Design
1. **Web-Server Subproject & Reverse Proxy**: Containerized Nginx Alpine web server (`paperless-webui`) serving static assets from content path and reverse-proxying `/api/` calls to `paperless-rest:8081`.
2. **Component-Based Web-UI**: Single-Page Application (SPA) built with React 18 and TypeScript, featuring dark theme dashboard, document cards, live statistics grid, upload modal with drag-and-drop, detail modal with lifecycle transition controls, and status history timeline.
3. **Multi-Layer Validation**:
   - Presentation Tier: Client-side validation in React forms and Jakarta Bean Validation (`@Valid`, `@NotBlank`, `@Size`) on request DTOs.
   - Business Layer: Dedicated domain validators (`DocumentModelValidator`, `DocumentStatusHistoryModelValidator`) and custom constraint (`@ValidFilename`) preventing path traversal attacks.
   - Optimistic Concurrency Control (OCC) using JPA `@Version` with HTTP 409 Conflict mapping.
   - Standard Spring Security (`permitAll` filter chain).

### Sprint 3: Message Queuing (RabbitMQ) & Mid-Term Review
1. **RabbitMQ Message Broker**: `rabbitmq:4-management` container (AMQP `5672`, management UI `15672`) with health check.
2. **Upload Event Publishing**: On `POST /api/v1/documents/upload` the REST server saves the metadata and sends an `OcrMessage` (document id, filename, content type, upload time) to `paperless.ocr.queue` via `paperless.document.exchange`. Metadata and message are handled in one transaction (transacted RabbitMQ channel): if the broker is not reachable, nothing is saved and the client gets `503 Service Unavailable`.
3. **OCR Worker (empty)**: New Spring Boot application `paperless-ocr-worker` that consumes the queue and logs every received OCR job. Invalid messages are rejected without requeue. Real OCR follows in Sprint 4.
4. **Layer-Specific Exceptions & Logging**: `MessageQueueException` (messaging layer, HTTP 503) in addition to the business and persistence exceptions, SLF4J logging at all critical points (request, upload, message sent / received, failures).
5. **Document Metadata Update**: `PUT /api/v1/documents/{id}` with `DocumentUpdateDto`. All document endpoints now use the CRUD specific DTOs (`DocumentCreateDto`, `DocumentUpdateDto`, `DocumentResponseDto`).
6. **CI/CD & Coverage**: GitHub Actions workflow (`.github/workflows/ci.yml`) running all tests, the web UI build and `docker compose build`. JaCoCo coverage report with a 70% line coverage gate.
7. **Documentation**: Architecture, design decisions, error handling, testing strategy and lessons learned in [`docs/architecture.md`](docs/architecture.md).

> For the complete REST API specification, parameter definitions, and request/response schemas, refer to [`docs/openapi.yaml`](docs/openapi.yaml).

---

## 2. Technology Stack

- **Java JDK**: Java 25 LTS
- **Framework**: Spring Boot 4.1.1
- **Security**: Spring Security 6.x (Stateless `SecurityFilterChain`)
- **Persistence**: Spring Data JPA / Hibernate ORM
- **Database**: PostgreSQL 16 (Alpine)
- **Message Broker**: RabbitMQ 4 (Spring AMQP, JSON messages)
- **Object Mapping**: MapStruct 1.6.2 (compile-time mappers, pure functions)
- **Frontend SPA**: React 18, TypeScript, Vite
- **Web Server / Reverse Proxy**: Nginx Alpine
- **Testing**: JUnit 5, Mockito, MockMvc, DataJpaTest (with H2 in PostgreSQL compatibility mode), JaCoCo coverage
- **CI**: GitHub Actions
- **Orchestration**: Docker Compose with multi-container health checks

---

## 3. Running the System Locally

### Prerequisites
- Docker & Docker Compose installed and running.
- (Optional for local development) JDK 25 and Node.js 20+.

### Step 1: Start Services with Docker Compose
From the repository root (`paperless-dms/`):

```bash
docker compose up -d --build
```

This starts:
1. `paperless-postgres`: PostgreSQL database on port `5432`.
2. `paperless-rabbitmq`: RabbitMQ broker on port `5672`, management UI on port `15672` (user `paperless`, password `paperless_secret`).
3. `paperless-rest`: Backend REST API on port `8081` (waits until PostgreSQL and RabbitMQ are healthy).
4. `paperless-ocr-worker`: OCR worker consuming `paperless.ocr.queue` (waits until RabbitMQ is healthy).
5. `paperless-webui`: Web Server & React SPA on port `80` (reverse proxies `/api/` to `paperless-rest:8081`).

### Step 2: Access the Application
- **Paperless Web UI**: Open your browser at [http://localhost/](http://localhost/)
- **Backend Health Check**: [http://localhost/api/v1/health](http://localhost/api/v1/health) (or directly via [http://localhost:8081/api/v1/health](http://localhost:8081/api/v1/health))
- **REST API Endpoints**: [http://localhost/api/v1/documents](http://localhost/api/v1/documents)
- **RabbitMQ Management UI**: [http://localhost:15672](http://localhost:15672)

### Sprint 3 Check: Upload leads to a worker log entry
Upload a PDF via the Web UI or the script below, then look at the worker log:

```bash
./tests/sprint3-queue-check.sh        # or: .	ests\sprint3-queue-check.ps1
docker compose logs paperless-ocr-worker
# ... Received OCR job for document 1 (file: sample.pdf, type: application/pdf, uploaded at: ...)
```

### Step 3: Verify Service Status
```bash
docker compose ps
```
All containers should report `running` (postgres and rabbitmq `healthy`).

### Step 4: Stop Services
```bash
docker compose down
# Or to also clean persistent database volumes:
docker compose down -v
```

---

## 4. Running Automated Tests

Tests run without requiring external Docker containers by using the isolated in-memory test profile (`src/test/resources/application.yaml`).

Execute all unit and slice tests:
```bash
cd paperless-rest
./gradlew test          # tests + JaCoCo report (build/reports/jacoco/test/html/index.html)
./gradlew check         # additionally fails if line coverage is below 70%

cd ../paperless-ocr-worker
./gradlew test
```

The same checks run automatically in GitHub Actions on every push and pull request.

---

## 5. API Documentation & Test Scripts

- **OpenAPI 3.0 Specification**: Located at `docs/openapi.yaml`.
- **Architecture & Design Decisions**: Located at `docs/architecture.md`.
- **Test Scripts**: `tests/document-upload-tests.http`, `tests/custom-usecase-tests.http`, `tests/document-upload-tests.ps1/.sh`, `tests/sprint3-queue-check.ps1/.sh`.
