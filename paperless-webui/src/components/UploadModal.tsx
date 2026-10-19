import React, { useState, useRef } from 'react';
import { formatBytes } from '../utils/formatters';

interface UploadModalProps {
  isOpen: boolean;
  onClose: () => void;
  onUpload: (file: File, title: string) => Promise<void>;
}

const MAX_FILE_SIZE_BYTES = 50 * 1024 * 1024; // 50 MB limit
const MAX_TITLE_LENGTH = 255;

export const UploadModal: React.FC<UploadModalProps> = ({
  isOpen,
  onClose,
  onUpload
}) => {
  const [title, setTitle] = useState('');
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [validationError, setValidationError] = useState<string | null>(null);
  const [isDragging, setIsDragging] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  if (!isOpen) return null;

  const handleClose = () => {
    setTitle('');
    setSelectedFile(null);
    setValidationError(null);
    setIsDragging(false);
    onClose();
  };

  const validateFile = (file: File): string | null => {
    const isPdf = file.name.toLowerCase().endsWith('.pdf') || file.type === 'application/pdf';
    if (!isPdf) {
      return 'Invalid file type: Only PDF documents (*.pdf) are supported.';
    }
    if (file.size <= 0) {
      return 'Invalid file: The selected file is empty.';
    }
    if (file.size > MAX_FILE_SIZE_BYTES) {
      return `File size (${formatBytes(file.size)}) exceeds the maximum allowed limit of 50 MB.`;
    }
    return null;
  };

  const handleFileSelection = (file: File) => {
    const errorMsg = validateFile(file);
    if (errorMsg) {
      setValidationError(errorMsg);
      setSelectedFile(null);
    } else {
      setValidationError(null);
      setSelectedFile(file);
    }
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files.length > 0) {
      handleFileSelection(e.target.files[0]);
    }
  };

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(true);
  };

  const handleDragLeave = () => {
    setIsDragging(false);
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(false);
    if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
      handleFileSelection(e.dataTransfer.files[0]);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedFile) {
      setValidationError('Please select a PDF file to upload.');
      return;
    }

    const fileError = validateFile(selectedFile);
    if (fileError) {
      setValidationError(fileError);
      return;
    }

    if (title.length > MAX_TITLE_LENGTH) {
      setValidationError(`Document title must not exceed ${MAX_TITLE_LENGTH} characters.`);
      return;
    }

    setValidationError(null);
    setIsSubmitting(true);
    try {
      await onUpload(selectedFile, title.trim());
      handleClose();
    } catch (err: unknown) {
      if (err instanceof Error) {
        setValidationError(err.message);
      } else {
        setValidationError('Upload failed. Please check your network and credentials.');
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div
      id="upload-modal"
      className="modal-overlay active"
      onClick={(e) => {
        if (e.target === e.currentTarget) handleClose();
      }}
    >
      <div className="modal-content">
        <div className="modal-header">
          <h2 className="modal-title">Upload PDF Document</h2>
          <button
            type="button"
            className="modal-close"
            id="upload-modal-close"
            onClick={handleClose}
          >
            &times;
          </button>
        </div>
        <form id="upload-form" onSubmit={handleSubmit}>
          <div className="modal-body">
            <div className="form-group">
              <label htmlFor="doc-title-input" className="form-label">
                Document Title (Optional, max 255 chars)
              </label>
              <input
                type="text"
                id="doc-title-input"
                className="form-control"
                placeholder="e.g., Annual Financial Report 2026"
                maxLength={MAX_TITLE_LENGTH}
                value={title}
                onChange={(e) => {
                  setTitle(e.target.value);
                  if (e.target.value.length > MAX_TITLE_LENGTH) {
                    setValidationError(`Title cannot exceed ${MAX_TITLE_LENGTH} characters.`);
                  } else if (validationError && validationError.includes('Title')) {
                    setValidationError(null);
                  }
                }}
              />
            </div>

            <div className="form-group">
              <label className="form-label">PDF File *</label>
              <div
                id="dropzone"
                className={`dropzone ${isDragging ? 'dragover' : ''}`}
                onClick={() => fileInputRef.current?.click()}
                onDragOver={handleDragOver}
                onDragLeave={handleDragLeave}
                onDrop={handleDrop}
              >
                <div className="dropzone-icon">
                  <svg
                    width="36"
                    height="36"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="1.5"
                  >
                    <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                    <polyline points="14 2 14 8 20 8" />
                  </svg>
                </div>
                <div className="dropzone-title">
                  Click to browse or drag and drop your file here
                </div>
                <div className="dropzone-desc">
                  Only PDF files up to 50 MB are supported
                </div>
                <input
                  type="file"
                  id="file-input"
                  ref={fileInputRef}
                  accept=".pdf,application/pdf"
                  style={{ display: 'none' }}
                  onChange={handleFileChange}
                />
              </div>
              {selectedFile && !validationError && (
                <div
                  id="selected-file-name"
                  style={{
                    marginTop: '0.5rem',
                    fontSize: '0.8125rem',
                    color: 'var(--accent-primary)',
                    fontWeight: 500
                  }}
                >
                  Selected: {selectedFile.name} ({formatBytes(selectedFile.size)})
                </div>
              )}
            </div>

            {validationError && (
              <div
                id="upload-error-alert"
                style={{
                  padding: '0.75rem',
                  borderRadius: 'var(--radius-sm)',
                  backgroundColor: 'rgba(239, 68, 68, 0.15)',
                  border: '1px solid rgba(239, 68, 68, 0.3)',
                  color: '#f87171',
                  fontSize: '0.8125rem',
                  marginTop: '0.5rem'
                }}
              >
                {validationError}
              </div>
            )}
          </div>
          <div className="modal-footer">
            <button
              type="button"
              className="btn btn-secondary"
              id="btn-cancel-upload"
              onClick={handleClose}
            >
              Cancel
            </button>
            <button
              type="submit"
              className="btn btn-primary"
              id="btn-submit-upload"
              disabled={isSubmitting || !selectedFile || !!validationError}
            >
              {isSubmitting ? 'Uploading...' : 'Upload Document'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
