export type DocumentStatus = 'RECEIVED' | 'PROCESSING' | 'REVIEW_PENDING' | 'COMPLETED' | 'REJECTED';

export interface DocumentItem {
  id: number;
  title: string | null;
  originalFilename: string;
  fileSize: number;
  contentType?: string;
  createdAt: string;
  modifiedAt?: string;
  status?: DocumentStatus;
  currentStatus?: DocumentStatus;
}

export interface DocumentStatusHistory {
  id: number;
  documentId: number;
  status: DocumentStatus;
  timestamp: string;
  changedBy: string;
  comment?: string;
}

export interface UserProfile {
  username: string;
  role: string;
}

export interface AuthResponse {
  token: string;
  username: string;
  role: string;
}

export interface ToastMessage {
  id: number;
  message: string;
  type: 'success' | 'error';
}

