import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';
import App from './App';
import { AuthContext } from './context/AuthContext';
import type { AuthContextValue } from './context/AuthContext';
import type { CurrentUser, UserRole } from './types';

vi.mock('./pages/JournalistSearchPage', () => ({
  JournalistSearchPage: () => <div>Search page</div>,
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
    <AuthContext.Provider value={authValue}>
      <MemoryRouter initialEntries={[route]}>
        <App />
      </MemoryRouter>
    </AuthContext.Provider>,
  );

describe('App', () => {
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
});
