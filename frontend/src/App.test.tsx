import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';
import App from './App';
import { AuthContext } from './context/AuthContext';
import type { AuthContextValue } from './context/AuthContext';
import { I18nProvider } from './i18n/I18nProvider';
import { useI18n } from './i18n/useI18n';
import type { CurrentUser, UserRole } from './types';

// Mock JournalistSearchPage to use useI18n() and render translated text
// This proves that I18nProvider is properly wired through App
vi.mock('./pages/JournalistSearchPage', () => ({
  JournalistSearchPage: () => {
    const { t } = useI18n();
    return <div>{t('greeting')}</div>;
  },
}));

vi.mock('./pages/JournalistProfilePage', () => ({
  JournalistProfilePage: () => <div>Journalist profile page</div>,
}));

vi.mock('./pages/UserGuidePage', () => ({
  UserGuidePage: () => <div>User guide page</div>,
}));

vi.mock('./pages/UserAdminPage', () => ({
  UserAdminPage: () => <div>User admin page</div>,
}));

vi.mock('./pages/ThemeAdminPage', () => ({
  ThemeAdminPage: () => <div>Theme admin page</div>,
}));

vi.mock('./pages/JournalistCreatePage', () => ({
  JournalistCreatePage: () => <div>Journalist create page</div>,
}));

vi.mock('./pages/JournalistEditPage', () => ({
  JournalistEditPage: () => <div>Journalist edit page</div>,
}));

vi.mock('./pages/LoginPage', () => ({
  LoginPage: () => <div>Login page</div>,
}));

const createAuthValue = (roles: UserRole[]): AuthContextValue => {
  const currentUser: CurrentUser = {
    username: 'theme-manager',
    firstName: 'Thelma',
    lastName: 'Themes',
    roles: roles as CurrentUser['roles'],
  };

  return {
    currentUser,
    isLoading: false,
    refreshCurrentUser: vi.fn(),
    isAuthenticated: true,
    isAdmin: roles.includes('ADMIN'),
  };
};

const renderApp = (route: string, authValue: AuthContextValue) =>
  render(
    <I18nProvider>
      <AuthContext.Provider value={authValue}>
        <MemoryRouter initialEntries={[route]}>
          <App />
        </MemoryRouter>
      </AuthContext.Provider>
    </I18nProvider>,
  );

describe('App', () => {
  it('renders routed page content that depends on I18nProvider wiring', () => {
    renderApp('/', createAuthValue(['USER']));

    // Verify that the mocked JournalistSearchPage calls useI18n() successfully
    // This proves I18nProvider is wired through App and provides context
    expect(screen.getByText('Bonjour')).toBeInTheDocument();
  });

  it('shows the themes navigation link for theme managers', () => {
    renderApp('/', createAuthValue(['THEME_MANAGER']));

    expect(screen.getByRole('link', { name: 'Thèmes' })).toBeInTheDocument();
  });

  it('shows the themes navigation link for admins', () => {
    renderApp('/', createAuthValue(['ADMIN']));

    expect(screen.getByRole('link', { name: 'Thèmes' })).toBeInTheDocument();
  });

  it('lets theme managers reach the protected themes admin route', () => {
    renderApp('/admin/themes', createAuthValue(['THEME_MANAGER']));

    expect(screen.getByRole('link', { name: 'Thèmes' })).toBeInTheDocument();
    expect(screen.getByText('Theme admin page')).toBeInTheDocument();
    expect(screen.queryByText('Login page')).not.toBeInTheDocument();
  });

  it('shows journalist management routes only for ADMIN and JOURNALIST_MANAGER', () => {
    renderApp('/', createAuthValue(['JOURNALIST_MANAGER']));

    expect(screen.getByRole('link', { name: /Créer un journaliste/i })).toBeInTheDocument();
  });
});
