import { DocumentItem, DocumentStatus, DocumentStatusHistory, ProblemDetail } from '../types';

export const API = {
  BASE_URL: '/api/v1',

  async parseError(res: Response, fallback: string): Promise<string> {
    try {
      const problem = (await res.json()) as ProblemDetail;
      if (problem) {
        if (problem.invalidParams && typeof problem.invalidParams === 'object') {
          const fieldErrors = Object.entries(problem.invalidParams)
            .map(([field, msg]) => `${field}: ${msg}`)
            .join('; ');
          if (fieldErrors) {
            return `${problem.detail || problem.title || fallback}: ${fieldErrors}`;
          }
        }
        return problem.detail || problem.title || problem.message || fallback;
      }
    } catch {
      // Ignore body parsing failures (e.g. empty or non-JSON responses)
    }
    return `${fallback} (${res.status})`;
  },

  async checkHealth(): Promise<boolean> {
    try {
      const res = await fetch(`${this.BASE_URL}/health`, { method: 'GET' });
      return res.ok;
    } catch {
      return false;
    }
  },

  async getDocuments(title: string = ''): Promise<DocumentItem[]> {
    const trimmed = title.trim();
    const url = trimmed
      ? `${this.BASE_URL}/documents/search?query=${encodeURIComponent(trimmed)}`
      : `${this.BASE_URL}/documents`;

    const res = await fetch(url, {
      method: 'GET',
      headers: {
        Accept: 'application/json'
      }
    });

    if (!res.ok) {
      const errorMessage = await this.parseError(res, 'Failed to fetch documents');
      throw new Error(errorMessage);
    }
    return res.json();
  },

  async getDocumentById(id: number): Promise<DocumentItem> {
    const res = await fetch(`${this.BASE_URL}/documents/${id}`, {
      method: 'GET',
      headers: {
        Accept: 'application/json'
      }
    });

    if (!res.ok) {
      const errorMessage = await this.parseError(res, `Document ${id} not found`);
      throw new Error(errorMessage);
    }
    return res.json();
  },

  async uploadDocument(file: File, title: string = ''): Promise<DocumentItem> {
    const formData = new FormData();
    formData.append('file', file);
    if (title.trim()) {
      formData.append('title', title.trim());
    }

    const res = await fetch(`${this.BASE_URL}/documents/upload`, {
      method: 'POST',
      body: formData
    });

    if (!res.ok) {
      const errorMessage = await this.parseError(res, 'Upload failed');
      throw new Error(errorMessage);
    }
    return res.json();
  },

  async deleteDocument(id: number): Promise<boolean> {
    const res = await fetch(`${this.BASE_URL}/documents/${id}`, {
      method: 'DELETE'
    });

    if (res.status === 204 || res.ok) {
      return true;
    }

    const errorMessage = await this.parseError(res, 'Delete failed');
    throw new Error(errorMessage);
  },

  async getDocumentStatus(id: number): Promise<{ status: DocumentStatus }> {
    const res = await fetch(`${this.BASE_URL}/documents/${id}/status`, {
      method: 'GET',
      headers: {
        Accept: 'application/json'
      }
    });

    if (!res.ok) {
      const errorMessage = await this.parseError(res, 'Failed to fetch status');
      throw new Error(errorMessage);
    }
    return res.json();
  },

  async updateDocumentStatus(
    id: number,
    targetStatus: DocumentStatus,
    comment: string = ''
  ): Promise<{ status: DocumentStatus }> {
    const payload = {
      status: targetStatus,
      comment
    };

    const res = await fetch(`${this.BASE_URL}/documents/${id}/status`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Accept: 'application/json'
      },
      body: JSON.stringify(payload)
    });

    if (!res.ok) {
      const errorMessage = await this.parseError(res, 'Status transition failed');
      throw new Error(errorMessage);
    }
    return res.json();
  },

  async getDocumentHistory(id: number): Promise<DocumentStatusHistory[]> {
    const res = await fetch(`${this.BASE_URL}/documents/${id}/history`, {
      method: 'GET',
      headers: {
        Accept: 'application/json'
      }
    });

    if (!res.ok) {
      const errorMessage = await this.parseError(res, 'Failed to fetch history');
      throw new Error(errorMessage);
    }
    return res.json();
  }
};
