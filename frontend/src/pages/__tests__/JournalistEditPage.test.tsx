import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { fetchWithAuth } from '../../api/apiClient';
import { I18nProvider } from '../../i18n/I18nProvider';
import { JournalistEditPage } from '../JournalistEditPage';

vi.mock('../../api/apiClient', async () => {
  const actual = await vi.importActual<typeof import('../../api/apiClient')>('../../api/apiClient');
  return {
    ...actual,
    fetchWithAuth: vi.fn(),
  };
});

describe('JournalistEditPage', () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('loads the journalist to edit', async () => {
    vi.mocked(fetchWithAuth).mockImplementation(async (url: string) => {
      if (url === '/api/v1/media') {
        return {
          json: async () => [{ id: 'media-1', name: 'Green Press' }],
        } as Response;
      }

      if (url === '/api/v1/themes') {
        return {
          json: async () => [{ id: 'theme-1', name: 'Biodiversity' }],
        } as Response;
      }

      if (url === '/api/v1/journalists/123') {
        return {
          json: async () => ({
            id: '123',
            firstName: 'Jane',
            lastName: 'Doe',
            globalEmail: 'jane@example.com',
            globalPhone: '+33123456789',
            activities: [],
          }),
        } as Response;
      }

      throw new Error(`Unexpected request: ${url}`);
    });

    render(
      <I18nProvider>
        <MemoryRouter initialEntries={['/journalists/123/edit']}>
          <Routes>
            <Route path="/journalists/:id/edit" element={<JournalistEditPage />} />
          </Routes>
        </MemoryRouter>
      </I18nProvider>,
    );

    expect(await screen.findByRole('heading', { name: 'Modifier un journaliste' })).toBeInTheDocument();
    expect(fetchWithAuth).toHaveBeenCalledWith('/api/v1/journalists/123');
  });
});
