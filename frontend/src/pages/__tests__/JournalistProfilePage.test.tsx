import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { ApiError, fetchWithAuth } from '../../api/apiClient';
import { JournalistProfilePage } from '../JournalistProfilePage';

vi.mock('../../api/apiClient', async () => {
  const actual = await vi.importActual<typeof import('../../api/apiClient')>('../../api/apiClient');
  return {
    ...actual,
    fetchWithAuth: vi.fn(),
  };
});

const renderProfilePage = () =>
  render(
    <MemoryRouter initialEntries={['/journalists/123']}>
      <Routes>
        <Route path="/journalists/:id" element={<JournalistProfilePage />} />
      </Routes>
    </MemoryRouter>,
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
      'Impossible de charger la fiche journaliste. Veuillez reessayer.',
    );
  });
});

