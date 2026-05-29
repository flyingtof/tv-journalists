import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { fetchWithAuth } from '../../api/apiClient';
import { I18nProvider } from '../../i18n/I18nProvider';
import { JournalistCreatePage } from '../JournalistCreatePage';

vi.mock('../../api/apiClient', async () => {
  const actual = await vi.importActual<typeof import('../../api/apiClient')>('../../api/apiClient');
  return {
    ...actual,
    fetchWithAuth: vi.fn(),
  };
});

describe('JournalistCreatePage', () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('loads media and theme options for the editor', async () => {
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

      throw new Error(`Unexpected request: ${url}`);
    });

    render(
      <I18nProvider>
        <MemoryRouter initialEntries={['/journalists/new']}>
          <Routes>
            <Route path="/journalists/new" element={<JournalistCreatePage />} />
          </Routes>
        </MemoryRouter>
      </I18nProvider>,
    );

    expect(await screen.findByRole('heading', { name: 'Créer un journaliste' })).toBeInTheDocument();
    expect(fetchWithAuth).toHaveBeenCalledWith('/api/v1/media');
    expect(fetchWithAuth).toHaveBeenCalledWith('/api/v1/themes');
  });
});
