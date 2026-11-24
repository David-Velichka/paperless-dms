import React, { useState, useEffect } from 'react';
import { DocumentItem, DocumentStatus, DocumentStatusHistory } from '../types';
import {
  formatBytes,
  formatDate,
  formatStatus,
  getDocStatus,
  STATUS_TRANSITIONS
} from '../utils/formatters';

interface DetailModalProps {
  document: DocumentItem | null;
  history: DocumentStatusHistory[];
  isLoadingHistory: boolean;
  onClose: () => void;
  onUpdateStatus: (
    docId: number,
    targetStatus: DocumentStatus,
    comment: string
  ) => Promise<void>;
  onUpdateTitle: (docId: number, title: string) => Promise<void>;
}

export const DetailModal: React.FC<DetailModalProps> = ({
  document,
  history,
  isLoadingHistory,
  onClose,
  onUpdateStatus,
  onUpdateTitle
}) => {
  const [selectedNextStatus, setSelectedNextStatus] = useState<DocumentStatus | ''>('');
  const [comment, setComment] = useState('');
  const [validationError, setValidationError] = useState<string | null>(null);
  const [isUpdating, setIsUpdating] = useState(false);
  const [titleInput, setTitleInput] = useState('');
  const [titleError, setTitleError] = useState<string | null>(null);
  const [isSavingTitle, setIsSavingTitle] = useState(false);

  const documentId = document ? document.id : null;
  const currentStatus = document ? getDocStatus(document) : null;

  // Another document: start with empty inputs, so a refresh or status change keeps what the user typed
  useEffect(() => {
    if (document) {
      setTitleInput(document.title || '');
      setTitleError(null);
      setComment('');
      setValidationError(null);
    }
  }, [documentId]);

  // New status: keep the selected transition if it is still allowed, otherwise offer the first allowed one
  useEffect(() => {
    if (currentStatus) {
      const allowed = STATUS_TRANSITIONS[currentStatus] || [];
      setSelectedNextStatus((prev) =>
        prev !== '' && allowed.includes(prev) ? prev : (allowed.length > 0 ? allowed[0] : '')
      );
    }
  }, [documentId, currentStatus]);

  if (!document) return null;

  const docStatus = getDocStatus(document);
  const nextStates = STATUS_TRANSITIONS[docStatus] || [];

  const handleApplyTransition = async () => {
    if (!selectedNextStatus) {
      setValidationError('Please select a valid target status.');
      return;
    }
    if (comment.length > 500) {
      setValidationError('Comment must not exceed 500 characters.');
      return;
    }

    setValidationError(null);
    setIsUpdating(true);
    try {
      await onUpdateStatus(document.id, selectedNextStatus, comment.trim());
      setComment('');
    } catch (err: unknown) {
      if (err instanceof Error) {
        setValidationError(err.message);
      } else {
        setValidationError('Failed to update status. Please try again.');
      }
    } finally {
      setIsUpdating(false);
    }
  };

  const handleSaveTitle = async () => {
    const trimmed = titleInput.trim();
    if (!trimmed) {
      setTitleError('Title must not be blank.');
      return;
    }
    if (trimmed.length > 255) {
      setTitleError('Title must not exceed 255 characters.');
      return;
    }

    setTitleError(null);
    setIsSavingTitle(true);
    try {
      await onUpdateTitle(document.id, trimmed);
    } catch (err: unknown) {
      setTitleError(err instanceof Error ? err.message : 'Failed to update title.');
    } finally {
      setIsSavingTitle(false);
    }
  };

  return (
    <div
      id="detail-modal"
      className="modal-overlay active"
      onClick={(e) => {
        if (e.target === e.currentTarget) onClose();
      }}
    >
      <div className="modal-content">
        <div className="modal-header">
          <h2 className="modal-title">Document Details &amp; Lifecycle</h2>
          <button
            type="button"
            className="modal-close"
            id="detail-modal-close"
            onClick={onClose}
          >
            &times;
          </button>
        </div>
        <div className="modal-body">
          {/* Metadata Grid */}
          <div
            style={{
              display: 'grid',
              gridTemplateColumns: '1fr 1fr',
              gap: '1rem',
              marginBottom: '1.5rem',
              background: 'rgba(255, 255, 255, 0.02)',
              padding: '1rem',
              borderRadius: 'var(--radius-md)',
              border: '1px solid var(--border-subtle)'
            }}
          >
            <div>
              <div
                style={{
                  fontSize: '0.75rem',
                  color: 'var(--text-muted)',
                  textTransform: 'uppercase'
                }}
              >
                Document ID
              </div>
              <div id="detail-id" style={{ fontWeight: 600, fontSize: '0.9375rem' }}>
                #{document.id}
              </div>
            </div>
            <div>
              <div
                style={{
                  fontSize: '0.75rem',
                  color: 'var(--text-muted)',
                  textTransform: 'uppercase'
                }}
              >
                Current Status
              </div>
              <div style={{ marginTop: '2px' }}>
                <span id="detail-status-badge" className={`badge badge-${docStatus}`}>
                  {formatStatus(docStatus)}
                </span>
              </div>
            </div>
            <div>
              <div
                style={{
                  fontSize: '0.75rem',
                  color: 'var(--text-muted)',
                  textTransform: 'uppercase'
                }}
              >
                Title
              </div>
              <div
                id="detail-title"
                style={{ fontWeight: 500, fontSize: '0.875rem' }}
              >
                {document.title || document.originalFilename}
              </div>
            </div>
            <div>
              <div
                style={{
                  fontSize: '0.75rem',
                  color: 'var(--text-muted)',
                  textTransform: 'uppercase'
                }}
              >
                Original Filename
              </div>
              <div
                id="detail-filename"
                style={{ fontWeight: 500, fontSize: '0.875rem' }}
              >
                {document.originalFilename}
              </div>
            </div>
            <div>
              <div
                style={{
                  fontSize: '0.75rem',
                  color: 'var(--text-muted)',
                  textTransform: 'uppercase'
                }}
              >
                File Size
              </div>
              <div id="detail-size" style={{ fontWeight: 500, fontSize: '0.875rem' }}>
                {formatBytes(document.fileSize)}
              </div>
            </div>
            <div>
              <div
                style={{
                  fontSize: '0.75rem',
                  color: 'var(--text-muted)',
                  textTransform: 'uppercase'
                }}
              >
                Uploaded At
              </div>
              <div
                id="detail-created"
                style={{ fontWeight: 500, fontSize: '0.875rem' }}
              >
                {formatDate(document.createdAt)}
              </div>
            </div>
          </div>

          {/* Title edit */}
          <div id="title-edit-container" style={{ marginBottom: '1.5rem' }}>
            <div className="form-group" style={{ marginBottom: '0.75rem' }}>
              <label htmlFor="title-input" className="form-label">
                Edit Title
              </label>
              <input
                type="text"
                id="title-input"
                className="form-control"
                value={titleInput}
                onChange={(e) => {
                  setTitleInput(e.target.value);
                  setTitleError(null);
                }}
              />
            </div>
            {titleError && (
              <div
                id="title-error-alert"
                style={{
                  padding: '0.625rem 0.75rem',
                  borderRadius: 'var(--radius-sm)',
                  backgroundColor: 'rgba(239, 68, 68, 0.15)',
                  border: '1px solid rgba(239, 68, 68, 0.3)',
                  color: '#f87171',
                  fontSize: '0.8125rem',
                  marginBottom: '0.75rem'
                }}
              >
                {titleError}
              </div>
            )}
            <button
              type="button"
              id="btn-save-title"
              className="btn btn-secondary btn-sm"
              style={{ width: '100%', justifyContent: 'center' }}
              disabled={isSavingTitle || titleInput.trim() === (document.title || '')}
              onClick={handleSaveTitle}
            >
              {isSavingTitle ? 'Saving...' : 'Save Title'}
            </button>
          </div>

          {/* Recognized text from the OCR worker (read-only) */}
          <div id="ocr-text-container" style={{ marginBottom: '1.5rem' }}>
            <h3
              style={{
                fontSize: '0.9375rem',
                fontWeight: 600,
                marginBottom: '0.5rem',
                color: 'var(--text-primary)'
              }}
            >
              Recognized Text (OCR)
            </h3>
            {document.ocrText ? (
              <div
                id="detail-ocr-text"
                style={{
                  whiteSpace: 'pre-wrap',
                  maxHeight: '200px',
                  overflow: 'auto',
                  fontSize: '0.8125rem',
                  background: 'rgba(255, 255, 255, 0.02)',
                  padding: '0.75rem',
                  borderRadius: 'var(--radius-md)',
                  border: '1px solid var(--border-subtle)'
                }}
              >
                {document.ocrText}
              </div>
            ) : (
              <div
                id="detail-ocr-hint"
                style={{ color: 'var(--text-muted)', fontSize: '0.8125rem' }}
              >
                {docStatus === 'RECEIVED' || docStatus === 'PROCESSING'
                  ? 'Text recognition is still running...'
                  : 'No recognized text available.'}
              </div>
            )}
          </div>

          {/* Status Transition Section (Custom Use Case 4) */}
          {nextStates.length > 0 && (
            <div id="transition-container" style={{ marginBottom: '1.5rem' }}>
              <h3
                style={{
                  fontSize: '0.9375rem',
                  fontWeight: 600,
                  marginBottom: '0.75rem',
                  color: 'var(--text-primary)'
                }}
              >
                Trigger Status Transition
              </h3>
              <div className="form-group">
                <label htmlFor="target-status-select" className="form-label">
                  Permissible Next Status
                </label>
                <select
                  id="target-status-select"
                  className="form-control"
                  value={selectedNextStatus}
                  onChange={(e) =>
                    setSelectedNextStatus(e.target.value as DocumentStatus)
                  }
                >
                  {nextStates.map((status) => (
                    <option key={status} value={status}>
                      Transition to: {formatStatus(status)}
                    </option>
                  ))}
                </select>
              </div>
              <div className="form-group" style={{ marginBottom: '0.75rem' }}>
                <label htmlFor="comment-input" className="form-label">
                  Transition Comment / Rationale
                </label>
                <input
                  type="text"
                  id="comment-input"
                  className="form-control"
                  placeholder="Optional audit comment"
                  value={comment}
                  onChange={(e) => setComment(e.target.value)}
                />
              </div>
              {validationError && (
                <div
                  id="transition-error-alert"
                  style={{
                    padding: '0.625rem 0.75rem',
                    borderRadius: 'var(--radius-sm)',
                    backgroundColor: 'rgba(239, 68, 68, 0.15)',
                    border: '1px solid rgba(239, 68, 68, 0.3)',
                    color: '#f87171',
                    fontSize: '0.8125rem',
                    marginBottom: '0.75rem'
                  }}
                >
                  {validationError}
                </div>
              )}
              <button
                type="button"
                id="btn-update-status"
                className="btn btn-primary btn-sm"
                style={{ width: '100%', justifyContent: 'center' }}
                disabled={isUpdating || !selectedNextStatus}
                onClick={handleApplyTransition}
              >
                {isUpdating ? 'Updating...' : 'Apply Transition'}
              </button>
            </div>
          )}

          {/* Audit History Timeline */}
          <div>
            <h3
              style={{
                fontSize: '0.9375rem',
                fontWeight: 600,
                marginBottom: '0.5rem',
                color: 'var(--text-primary)'
              }}
            >
              Lifecycle Audit History
            </h3>
            <div id="history-timeline" className="timeline">
              {isLoadingHistory ? (
                <div style={{ color: 'var(--text-muted)', fontSize: '0.8125rem' }}>
                  Loading audit trail...
                </div>
              ) : history.length === 0 ? (
                <div style={{ color: 'var(--text-muted)', fontSize: '0.8125rem' }}>
                  No history records found.
                </div>
              ) : (
                history.map((item) => (
                  <div key={item.id} className="timeline-item">
                    <div className="timeline-dot" />
                    <div className="timeline-header">
                      <span className={`badge badge-${item.status}`}>
                        {formatStatus(item.status)}
                      </span>
                      <span className="timeline-time">{formatDate(item.timestamp)}</span>
                    </div>
                    {item.comment && (
                      <div className="timeline-comment">{item.comment}</div>
                    )}
                  </div>
                ))
              )}
            </div>
          </div>
        </div>
        <div className="modal-footer">
          <button
            type="button"
            className="btn btn-secondary"
            id="detail-modal-close-footer"
            onClick={onClose}
          >
            Close
          </button>
        </div>
      </div>
    </div>
  );
};
