# Changelog

All notable changes to the Paperless Document Management System will be documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
