import React from 'react';
import { DocumentItem } from '../types';
import { getDocStatus } from '../utils/formatters';

interface StatsGridProps {
  documents: DocumentItem[];
}

export const StatsGrid: React.FC<StatsGridProps> = ({ documents }) => {
  const total = documents.length;
  const processing = documents.filter(d => getDocStatus(d) === 'PROCESSING').length;
  const review = documents.filter(d => getDocStatus(d) === 'REVIEW_PENDING').length;
  const completed = documents.filter(d => getDocStatus(d) === 'COMPLETED').length;

  return (
    <section className="stats-grid" id="stats-section">
      <div className="stat-card">
        <div className="stat-header">
          <span className="stat-label">Total Documents</span>
        </div>
        <div className="stat-value" id="stat-total">{total}</div>
      </div>
      <div className="stat-card">
        <div className="stat-header">
          <span className="stat-label">Processing</span>
        </div>
        <div className="stat-value" id="stat-processing">{processing}</div>
      </div>
      <div className="stat-card">
        <div className="stat-header">
          <span className="stat-label">Review Pending</span>
        </div>
        <div className="stat-value" id="stat-review">{review}</div>
      </div>
      <div className="stat-card">
        <div className="stat-header">
          <span className="stat-label">Completed</span>
        </div>
        <div className="stat-value" id="stat-completed">{completed}</div>
      </div>
    </section>
  );
};

