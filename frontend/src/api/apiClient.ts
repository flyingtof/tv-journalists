/**
 * A wrapper around the native fetch API that handles authentication and redirection.
 *
 * @param url The URL to fetch.
 * @param options The options to pass to the fetch call.
 * @returns A promise that resolves to the response.
 * @throws An error if the fetch fails or if the user is unauthorized.
 */
export const fetchWithAuth = async (url: string, options: RequestInit = {}): Promise<Response> => {
  const response = await fetch(url, options);

  if (response.status === 401) {
    // Save the current page URL so we can restore it after login
    const currentPath = window.location.pathname + window.location.search;
    if (currentPath !== '/login') {
      sessionStorage.setItem('redirectAfterLogin', currentPath);
    }
    window.location.href = '/login';

    // Throw an error to stop the execution of the current code path.
    throw new Error('Unauthorized');
  }

  if (!response.ok) {
    // For other errors (e.g., 500), throw an error to be caught by the caller.
    throw new Error(`API request failed with status ${response.status}`);
  }

  return response;
};
