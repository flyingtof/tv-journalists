import { useCallback, useEffect, useMemo, useState, type ChangeEvent, type FormEvent } from 'react';
import { fetchWithAuth, UnauthorizedError } from '../api/apiClient';
import '../styles/UserAdmin.css';
import type { UserRole, UserSummary } from '../types';

interface CreateUserFormState {
  username: string;
  password: string;
  firstName: string;
  lastName: string;
  enabled: boolean;
  roles: UserRole[];
}

const ROLE_OPTIONS: Array<{ value: UserRole; label: string }> = [
  { value: 'USER', label: 'Utilisateur standard' },
  { value: 'ADMIN', label: 'Administrateur' },
];

const initialFormState = (): CreateUserFormState => ({
  username: '',
  password: '',
  firstName: '',
  lastName: '',
  enabled: true,
  roles: ['USER'],
});

const formatRoles = (roles: UserRole[]) => roles.join(', ');

export const UserAdminPage = () => {
  const [users, setUsers] = useState<UserSummary[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [formState, setFormState] = useState<CreateUserFormState>(initialFormState);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [submitSuccess, setSubmitSuccess] = useState<string | null>(null);

  const fetchUsers = useCallback(async () => {
    const response = await fetchWithAuth('/api/v1/users');
    return (await response.json()) as UserSummary[];
  }, []);

  useEffect(() => {
    let isMounted = true;

    const bootstrapUsers = async () => {
      try {
        const data = await fetchUsers();
        if (isMounted) {
          setUsers(data);
        }
      } catch (error) {
        if (error instanceof UnauthorizedError) {
          return;
        }

        console.error('Failed to load users:', error);
        if (isMounted) {
          setLoadError('Impossible de charger les utilisateurs.');
        }
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    };

    void bootstrapUsers();

    return () => {
      isMounted = false;
    };
  }, [fetchUsers]);

  const loadUsers = useCallback(async () => {
    setIsLoading(true);
    setLoadError(null);

    try {
      const data = await fetchUsers();
      setUsers(data);
    } catch (error) {
      if (error instanceof UnauthorizedError) {
        return;
      }

      console.error('Failed to load users:', error);
      setLoadError('Impossible de charger les utilisateurs.');
    } finally {
      setIsLoading(false);
    }
  }, [fetchUsers]);

  const sortedUsers = useMemo(
    () => [...users].sort((left, right) => left.username.localeCompare(right.username)),
    [users],
  );

  const handleInputChange = (event: ChangeEvent<HTMLInputElement>) => {
    const { name, value, checked, type } = event.target;

    setFormState((current) => ({
      ...current,
      [name]: type === 'checkbox' ? checked : value,
    }));
  };

  const handleRoleChange = (role: UserRole) => {
    setFormState((current) => {
      const hasRole = current.roles.includes(role);
      const roles = hasRole
        ? current.roles.filter((existingRole) => existingRole !== role)
        : [...current.roles, role];

      return {
        ...current,
        roles,
      };
    });
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setIsSubmitting(true);
    setSubmitError(null);
    setSubmitSuccess(null);

    try {
      await fetchWithAuth('/api/v1/users', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          username: formState.username.trim(),
          password: formState.password,
          firstName: formState.firstName.trim(),
          lastName: formState.lastName.trim(),
          roles: formState.roles,
          enabled: formState.enabled,
        }),
      });

      setSubmitSuccess('Utilisateur créé.');
      setFormState(initialFormState());
      await loadUsers();
    } catch (error) {
      if (error instanceof UnauthorizedError) {
        return;
      }

      console.error('Failed to create user:', error);
      setSubmitError('Impossible de créer l’utilisateur.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="user-admin-page">
      <section className="user-admin-card user-admin-intro">
        <h1>Gestion des utilisateurs</h1>
        <p>Créez un compte et consultez rapidement les accès existants.</p>
      </section>

      <div className="user-admin-layout">
        <section className="user-admin-card" aria-labelledby="create-user-title">
          <div className="user-admin-section-header">
            <h2 id="create-user-title">Créer un utilisateur</h2>
            <p>Définissez un identifiant, un mot de passe initial et les rôles à attribuer.</p>
          </div>

          <form className="user-admin-form" onSubmit={handleSubmit}>
            <label className="user-admin-field">
              <span>Nom d’utilisateur</span>
              <input
                type="text"
                name="username"
                value={formState.username}
                onChange={handleInputChange}
                required
                autoComplete="off"
              />
            </label>

            <label className="user-admin-field">
              <span>Mot de passe initial</span>
              <input
                type="password"
                name="password"
                value={formState.password}
                onChange={handleInputChange}
                required
              />
            </label>

            <div className="user-admin-form-grid">
              <label className="user-admin-field">
                <span>Prénom</span>
                <input
                  type="text"
                  name="firstName"
                  value={formState.firstName}
                  onChange={handleInputChange}
                  required
                />
              </label>

              <label className="user-admin-field">
                <span>Nom</span>
                <input
                  type="text"
                  name="lastName"
                  value={formState.lastName}
                  onChange={handleInputChange}
                  required
                />
              </label>
            </div>

            <fieldset className="user-admin-fieldset">
              <legend>Rôles</legend>
              <div className="user-admin-checkbox-list">
                {ROLE_OPTIONS.map((roleOption) => (
                  <label key={roleOption.value} className="user-admin-checkbox">
                    <input
                      type="checkbox"
                      checked={formState.roles.includes(roleOption.value)}
                      onChange={() => handleRoleChange(roleOption.value)}
                    />
                    <span>{roleOption.label}</span>
                  </label>
                ))}
              </div>
            </fieldset>

            <label className="user-admin-checkbox user-admin-checkbox-inline">
              <input
                type="checkbox"
                name="enabled"
                checked={formState.enabled}
                onChange={handleInputChange}
              />
              <span>Compte actif</span>
            </label>

            {submitSuccess && (
              <p className="user-admin-feedback user-admin-feedback-success" role="status">
                {submitSuccess}
              </p>
            )}

            {submitError && (
              <p className="user-admin-feedback user-admin-feedback-error" role="alert">
                {submitError}
              </p>
            )}

            <button type="submit" className="user-admin-submit" disabled={isSubmitting || formState.roles.length === 0}>
              {isSubmitting ? 'Création…' : 'Créer l’utilisateur'}
            </button>
          </form>
        </section>

        <section className="user-admin-card" aria-labelledby="users-list-title">
          <div className="user-admin-section-header">
            <h2 id="users-list-title">Utilisateurs existants</h2>
            <p>{sortedUsers.length} compte(s) affiché(s).</p>
          </div>

          {loadError && (
            <p className="user-admin-feedback user-admin-feedback-error" role="alert">
              {loadError}
            </p>
          )}

          {isLoading ? (
            <p className="user-admin-empty-state" role="status">
              Chargement des utilisateurs…
            </p>
          ) : sortedUsers.length === 0 ? (
            <p className="user-admin-empty-state">Aucun utilisateur trouvé.</p>
          ) : (
            <div className="user-admin-table-wrapper">
              <table className="user-admin-table">
                <thead>
                  <tr>
                    <th scope="col">Utilisateur</th>
                    <th scope="col">Rôles</th>
                    <th scope="col">Statut</th>
                  </tr>
                </thead>
                <tbody>
                  {sortedUsers.map((user) => (
                    <tr key={user.id}>
                      <td>{user.username}</td>
                      <td>{formatRoles(user.roles)}</td>
                      <td>
                        <span
                          className={`user-admin-status ${user.enabled ? 'user-admin-status-enabled' : 'user-admin-status-disabled'}`}
                        >
                          {user.enabled ? 'Actif' : 'Désactivé'}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>
      </div>
    </div>
  );
};
