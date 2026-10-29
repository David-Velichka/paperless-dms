# Changelog

All notable changes to the Paperless Document Management System will be documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.3.0] - 2026-10-30
### Added
- RabbitMQ 4 broker container (`paperless-rabbitmq`) with management UI and health check in `docker-compose.yml`.
- `OcrMessageProducer` publishes an `OcrMessage` to `paperless.ocr.queue` on every document upload (transacted channel, delivered after the database commit).
- New `paperless-ocr-worker` Spring Boot application consuming the OCR queue and logging received jobs (OCR processing itself follows in Sprint 4).
- `MessageQueueException` mapped to HTTP 503 Service Unavailable when the broker is not reachable.
- `PUT /api/v1/documents/{id}` to update document metadata (title).
- GitHub Actions CI workflow (backend tests, worker tests, web UI build, `docker compose build`).
- JaCoCo coverage report with 70% line coverage gate, domain model unit tests.
- `docs/architecture.md` with component and sequence diagrams, design decisions, testing strategy and lessons learned.
- `tests/sprint3-queue-check.ps1` and `.sh` for the Sprint 3 queue check.

### Changed
- Document upload is one transaction: if the OCR message cannot be sent, metadata and history are rolled back.
- `DocumentController` uses the CRUD specific DTOs (`DocumentCreateDto`, `DocumentUpdateDto`, `DocumentResponseDto`).
- Empty upload now throws `InvalidDocumentException` and returns a 400 ProblemDetail instead of an empty body.
- Gradle foojay toolchain resolver updated to 1.0.0 (required for Gradle 9).

### Removed
- Legacy composite `DocumentDto` and its mapper methods.

### Fixed
- Invalid YAML in `openapi.yaml` (unquoted example value).
- Responses after saving an existing document now contain the new `version` and `modifiedAt` (`saveAndFlush` in `DocumentPersistenceService`).
- Upload no longer hangs up to 60s when RabbitMQ is down: `spring.rabbitmq.connection-timeout: 5s`, then 503.

## [0.2.2] - 2026-10-20
### Added
- Standard Spring Security `SecurityFilterChain` (`permitAll()`) providing baseline security infrastructure without requiring authentication credentials.
- Wired semantic domain exceptions: `DocumentNotFoundException` (HTTP 404 Not Found) and `InvalidStatusTransitionException` (HTTP 422 Unprocessable Content) in business services and domain models adhering to RFC 7807 `ProblemDetail`.

### Changed
- Completely removed residual mock actor fields (`changedBy`) across entities, database schema, domain models, DTOs, service interfaces, and frontend UI components.
- Aligned test scripts (`document-upload-tests.ps1`, `document-upload-tests.sh`, `document-upload-tests.http`, `custom-usecase-tests.http`) and architectural concept documentation to `/api/v1/*` routes.
- Updated project `README.md` with complete Sprint 2 deliverables, multi-container access instructions (`http://localhost/`), and test guidelines.

## [0.2.1] - 2026-10-20
### Removed
- Removed user authentication, JWT tokens, login endpoints (`/api/v1/auth/login`, `/api/v1/auth/me`), and user management per course lecturer guidance to streamline architecture and eliminate out-of-scope complexity.

## [0.2.0] - 2026-10-15
### Added
- React 18 TypeScript Single-Page Application (SPA) in `paperless-webui`.
- Nginx Alpine reverse proxy routing static assets on `/` and API requests on `/api/v1/`.
- Multi-layer form and model validation with `@ValidFilename` path traversal checks.
- Optimistic Concurrency Control (`@Version`) with HTTP 409 Conflict handling.

### Changed
- Migrated REST endpoints to versioned paths (`/api/v1/*`).
- Standardized error responses on RFC 7807 `ProblemDetail`.
- Segmented monolithic DTOs into CRUD command and query DTOs (`DocumentCreateDto`, `DocumentUpdateDto`, `DocumentResponseDto`).
- Enforced Anti-Corruption Layer (ACL) via dedicated persistence services (`DocumentPersistenceService`, `DocumentStatusHistoryPersistenceService`).
- Made DELETE endpoints strictly idempotent returning HTTP 204 No Content.

## [0.1.0] - 2026-10-01
### Added
- Spring Boot 4 REST API server (`paperless-rest`).
- PostgreSQL 16 containerized relational database persistence.
- Document upload endpoint (`POST /api/v1/documents/upload`).
- Status history aggregate (4th use case) tracking document lifecycle transitions.
- Compile-time MapStruct mapping between domain models and DAL entities.
