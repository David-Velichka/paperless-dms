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

> For the complete REST API specification, parameter definitions, and request/response schemas, refer to [`docs/openapi.yaml`](docs/openapi.yaml).

---

## 2. Technology Stack

- **Java JDK**: Java 25 LTS
- **Framework**: Spring Boot 4.1.1
- **Security**: Spring Security 6.x (Stateless `SecurityFilterChain`)
- **Persistence**: Spring Data JPA / Hibernate ORM
- **Database**: PostgreSQL 16 (Alpine)
- **Object Mapping**: MapStruct 1.6.2 (compile-time mappers, pure functions)
- **Frontend SPA**: React 18, TypeScript, Vite
- **Web Server / Reverse Proxy**: Nginx Alpine
- **Testing**: JUnit 5, Mockito, MockMvc, DataJpaTest (with H2 in PostgreSQL compatibility mode)
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
2. `paperless-rest`: Backend REST API on port `8081` (waits until PostgreSQL is healthy).
3. `paperless-webui`: Web Server & React SPA on port `80` (reverse proxies `/api/` to `paperless-rest:8081`).

### Step 2: Access the Application
- **Paperless Web UI**: Open your browser at [http://localhost/](http://localhost/)
- **Backend Health Check**: [http://localhost/api/v1/health](http://localhost/api/v1/health) (or directly via [http://localhost:8081/api/v1/health](http://localhost:8081/api/v1/health))
- **REST API Endpoints**: [http://localhost/api/v1/documents](http://localhost/api/v1/documents)

### Step 3: Verify Service Status
```bash
docker compose ps
```
All containers should report `running` (and postgres `healthy`).

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
./gradlew test
```

---

## 5. API Documentation & Test Scripts

- **OpenAPI 3.0 Specification**: Located at `docs/openapi.yaml`.
