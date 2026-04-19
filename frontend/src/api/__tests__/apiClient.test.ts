import { afterEach, describe, expect, it, vi } from 'vitest';
import { fetchAuthBootstrap, fetchWithAuth, UnauthorizedError } from '../apiClient';

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

describe('fetchWithAuth', () => {
  afterEach(() => {
    vi.restoreAllMocks();
    sessionStorage.clear();
  });

  it('always sends cookie credentials', async () => {
    const fetchMock = vi.fn().mockResolvedValue({
      status: 200,
      ok: true,
    });

    vi.stubGlobal('fetch', fetchMock);

    await fetchWithAuth('/api/v1/auth/me', {
      credentials: 'omit',
      headers: {
        Accept: 'application/json',
      },
    } as RequestInit);

    expect(fetchMock).toHaveBeenCalledWith(
      '/api/v1/auth/me',
      expect.objectContaining({
        credentials: 'include',
        headers: {
          Accept: 'application/json',
        },
      }),
    );
  });

  it('redirects unauthorized protected requests to login with a typed error', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue({
        status: 401,
        ok: false,
      }),
    );

    const location = mockLocation('/guide', '?page=2');

    await expect(fetchWithAuth('/api/v1/auth/me')).rejects.toBeInstanceOf(UnauthorizedError);
    expect(sessionStorage.getItem('redirectAfterLogin')).toBe('/guide?page=2');
    expect(location.href).toBe('/login');
    expect(location.hrefAssignments).toBe(1);

    location.restore();
  });

  it('does not force a redirect when already on the login page', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue({
        status: 401,
        ok: false,
      }),
    );

    const location = mockLocation('/login');

    await expect(fetchWithAuth('/api/v1/auth/me')).rejects.toBeInstanceOf(UnauthorizedError);
    expect(sessionStorage.getItem('redirectAfterLogin')).toBeNull();
    expect(location.href).toBe('/login');
    expect(location.hrefAssignments).toBe(0);

    location.restore();
  });

  it('does not redirect during auth bootstrap when the session is missing', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue({
        status: 401,
        ok: false,
      }),
    );

    const location = mockLocation('/guide', '?page=2');

    await expect(fetchAuthBootstrap('/api/v1/auth/me')).rejects.toBeInstanceOf(UnauthorizedError);
    expect(sessionStorage.getItem('redirectAfterLogin')).toBeNull();
    expect(location.href).toBe('/guide?page=2');
    expect(location.hrefAssignments).toBe(0);

    location.restore();
  });
});
