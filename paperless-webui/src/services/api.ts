import { AuthResponse, DocumentItem, DocumentStatus, DocumentStatusHistory, ProblemDetail, UserProfile } from '../types';

export const API = {
  BASE_URL: '/api/v1',
  TOKEN_KEY: 'paperless_jwt_token',
  USER_KEY: 'paperless_user',

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  },

  setToken(token: string, user: UserProfile): void {
    if (token) {
      localStorage.setItem(this.TOKEN_KEY, token);
    }
    if (user) {
      localStorage.setItem(this.USER_KEY, JSON.stringify(user));
    }
  },

  clearToken(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
  },

  getUser(): UserProfile | null {
    try {
      const data = localStorage.getItem(this.USER_KEY);
      return data ? JSON.parse(data) : null;
    } catch {
      return null;
    }
  },

  isAuthenticated(): boolean {
    return Boolean(this.getToken());
  },

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

  async login(username: string, password: string): Promise<AuthResponse> {
    const res = await fetch(`${this.BASE_URL}/auth/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ username, password })
    });

    if (!res.ok) {
      const errorMessage = await this.parseError(res, 'Invalid username or password');
      throw new Error(errorMessage);
    }

    const data: AuthResponse = await res.json();
    this.setToken(data.token, { username: data.username, role: data.role });
    return data;
  },

  async getCurrentUser(): Promise<UserProfile> {
    const res = await this.fetchWithAuth(`${this.BASE_URL}/auth/me`);
    if (!res.ok) {
      const errorMessage = await this.parseError(res, 'Failed to fetch user profile');
      throw new Error(errorMessage);
    }
    return res.json();
  },

  logout(): void {
    this.clearToken();
    window.dispatchEvent(new CustomEvent('auth:logout'));
  },

  async fetchWithAuth(url: string, options: RequestInit = {}): Promise<Response> {
    const headers = new Headers(options.headers || {});
    const token = this.getToken();
    if (token) {
      headers.set('Authorization', `Bearer ${token}`);
    }

    const response = await fetch(url, {
      ...options,
      headers
    });

    if (response.status === 401 && token) {
      this.clearToken();
      window.dispatchEvent(new CustomEvent('auth:unauthorized'));
    }

    return response;
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

    const res = await this.fetchWithAuth(url);
    if (!res.ok) {
      const errorMessage = await this.parseError(res, 'Failed to fetch documents');
      throw new Error(errorMessage);
    }
    return res.json();
  },

  async getDocumentById(id: number): Promise<DocumentItem> {
    const res = await this.fetchWithAuth(`${this.BASE_URL}/documents/${id}`);
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

    const res = await this.fetchWithAuth(`${this.BASE_URL}/documents/upload`, {
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
    const res = await this.fetchWithAuth(`${this.BASE_URL}/documents/${id}`, {
      method: 'DELETE'
    });

    if (res.status === 204 || res.ok) {
      return true;
    }

    const errorMessage = await this.parseError(res, `Delete failed`);
    throw new Error(errorMessage);
  },

  async getDocumentStatus(id: number): Promise<{ status: DocumentStatus }> {
    const res = await this.fetchWithAuth(`${this.BASE_URL}/documents/${id}/status`);
    if (!res.ok) {
      const errorMessage = await this.parseError(res, 'Failed to fetch status');
      throw new Error(errorMessage);
    }
    return res.json();
  },

  async updateDocumentStatus(
    id: number,
    targetStatus: DocumentStatus,
    changedBy: string = 'Web User',
    comment: string = ''
  ): Promise<{ status: DocumentStatus }> {
    const payload = {
      status: targetStatus,
      changedBy,
      comment
    };

    const res = await this.fetchWithAuth(`${this.BASE_URL}/documents/${id}/status`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
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
    const res = await this.fetchWithAuth(`${this.BASE_URL}/documents/${id}/history`);
    if (!res.ok) {
      const errorMessage = await this.parseError(res, 'Failed to fetch history');
      throw new Error(errorMessage);
    }
    return res.json();
  }
};

