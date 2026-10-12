/**
 * REST API client for Paperless DMS backend
 * All requests route via Nginx reverse proxy (/api/ -> paperless-rest:8081/api/)
 * Stateless authentication is implemented with RFC 7519 JWT Bearer tokens
 */
export const API = {
    BASE_URL: '/api',
    TOKEN_KEY: 'paperless_jwt_token',
    USER_KEY: 'paperless_user',

    /**
     * Get stored JWT token
     */
    getToken() {
        return localStorage.getItem(this.TOKEN_KEY);
    },

    /**
     * Store JWT token and user info
     */
    setToken(token, user) {
        if (token) {
            localStorage.setItem(this.TOKEN_KEY, token);
        }
        if (user) {
            localStorage.setItem(this.USER_KEY, JSON.stringify(user));
        }
    },

    /**
     * Clear token and user session
     */
    clearToken() {
        localStorage.removeItem(this.TOKEN_KEY);
        localStorage.removeItem(this.USER_KEY);
    },

    /**
     * Get current user metadata
     */
    getUser() {
        try {
            const data = localStorage.getItem(this.USER_KEY);
            return data ? JSON.parse(data) : null;
        } catch {
            return null;
        }
    },

    /**
     * Check if user is currently authenticated
     */
    isAuthenticated() {
        return Boolean(this.getToken());
    },

    /**
     * Authenticate with username and password
     */
    async login(username, password) {
        const res = await fetch(`${this.BASE_URL}/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ username, password })
        });

        if (!res.ok) {
            const err = await res.json().catch(() => ({}));
            throw new Error(err.message || 'Invalid username or password');
        }

        const data = await res.json();
        this.setToken(data.token, { username: data.username, role: data.role });
        return data;
    },

    /**
     * Fetch current user profile with token
     */
    async getCurrentUser() {
        const res = await this.fetchWithAuth(`${this.BASE_URL}/auth/me`);
        if (!res.ok) {
            throw new Error('Failed to fetch user profile');
        }
        return res.json();
    },

    /**
     * Logout and clear local session
     */
    logout() {
        this.clearToken();
        window.dispatchEvent(new CustomEvent('auth:logout'));
    },

    /**
     * Wrapper around fetch that automatically appends the Bearer token
     * and handles HTTP 401 Unauthorized responses
     */
    async fetchWithAuth(url, options = {}) {
        const headers = new Headers(options.headers || {});
        const token = this.getToken();
        if (token) {
            headers.set('Authorization', `Bearer ${token}`);
        }

        const response = await fetch(url, {
            ...options,
            headers
        });

        if (response.status === 401) {
            this.clearToken();
            window.dispatchEvent(new CustomEvent('auth:unauthorized'));
        }

        return response;
    },

    /**
     * Check backend health / connectivity
     */
    async checkHealth() {
        try {
            const res = await this.fetchWithAuth(`${this.BASE_URL}/documents`, { method: 'GET' });
            return res.ok || res.status === 401; // Reachable even if unauthorized
        } catch {
            return false;
        }
    },

    /**
     * Get list of documents, optionally filtered by title
     */
    async getDocuments(title = '') {
        const url = title.trim()
            ? `${this.BASE_URL}/documents/search?query=${encodeURIComponent(title.trim())}`
            : `${this.BASE_URL}/documents`;

        const res = await this.fetchWithAuth(url);
        if (!res.ok) {
            const err = await res.json().catch(() => ({}));
            throw new Error(err.message || `Failed to fetch documents (${res.status})`);
        }
        return res.json();
    },

    /**
     * Get single document metadata by ID
     */
    async getDocumentById(id) {
        const res = await this.fetchWithAuth(`${this.BASE_URL}/documents/${id}`);
        if (!res.ok) {
            const err = await res.json().catch(() => ({}));
            throw new Error(err.message || `Document ${id} not found (${res.status})`);
        }
        return res.json();
    },

    /**
     * Upload document (PDF multipart/form-data)
     */
    async uploadDocument(file, title = '') {
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
            const err = await res.json().catch(() => ({}));
            throw new Error(err.message || `Upload failed (${res.status})`);
        }
        return res.json();
    },

    /**
     * Delete document by ID
     */
    async deleteDocument(id) {
        const res = await this.fetchWithAuth(`${this.BASE_URL}/documents/${id}`, {
            method: 'DELETE'
        });

        if (!res.ok) {
            const err = await res.json().catch(() => ({}));
            throw new Error(err.message || `Delete failed (${res.status})`);
        }
        return true;
    },

    /**
     * Get document status
     */
    async getDocumentStatus(id) {
        const res = await this.fetchWithAuth(`${this.BASE_URL}/documents/${id}/status`);
        if (!res.ok) {
            const err = await res.json().catch(() => ({}));
            throw new Error(err.message || `Failed to fetch status (${res.status})`);
        }
        return res.json();
    },

    /**
     * Update document lifecycle status (Custom Use Case 4)
     */
    async updateDocumentStatus(id, targetStatus, changedBy = 'Web User', comment = '') {
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
            const err = await res.json().catch(() => ({}));
            throw new Error(err.message || `Status transition failed (${res.status})`);
        }
        return res.json();
    },

    /**
     * Get document status audit log history
     */
    async getDocumentHistory(id) {
        const res = await this.fetchWithAuth(`${this.BASE_URL}/documents/${id}/history`);
        if (!res.ok) {
            const err = await res.json().catch(() => ({}));
            throw new Error(err.message || `Failed to fetch history (${res.status})`);
        }
        return res.json();
    }
};
