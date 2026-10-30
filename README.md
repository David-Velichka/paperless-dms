# Paperless: Document Management System (Paperless DMS)

A lightweight Document Management System developed as part of the **Software Engineering 3 (SWEN3)** course at FH Technikum Wien.

## 1. Technology Stack

- **Java JDK**: Java 25 LTS
- **Framework**: Spring Boot 4.1.1
- **Security**: Spring Security 7.1 (Stateless `SecurityFilterChain`)
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

## 2. Running the System Locally

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

## 3. Running Automated Tests

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

## 4. API Documentation & Test Scripts

- **OpenAPI 3.0 Specification**: Located at `docs/openapi.yaml`.
- **Architecture & Design Decisions**: Located at `docs/architecture.md`.
- **Test Scripts**: `tests/document-upload-tests.http`, `tests/custom-usecase-tests.http`, `tests/document-upload-tests.ps1/.sh`, `tests/sprint3-queue-check.ps1/.sh`.
