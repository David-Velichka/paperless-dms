/**
 * REST API client for Paperless DMS backend
 * All requests route via Nginx reverse proxy (/api/ -> paperless-rest:8081/api/)
 */
export const API = {
    BASE_URL: '/api',

    /**
     * Check backend health / connectivity
     */
    async checkHealth() {
        try {
            const res = await fetch(`${this.BASE_URL}/documents`, { method: 'GET' });
            return res.ok;
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
            
        const res = await fetch(url);
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
        const res = await fetch(`${this.BASE_URL}/documents/${id}`);
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

        const res = await fetch(`${this.BASE_URL}/documents/upload`, {
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
        const res = await fetch(`${this.BASE_URL}/documents/${id}`, {
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
        const res = await fetch(`${this.BASE_URL}/documents/${id}/status`);
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

        const res = await fetch(`${this.BASE_URL}/documents/${id}/status`, {
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
        const res = await fetch(`${this.BASE_URL}/documents/${id}/history`);
        if (!res.ok) {
            const err = await res.json().catch(() => ({}));
            throw new Error(err.message || `Failed to fetch history (${res.status})`);
        }
        return res.json();
    }
};
