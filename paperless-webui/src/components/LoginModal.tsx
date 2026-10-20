import React, { useState } from 'react';

interface LoginModalProps {
  isOpen: boolean;
  onClose: () => void;
  onLogin: (username: string, password: string) => Promise<void>;
}

export const LoginModal: React.FC<LoginModalProps> = ({
  isOpen,
  onClose,
  onLogin
}) => {
  const [username, setUsername] = useState('admin');
  const [password, setPassword] = useState('admin123');
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  if (!isOpen) return null;

  const handleClose = () => {
    setError(null);
    onClose();
  };

  const handleQuickAdmin = () => {
    setUsername('admin');
    setPassword('admin123');
    setError(null);
  };

  const handleQuickUser = () => {
    setUsername('user');
    setPassword('password');
    setError(null);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setIsSubmitting(true);

    try {
      await onLogin(username.trim(), password);
      handleClose();
    } catch (err: unknown) {
      if (err instanceof Error) {
        setError(err.message);
      } else {
        setError('Invalid username or password');
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div
      id="login-modal"
      className="modal-overlay active"
      onClick={(e) => {
        if (e.target === e.currentTarget) handleClose();
      }}
    >
      <div className="modal-content" style={{ maxWidth: '440px' }}>
        <div className="modal-header">
          <h2 className="modal-title">Sign In to Paperless DMS</h2>
          <button
            type="button"
            className="modal-close"
            id="login-modal-close"
            onClick={handleClose}
          >
            &times;
          </button>
        </div>
        <form id="login-form" onSubmit={handleSubmit}>
          <div className="modal-body">
            <p
              style={{
                color: 'var(--text-secondary)',
                fontSize: '0.875rem',
                marginBottom: '1.25rem'
              }}
            >
              Authentication required for document operations. Demo credentials:
            </p>
            <div
              style={{
                display: 'flex',
                gap: '0.5rem',
                marginBottom: '1.25rem',
                flexWrap: 'wrap'
              }}
            >
              <button
                type="button"
                className="btn btn-secondary btn-sm"
                id="btn-quick-admin"
                style={{ fontSize: '0.75rem', padding: '4px 10px' }}
                onClick={handleQuickAdmin}
              >
                Fill Admin (admin / admin123)
              </button>
              <button
                type="button"
                className="btn btn-secondary btn-sm"
                id="btn-quick-user"
                style={{ fontSize: '0.75rem', padding: '4px 10px' }}
                onClick={handleQuickUser}
              >
                Fill User (user / password)
              </button>
            </div>

            <div className="form-group">
              <label htmlFor="login-username" className="form-label">
                Username
              </label>
              <input
                type="text"
                id="login-username"
                className="form-control"
                placeholder="admin"
                required
                autoComplete="username"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
              />
            </div>

            <div className="form-group">
              <label htmlFor="login-password" className="form-label">
                Password
              </label>
              <input
                type="password"
                id="login-password"
                className="form-control"
                placeholder="••••••••"
                required
                autoComplete="current-password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />
            </div>

            {error && (
              <div
                id="login-error-alert"
                style={{
                  padding: '0.75rem',
                  borderRadius: 'var(--radius-sm)',
                  backgroundColor: 'rgba(239, 68, 68, 0.15)',
                  border: '1px solid rgba(239, 68, 68, 0.3)',
                  color: '#f87171',
                  fontSize: '0.8125rem',
                  marginTop: '0.75rem'
                }}
              >
                {error}
              </div>
            )}
          </div>
          <div className="modal-footer">
            <button
              type="button"
              className="btn btn-secondary"
              id="btn-cancel-login"
              onClick={handleClose}
            >
              Cancel
            </button>
            <button
              type="submit"
              className="btn btn-primary"
              id="btn-submit-login"
              disabled={isSubmitting}
            >
              {isSubmitting ? 'Signing in...' : 'Sign In'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

