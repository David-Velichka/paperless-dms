import React, { useState } from 'react';
import { DocumentItem } from '../types';

interface DeleteModalProps {
  document: DocumentItem | null;
  onClose: () => void;
  onConfirm: (docId: number) => Promise<void>;
}

export const DeleteModal: React.FC<DeleteModalProps> = ({
  document,
  onClose,
  onConfirm
}) => {
  const [isDeleting, setIsDeleting] = useState(false);

  if (!document) return null;

  const handleConfirm = async () => {
    setIsDeleting(true);
    try {
      await onConfirm(document.id);
      onClose();
    } finally {
      setIsDeleting(false);
    }
  };

  return (
    <div
      id="delete-modal"
      className="modal-overlay active"
      onClick={(e) => {
        if (e.target === e.currentTarget) onClose();
      }}
    >
      <div className="modal-content" style={{ maxWidth: '440px' }}>
        <div className="modal-header">
          <h2 className="modal-title">Delete Document</h2>
          <button
            type="button"
            className="modal-close"
            id="delete-modal-close"
            onClick={onClose}
          >
            &times;
          </button>
        </div>
        <div className="modal-body">
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem' }}>
            Are you sure you want to permanently delete document{' '}
            <strong id="delete-doc-title" style={{ color: 'var(--text-primary)' }}>
              {document.title || document.originalFilename}
            </strong>
            ?
          </p>
          <p
            style={{
              color: '#f87171',
              fontSize: '0.8125rem',
              marginTop: '0.5rem'
            }}
          >
            This action will also cascade and remove all associated status history
            audit records from PostgreSQL.
          </p>
        </div>
        <div className="modal-footer">
          <button
            type="button"
            className="btn btn-secondary"
            id="btn-cancel-delete"
            onClick={onClose}
          >
            Cancel
          </button>
          <button
            type="button"
            className="btn btn-danger"
            id="btn-confirm-delete"
            disabled={isDeleting}
            onClick={handleConfirm}
          >
            {isDeleting ? 'Deleting...' : 'Delete Document'}
          </button>
        </div>
      </div>
    </div>
  );
};

