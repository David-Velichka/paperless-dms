import React from 'react';
import { DocumentItem } from '../types';
import { formatBytes, formatDate, getDocStatus } from '../utils/formatters';

interface DocumentTableProps {
  documents: DocumentItem[];
  selectedStatus: string;
  onViewDetails: (docId: number) => void;
  onDeleteDoc: (doc: DocumentItem) => void;
}

export const DocumentTable: React.FC<DocumentTableProps> = ({
  documents,
  selectedStatus,
  onViewDetails,
  onDeleteDoc
}) => {
  const filtered = documents.filter((doc) => {
    const docStatus = getDocStatus(doc);
    if (selectedStatus === 'ALL') return true;
    return docStatus === selectedStatus;
  });

  return (
    <section className="card" id="documents-section">
      <div className="table-responsive">
        <table className="table" id="documents-table">
          <thead>
            <tr>
              <th style={{ width: '80px' }}>ID</th>
              <th>Document Title</th>
              <th>File Size</th>
              <th>Uploaded At</th>
              <th>Lifecycle Status</th>
              <th style={{ width: '180px' }}>Actions</th>
            </tr>
          </thead>
          <tbody id="docs-table-body">
            {filtered.map((doc) => {
              const docStatus = getDocStatus(doc);
              const displayName = doc.title || doc.originalFilename;

              return (
                <tr key={doc.id} id={`doc-row-${doc.id}`}>
                  <td>#{doc.id}</td>
                  <td>
                    <div className="doc-title-cell">
                      <div className="pdf-icon">PDF</div>
                      <div>
                        <div className="doc-name">{displayName}</div>
                        <div className="doc-subname">{doc.originalFilename}</div>
                      </div>
                    </div>
                  </td>
                  <td>{formatBytes(doc.fileSize)}</td>
                  <td>{formatDate(doc.createdAt)}</td>
                  <td>
                    <span className={`badge badge-${docStatus}`}>{docStatus}</span>
                  </td>
                  <td>
                    <div className="action-buttons">
                      <button
                        type="button"
                        className="btn btn-secondary btn-sm btn-view-doc"
                        data-id={doc.id}
                        onClick={() => onViewDetails(doc.id)}
                      >
                        Details
                      </button>
                      <button
                        type="button"
                        className="btn btn-danger btn-sm btn-delete-doc"
                        data-id={doc.id}
                        onClick={() => onDeleteDoc(doc)}
                      >
                        Delete
                      </button>
                    </div>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>

      {filtered.length === 0 && (
        <div id="empty-state" className="empty-state">
          <div className="empty-icon">
            <svg
              width="40"
              height="40"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="1.5"
            >
              <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z" />
            </svg>
          </div>
          <div className="empty-text">No documents found</div>
          <div className="empty-subtext">
            Upload a PDF document to begin archiving and managing files.
          </div>
        </div>
      )}
    </section>
  );
};

