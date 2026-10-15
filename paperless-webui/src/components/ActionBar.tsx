import React, { useState, useEffect, useRef } from 'react';
import { DocumentStatus } from '../types';

interface ActionBarProps {
  onSearchChange: (search: string) => void;
  selectedStatus: string;
  onStatusChange: (status: string) => void;
  onOpenUpload: () => void;
}

export const ActionBar: React.FC<ActionBarProps> = ({
  onSearchChange,
  selectedStatus,
  onStatusChange,
  onOpenUpload
}) => {
  const [searchValue, setSearchValue] = useState('');
  const isFirstMount = useRef(true);

  useEffect(() => {
    if (isFirstMount.current) {
      isFirstMount.current = false;
      return;
    }
    const timer = setTimeout(() => {
      onSearchChange(searchValue);
    }, 300);
    return () => clearTimeout(timer);
  }, [searchValue, onSearchChange]);

  return (
    <section className="action-bar" id="action-bar">
      <div className="search-filter-group">
        <div className="search-box">
          <span className="search-icon">
            <svg
              width="14"
              height="14"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
            >
              <circle cx="11" cy="11" r="8" />
              <line x1="21" y1="21" x2="16.65" y2="16.65" />
            </svg>
          </span>
          <input
            type="text"
            id="search-input"
            className="search-input"
            placeholder="Search documents by title..."
            value={searchValue}
            onChange={(e) => setSearchValue(e.target.value)}
          />
        </div>
        <select
          id="status-filter"
          className="filter-select"
          value={selectedStatus}
          onChange={(e) => onStatusChange(e.target.value as DocumentStatus | 'ALL')}
        >
          <option value="ALL">All Statuses</option>
          <option value="RECEIVED">Received</option>
          <option value="PROCESSING">Processing</option>
          <option value="REVIEW_PENDING">Review Pending</option>
          <option value="COMPLETED">Completed</option>
          <option value="REJECTED">Rejected</option>
        </select>
      </div>
      <button
        id="btn-open-upload"
        className="btn btn-primary"
        onClick={onOpenUpload}
      >
        <span>+</span> Upload Document
      </button>
    </section>
  );
};
