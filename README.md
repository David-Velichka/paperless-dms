# Paperless: Document Management System (Paperless DMS)

A lightweight Document Management System developed as part of the **Software Engineering 3 (SWEN3 | BIF5)** course at FH Technikum Wien.

## 1. Sprint 1 Deliverables & Implemented Use Cases

1. **Document Upload & Ingestion**: Multipart PDF file upload with title metadata, automatic initialization to `RECEIVED` status, and metadata persistence in PostgreSQL.
2. **Document Management & Query**: Full CRUD operations for documents (retrieval, search, deletion with cascade cleanup).
3. **4th Custom Use-Case (Document State & Status History)**:
   - Tracks document lifecycle states (`RECEIVED`, `PROCESSING`, `REVIEW_PENDING`, `COMPLETED`, `REJECTED`).
   - Immutable, append-only status transition audit trail (`DocumentStatusHistoryEntity`, `1:n` relation).
   - Concept documentation: see [`docs/custom-usecase-concept.md`](docs/custom-usecase-concept.md).

> For the complete REST API specification, parameter definitions, and request/response schemas, refer to [`docs/openapi.yaml`](docs/openapi.yaml).

---

## 2. Technology Stack

- **Java JDK**: Java 25 LTS
- **Framework**: Spring Boot 4.1.1
- **Persistence**: Spring Data JPA / Hibernate ORM
- **Database**: PostgreSQL 16 (Alpine)
- **Object Mapping**: MapStruct 1.6.2
- **Testing**: JUnit 5, Mockito, MockMvc, DataJpaTest (with H2 in PostgreSQL compatibility mode)
- **Orchestration**: Docker Compose with health-checked service dependencies

---

## 3. Running the System Locally

### Prerequisites
- Docker & Docker Compose installed and running.
- (Optional for local development) JDK 25.

### Step 1: Start Services with Docker Compose
From the repository root (`paperless-dms/`):

```bash
docker compose up -d --build
```

This starts:
1. `paperless-postgres`: PostgreSQL database on port `5432`.
2. `paperless-rest`: Backend REST API on port `8081` (waits until PostgreSQL is healthy).

### Step 2: Verify Service Status
```bash
docker compose ps
```
Both containers should report `running` (and postgres `healthy`).

### Step 3: Stop Services
```bash
docker compose down
# Or to also clean persistent data volumes:
docker compose down -v
```

---

## 4. Running Automated Tests

Tests run without requiring external Docker containers by using the isolated in-memory test profile (`src/test/resources/application.yaml`).

Execute all unit and slice tests:
```bash
cd paperless-rest
./gradlew test
```

---

## 5. API Documentation & Test Scripts

- **OpenAPI 3.0 Specification**: Located at `docs/openapi.yaml`.
- **HTTP Test Requests**: Located in `tests/`:
  - `tests/document-upload-tests.http`: Multipart upload and CRUD verification.
  - `tests/custom-usecase-tests.http`: Lifecycle status transitions, audit history queries, and validation failure tests.
