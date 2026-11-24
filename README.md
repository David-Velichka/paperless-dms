# Paperless: Document Management System (Paperless DMS)

A lightweight Document Management System developed as part of the **Software Engineering 3 (SWEN3)** course at FH Technikum Wien.

## 1. Technology Stack

- **Java JDK**: Java 25 LTS
- **Framework**: Spring Boot 4.2.0
- **Security**: Spring Security 7.1 (Stateless `SecurityFilterChain`)
- **Persistence**: Spring Data JPA / Hibernate ORM
- **Database**: PostgreSQL 16 (Alpine)
- **Message Broker**: RabbitMQ 4 (Spring AMQP, JSON messages, dead-letter queues for failed OCR jobs and OCR results)
- **Object Storage**: RustFS 1.0.6 (S3 compatible, accessed with the AWS SDK for Java v2)
- **OCR**: Tesseract (Tess4J 5.21.0) with Ghostscript for the PDF to image conversion
- **Search**: Elasticsearch 9.5.6 (Spring Data Elasticsearch, full-text and fuzzy search), Kibana 9.5.6
- **Stability**: Resilience4j circuit breaker with fallback on the search, timeouts on all outbound clients
- **Object Mapping**: MapStruct 1.6.3 (compile-time mappers, pure functions)
- **Frontend SPA**: React 19, TypeScript, Vite
- **Web Server / Reverse Proxy**: Nginx Alpine
- **Testing**: JUnit 5, Mockito, MockMvc, DataJpaTest (with H2 in PostgreSQL compatibility mode), JaCoCo coverage
- **CI**: GitHub Actions
- **Orchestration**: Docker Compose with multi-container health checks

---

## 2. Running the System Locally

### Prerequisites
- Docker & Docker Compose installed and running. Give Docker Desktop about 4 GB of memory, because Elasticsearch and Kibana need it.
- (Optional for local development) JDK 25 and Node.js 26+.

### Step 0: Create the `.env` file
User names and passwords are read from a `.env` file. Copy the example file and set your own values:

```bash
cp .env.example .env          # Windows PowerShell: Copy-Item .env.example .env
```

| Variable | Used for |
|---|---|
| `POSTGRES_USER`, `POSTGRES_PASSWORD` | PostgreSQL container and the database login of `paperless-rest` |
| `RABBITMQ_DEFAULT_USER`, `RABBITMQ_DEFAULT_PASS` | RabbitMQ container, `paperless-rest` and `paperless-ocr-worker` |
| `RUSTFS_ACCESS_KEY`, `RUSTFS_SECRET_KEY` | RustFS object storage and its console login (access key at least 3, secret key at least 8 characters, empty values are refused) |

`.env` is listed in `.gitignore` and must never be committed. If you change the PostgreSQL values after the first start, the existing database volume keeps the old login (use `docker compose down -v` to recreate it).

### Step 1: Start Services with Docker Compose
From the repository root (`paperless-dms/`):

```bash
docker compose up -d --build
```

This starts:
1. `paperless-postgres`: PostgreSQL database on port `5432`.
2. `paperless-rabbitmq`: RabbitMQ broker on port `5672`, management UI on port `15672` (login: `RABBITMQ_DEFAULT_USER` / `RABBITMQ_DEFAULT_PASS` from your `.env`).
3. `paperless-rustfs`: RustFS object storage (S3 API) on port `9000`, web console on port `9090` (login: `RUSTFS_ACCESS_KEY` / `RUSTFS_SECRET_KEY` from your `.env`).
4. `paperless-elasticsearch`: Elasticsearch (single node, security disabled for local use) on port `9200`.
5. `paperless-kibana`: Kibana on port `5601` (waits until Elasticsearch is healthy).
6. `paperless-rest`: Backend REST API on port `8081` (waits until PostgreSQL, RabbitMQ, RustFS and Elasticsearch are healthy).
7. `paperless-ocr-worker`: OCR worker consuming `paperless.ocr.queue`: loads the PDF from RustFS, runs Ghostscript and Tesseract, indexes the text in Elasticsearch and reports the result back on `paperless.ocr.result.queue` (waits until RabbitMQ, RustFS and Elasticsearch are healthy).
8. `paperless-webui`: Web Server & React SPA on port `80` (reverse proxies `/api/` to `paperless-rest:8081`).

### Step 2: Access the Application
| Service | Port | URL | Login |
|---|---|---|---|
| Paperless Web UI (nginx, proxies `/api/` to the REST server) | 80 | [http://localhost/](http://localhost/) | none |
| REST API via nginx | 80 | [http://localhost/api/v1/documents](http://localhost/api/v1/documents) | none |
| Backend health check | 80 / 8081 | [http://localhost/api/v1/health](http://localhost/api/v1/health) | none |
| Paperless REST server (direct) | 8081 | [http://localhost:8081](http://localhost:8081) | none |
| Swagger UI (generated API documentation, backend port only) | 8081 | [http://localhost:8081/swagger-ui/index.html](http://localhost:8081/swagger-ui/index.html) | none |
| RabbitMQ Management UI | 15672 | [http://localhost:15672](http://localhost:15672) | `RABBITMQ_DEFAULT_USER` / `RABBITMQ_DEFAULT_PASS` from `.env` |
| RabbitMQ AMQP broker | 5672 | `amqp://localhost:5672` | same as above |
| RustFS Console | 9090 | [http://localhost:9090](http://localhost:9090) | `RUSTFS_ACCESS_KEY` / `RUSTFS_SECRET_KEY` from `.env` |
| RustFS S3 API | 9000 | [http://localhost:9000](http://localhost:9000) | same as above |
| Elasticsearch (returns the version JSON) | 9200 | [http://localhost:9200](http://localhost:9200) | none (security disabled for local use) |
| Kibana (needs about a minute after the start) | 5601 | [http://localhost:5601](http://localhost:5601) | none |
| PostgreSQL (database `paperless`) | 5432 | `localhost:5432` | `POSTGRES_USER` / `POSTGRES_PASSWORD` from `.env` |

The OCR worker (`paperless-ocr-worker`) has no port, it only talks to RabbitMQ, RustFS and Elasticsearch.

### Sprint 3 Check: Upload leads to a worker log entry
Upload a PDF via the Web UI or the script below, then look at the worker log:

```bash
.\tests\sprint3-queue-check.ps1
docker compose logs paperless-ocr-worker
# ... Received OCR job documentId=1 filename='sample.pdf' contentType=application/pdf uploadedAt=... storageKey=...
```

### Sprint 4 Check: Upload HelloWorld.pdf and search "Hello"
1. Open [http://localhost/](http://localhost/) and upload `tests/sample-documents/HelloWorld.pdf`.
2. Wait until the status badge shows `Review Pending` (about 10 to 30 seconds, the list refreshes by itself).
3. Type `Hello` into the search box: the document is listed. The search is fuzzy, `Helo` finds it as well.

### Step 3: Verify Service Status
```bash
docker compose ps
```
All containers should report `running` (postgres, rabbitmq, rustfs and elasticsearch `healthy`).

**Existing RabbitMQ volume:** `paperless.ocr.queue` and `paperless.ocr.result.queue` are declared with a dead-letter exchange (`paperless.dlx`). Failed OCR jobs land in `paperless.ocr.dlq`, OCR results the REST server could not store land in `paperless.ocr.result.dlq`. A broker that still holds one of the old queues without these arguments refuses the new declaration with `PRECONDITION_FAILED`, and `paperless-rest` and `paperless-ocr-worker` do not start. Delete both queues once in the RabbitMQ Management UI (Queues and Streams, Delete) or run `docker compose down -v`. Fresh clones are not affected.

### Troubleshooting
- **Elasticsearch exits with `vm.max_map_count` too low** (Docker Desktop on Windows): `wsl -d docker-desktop sysctl -w vm.max_map_count=262144`, then `docker compose up -d` again.
- **Web UI answers `502 Bad Gateway`** after only `paperless-rest` was recreated: nginx still points to the old container address. Run `docker compose restart paperless-webui`.
- **`docker compose build` stops with `additional privileges requested`** (some Docker Compose versions on Windows): `$env:BUILDX_BAKE_ENTITLEMENTS_FS=0; docker compose build` (PowerShell) or `BUILDX_BAKE_ENTITLEMENTS_FS=0 docker compose build` (bash).
- **A document stays `PROCESSING`**: usually the OCR job failed after all retries and is in `paperless.ocr.dlq` (RabbitMQ Management UI), see `docker compose logs paperless-ocr-worker`. If the worker logged `OCR result sent ... status=COMPLETED`, the REST server could not store the result (for example PostgreSQL was down) and the event is in `paperless.ocr.result.dlq`. Once the cause is fixed, move it back with the RabbitMQ Management UI (Queues and Streams, `paperless.ocr.result.dlq`, Get messages, then publish the payload again to `paperless.document.exchange` with routing key `document.ocr.result`). The document can also be rejected in the web UI.

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
./gradlew check         # tests + JaCoCo, also fails below 70% line coverage
```

`TesseractOcrEngineTest` runs a real OCR on `HelloWorld.pdf`. It is skipped when `gs` or Tesseract is not installed (for example on Windows) and runs in CI, where both are installed.

The same checks run automatically in GitHub Actions on every push to `main` and the sprint branches and on every pull request to `main`.

---

## 4. API Documentation & Test Scripts

- **OpenAPI 3.0 Specification**: Located at `docs/openapi.yaml`.
- **Architecture & Design Decisions**: Located at `docs/architecture.md`.
- **Test scripts** (`tests/`): `document-upload-tests.http` / `.ps1` (upload, search, update), `custom-usecase-tests.http` (status and history), `sprint3-queue-check.ps1` (upload reaches the worker).

---

## 5. Document Status Lifecycle

Every document has a status. A change is only accepted along these transitions, every other change is answered with `422 Unprocessable Content` (see `docs/openapi.yaml`):

| From | Allowed target status |
|---|---|
| `RECEIVED` | `PROCESSING`, `REJECTED` |
| `PROCESSING` | `REVIEW_PENDING`, `REJECTED` |
| `REVIEW_PENDING` | `COMPLETED`, `REJECTED`, `PROCESSING` |
| `COMPLETED` | none (final) |
| `REJECTED` | none (final) |

The first two steps happen automatically: when the OCR worker starts a job the document moves to `PROCESSING`, when the text is recognized and indexed it moves to `REVIEW_PENDING`. A user then completes or rejects it in the web UI, which offers all 7 transitions above. Every change is stored in the status history with an optional comment (no comment is stored as `null`).