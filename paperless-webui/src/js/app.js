import { API } from './api.js';

// Valid status transitions per business rules
const TRANSITIONS = {
    RECEIVED: ['PROCESSING'],
    PROCESSING: ['REVIEW_PENDING'],
    REVIEW_PENDING: ['COMPLETED', 'REJECTED'],
    COMPLETED: [],
    REJECTED: []
};

// Application State
const state = {
    documents: [],
    filterTitle: '',
    filterStatus: 'ALL',
    selectedDoc: null,
    docToDelete: null
};

// DOM Elements
const elements = {
    systemStatus: document.getElementById('system-status'),
    statusDot: document.getElementById('status-dot'),
    statusText: document.getElementById('status-text'),
    
    // Stats
    statTotal: document.getElementById('stat-total'),
    statProcessing: document.getElementById('stat-processing'),
    statReview: document.getElementById('stat-review'),
    statCompleted: document.getElementById('stat-completed'),

    // Search & Filter
    searchInput: document.getElementById('search-input'),
    statusFilter: document.getElementById('status-filter'),
    btnOpenUpload: document.getElementById('btn-open-upload'),

    // Table
    docsTableBody: document.getElementById('docs-table-body'),
    emptyState: document.getElementById('empty-state'),

    // Upload Modal
    uploadModal: document.getElementById('upload-modal'),
    uploadForm: document.getElementById('upload-form'),
    dropzone: document.getElementById('dropzone'),
    fileInput: document.getElementById('file-input'),
    selectedFileName: document.getElementById('selected-file-name'),
    docTitleInput: document.getElementById('doc-title-input'),
    uploadModalClose: document.getElementById('upload-modal-close'),
    btnCancelUpload: document.getElementById('btn-cancel-upload'),
    btnSubmitUpload: document.getElementById('btn-submit-upload'),

    // Detail Modal
    detailModal: document.getElementById('detail-modal'),
    detailModalClose: document.getElementById('detail-modal-close'),
    detailId: document.getElementById('detail-id'),
    detailTitle: document.getElementById('detail-title'),
    detailFilename: document.getElementById('detail-filename'),
    detailSize: document.getElementById('detail-size'),
    detailCreated: document.getElementById('detail-created'),
    detailStatusBadge: document.getElementById('detail-status-badge'),
    
    // Transition Controls
    transitionContainer: document.getElementById('transition-container'),
    targetStatusSelect: document.getElementById('target-status-select'),
    actorInput: document.getElementById('actor-input'),
    commentInput: document.getElementById('comment-input'),
    btnUpdateStatus: document.getElementById('btn-update-status'),
    historyTimeline: document.getElementById('history-timeline'),

    // Delete Modal
    deleteModal: document.getElementById('delete-modal'),
    deleteModalClose: document.getElementById('delete-modal-close'),
    deleteDocTitle: document.getElementById('delete-doc-title'),
    btnConfirmDelete: document.getElementById('btn-confirm-delete'),
    btnCancelDelete: document.getElementById('btn-cancel-delete'),

    // Auth Header Controls
    userBadge: document.getElementById('user-badge'),
    userDisplayName: document.getElementById('user-display-name'),
    userDisplayRole: document.getElementById('user-display-role'),
    btnLoginModal: document.getElementById('btn-login-modal'),
    btnLogout: document.getElementById('btn-logout'),

    // Login Modal
    loginModal: document.getElementById('login-modal'),
    loginForm: document.getElementById('login-form'),
    loginUsername: document.getElementById('login-username'),
    loginPassword: document.getElementById('login-password'),
    loginErrorAlert: document.getElementById('login-error-alert'),
    loginModalClose: document.getElementById('login-modal-close'),
    btnCancelLogin: document.getElementById('btn-cancel-login'),
    btnSubmitLogin: document.getElementById('btn-submit-login'),
    btnQuickAdmin: document.getElementById('btn-quick-admin'),
    btnQuickUser: document.getElementById('btn-quick-user'),

    // Toast Container
    toastContainer: document.getElementById('toast-container')
};

// Utilities
function formatBytes(bytes, decimals = 2) {
    if (!bytes || bytes === 0) return '0 Bytes';
    const k = 1024;
    const dm = decimals < 0 ? 0 : decimals;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
}

function formatDate(isoString) {
    if (!isoString) return '-';
    const date = new Date(isoString);
    return date.toLocaleString();
}

function showToast(message, type = 'success') {
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.innerHTML = `<span>${type === 'success' ? '✓' : '⚠'}</span> <span>${message}</span>`;
    elements.toastContainer.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(10px)';
        toast.style.transition = 'all 0.3s ease';
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

// Data Fetching & Rendering
async function loadDocuments() {
    const isOnline = await API.checkHealth();
    updateSystemHealth(isOnline);

    if (!API.isAuthenticated()) {
        state.documents = [];
        updateStats();
        renderTable();
        return;
    }

    try {
        const docs = await API.getDocuments(state.filterTitle);
        state.documents = docs;
        updateStats();
        renderTable();
    } catch (err) {
        console.error('Failed to load documents:', err);
        showToast(err.message || 'Failed to fetch documents', 'error');
    }
}

function updateSystemHealth(isOnline) {
    if (isOnline) {
        elements.statusDot.className = 'status-dot';
        elements.statusText.textContent = 'API Connected';
    } else {
        elements.statusDot.className = 'status-dot offline';
        elements.statusText.textContent = 'API Offline';
    }
}

function getDocStatus(doc) {
    return doc.currentStatus || doc.status || 'RECEIVED';
}

function updateStats() {
    const total = state.documents.length;
    const processing = state.documents.filter(d => getDocStatus(d) === 'PROCESSING').length;
    const review = state.documents.filter(d => getDocStatus(d) === 'REVIEW_PENDING').length;
    const completed = state.documents.filter(d => getDocStatus(d) === 'COMPLETED').length;

    elements.statTotal.textContent = total;
    elements.statProcessing.textContent = processing;
    elements.statReview.textContent = review;
    elements.statCompleted.textContent = completed;
}

function renderTable() {
    elements.docsTableBody.innerHTML = '';

    const filtered = state.documents.filter(doc => {
        const docStatus = getDocStatus(doc);
        if (state.filterStatus === 'ALL') return true;
        return docStatus === state.filterStatus;
    });

    if (filtered.length === 0) {
        elements.emptyState.style.display = 'block';
        return;
    }

    elements.emptyState.style.display = 'none';

    filtered.forEach(doc => {
        const row = document.createElement('tr');
        row.id = `doc-row-${doc.id}`;
        const docStatus = getDocStatus(doc);

        row.innerHTML = `
            <td>#${doc.id}</td>
            <td>
                <div class="doc-title-cell">
                    <div class="pdf-icon">PDF</div>
                    <div>
                        <div class="doc-name">${escapeHtml(doc.title || doc.originalFilename)}</div>
                        <div class="doc-subname">${escapeHtml(doc.originalFilename)}</div>
                    </div>
                </div>
            </td>
            <td>${formatBytes(doc.fileSize)}</td>
            <td>${formatDate(doc.createdAt)}</td>
            <td>
                <span class="badge badge-${docStatus}">${docStatus}</span>
            </td>
            <td>
                <div class="action-buttons">
                    <button class="btn btn-secondary btn-sm btn-view-doc" data-id="${doc.id}">
                        Details
                    </button>
                    <button class="btn btn-danger btn-sm btn-delete-doc" data-id="${doc.id}">
                        Delete
                    </button>
                </div>
            </td>
        `;

        elements.docsTableBody.appendChild(row);
    });

    // Wire up row action buttons
    document.querySelectorAll('.btn-view-doc').forEach(btn => {
        btn.addEventListener('click', () => openDetailModal(Number(btn.dataset.id)));
    });

    document.querySelectorAll('.btn-delete-doc').forEach(btn => {
        btn.addEventListener('click', () => openDeleteModal(Number(btn.dataset.id)));
    });
}

function escapeHtml(text) {
    if (!text) return '';
    return text
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

// Upload Modal Logic
function openUploadModal() {
    elements.uploadForm.reset();
    elements.selectedFileName.textContent = '';
    elements.uploadModal.classList.add('active');
}

function closeUploadModal() {
    elements.uploadModal.classList.remove('active');
}

async function handleUploadSubmit(e) {
    e.preventDefault();
    const file = elements.fileInput.files[0];
    if (!file) {
        showToast('Please select a PDF document to upload', 'error');
        return;
    }

    if (!file.name.toLowerCase().endsWith('.pdf')) {
        showToast('Invalid file format. Only PDF documents are supported.', 'error');
        return;
    }

    if (file.size > 50 * 1024 * 1024) {
        showToast('File size exceeds the 50 MB limit.', 'error');
        return;
    }

    elements.btnSubmitUpload.disabled = true;
    elements.btnSubmitUpload.textContent = 'Uploading...';

    try {
        const title = elements.docTitleInput.value;
        const uploaded = await API.uploadDocument(file, title);
        showToast(`Document "${uploaded.title || uploaded.originalFilename}" uploaded successfully!`);
        closeUploadModal();
        await loadDocuments();
    } catch (err) {
        console.error('Upload failed:', err);
        showToast(err.message || 'Failed to upload document', 'error');
    } finally {
        elements.btnSubmitUpload.disabled = false;
        elements.btnSubmitUpload.textContent = 'Upload Document';
    }
}

// Detail & Status Lifecycle Modal
async function openDetailModal(docId) {
    try {
        const doc = await API.getDocumentById(docId);
        state.selectedDoc = doc;

        const docStatus = getDocStatus(doc);
        elements.detailId.textContent = `#${doc.id}`;
        elements.detailTitle.textContent = doc.title || doc.originalFilename;
        elements.detailFilename.textContent = doc.originalFilename;
        elements.detailSize.textContent = formatBytes(doc.fileSize);
        elements.detailCreated.textContent = formatDate(doc.createdAt);
        elements.detailStatusBadge.className = `badge badge-${docStatus}`;
        elements.detailStatusBadge.textContent = docStatus;

        // Render transition options
        const nextStates = TRANSITIONS[docStatus] || [];
        elements.targetStatusSelect.innerHTML = '';

        if (nextStates.length === 0) {
            elements.transitionContainer.style.display = 'none';
        } else {
            elements.transitionContainer.style.display = 'block';
            nextStates.forEach(status => {
                const opt = document.createElement('option');
                opt.value = status;
                opt.textContent = `Transition to: ${status}`;
                elements.targetStatusSelect.appendChild(opt);
            });
            elements.actorInput.value = 'Web Admin';
            elements.commentInput.value = '';
        }

        // Render Status History Timeline
        await loadAuditHistory(doc.id);

        elements.detailModal.classList.add('active');
    } catch (err) {
        console.error('Failed to load document details:', err);
        showToast(err.message || 'Error loading document details', 'error');
    }
}

async function loadAuditHistory(docId) {
    elements.historyTimeline.innerHTML = '<div style="color: var(--text-muted); font-size: 0.8125rem;">Loading audit trail...</div>';
    try {
        const history = await API.getDocumentHistory(docId);
        elements.historyTimeline.innerHTML = '';

        if (!history || history.length === 0) {
            elements.historyTimeline.innerHTML = '<div style="color: var(--text-muted); font-size: 0.8125rem;">No history records found.</div>';
            return;
        }

        // Show in chronological order
        history.forEach(item => {
            const el = document.createElement('div');
            el.className = 'timeline-item';
            el.innerHTML = `
                <div class="timeline-dot"></div>
                <div class="timeline-header">
                    <span class="badge badge-${item.status}">${item.status}</span>
                    <span class="timeline-time">${formatDate(item.timestamp)}</span>
                </div>
                <div class="timeline-actor">Changed by: ${escapeHtml(item.changedBy || 'System')}</div>
                ${item.comment ? `<div class="timeline-comment">${escapeHtml(item.comment)}</div>` : ''}
            `;
            elements.historyTimeline.appendChild(el);
        });
    } catch (err) {
        console.error('Failed to load history:', err);
        elements.historyTimeline.innerHTML = '<div style="color: #f87171; font-size: 0.8125rem;">Failed to load audit trail.</div>';
    }
}

async function handleStatusUpdate() {
    if (!API.isAuthenticated()) {
        showToast('Please sign in to update document lifecycle.', 'error');
        openLoginModal();
        return;
    }
    if (!state.selectedDoc) return;

    const targetStatus = elements.targetStatusSelect.value;
    const actor = elements.actorInput.value.trim() || 'Web User';
    const comment = elements.commentInput.value.trim();

    elements.btnUpdateStatus.disabled = true;
    elements.btnUpdateStatus.textContent = 'Updating...';

    try {
        const updated = await API.updateDocumentStatus(state.selectedDoc.id, targetStatus, actor, comment);
        showToast(`Document status transitioned to ${updated.status}`);
        // Refresh details modal
        await openDetailModal(state.selectedDoc.id);
        // Refresh table in background
        await loadDocuments();
    } catch (err) {
        console.error('Status transition error:', err);
        showToast(err.message || 'Status transition rejected by business invariants', 'error');
    } finally {
        elements.btnUpdateStatus.disabled = false;
        elements.btnUpdateStatus.textContent = 'Apply Transition';
    }
}

function closeDetailModal() {
    elements.detailModal.classList.remove('active');
    state.selectedDoc = null;
}

// Delete Modal Logic
function openDeleteModal(docId) {
    if (!API.isAuthenticated()) {
        showToast('Please sign in to delete documents.', 'error');
        openLoginModal();
        return;
    }
    const doc = state.documents.find(d => d.id === docId);
    if (!doc) return;

    state.docToDelete = doc;
    elements.deleteDocTitle.textContent = doc.title || doc.originalFilename;
    elements.deleteModal.classList.add('active');
}

function closeDeleteModal() {
    elements.deleteModal.classList.remove('active');
    state.docToDelete = null;
}

async function handleConfirmDelete() {
    if (!state.docToDelete) return;

    elements.btnConfirmDelete.disabled = true;
    elements.btnConfirmDelete.textContent = 'Deleting...';

    try {
        await API.deleteDocument(state.docToDelete.id);
        showToast(`Document #${state.docToDelete.id} deleted successfully`);
        closeDeleteModal();
        await loadDocuments();
    } catch (err) {
        console.error('Delete failed:', err);
        showToast(err.message || 'Failed to delete document', 'error');
    } finally {
        elements.btnConfirmDelete.disabled = false;
        elements.btnConfirmDelete.textContent = 'Delete Document';
    }
}

// Authentication Handling
function updateAuthUI() {
    const user = API.getUser();
    if (API.isAuthenticated() && user) {
        elements.userBadge.style.display = 'flex';
        elements.userDisplayName.textContent = user.username;
        elements.userDisplayRole.textContent = user.role || 'USER';
        elements.btnLoginModal.style.display = 'none';
        elements.btnLogout.style.display = 'inline-flex';
        if (elements.actorInput) {
            elements.actorInput.value = user.username;
        }
    } else {
        elements.userBadge.style.display = 'none';
        elements.btnLoginModal.style.display = 'inline-flex';
        elements.btnLogout.style.display = 'none';
        if (elements.actorInput) {
            elements.actorInput.value = 'Web User';
        }
    }
}

function openLoginModal() {
    elements.loginErrorAlert.style.display = 'none';
    elements.loginModal.classList.add('active');
    elements.loginUsername.focus();
}

function closeLoginModal() {
    elements.loginModal.classList.remove('active');
    elements.loginForm.reset();
    elements.loginErrorAlert.style.display = 'none';
}

async function handleLoginSubmit(e) {
    e.preventDefault();
    const username = elements.loginUsername.value.trim();
    const password = elements.loginPassword.value;

    elements.btnSubmitLogin.disabled = true;
    elements.btnSubmitLogin.textContent = 'Signing in...';
    elements.loginErrorAlert.style.display = 'none';

    try {
        const response = await API.login(username, password);
        showToast(`Signed in as ${response.username} (${response.role})`);
        closeLoginModal();
        updateAuthUI();
        await loadDocuments();
    } catch (err) {
        elements.loginErrorAlert.textContent = err.message || 'Invalid username or password';
        elements.loginErrorAlert.style.display = 'block';
    } finally {
        elements.btnSubmitLogin.disabled = false;
        elements.btnSubmitLogin.textContent = 'Sign In';
    }
}

// Event Listeners Initialization
function initEventListeners() {
    // Search input with debounce
    let debounceTimer;
    elements.searchInput.addEventListener('input', (e) => {
        clearTimeout(debounceTimer);
        debounceTimer = setTimeout(() => {
            state.filterTitle = e.target.value.trim();
            loadDocuments();
        }, 300);
    });

    // Status filter dropdown
    elements.statusFilter.addEventListener('change', (e) => {
        state.filterStatus = e.target.value;
        renderTable();
    });

    // Open Upload Modal
    elements.btnOpenUpload.addEventListener('click', () => {
        if (!API.isAuthenticated()) {
            showToast('Please sign in to upload documents.', 'error');
            openLoginModal();
            return;
        }
        openUploadModal();
    });
    elements.uploadModalClose.addEventListener('click', closeUploadModal);
    elements.btnCancelUpload.addEventListener('click', closeUploadModal);

    // Auth & Login Controls
    elements.btnLoginModal.addEventListener('click', openLoginModal);
    elements.loginModalClose.addEventListener('click', closeLoginModal);
    elements.btnCancelLogin.addEventListener('click', closeLoginModal);
    elements.loginForm.addEventListener('submit', handleLoginSubmit);
    elements.btnLogout.addEventListener('click', () => API.logout());

    // Quick demo accounts
    elements.btnQuickAdmin.addEventListener('click', () => {
        elements.loginUsername.value = 'admin';
        elements.loginPassword.value = 'admin123';
    });
    elements.btnQuickUser.addEventListener('click', () => {
        elements.loginUsername.value = 'user';
        elements.loginPassword.value = 'password';
    });

    // Dropzone interactions
    elements.dropzone.addEventListener('click', () => elements.fileInput.click());
    
    elements.fileInput.addEventListener('change', () => {
        if (elements.fileInput.files.length > 0) {
            elements.selectedFileName.textContent = `Selected: ${elements.fileInput.files[0].name} (${formatBytes(elements.fileInput.files[0].size)})`;
        }
    });

    elements.dropzone.addEventListener('dragover', (e) => {
        e.preventDefault();
        elements.dropzone.classList.add('dragover');
    });

    elements.dropzone.addEventListener('dragleave', () => {
        elements.dropzone.classList.remove('dragover');
    });

    elements.dropzone.addEventListener('drop', (e) => {
        e.preventDefault();
        elements.dropzone.classList.remove('dragover');
        if (e.dataTransfer.files.length > 0) {
            elements.fileInput.files = e.dataTransfer.files;
            elements.selectedFileName.textContent = `Selected: ${e.dataTransfer.files[0].name} (${formatBytes(e.dataTransfer.files[0].size)})`;
        }
    });

    // Upload Form Submit
    elements.uploadForm.addEventListener('submit', handleUploadSubmit);

    // Detail Modal Close & Status Update
    elements.detailModalClose.addEventListener('click', closeDetailModal);
    elements.btnUpdateStatus.addEventListener('click', handleStatusUpdate);

    // Delete Modal
    elements.deleteModalClose.addEventListener('click', closeDeleteModal);
    elements.btnCancelDelete.addEventListener('click', closeDeleteModal);
    elements.btnConfirmDelete.addEventListener('click', handleConfirmDelete);

    // Auth events from API
    window.addEventListener('auth:unauthorized', () => {
        updateAuthUI();
        showToast('Authentication required or session expired. Please sign in.', 'error');
        openLoginModal();
    });

    window.addEventListener('auth:logout', () => {
        updateAuthUI();
        showToast('Signed out successfully.');
        loadDocuments();
    });

    // Close Modals on backdrop click
    [elements.uploadModal, elements.detailModal, elements.deleteModal, elements.loginModal].forEach(modal => {
        if (!modal) return;
        modal.addEventListener('click', (e) => {
            if (e.target === modal) {
                modal.classList.remove('active');
            }
        });
    });
}

// Initial Boot
document.addEventListener('DOMContentLoaded', () => {
    initEventListeners();
    updateAuthUI();
    loadDocuments();
});

