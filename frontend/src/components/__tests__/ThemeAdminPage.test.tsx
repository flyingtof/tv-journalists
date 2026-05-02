import { act, render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { ThemeAdminPage } from '../../pages/ThemeAdminPage';
import type { Theme } from '../../types';

type MockResponse = {
  status: number;
  ok: boolean;
  json: () => Promise<unknown>;
};

const jsonResponse = (data: unknown, status = 200): MockResponse => ({
  status,
  ok: status >= 200 && status < 300,
  json: async () => data,
});

describe('ThemeAdminPage', () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('loads and displays existing themes alphabetically', async () => {
    const themes: Theme[] = [
      { id: '3', name: 'Zoologie' },
      { id: '1', name: 'Culture' },
      { id: '2', name: 'Actualités' },
    ];

    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(jsonResponse(themes)));

    render(<ThemeAdminPage />);

    await screen.findByRole('button', { name: 'Sélectionner le thème Actualités' });

    const themeButtons = within(screen.getByRole('list', { name: 'Liste des thèmes' }))
      .getAllByRole('button', { name: /Sélectionner le thème/ });

    expect(themeButtons.map((button) => button.textContent)).toEqual([
      'Actualités',
      'Culture',
      'Zoologie',
    ]);
  });

  it('creates a theme and refreshes the list', async () => {
    const initialThemes: Theme[] = [
      { id: '1', name: 'Culture' },
    ];
    const refreshedThemes: Theme[] = [
      { id: '2', name: 'Analyse' },
      ...initialThemes,
    ];
    let resolveCreate: ((value: MockResponse) => void) | null = null;
    const createResponse = new Promise<MockResponse>((resolve) => {
      resolveCreate = resolve;
    });

    const fetchMock = vi
      .fn()
      .mockResolvedValueOnce(jsonResponse(initialThemes))
      .mockImplementationOnce(() => createResponse)
      .mockResolvedValueOnce(jsonResponse(refreshedThemes));

    vi.stubGlobal('fetch', fetchMock);

    render(<ThemeAdminPage />);

    await screen.findByRole('button', { name: 'Sélectionner le thème Culture' });

    const user = userEvent.setup();
    await user.type(screen.getByLabelText('Nom du thème'), 'Analyse');
    const nameInput = screen.getByLabelText('Nom du thème');
    await user.click(screen.getByRole('button', { name: 'Créer' }));

    await waitFor(() => {
      expect(nameInput).toBeDisabled();
    });
    expect(screen.getByRole('button', { name: 'Sélectionner le thème Culture' })).toBeDisabled();

    await act(async () => {
      resolveCreate?.(jsonResponse({ id: '2', name: 'Analyse' }, 201));
    });

    await waitFor(() => {
      expect(fetchMock).toHaveBeenNthCalledWith(
        2,
        '/api/v1/themes',
        expect.objectContaining({
          method: 'POST',
          credentials: 'include',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ name: 'Analyse' }),
        }),
      );
    });

    expect(await screen.findByText('Thème créé.')).toBeInTheDocument();
    expect(await screen.findByRole('button', { name: 'Sélectionner le thème Analyse' })).toBeInTheDocument();
    expect(screen.getByLabelText('Nom du thème')).toHaveValue('');
  });

  it('keeps the existing list visible while refreshing after create', async () => {
    const initialThemes: Theme[] = [
      { id: '1', name: 'Culture' },
    ];
    const refreshedThemes: Theme[] = [
      { id: '2', name: 'Analyse' },
      ...initialThemes,
    ];
    let resolveCreate: ((value: MockResponse) => void) | null = null;
    let resolveRefresh: ((value: MockResponse) => void) | null = null;

    const createResponse = new Promise<MockResponse>((resolve) => {
      resolveCreate = resolve;
    });
    const refreshResponse = new Promise<MockResponse>((resolve) => {
      resolveRefresh = resolve;
    });

    const fetchMock = vi
      .fn()
      .mockResolvedValueOnce(jsonResponse(initialThemes))
      .mockImplementationOnce(() => createResponse)
      .mockImplementationOnce(() => refreshResponse);

    vi.stubGlobal('fetch', fetchMock);

    render(<ThemeAdminPage />);

    await screen.findByRole('button', { name: 'Sélectionner le thème Culture' });

    const user = userEvent.setup();
    await user.type(screen.getByLabelText('Nom du thème'), 'Analyse');
    await user.click(screen.getByRole('button', { name: 'Créer' }));

    await act(async () => {
      resolveCreate?.(jsonResponse({ id: '2', name: 'Analyse' }, 201));
    });

    await waitFor(() => {
      expect(fetchMock).toHaveBeenNthCalledWith(3, '/api/v1/themes', expect.objectContaining({ credentials: 'include' }));
    });

    expect(screen.getByRole('button', { name: 'Sélectionner le thème Culture' })).toBeInTheDocument();
    expect(screen.queryByText('Chargement des thèmes...')).not.toBeInTheDocument();

    await act(async () => {
      resolveRefresh?.(jsonResponse(refreshedThemes));
    });

    expect(await screen.findByRole('button', { name: 'Sélectionner le thème Analyse' })).toBeInTheDocument();
  });

  it('updates a selected theme and refreshes the list', async () => {
    const initialThemes: Theme[] = [
      { id: '1', name: 'Culture' },
      { id: '2', name: 'Politique' },
    ];
    const refreshedThemes: Theme[] = [
      { id: '1', name: 'Culture générale' },
      { id: '2', name: 'Politique' },
    ];

    const fetchMock = vi
      .fn()
      .mockResolvedValueOnce(jsonResponse(initialThemes))
      .mockResolvedValueOnce(jsonResponse({ id: '1', name: 'Culture générale' }))
      .mockResolvedValueOnce(jsonResponse(refreshedThemes));

    vi.stubGlobal('fetch', fetchMock);

    render(<ThemeAdminPage />);

    const user = userEvent.setup();
    await user.click(await screen.findByRole('button', { name: 'Sélectionner le thème Culture' }));

    const nameInput = screen.getByLabelText('Nom du thème');
    await user.clear(nameInput);
    await user.type(nameInput, 'Culture générale');
    await user.click(screen.getByRole('button', { name: 'Enregistrer le thème' }));

    await waitFor(() => {
      expect(fetchMock).toHaveBeenNthCalledWith(
        2,
        '/api/v1/themes/1',
        expect.objectContaining({
          method: 'PUT',
          credentials: 'include',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ name: 'Culture générale' }),
        }),
      );
    });

    expect(await screen.findByText('Thème mis à jour.')).toBeInTheDocument();
    expect(await screen.findByRole('button', { name: 'Sélectionner le thème Culture générale' })).toBeInTheDocument();
    expect(screen.getByLabelText('Nom du thème')).toHaveValue('Culture générale');
  });

  it('keeps the existing list visible while refreshing after update', async () => {
    const initialThemes: Theme[] = [
      { id: '1', name: 'Culture' },
      { id: '2', name: 'Politique' },
    ];
    const refreshedThemes: Theme[] = [
      { id: '1', name: 'Culture générale' },
      { id: '2', name: 'Politique' },
    ];
    let resolveUpdate: ((value: MockResponse) => void) | null = null;
    let resolveRefresh: ((value: MockResponse) => void) | null = null;

    const updateResponse = new Promise<MockResponse>((resolve) => {
      resolveUpdate = resolve;
    });
    const refreshResponse = new Promise<MockResponse>((resolve) => {
      resolveRefresh = resolve;
    });

    const fetchMock = vi
      .fn()
      .mockResolvedValueOnce(jsonResponse(initialThemes))
      .mockImplementationOnce(() => updateResponse)
      .mockImplementationOnce(() => refreshResponse);

    vi.stubGlobal('fetch', fetchMock);

    render(<ThemeAdminPage />);

    const user = userEvent.setup();
    await user.click(await screen.findByRole('button', { name: 'Sélectionner le thème Culture' }));

    const nameInput = screen.getByLabelText('Nom du thème');
    await user.clear(nameInput);
    await user.type(nameInput, 'Culture générale');
    await user.click(screen.getByRole('button', { name: 'Enregistrer le thème' }));

    await act(async () => {
      resolveUpdate?.(jsonResponse({ id: '1', name: 'Culture générale' }));
    });

    await waitFor(() => {
      expect(fetchMock).toHaveBeenNthCalledWith(3, '/api/v1/themes', expect.objectContaining({ credentials: 'include' }));
    });

    expect(screen.getByRole('button', { name: 'Sélectionner le thème Culture' })).toBeInTheDocument();
    expect(screen.queryByText('Chargement des thèmes...')).not.toBeInTheDocument();

    await act(async () => {
      resolveRefresh?.(jsonResponse(refreshedThemes));
    });

    expect(await screen.findByRole('button', { name: 'Sélectionner le thème Culture générale' })).toBeInTheDocument();
  });

  it('deletes a selected theme, refreshes the list and resets the form', async () => {
    const initialThemes: Theme[] = [
      { id: '1', name: 'Culture' },
      { id: '2', name: 'Politique' },
    ];
    const refreshedThemes: Theme[] = [
      { id: '2', name: 'Politique' },
    ];

    const fetchMock = vi
      .fn()
      .mockResolvedValueOnce(jsonResponse(initialThemes))
      .mockResolvedValueOnce(jsonResponse({}, 204))
      .mockResolvedValueOnce(jsonResponse(refreshedThemes));

    vi.stubGlobal('fetch', fetchMock);
    vi.spyOn(window, 'confirm').mockReturnValue(true);

    render(<ThemeAdminPage />);

    const user = userEvent.setup();
    await user.click(await screen.findByRole('button', { name: 'Sélectionner le thème Culture' }));

    expect(screen.getByLabelText('Nom du thème')).toHaveValue('Culture');

    await user.click(screen.getByRole('button', { name: 'Supprimer' }));

    await waitFor(() => {
      expect(fetchMock).toHaveBeenNthCalledWith(
        2,
        '/api/v1/themes/1',
        expect.objectContaining({
          method: 'DELETE',
          credentials: 'include',
        }),
      );
    });

    await waitFor(() => {
      expect(fetchMock).toHaveBeenNthCalledWith(3, '/api/v1/themes', expect.objectContaining({ credentials: 'include' }));
    });

    expect(await screen.findByText('Thème supprimé.')).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Supprimer' })).not.toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Créer' })).toBeInTheDocument();
    expect(screen.getByLabelText('Nom du thème')).toHaveValue('');
    expect(screen.queryByRole('button', { name: 'Sélectionner le thème Culture' })).not.toBeInTheDocument();
    expect(await screen.findByRole('button', { name: 'Sélectionner le thème Politique' })).toBeInTheDocument();
  });

  it('keeps the existing list visible while refreshing after delete', async () => {
    const initialThemes: Theme[] = [
      { id: '1', name: 'Culture' },
      { id: '2', name: 'Politique' },
    ];
    const refreshedThemes: Theme[] = [
      { id: '2', name: 'Politique' },
    ];
    let resolveDelete: ((value: MockResponse) => void) | null = null;
    let resolveRefresh: ((value: MockResponse) => void) | null = null;

    const deleteResponse = new Promise<MockResponse>((resolve) => {
      resolveDelete = resolve;
    });
    const refreshResponse = new Promise<MockResponse>((resolve) => {
      resolveRefresh = resolve;
    });

    const fetchMock = vi
      .fn()
      .mockResolvedValueOnce(jsonResponse(initialThemes))
      .mockImplementationOnce(() => deleteResponse)
      .mockImplementationOnce(() => refreshResponse);

    vi.stubGlobal('fetch', fetchMock);
    vi.spyOn(window, 'confirm').mockReturnValue(true);

    render(<ThemeAdminPage />);

    const user = userEvent.setup();
    await user.click(await screen.findByRole('button', { name: 'Sélectionner le thème Culture' }));
    await user.click(screen.getByRole('button', { name: 'Supprimer' }));

    await act(async () => {
      resolveDelete?.(jsonResponse({}, 204));
    });

    await waitFor(() => {
      expect(fetchMock).toHaveBeenNthCalledWith(3, '/api/v1/themes', expect.objectContaining({ credentials: 'include' }));
    });

    expect(screen.getByRole('button', { name: 'Sélectionner le thème Politique' })).toBeInTheDocument();
    expect(screen.queryByText('Chargement des thèmes...')).not.toBeInTheDocument();

    await act(async () => {
      resolveRefresh?.(jsonResponse(refreshedThemes));
    });

    expect(screen.queryByRole('button', { name: 'Sélectionner le thème Culture' })).not.toBeInTheDocument();
  });

  it('does not delete a theme when confirmation is declined', async () => {
    const themes: Theme[] = [
      { id: '1', name: 'Culture' },
    ];

    const fetchMock = vi.fn().mockResolvedValueOnce(jsonResponse(themes));

    vi.stubGlobal('fetch', fetchMock);
    const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(false);

    render(<ThemeAdminPage />);

    const user = userEvent.setup();
    await user.click(await screen.findByRole('button', { name: 'Sélectionner le thème Culture' }));
    await user.click(screen.getByRole('button', { name: 'Supprimer' }));

    expect(confirmSpy).toHaveBeenCalledWith('Supprimer définitivement le thème « Culture » ?');
    expect(fetchMock).toHaveBeenCalledTimes(1);
  });

  it('shows explicit error when delete is refused because the theme is in use', async () => {
    const themes: Theme[] = [
      { id: '1', name: 'Culture' },
    ];

    const fetchMock = vi
      .fn()
      .mockResolvedValueOnce(jsonResponse(themes))
      .mockResolvedValueOnce({
        status: 409,
        ok: false,
        json: async () => ({}),
      });

    vi.stubGlobal('fetch', fetchMock);
    vi.spyOn(window, 'confirm').mockReturnValue(true);

    render(<ThemeAdminPage />);

    const user = userEvent.setup();
    await user.click(await screen.findByRole('button', { name: 'Sélectionner le thème Culture' }));
    await user.click(screen.getByRole('button', { name: 'Supprimer' }));

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'Impossible de supprimer ce thème car il est encore utilisé.',
    );
  });
});
