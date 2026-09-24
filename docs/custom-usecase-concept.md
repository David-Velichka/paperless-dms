# Custom Use-Case Concept: Document State & Status History

## 1. Selected Use-Case
- Choice: Recommendation 2 from course suggestions (Document state with history log).
- Goal: Track lifecycle state of each document (e.g., received, processing, completed, rejected) with a full, immutable audit trail (who changed what, when, and why).

---

## 2. Entity Definitions (1:n Relation)

### A. Document Entity (Updated)
- `currentStatus`: Current lifecycle state of the document (`RECEIVED`, `PROCESSING`, `REVIEW_PENDING`, `COMPLETED`, `REJECTED`).

### B. DocumentStatusHistory Entity
- `id`: Long (PK, auto-increment)
- `documentId`: Long (FK to documents table, not null)
- `status`: String / Enum (new state)
- `previousStatus`: String / Enum (state before this change, null on initial upload)
- `changedBy`: String (actor: username or system worker, max 100 chars, not null)
- `timestamp`: OffsetDateTime (timestamp of transition, not null)
- `comment`: String (optional note / reason, max 500 chars)

### C. Relationship
- `Document (1) <---> (n) DocumentStatusHistory`
- 1 Document has 0..n status history entries.
- Each history entry belongs to exactly 1 Document.

---

## 3. Business Components Concept

### Validation
- Transition check: Target status must be a valid follow-up state from current status (e.g., cannot jump directly from `REJECTED` to `COMPLETED`).
- Document check: Document ID must exist in database before creating history entry.
- Actor check: `changedBy` must not be blank or null, max 100 characters.
- Comment check: Optional, but max 500 characters if provided.
- Input validation: Jakarta Bean Validation (`@NotNull`, `@Size`) at boundary, custom business checks in service.

### Rules
- Rule 1 (Initial State): Uploading a document automatically assigns status `RECEIVED`.
- Rule 2 (Initial History): Creating a document automatically writes the first history entry (`status: RECEIVED, previousStatus: null, comment: "Document uploaded"`).
- Rule 3 (Immutability): Status history entries are append-only. No update or manual delete allowed (audit trail integrity).
- Rule 4 (Lifecycle Binding): When a document is deleted, all associated status history entries are cascade-deleted.

### Logic
- `changeStatus(documentId, targetStatus, changedBy, comment)`:
  1. Fetch Document by ID -> throw `BusinessLayerException` if missing.
  2. Verify transition is permitted (`currentStatus -> targetStatus`) -> throw `BusinessLayerException` if invalid.
  3. Update `document.currentStatus = targetStatus`.
  4. Create `DocumentStatusHistory` record with `previousStatus = oldStatus`, `status = targetStatus`, `changedBy`, `timestamp = now()`, `comment`.
  5. Save Document and save History in single `@Transactional` method.
  6. Return updated domain model.
- `getStatusHistory(documentId)`:
  1. Verify Document exists -> throw `BusinessLayerException` if missing.
  2. Fetch list from repository ordered by timestamp descending.
  3. Map DAL entities to BL domain models via MapStruct.

### Persistence
- PostgreSQL Tables:
  - `documents`: contains `current_status` column.
  - `document_status_histories`: table with foreign key `document_id REFERENCES documents(id) ON DELETE CASCADE`.
- Spring Data JPA:
  - `DocumentRepository`: standard CRUD + update status.
  - `DocumentStatusHistoryRepository`: derived query `List<DocumentStatusHistory> findByDocumentIdOrderByTimestampDesc(Long documentId)`.

---

## 4. Business Workflow Outline

1. **Step 1: Document Ingestion (Upload)**
   - Client sends PDF via `POST /api/documents/upload`.
   - Business service saves document with `currentStatus = RECEIVED`.
   - Business service logs initial history record: `(status: RECEIVED, changedBy: "uploader", comment: "Initial upload")`.

2. **Step 2: Processing (OCR / Inspection)**
   - Background worker or processor picks up document.
   - Status transitions to `PROCESSING` with `changedBy = "ocr-worker"`.
   - History entry recorded.

3. **Step 3: Verification (Review Pending)**
   - Text extracted / processing finished.
   - Status updated to `REVIEW_PENDING`.
   - History entry recorded.

4. **Step 4: Final Decision (Approval / Rejection)**
   - Reviewer / User inspects document via WebUI.
   - Action: Approve -> status becomes `COMPLETED`.
   - Action: Reject -> status becomes `REJECTED` with required comment.
   - History entry recorded.

5. **Step 5: Audit Trail Query**
   - User views document details in UI.
   - Client queries `GET /api/documents/{id}/history`.
   - Returns full chronological log of all transitions, timestamps, actors, and comments.
