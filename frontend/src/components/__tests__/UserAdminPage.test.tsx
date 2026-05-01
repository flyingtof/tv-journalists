import { render, screen, waitFor, within } from '@testing-library/react';
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

  it('opens an edit panel for the selected user', async () => {
    const existingUsers: UserSummary[] = [
      {
        id: '1',
        username: 'admin',
        firstName: 'Alice',
        lastName: 'Admin',
        enabled: true,
        roles: ['ADMIN', 'USER'],
      },
    ];

    const fetchMock = vi.fn().mockResolvedValue(jsonResponse(existingUsers));

    vi.stubGlobal('fetch', fetchMock);

    render(<UserAdminPage />);

    await screen.findByRole('cell', { name: 'admin' });

    const user = userEvent.setup();
    await user.click(screen.getByRole('button', { name: 'Modifier admin' }));

    const panel = await screen.findByRole('region', { name: "Modifier l’utilisateur" });
    const panelWithin = within(panel);

    expect(panelWithin.getByLabelText('Prénom')).toHaveValue('Alice');
    expect(panelWithin.getByLabelText('Nom')).toHaveValue('Admin');
    expect(panelWithin.getByLabelText('Administrateur')).toBeChecked();
    expect(panelWithin.getByLabelText('Compte actif')).toBeChecked();
  });

  it('scrolls the edit panel into view when selecting a user', async () => {
    const existingUsers: UserSummary[] = [
      {
        id: '1',
        username: 'admin',
        firstName: 'Alice',
        lastName: 'Admin',
        enabled: true,
        roles: ['ADMIN', 'USER'],
      },
    ];

    const fetchMock = vi.fn().mockResolvedValue(jsonResponse(existingUsers));
    const scrollIntoViewMock = vi.fn();

    vi.stubGlobal('fetch', fetchMock);
    Object.defineProperty(window.HTMLElement.prototype, 'scrollIntoView', {
      configurable: true,
      value: scrollIntoViewMock,
    });

    render(<UserAdminPage />);

    await screen.findByRole('cell', { name: 'admin' });

    const user = userEvent.setup();
    await user.click(screen.getByRole('button', { name: 'Modifier admin' }));

    await screen.findByRole('region', { name: "Modifier l’utilisateur" });

    await waitFor(() => {
      expect(scrollIntoViewMock).toHaveBeenCalled();
    });
  });

  it('requires at least 8 characters for a password reset', async () => {
    const existingUsers: UserSummary[] = [
      {
        id: '1',
        username: 'admin',
        firstName: 'Alice',
        lastName: 'Admin',
        enabled: true,
        roles: ['ADMIN', 'USER'],
      },
    ];

    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(jsonResponse(existingUsers)));

    render(<UserAdminPage />);

    await screen.findByRole('cell', { name: 'admin' });

    const user = userEvent.setup();
    await user.click(screen.getByRole('button', { name: 'Modifier admin' }));

    const panel = await screen.findByRole('region', { name: "Modifier l’utilisateur" });

    expect(within(panel).getByLabelText('Nouveau mot de passe')).toHaveAttribute('minlength', '8');
  });

  it('closes the edit panel when requested explicitly', async () => {
    const existingUsers: UserSummary[] = [
      {
        id: '1',
        username: 'admin',
        firstName: 'Alice',
        lastName: 'Admin',
        enabled: true,
        roles: ['ADMIN', 'USER'],
      },
    ];

    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(jsonResponse(existingUsers)));

    render(<UserAdminPage />);

    await screen.findByRole('cell', { name: 'admin' });

    const user = userEvent.setup();
    await user.click(screen.getByRole('button', { name: 'Modifier admin' }));
    await user.click(await screen.findByRole('button', { name: 'Fermer le panneau d’édition' }));

    expect(screen.queryByRole('region', { name: "Modifier l’utilisateur" })).not.toBeInTheDocument();
  });

  it('submits an update request for the selected user and refreshes the list', async () => {
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

    const updatedUser: UserSummary = {
      id: '1',
      username: 'admin',
      firstName: 'Alicia',
      lastName: 'Editor',
      enabled: false,
      roles: ['USER'],
    };

    const refreshedUsers: UserSummary[] = [updatedUser];

    const fetchMock = vi
      .fn()
      .mockResolvedValueOnce(jsonResponse(initialUsers))
      .mockResolvedValueOnce(jsonResponse(updatedUser))
      .mockResolvedValueOnce(jsonResponse(refreshedUsers));

    vi.stubGlobal('fetch', fetchMock);

    render(<UserAdminPage />);

    await screen.findByRole('cell', { name: 'admin' });

    const user = userEvent.setup();
    await user.click(screen.getByRole('button', { name: 'Modifier admin' }));

    const panel = await screen.findByRole('region', { name: "Modifier l’utilisateur" });
    const panelWithin = within(panel);

    await user.clear(panelWithin.getByLabelText('Prénom'));
    await user.type(panelWithin.getByLabelText('Prénom'), 'Alicia');
    await user.clear(panelWithin.getByLabelText('Nom'));
    await user.type(panelWithin.getByLabelText('Nom'), 'Editor');
    await user.click(panelWithin.getByLabelText('Administrateur'));
    await user.click(panelWithin.getByLabelText('Compte actif'));
    await user.click(panelWithin.getByRole('button', { name: 'Enregistrer les modifications' }));

    await waitFor(() => {
      expect(fetchMock).toHaveBeenNthCalledWith(
        2,
        '/api/v1/users/1',
        expect.objectContaining({
          method: 'PUT',
          credentials: 'include',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            firstName: 'Alicia',
            lastName: 'Editor',
            roles: ['USER'],
            enabled: false,
          }),
        }),
      );
    });

    expect(await screen.findByText('Utilisateur mis à jour.')).toBeInTheDocument();
    expect(panelWithin.getByLabelText('Prénom')).toHaveValue('Alicia');
    expect(screen.getByRole('cell', { name: 'USER' })).toBeInTheDocument();
    expect(screen.getByRole('cell', { name: 'Désactivé' })).toBeInTheDocument();
  });

  it('disables the update submit button when no role remains selected', async () => {
    const existingUsers: UserSummary[] = [
      {
        id: '1',
        username: 'admin',
        firstName: 'Alice',
        lastName: 'Admin',
        enabled: true,
        roles: ['USER'],
      },
    ];

    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(jsonResponse(existingUsers)));

    render(<UserAdminPage />);

    await screen.findByRole('cell', { name: 'admin' });

    const user = userEvent.setup();
    await user.click(screen.getByRole('button', { name: 'Modifier admin' }));

    const panel = await screen.findByRole('region', { name: "Modifier l’utilisateur" });
    const panelWithin = within(panel);

    await user.click(panelWithin.getByLabelText('Utilisateur standard'));

    expect(panelWithin.getByRole('button', { name: 'Enregistrer les modifications' })).toBeDisabled();
  });

  it('shows an error when updating a user fails', async () => {
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

    const fetchMock = vi
      .fn()
      .mockResolvedValueOnce(jsonResponse(initialUsers))
      .mockResolvedValueOnce({
        status: 500,
        ok: false,
        json: async () => ({}),
      });

    vi.stubGlobal('fetch', fetchMock);

    render(<UserAdminPage />);

    await screen.findByRole('cell', { name: 'admin' });

    const user = userEvent.setup();
    await user.click(screen.getByRole('button', { name: 'Modifier admin' }));

    const panel = await screen.findByRole('region', { name: "Modifier l’utilisateur" });
    const panelWithin = within(panel);

    await user.click(panelWithin.getByRole('button', { name: 'Enregistrer les modifications' }));

    expect(await panelWithin.findByRole('alert')).toHaveTextContent('Impossible de mettre à jour l’utilisateur.');
  });

  it('submits a password reset for the selected user and clears the field after success', async () => {
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

    const refreshedUsers: UserSummary[] = existingUsers;

    const fetchMock = vi
      .fn()
      .mockResolvedValueOnce(jsonResponse(existingUsers))
      .mockResolvedValueOnce(jsonResponse({ id: '1' }, 200))
      .mockResolvedValueOnce(jsonResponse(refreshedUsers));

    vi.stubGlobal('fetch', fetchMock);

    render(<UserAdminPage />);

    await screen.findByRole('cell', { name: 'admin' });

    const user = userEvent.setup();
    await user.click(screen.getByRole('button', { name: 'Modifier admin' }));

    const panel = await screen.findByRole('region', { name: "Modifier l’utilisateur" });
    const panelWithin = within(panel);

    await user.type(panelWithin.getByLabelText('Nouveau mot de passe'), 'reset123!');
    await user.click(panelWithin.getByRole('button', { name: 'Réinitialiser le mot de passe' }));

    await waitFor(() => {
      expect(fetchMock).toHaveBeenNthCalledWith(
        2,
        '/api/v1/users/1/password-reset',
        expect.objectContaining({
          method: 'POST',
          credentials: 'include',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ password: 'reset123!' }),
        }),
      );
    });

    await waitFor(() => {
      expect(fetchMock).toHaveBeenNthCalledWith(
        3,
        '/api/v1/users',
        expect.objectContaining({ credentials: 'include' }),
      );
    });

    expect(await screen.findByText('Mot de passe réinitialisé.')).toBeInTheDocument();
    expect(panelWithin.getByLabelText('Nouveau mot de passe')).toHaveValue('');
  });

  it('shows an error when resetting a password fails', async () => {
    const existingUsers: UserSummary[] = [
      {
        id: '1',
        username: 'admin',
        firstName: 'Alice',
        lastName: 'Admin',
        enabled: true,
        roles: ['ADMIN', 'USER'],
      },
    ];

    const fetchMock = vi
      .fn()
      .mockResolvedValueOnce(jsonResponse(existingUsers))
      .mockResolvedValueOnce({
        status: 400,
        ok: false,
        json: async () => ({}),
      });

    vi.stubGlobal('fetch', fetchMock);

    render(<UserAdminPage />);

    await screen.findByRole('cell', { name: 'admin' });

    const user = userEvent.setup();
    await user.click(screen.getByRole('button', { name: 'Modifier admin' }));

    const panel = await screen.findByRole('region', { name: "Modifier l’utilisateur" });
    const panelWithin = within(panel);

    await user.type(panelWithin.getByLabelText('Nouveau mot de passe'), 'reset123!');
    await user.click(panelWithin.getByRole('button', { name: 'Réinitialiser le mot de passe' }));

    expect(await panelWithin.findByRole('alert')).toHaveTextContent('Impossible de réinitialiser le mot de passe.');
  });

  it('clears edit feedback and reset input when selecting another user', async () => {
    const initialUsers: UserSummary[] = [
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

    const updatedUser: UserSummary = {
      id: '1',
      username: 'admin',
      firstName: 'Alicia',
      lastName: 'Admin',
      enabled: true,
      roles: ['ADMIN', 'USER'],
    };

    const refreshedUsers: UserSummary[] = [updatedUser, initialUsers[1]];

    const fetchMock = vi
      .fn()
      .mockResolvedValueOnce(jsonResponse(initialUsers))
      .mockResolvedValueOnce(jsonResponse(updatedUser))
      .mockResolvedValueOnce(jsonResponse(refreshedUsers));

    vi.stubGlobal('fetch', fetchMock);

    render(<UserAdminPage />);

    await screen.findByRole('cell', { name: 'admin' });

    const user = userEvent.setup();
    await user.click(screen.getByRole('button', { name: 'Modifier admin' }));

    const panel = await screen.findByRole('region', { name: "Modifier l’utilisateur" });
    const panelWithin = within(panel);

    // Perform an update to create the edit success message
    await user.clear(panelWithin.getByLabelText('Prénom'));
    await user.type(panelWithin.getByLabelText('Prénom'), 'Alicia');
    await user.click(panelWithin.getByRole('button', { name: 'Enregistrer les modifications' }));

    await waitFor(() => {
      expect(fetchMock).toHaveBeenNthCalledWith(
        2,
        '/api/v1/users/1',
        expect.objectContaining({
          method: 'PUT',
          credentials: 'include',
          headers: { 'Content-Type': 'application/json' },
        }),
      );
    });

    expect(await screen.findByText('Utilisateur mis à jour.')).toBeInTheDocument();

    // Type into the reset field (but do not submit)
    await user.type(panelWithin.getByLabelText('Nouveau mot de passe'), 'temp123!');

    // Switch to another user
    await user.click(screen.getByRole('button', { name: 'Modifier reader' }));

    const newPanel = await screen.findByRole('region', { name: "Modifier l’utilisateur" });
    const newWithin = within(newPanel);

    expect(newWithin.getByLabelText('Prénom')).toHaveValue('Rita');
    expect(screen.queryByText('Utilisateur mis à jour.')).not.toBeInTheDocument();
    expect(newWithin.getByLabelText('Nouveau mot de passe')).toHaveValue('');
  });
});
