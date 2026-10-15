import React from 'react';
import { UserProfile } from '../types';

interface HeaderProps {
  isOnline: boolean;
  currentUser: UserProfile | null;
  onOpenLogin: () => void;
  onLogout: () => void;
}

export const Header: React.FC<HeaderProps> = ({
  isOnline,
  currentUser,
  onOpenLogin,
  onLogout
}) => {
  return (
    <header className="header">
      <div className="container header-content">
        <a href="/" className="logo-group" id="logo-link">
          <div className="logo-badge">P</div>
          <div>
            <h1 className="logo-title">Paperless DMS</h1>
            <div className="logo-subtitle">SWEN3 Semester Project</div>
          </div>
        </a>
        <div className="header-right">
          <div className="system-status" id="system-status">
            <span
              className={`status-dot ${isOnline ? '' : 'offline'}`}
              id="status-dot"
            />
            <span id="status-text">{isOnline ? 'API Connected' : 'API Offline'}</span>
          </div>
          <div className="auth-bar" id="auth-bar">
            {currentUser ? (
              <div id="user-badge" className="user-badge">
                <span className="user-label">User:</span>
                <span id="user-display-name">{currentUser.username}</span>
                <span id="user-display-role" className="badge badge-info">
                  {currentUser.role || 'USER'}
                </span>
              </div>
            ) : null}
            {!currentUser ? (
              <button
                type="button"
                id="btn-login-modal"
                className="btn btn-secondary btn-sm"
                onClick={onOpenLogin}
              >
                Sign In
              </button>
            ) : (
              <button
                type="button"
                id="btn-logout"
                className="btn btn-secondary btn-sm"
                onClick={onLogout}
              >
                Sign Out
              </button>
            )}
          </div>
        </div>
      </div>
    </header>
  );
};

