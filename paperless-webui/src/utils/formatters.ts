import { DocumentItem, DocumentStatus } from '../types';

export const STATUS_TRANSITIONS: Record<DocumentStatus, DocumentStatus[]> = {
  RECEIVED: ['PROCESSING'],
  PROCESSING: ['REVIEW_PENDING'],
  REVIEW_PENDING: ['COMPLETED', 'REJECTED'],
  COMPLETED: [],
  REJECTED: []
};

export function formatBytes(bytes: number, decimals: number = 2): string {
  if (!bytes || bytes === 0) return '0 Bytes';
  const k = 1024;
  const dm = decimals < 0 ? 0 : decimals;
  const sizes = ['Bytes', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
}

export function formatDate(isoString: string | null | undefined): string {
  if (!isoString) return '-';
  const date = new Date(isoString);
  return date.toLocaleString();
}

export function getDocStatus(doc: DocumentItem): DocumentStatus {
  return doc.currentStatus || doc.status || 'RECEIVED';
}

