export class UnauthorizedError extends Error {
  constructor(message = 'Unauthorized') {
    super(message);
    this.name = 'UnauthorizedError';
  }
}

type AuthenticatedRequestInit = Omit<RequestInit, 'credentials'>;

const fetchWithIncludedCredentials = (url: string, options: AuthenticatedRequestInit = {}) =>
  fetch(url, {
    ...options,
    credentials: 'include',
  });

const validateResponse = (response: Response) => {
  if (response.status === 401) {
    throw new UnauthorizedError();
  }

  if (!response.ok) {
    throw new Error(`API request failed with status ${response.status}`);
  }

  return response;
};

/**
 * A wrapper around the native fetch API that handles authentication and redirection.
 *
 * @param url The URL to fetch.
 * @param options The options to pass to the fetch call.
 * @returns A promise that resolves to the response.
 * @throws An error if the fetch fails or if the user is unauthorized.
 */
export const fetchWithAuth = async (
  url: string,
  options: AuthenticatedRequestInit = {},
): Promise<Response> => {
  const response = await fetchWithIncludedCredentials(url, options);

  if (response.status === 401) {
    // Save the current page URL so we can restore it after login
    const currentPath = window.location.pathname + window.location.search;
    if (currentPath !== '/login') {
      sessionStorage.setItem('redirectAfterLogin', currentPath);
      window.location.href = '/login';
    }

    throw new UnauthorizedError();
  }

  return validateResponse(response);
};

export const fetchAuthBootstrap = async (
  url: string,
  options: AuthenticatedRequestInit = {},
): Promise<Response> => {
  const response = await fetchWithIncludedCredentials(url, options);

  return validateResponse(response);
};
