import React, { useState, useEffect, useCallback } from 'react';
import { DocumentItem, DocumentStatus, DocumentStatusHistory, ToastMessage, UserProfile } from './types';
import { API } from './services/api';
import { Header } from './components/Header';
import { StatsGrid } from './components/StatsGrid';
import { ActionBar } from './components/ActionBar';
import { DocumentTable } from './components/DocumentTable';
import { UploadModal } from './components/UploadModal';
import { DetailModal } from './components/DetailModal';
import { DeleteModal } from './components/DeleteModal';
import { LoginModal } from './components/LoginModal';
import { ToastContainer } from './components/ToastContainer';

export const App: React.FC = () => {
  const [isOnline, setIsOnline] = useState<boolean>(true);
  const [currentUser, setCurrentUser] = useState<UserProfile | null>(() => API.getUser());
  const [documents, setDocuments] = useState<DocumentItem[]>([]);
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [selectedStatus, setSelectedStatus] = useState<string>('ALL');

  // Modals state
  const [isUploadOpen, setIsUploadOpen] = useState<boolean>(false);
  const [isLoginOpen, setIsLoginOpen] = useState<boolean>(false);
  const [selectedDoc, setSelectedDoc] = useState<DocumentItem | null>(null);
  const [history, setHistory] = useState<DocumentStatusHistory[]>([]);
  const [isLoadingHistory, setIsLoadingHistory] = useState<boolean>(false);
  const [docToDelete, setDocToDelete] = useState<DocumentItem | null>(null);

  // Toasts
  const [toasts, setToasts] = useState<ToastMessage[]>([]);

  const addToast = useCallback((message: string, type: 'success' | 'error' = 'success') => {
    const id = Date.now() + Math.random();
    setToasts((prev) => [...prev, { id, message, type }]);

    setTimeout(() => {
      setToasts((prev) => prev.filter((t) => t.id !== id));
    }, 4000);
  }, []);

  const loadDocuments = useCallback(async (query: string = '') => {
    const online = await API.checkHealth();
    setIsOnline(online);

    if (!API.isAuthenticated()) {
      setDocuments([]);
      return;
    }

    try {
      const docs = await API.getDocuments(query);
      setDocuments(docs);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to fetch documents';
      addToast(msg, 'error');
    }
  }, [addToast]);

  useEffect(() => {
    loadDocuments();

    const handleUnauthorized = () => {
      setCurrentUser(null);
      setDocuments([]);
      addToast('Authentication required or session expired. Please sign in.', 'error');
      setIsLoginOpen(true);
    };

    const handleLogoutEvent = () => {
      setCurrentUser(null);
      setDocuments([]);
      addToast('Signed out successfully.');
    };

    window.addEventListener('auth:unauthorized', handleUnauthorized);
    window.addEventListener('auth:logout', handleLogoutEvent);

    return () => {
      window.removeEventListener('auth:unauthorized', handleUnauthorized);
      window.removeEventListener('auth:logout', handleLogoutEvent);
    };
  }, [loadDocuments, addToast]);

  const handleSearchChange = useCallback((query: string) => {
    setSearchQuery(query);
    loadDocuments(query);
  }, [loadDocuments]);

  const handleOpenUpload = useCallback(() => {
    if (!API.isAuthenticated()) {
      addToast('Please sign in to upload documents.', 'error');
      setIsLoginOpen(true);
      return;
    }
    setIsUploadOpen(true);
  }, [addToast]);

  const handleUploadSubmit = useCallback(async (file: File, title: string) => {
    if (!file.name.toLowerCase().endsWith('.pdf')) {
      addToast('Invalid file format. Only PDF documents are supported.', 'error');
      return;
    }

    if (file.size > 50 * 1024 * 1024) {
      addToast('File size exceeds the 50 MB limit.', 'error');
      return;
    }

    try {
      const uploaded = await API.uploadDocument(file, title);
      addToast(`Document "${uploaded.title || uploaded.originalFilename}" uploaded successfully!`);
      await loadDocuments(searchQuery);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to upload document';
      addToast(msg, 'error');
      throw err;
    }
  }, [addToast, loadDocuments, searchQuery]);

  const handleViewDetails = useCallback(async (docId: number) => {
    try {
      const doc = await API.getDocumentById(docId);
      setSelectedDoc(doc);
      setIsLoadingHistory(true);
      try {
        const hist = await API.getDocumentHistory(docId);
        setHistory(hist);
      } catch {
        setHistory([]);
      } finally {
        setIsLoadingHistory(false);
      }
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Error loading document details';
      addToast(msg, 'error');
    }
  }, [addToast]);

  const handleUpdateStatus = useCallback(async (
    docId: number,
    targetStatus: DocumentStatus,
    actor: string,
    comment: string
  ) => {
    if (!API.isAuthenticated()) {
      addToast('Please sign in to update document lifecycle.', 'error');
      setIsLoginOpen(true);
      return;
    }

    try {
      const updated = await API.updateDocumentStatus(docId, targetStatus, actor, comment);
      addToast(`Document status transitioned to ${updated.status}`);
      // Refresh current document view
      const doc = await API.getDocumentById(docId);
      setSelectedDoc(doc);
      const hist = await API.getDocumentHistory(docId);
      setHistory(hist);
      // Refresh list
      await loadDocuments(searchQuery);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Status transition rejected';
      addToast(msg, 'error');
      throw err;
    }
  }, [addToast, loadDocuments, searchQuery]);

  const handleDeleteRequest = useCallback((doc: DocumentItem) => {
    if (!API.isAuthenticated()) {
      addToast('Please sign in to delete documents.', 'error');
      setIsLoginOpen(true);
      return;
    }
    setDocToDelete(doc);
  }, [addToast]);

  const handleConfirmDelete = useCallback(async (docId: number) => {
    try {
      await API.deleteDocument(docId);
      addToast(`Document #${docId} deleted successfully`);
      await loadDocuments(searchQuery);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to delete document';
      addToast(msg, 'error');
      throw err;
    }
  }, [addToast, loadDocuments, searchQuery]);

  const handleLogin = useCallback(async (username: string, password: string) => {
    const res = await API.login(username, password);
    setCurrentUser({ username: res.username, role: res.role });
    addToast(`Signed in as ${res.username} (${res.role})`);
    await loadDocuments(searchQuery);
  }, [addToast, loadDocuments, searchQuery]);

  const handleLogout = useCallback(() => {
    API.logout();
  }, []);

  return (
    <>
      <Header
        isOnline={isOnline}
        currentUser={currentUser}
        onOpenLogin={() => setIsLoginOpen(true)}
        onLogout={handleLogout}
      />

      <main className="main">
        <div className="container">
          <StatsGrid documents={documents} />

          <ActionBar
            onSearchChange={handleSearchChange}
            selectedStatus={selectedStatus}
            onStatusChange={setSelectedStatus}
            onOpenUpload={handleOpenUpload}
          />

          <DocumentTable
            documents={documents}
            selectedStatus={selectedStatus}
            onViewDetails={handleViewDetails}
            onDeleteDoc={handleDeleteRequest}
          />
        </div>
      </main>

      <footer className="footer">
        <div className="container">
          Paperless DMS &bull; SWEN3 Semester Project &bull; BIF5 Technikum Wien
        </div>
      </footer>

      {/* Modals */}
      <UploadModal
        isOpen={isUploadOpen}
        onClose={() => setIsUploadOpen(false)}
        onUpload={handleUploadSubmit}
      />

      <DetailModal
        document={selectedDoc}
        history={history}
        isLoadingHistory={isLoadingHistory}
        defaultActor={currentUser ? currentUser.username : 'Web User'}
        onClose={() => setSelectedDoc(null)}
        onUpdateStatus={handleUpdateStatus}
      />

      <DeleteModal
        document={docToDelete}
        onClose={() => setDocToDelete(null)}
        onConfirm={handleConfirmDelete}
      />

      <LoginModal
        isOpen={isLoginOpen}
        onClose={() => setIsLoginOpen(false)}
        onLogin={handleLogin}
      />

      <ToastContainer toasts={toasts} />
    </>
  );
};

export default App;
