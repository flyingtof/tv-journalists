import { render, screen, waitFor } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { AuthProvider, useAuth } from '../AuthContext';

const mockLocation = (pathname = '/guide', search = '') => {
  let hrefValue = `${pathname}${search}`;
  let hrefAssignments = 0;
  const hrefDescriptor = Object.getOwnPropertyDescriptor(window, 'location');

  Object.defineProperty(window, 'location', {
    configurable: true,
    value: {
      pathname,
      search,
      get href() {
        return hrefValue;
      },
      set href(value: string) {
        hrefAssignments += 1;
        hrefValue = value;
      },
    },
  });

  return {
    restore: () => {
      if (hrefDescriptor) {
        Object.defineProperty(window, 'location', hrefDescriptor);
      }
    },
    get href() {
      return hrefValue;
    },
    get hrefAssignments() {
      return hrefAssignments;
    },
  };
};

const AuthStateProbe = () => {
  const { currentUser, isAuthenticated, isLoading } = useAuth();

  return (
    <div>
      <span data-testid="loading">{String(isLoading)}</span>
      <span data-testid="authenticated">{String(isAuthenticated)}</span>
      <span data-testid="user">{currentUser?.username ?? 'none'}</span>
    </div>
  );
};

describe('AuthProvider', () => {
  afterEach(() => {
    vi.restoreAllMocks();
    sessionStorage.clear();
  });

  it('marks the user unauthenticated without redirecting during bootstrap when /auth/me returns 401', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue({
        status: 401,
        ok: false,
      }),
    );

    const location = mockLocation('/guide', '?page=2');

    render(
      <AuthProvider>
        <AuthStateProbe />
      </AuthProvider>,
    );

    expect(screen.getByTestId('loading')).toHaveTextContent('true');

    await waitFor(() => {
      expect(screen.getByTestId('loading')).toHaveTextContent('false');
    });

    expect(screen.getByTestId('authenticated')).toHaveTextContent('false');
    expect(screen.getByTestId('user')).toHaveTextContent('none');
    expect(sessionStorage.getItem('redirectAfterLogin')).toBeNull();
    expect(location.href).toBe('/guide?page=2');
    expect(location.hrefAssignments).toBe(0);

    location.restore();
  });
});
