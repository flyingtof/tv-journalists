import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { UserAdminPage } from '../../pages/UserAdminPage';
import type { UserSummary } from '../../types';

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

describe('UserAdminPage', () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('loads existing users and shows their roles and status', async () => {
    const existingUsers: UserSummary[] = [
      {
        id: '1',
        username: 'admin',
        firstName: 'Alice',
        lastName: 'Admin',
        enabled: true,
        roles: ['ADMIN', 'USER'],
      },
      {
        id: '2',
        username: 'reader',
        firstName: 'Rita',
        lastName: 'Reader',
        enabled: false,
        roles: ['USER'],
      },
    ];

    const fetchMock = vi.fn().mockResolvedValue(
      jsonResponse(existingUsers),
    );

    vi.stubGlobal('fetch', fetchMock);

    render(<UserAdminPage />);

    expect(await screen.findByRole('cell', { name: 'admin' })).toBeInTheDocument();
    expect(screen.getByRole('cell', { name: 'ADMIN, USER' })).toBeInTheDocument();
    expect(screen.getByRole('cell', { name: 'Actif' })).toBeInTheDocument();
    expect(screen.getByRole('cell', { name: 'reader' })).toBeInTheDocument();
    expect(screen.getByRole('cell', { name: 'Désactivé' })).toBeInTheDocument();
    expect(fetchMock).toHaveBeenCalledWith('/api/v1/users', expect.objectContaining({ credentials: 'include' }));
  });

  it('declares explicit autocomplete metadata on the create-user form', async () => {
    const fetchMock = vi.fn().mockResolvedValue(jsonResponse([]));

    vi.stubGlobal('fetch', fetchMock);

    render(<UserAdminPage />);

    await screen.findByRole('heading', { name: 'Créer un utilisateur' });

    expect(screen.getByLabelText('Nom d’utilisateur')).toHaveAttribute('autocomplete', 'username');
    expect(screen.getByLabelText('Mot de passe initial')).toHaveAttribute('autocomplete', 'new-password');
    expect(screen.getByLabelText('Prénom')).toHaveAttribute('autocomplete', 'given-name');
    expect(screen.getByLabelText('Nom')).toHaveAttribute('autocomplete', 'family-name');
  });

  it('submits a create request with the entered values and refreshes the list', async () => {
    const initialUsers: UserSummary[] = [
      {
        id: '1',
        username: 'admin',
        firstName: 'Alice',
        lastName: 'Admin',
        enabled: true,
        roles: ['ADMIN', 'USER'],
      },
    ];
    const refreshedUsers: UserSummary[] = [
      ...initialUsers,
      {
        id: '2',
        username: 'manager',
        firstName: 'Marie',
        lastName: 'Manager',
        enabled: false,
        roles: ['ADMIN', 'USER'],
      },
    ];

    const fetchMock = vi
      .fn()
      .mockResolvedValueOnce(jsonResponse(initialUsers))
      .mockResolvedValueOnce(
        jsonResponse(
          {
            id: '2',
            username: 'manager',
            firstName: 'Marie',
            lastName: 'Manager',
            enabled: false,
            roles: ['ADMIN', 'USER'],
          },
          201,
        ),
      )
      .mockResolvedValueOnce(jsonResponse(refreshedUsers));

    vi.stubGlobal('fetch', fetchMock);

    render(<UserAdminPage />);

    await screen.findByRole('cell', { name: 'admin' });

    const user = userEvent.setup();
    await user.type(screen.getByLabelText('Nom d’utilisateur'), 'manager');
    await user.type(screen.getByLabelText('Mot de passe initial'), 'manager123!');
    await user.type(screen.getByLabelText('Prénom'), 'Marie');
    await user.type(screen.getByLabelText('Nom'), 'Manager');
    await user.click(screen.getByLabelText('Administrateur'));
    await user.click(screen.getByLabelText('Compte actif'));
    await user.click(screen.getByRole('button', { name: 'Créer l’utilisateur' }));

    await waitFor(() => {
      expect(fetchMock).toHaveBeenNthCalledWith(
        2,
        '/api/v1/users',
        expect.objectContaining({
          method: 'POST',
          credentials: 'include',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            username: 'manager',
            password: 'manager123!',
            firstName: 'Marie',
            lastName: 'Manager',
            roles: ['USER', 'ADMIN'],
            enabled: false,
          }),
        }),
      );
    });

    expect(await screen.findByText('Utilisateur créé.')).toBeInTheDocument();
    expect(await screen.findByRole('cell', { name: 'manager' })).toBeInTheDocument();
    expect(screen.getAllByRole('cell', { name: 'ADMIN, USER' })).toHaveLength(2);
  });
});
