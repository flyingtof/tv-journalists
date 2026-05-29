import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { ApiError, fetchWithAuth } from '../../api/apiClient';
import { AuthContext } from '../../context/AuthContext';
import type { AuthContextValue } from '../../context/AuthContext';
import type { CurrentUser } from '../../types';
import { JournalistProfilePage } from '../JournalistProfilePage';
import { I18nProvider } from '../../i18n/I18nProvider';

vi.mock('../../api/apiClient', async () => {
  const actual = await vi.importActual<typeof import('../../api/apiClient')>('../../api/apiClient');
  return {
    ...actual,
    fetchWithAuth: vi.fn(),
  };
});

const createAuthValue = (roles: string[]): AuthContextValue => {
  const currentUser: CurrentUser = {
    username: 'test-user',
    firstName: 'Test',
    lastName: 'User',
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

const renderProfilePage = (authValue?: AuthContextValue) => {
  const auth = authValue || createAuthValue(['USER']);
  return render(
    <I18nProvider>
      <AuthContext.Provider value={auth}>
        <MemoryRouter initialEntries={['/journalists/123']}>
          <Routes>
            <Route path="/journalists/:id" element={<JournalistProfilePage />} />
            <Route path="/journalists/:id/edit" element={<div>Edit page</div>} />
          </Routes>
        </MemoryRouter>
      </AuthContext.Provider>
    </I18nProvider>,
  );
};

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

  describe('Edit action', () => {
    it('shows edit link for JOURNALIST_MANAGER', async () => {
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

      renderProfilePage(createAuthValue(['JOURNALIST_MANAGER']));

      await screen.findByText('John Doe');
      expect(screen.getByRole('link', { name: /modifier/i })).toBeInTheDocument();
    });

    it('shows edit link for ADMIN', async () => {
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

      renderProfilePage(createAuthValue(['ADMIN']));

      await screen.findByText('Jane Smith');
      expect(screen.getByRole('link', { name: /modifier/i })).toBeInTheDocument();
    });

    it('does not show edit link for USER', async () => {
      const journalist = {
        id: '123',
        firstName: 'Bob',
        lastName: 'Brown',
        globalEmail: 'bob@example.com',
        globalPhone: '+1122334455',
        activities: [],
      };

      vi.mocked(fetchWithAuth).mockResolvedValue({
        json: async () => journalist,
      } as Response);

      renderProfilePage(createAuthValue(['USER']));

      await screen.findByText('Bob Brown');
      expect(screen.queryByRole('link', { name: /modifier/i })).not.toBeInTheDocument();
    });

    it('edit link navigates to edit page with correct id', async () => {
      const journalist = {
        id: '456',
        firstName: 'Alice',
        lastName: 'Green',
        globalEmail: 'alice@example.com',
        globalPhone: '+5555555555',
        activities: [],
      };

      vi.mocked(fetchWithAuth).mockResolvedValue({
        json: async () => journalist,
      } as Response);

      render(
        <I18nProvider>
          <AuthContext.Provider value={createAuthValue(['JOURNALIST_MANAGER'])}>
            <MemoryRouter initialEntries={['/journalists/456']}>
              <Routes>
                <Route path="/journalists/:id" element={<JournalistProfilePage />} />
                <Route path="/journalists/:id/edit" element={<div>Edit page</div>} />
              </Routes>
            </MemoryRouter>
          </AuthContext.Provider>
        </I18nProvider>,
      );

      await screen.findByText('Alice Green');
      const editLink = screen.getByRole('link', { name: /modifier/i });
      expect(editLink).toHaveAttribute('href', '/journalists/456/edit');
    });
  });

  describe('Delete action', () => {
    it('shows delete button for JOURNALIST_MANAGER', async () => {
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

      renderProfilePage(createAuthValue(['JOURNALIST_MANAGER']));

      await screen.findByText('John Doe');
      expect(screen.getByRole('button', { name: /supprimer/i })).toBeInTheDocument();
    });

    it('shows delete button for ADMIN', async () => {
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

      renderProfilePage(createAuthValue(['ADMIN']));

      await screen.findByText('Jane Smith');
      expect(screen.getByRole('button', { name: /supprimer/i })).toBeInTheDocument();
    });

    it('does not show delete button for USER', async () => {
      const journalist = {
        id: '123',
        firstName: 'Bob',
        lastName: 'Brown',
        globalEmail: 'bob@example.com',
        globalPhone: '+1122334455',
        activities: [],
      };

      vi.mocked(fetchWithAuth).mockResolvedValue({
        json: async () => journalist,
      } as Response);

      renderProfilePage(createAuthValue(['USER']));

      await screen.findByText('Bob Brown');
      expect(screen.queryByRole('button', { name: /supprimer/i })).not.toBeInTheDocument();
    });
  });
});

