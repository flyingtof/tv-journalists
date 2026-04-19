import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { AuthContext } from '../../context/AuthContext';
import { ProtectedRoute } from '../ProtectedRoute';
import type { AuthContextValue } from '../../context/AuthContext';
import type { CurrentUser } from '../../types';

const createAuthValue = ({
  currentUser = null,
  isLoading = false,
}: {
  currentUser?: CurrentUser | null;
  isLoading?: boolean;
} = {}): AuthContextValue => ({
  currentUser,
  isLoading,
  refreshCurrentUser: vi.fn(),
  isAuthenticated: currentUser !== null,
  isAdmin: currentUser?.roles.includes('ADMIN') ?? false,
});

const renderProtectedRoute = (
  authValue: AuthContextValue,
  route = '/guide',
  routeElement: React.ReactNode = <ProtectedRoute />,
) =>
  render(
    <AuthContext.Provider value={authValue}>
      <MemoryRouter initialEntries={[route]}>
        <Routes>
          <Route path="/login" element={<div>Login page</div>} />
          <Route path="/" element={<div>Search page</div>} />
          <Route element={routeElement}>
            <Route path="/guide" element={<div>Guide page</div>} />
            <Route path="/admin/users" element={<div>Admin page</div>} />
          </Route>
        </Routes>
      </MemoryRouter>
    </AuthContext.Provider>,
  );

describe('ProtectedRoute', () => {
  afterEach(() => {
    sessionStorage.clear();
  });

  it('redirects unauthenticated users to login and preserves the target route', () => {
    renderProtectedRoute(createAuthValue(), '/guide?tab=filters');

    expect(screen.getByText('Login page')).toBeInTheDocument();
    expect(sessionStorage.getItem('redirectAfterLogin')).toBe('/guide?tab=filters');
  });

  it('shows an accessible loading state while auth is bootstrapping', () => {
    renderProtectedRoute(createAuthValue({ isLoading: true }));

    expect(screen.getByRole('status')).toHaveTextContent('Chargement de la session');
  });

  it('allows authenticated users onto protected routes', () => {
    renderProtectedRoute(
      createAuthValue({
        currentUser: {
          username: 'reader',
          firstName: 'Regular',
          lastName: 'User',
          roles: ['USER'],
        },
      }),
    );

    expect(screen.getByText('Guide page')).toBeInTheDocument();
    expect(screen.queryByText('Login page')).not.toBeInTheDocument();
  });

  it('redirects non-admin users away from admin routes', () => {
    renderProtectedRoute(
      createAuthValue({
        currentUser: {
          username: 'reader',
          firstName: 'Regular',
          lastName: 'User',
          roles: ['USER'],
        },
      }),
      '/admin/users',
      <ProtectedRoute requiredRole="ADMIN" />,
    );

    expect(screen.getByText('Search page')).toBeInTheDocument();
    expect(screen.queryByText('Admin page')).not.toBeInTheDocument();
  });
});
