import React, { useState, useEffect, useCallback } from 'react';
import { DocumentItem, DocumentStatus, DocumentStatusHistory, ToastMessage } from './types';
import { API } from './services/api';
import { Header } from './components/Header';
import { StatsGrid } from './components/StatsGrid';
import { ActionBar } from './components/ActionBar';
import { DocumentTable } from './components/DocumentTable';
import { UploadModal } from './components/UploadModal';
import { DetailModal } from './components/DetailModal';
import { DeleteModal } from './components/DeleteModal';
import { ToastContainer } from './components/ToastContainer';

export const App: React.FC = () => {
  const [isOnline, setIsOnline] = useState<boolean>(true);
  const [documents, setDocuments] = useState<DocumentItem[]>([]);
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [selectedStatus, setSelectedStatus] = useState<string>('ALL');

  // Modals state
  const [isUploadOpen, setIsUploadOpen] = useState<boolean>(false);
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

    try {
      const docs = await API.getDocuments(query);
      setDocuments(docs);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to fetch documents';
      addToast(msg, 'error');
    }
  }, [addToast]);

  // Debounce search query changes
  useEffect(() => {
    const handler = setTimeout(() => {
      loadDocuments(searchQuery);
    }, 250);

    return () => {
      clearTimeout(handler);
    };
  }, [searchQuery, loadDocuments]);

  const handleSearchChange = useCallback((query: string) => {
    setSearchQuery(query);
  }, []);

  const handleOpenUpload = useCallback(() => {
    setIsUploadOpen(true);
  }, []);

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
    comment: string
  ) => {
    try {
      const updated = await API.updateDocumentStatus(docId, targetStatus, comment);
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
    setDocToDelete(doc);
  }, []);

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

  return (
    <>
      <Header isOnline={isOnline} />

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
        onClose={() => setSelectedDoc(null)}
        onUpdateStatus={handleUpdateStatus}
      />

      <DeleteModal
        document={docToDelete}
        onClose={() => setDocToDelete(null)}
        onConfirm={handleConfirmDelete}
      />

      <ToastContainer toasts={toasts} />
    </>
  );
};

export default App;
