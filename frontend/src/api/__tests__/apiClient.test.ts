import { afterEach, describe, expect, it, vi } from 'vitest';
import { ApiError, fetchAuthBootstrap, fetchWithAuth, UnauthorizedError } from '../apiClient';

type MockResponse = {
  status: number;
  ok: boolean;
  headers?: { get: (name: string) => string | null };
  json?: () => Promise<unknown>;
};

const jsonResponse = (status: number, body: unknown): MockResponse => ({
  status,
  ok: status >= 200 && status < 300,
  headers: { get: () => 'application/json' },
  json: async () => body,
});

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

  it('rejects failed requests with a typed API error containing the response status', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue({
        status: 409,
        ok: false,
      }),
    );

    const request = fetchWithAuth('/api/v1/themes/1', { method: 'DELETE' });

    await expect(request).rejects.toBeInstanceOf(ApiError);
    await expect(request).rejects.toMatchObject({
      name: 'ApiError',
      status: 409,
      message: 'API request failed with status 409',
    });
  });

  it('propagates backend error payload in ApiError when available', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue(
        jsonResponse(400, {
          status: 400,
          message: "Unknown sort direction 'sideways'. Allowed: asc, desc",
          code: 'INVALID_SORT_DIRECTION',
        }),
      ),
    );

    const request = fetchWithAuth('/api/v1/journalists?sort=lastName,sideways');

    await expect(request).rejects.toBeInstanceOf(ApiError);
    await expect(request).rejects.toMatchObject({
      status: 400,
      message: "Unknown sort direction 'sideways'. Allowed: asc, desc",
      code: 'INVALID_SORT_DIRECTION',
    });
  });
});
