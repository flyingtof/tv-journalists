import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { ApiError, fetchWithAuth } from '../../api/apiClient';
import { JournalistProfilePage } from '../JournalistProfilePage';
import { I18nProvider } from '../../i18n/I18nProvider';

vi.mock('../../api/apiClient', async () => {
  const actual = await vi.importActual<typeof import('../../api/apiClient')>('../../api/apiClient');
  return {
    ...actual,
    fetchWithAuth: vi.fn(),
  };
});

const renderProfilePage = () =>
  render(
    <I18nProvider>
      <MemoryRouter initialEntries={['/journalists/123']}>
        <Routes>
          <Route path="/journalists/:id" element={<JournalistProfilePage />} />
        </Routes>
      </MemoryRouter>
    </I18nProvider>,
  );

describe('JournalistProfilePage', () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('shows a not-found message on 404', async () => {
    vi.mocked(fetchWithAuth).mockRejectedValue(new ApiError(404, 'Not found'));

    renderProfilePage();

    expect(await screen.findByRole('alert')).toHaveTextContent('Journaliste introuvable');
  });

  it('shows a generic load error on non-404 failures', async () => {
    vi.mocked(fetchWithAuth).mockRejectedValue(new ApiError(500, 'Server error'));

    renderProfilePage();

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'Impossible de charger la fiche journaliste. Veuillez réessayer.',
    );
  });

  it('renders loading state with i18n text', () => {
    vi.mocked(fetchWithAuth).mockImplementation(() => new Promise(() => {}));

    renderProfilePage();

    expect(screen.getByText('Chargement...')).toBeInTheDocument();
  });

  it('renders contact section title using i18n', async () => {
    const journalist = {
      id: '123',
      firstName: 'John',
      lastName: 'Doe',
      globalEmail: 'john@example.com',
      globalPhone: '+1234567890',
      activities: [],
    };

    vi.mocked(fetchWithAuth).mockResolvedValue({
      json: async () => journalist,
    } as Response);

    renderProfilePage();

    await screen.findByText('John Doe');
    expect(screen.getByText('Informations de Contact')).toBeInTheDocument();
  });

  it('renders activities section title using i18n', async () => {
    const journalist = {
      id: '123',
      firstName: 'Jane',
      lastName: 'Smith',
      globalEmail: 'jane@example.com',
      globalPhone: '+0987654321',
      activities: [],
    };

    vi.mocked(fetchWithAuth).mockResolvedValue({
      json: async () => journalist,
    } as Response);

    renderProfilePage();

    await screen.findByText('Jane Smith');
    expect(screen.getByText('Activités Média')).toBeInTheDocument();
  });
});

