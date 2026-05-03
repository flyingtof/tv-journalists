import React from 'react';
import { useI18n } from '../i18n/useI18n';
import '../styles/Login.css';

export const LoginPage: React.FC = () => {
  const { t } = useI18n();
  
  // Spring Security often passes 'error' in query param on failure
  const params = new URLSearchParams(window.location.search);
  const isError = params.has('error');
  const isLogout = params.has('logout');

  return (
    <div className="login-page">
      <div className="login-card">
        <div className="login-header">
          <h1 className="login-title">Connexion</h1>
          <p className="login-subtitle">Accédez à votre espace TV Journalists</p>
        </div>

        {isError && (
          <div className="alert alert-error">
            {t('login.invalidCredentials')}
          </div>
        )}

        {isLogout && (
          <div className="alert alert-success">
            {t('login.logoutSuccess')}
          </div>
        )}

        <form action="/api/login" method="POST">
          <div className="form-group">
            <label htmlFor="username" className="form-label">
              {t('login.username')}
            </label>
            <input
              type="text"
              id="username"
              name="username"
              autoComplete="username"
              required
              className="form-input"
              placeholder="votre utilisateur"
            />
          </div>

          <div className="form-group-last">
            <label htmlFor="password" className="form-label">
              {t('login.password')}
            </label>
            <input
              type="password"
              id="password"
              name="password"
              autoComplete="current-password"
              required
              className="form-input"
              placeholder="••••••••"
            />
          </div>

          <button type="submit" className="login-button">
            {t('login.submit')}
          </button>
        </form>
      </div>
    </div>
  );
};
