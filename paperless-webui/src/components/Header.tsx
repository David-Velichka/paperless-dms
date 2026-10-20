import React from 'react';

interface HeaderProps {
  isOnline: boolean;
}

export const Header: React.FC<HeaderProps> = ({ isOnline }) => {
  return (
    <header className="header">
      <div className="container header-content">
        <a href="/" className="logo-group" id="logo-link">
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
        </div>
      </div>
    </header>
  );
};
