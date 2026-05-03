import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { ApiError, fetchWithAuth } from '../../api/apiClient';
import { JournalistSearchPage } from '../JournalistSearchPage';
import { I18nProvider } from '../../i18n/I18nProvider';

vi.mock('../../components/Autocomplete', () => ({
  Autocomplete: () => <div data-testid="autocomplete" />,
}));

vi.mock('../../components/JournalistList', () => ({
  JournalistList: () => <div data-testid="journalist-list" />,
}));

vi.mock('../../api/apiClient', async () => {
  const actual = await vi.importActual<typeof import('../../api/apiClient')>('../../api/apiClient');
  return {
    ...actual,
    fetchWithAuth: vi.fn(),
  };
});

const jsonResponse = (data: unknown) =>
  ({
    json: async () => data,
  }) as Response;

const emptyPage = {
  content: [],
  totalPages: 0,
  totalElements: 0,
  size: 10,
  number: 0,
  first: true,
  last: true,
  numberOfElements: 0,
  empty: true,
};

const renderSearchPage = () =>
  render(
    <I18nProvider>
      <MemoryRouter initialEntries={['/']}>
        <JournalistSearchPage />
      </MemoryRouter>
    </I18nProvider>,
  );

describe('JournalistSearchPage', () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('sanitizes invalid page and size values from the URL before searching', async () => {
    const fetchWithAuthMock = vi.mocked(fetchWithAuth);
    fetchWithAuthMock.mockImplementation(async (url: string) => {
      if (url === '/api/v1/media' || url === '/api/v1/themes') {
        return jsonResponse([]);
      }
      return jsonResponse(emptyPage);
    });

    renderSearchPage();

    await waitFor(() => {
      expect(fetchWithAuthMock).toHaveBeenCalledWith(
        expect.stringContaining('/api/v1/journalists?page=0&size=10&sort=lastName%2Casc'),
      );
    });
  });

  it('shows an alert when loading journalists fails', async () => {
    const fetchWithAuthMock = vi.mocked(fetchWithAuth);
    fetchWithAuthMock.mockImplementation(async (url: string) => {
      if (url === '/api/v1/media' || url === '/api/v1/themes') {
        return jsonResponse([]);
      }
      throw new ApiError(500, 'Erreur backend');
    });

    renderSearchPage();

    expect(await screen.findByRole('alert')).toHaveTextContent('Erreur backend');
  });

  it('renders page title using i18n', async () => {
    const fetchWithAuthMock = vi.mocked(fetchWithAuth);
    fetchWithAuthMock.mockImplementation(async (url: string) => {
      if (url === '/api/v1/media' || url === '/api/v1/themes') {
        return jsonResponse([]);
      }
      return jsonResponse(emptyPage);
    });

    render(
      <I18nProvider>
        <MemoryRouter initialEntries={['/']}>
          <JournalistSearchPage />
        </MemoryRouter>
      </I18nProvider>,
    );

    await waitFor(() => {
      expect(screen.getByRole('heading', { name: 'Recherche de journalistes', level: 1 })).toBeInTheDocument();
    });
  });

  it('renders pagination buttons with i18n text', async () => {
    const fetchWithAuthMock = vi.mocked(fetchWithAuth);
    fetchWithAuthMock.mockImplementation(async (url: string) => {
      if (url === '/api/v1/media' || url === '/api/v1/themes') {
        return jsonResponse([]);
      }
      const page = {
        ...emptyPage,
        first: false,
        last: false,
        totalElements: 50,
      };
      return jsonResponse(page);
    });

    renderSearchPage();

    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Précédent' })).toBeInTheDocument();
      expect(screen.getByRole('button', { name: 'Suivant' })).toBeInTheDocument();
    });
  });
});

