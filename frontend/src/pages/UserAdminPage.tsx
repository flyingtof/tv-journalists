import { useCallback, useEffect, useMemo, useRef, useState, type ChangeEvent, type FormEvent } from 'react';
import { fetchWithAuth, UnauthorizedError } from '../api/apiClient';
import { useI18n } from '../i18n/useI18n';
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

interface EditUserFormState {
  firstName: string;
  lastName: string;
  enabled: boolean;
  roles: UserRole[];
}

const initialFormState = (): CreateUserFormState => ({
  username: '',
  password: '',
  firstName: '',
  lastName: '',
  enabled: true,
  roles: ['USER'],
});

const initialEditFormState = (): EditUserFormState => ({
  firstName: '',
  lastName: '',
  enabled: true,
  roles: ['USER'],
});

const formatRoles = (roles: UserRole[]) => roles.join(', ');

const deriveEditFormState = (user: UserSummary): EditUserFormState => ({
  firstName: user.firstName,
  lastName: user.lastName,
  enabled: user.enabled,
  roles: [...user.roles],
});

export const UserAdminPage = () => {
  const { t } = useI18n();
  
  const roleOptions: Array<{ value: UserRole; label: string }> = [
    { value: 'USER', label: t('userAdmin.roleUser') },
    { value: 'THEME_MANAGER', label: t('userAdmin.roleThemeManager') },
    { value: 'ADMIN', label: t('userAdmin.roleAdmin') },
  ];
  
  const [users, setUsers] = useState<UserSummary[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [formState, setFormState] = useState<CreateUserFormState>(initialFormState);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [submitSuccess, setSubmitSuccess] = useState<string | null>(null);
  const [selectedUserId, setSelectedUserId] = useState<string | null>(null);
  const [editFormState, setEditFormState] = useState<EditUserFormState>(initialEditFormState);
  const [isUpdatingUser, setIsUpdatingUser] = useState(false);
  const [updateError, setUpdateError] = useState<string | null>(null);
  const [updateSuccess, setUpdateSuccess] = useState<string | null>(null);
  const [resetPassword, setResetPassword] = useState('');
  const [isResettingPassword, setIsResettingPassword] = useState(false);
  const [resetPasswordError, setResetPasswordError] = useState<string | null>(null);
  const [resetPasswordSuccess, setResetPasswordSuccess] = useState<string | null>(null);
  const previousSelectedUserIdRef = useRef<string | null>(null);
  const editPanelRef = useRef<HTMLElement | null>(null);
  const firstEditFieldRef = useRef<HTMLInputElement | null>(null);

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
          setLoadError(t('userAdmin.loadError'));
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
  }, [fetchUsers, t]);

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
      setLoadError(t('userAdmin.loadError'));
    } finally {
      setIsLoading(false);
    }
  }, [fetchUsers, t]);

  const sortedUsers = useMemo(
    () => [...users].sort((left, right) => left.username.localeCompare(right.username)),
    [users],
  );

  const selectedUser = useMemo(
    () => sortedUsers.find((user) => user.id === selectedUserId) ?? null,
    [selectedUserId, sortedUsers],
  );

  useEffect(() => {
    if (!selectedUserId) {
      previousSelectedUserIdRef.current = null;
      return;
    }

    const hasSelectedUserChanged = previousSelectedUserIdRef.current !== selectedUserId;
    previousSelectedUserIdRef.current = selectedUserId;

    if (!selectedUser) {
      setSelectedUserId(null);
      setEditFormState(initialEditFormState());
      setResetPassword('');
      setResetPasswordError(null);
      setResetPasswordSuccess(null);
      setUpdateError(null);
      setUpdateSuccess(null);
      return;
    }

    if (hasSelectedUserChanged) {
      setEditFormState(deriveEditFormState(selectedUser));
      setResetPassword('');
      setResetPasswordError(null);
      setResetPasswordSuccess(null);
      setUpdateError(null);
      setUpdateSuccess(null);

      const focusAndScroll = () => {
        if (typeof editPanelRef.current?.scrollIntoView === 'function') {
          editPanelRef.current.scrollIntoView({ block: 'start', behavior: 'smooth' });
        }

        const activeElement = document.activeElement;
        if (
          activeElement instanceof HTMLElement
          && activeElement !== document.body
          && editPanelRef.current?.contains(activeElement)
        ) {
          return;
        }

        firstEditFieldRef.current?.focus();
      };

      if (typeof window.requestAnimationFrame === 'function') {
        window.requestAnimationFrame(focusAndScroll);
      } else {
        focusAndScroll();
      }
    }
  }, [selectedUser, selectedUserId]);

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

  const openEditPanel = (user: UserSummary) => {
    setSelectedUserId(user.id);
  };

  const closeEditPanel = () => {
    setSelectedUserId(null);
    setEditFormState(initialEditFormState());
    setResetPassword('');
    setResetPasswordError(null);
    setResetPasswordSuccess(null);
    setUpdateError(null);
    setUpdateSuccess(null);
  };

  const handleEditInputChange = (event: ChangeEvent<HTMLInputElement>) => {
    const { name, value, checked, type } = event.target;

    setEditFormState((current) => ({
      ...current,
      [name]: type === 'checkbox' ? checked : value,
    }));
    setUpdateError(null);
    setUpdateSuccess(null);
  };

  const toggleEditRole = (role: UserRole) => {
    setEditFormState((current) => {
      const hasRole = current.roles.includes(role);
      const roles = hasRole
        ? current.roles.filter((existingRole) => existingRole !== role)
        : [...current.roles, role];

      return {
        ...current,
        roles,
      };
    });
    setUpdateError(null);
    setUpdateSuccess(null);
  };

  const handleResetPasswordInputChange = (event: ChangeEvent<HTMLInputElement>) => {
    setResetPassword(event.target.value);
    setResetPasswordError(null);
    setResetPasswordSuccess(null);
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

      setSubmitSuccess(t('userAdmin.createSuccess'));
      setFormState(initialFormState());
      await loadUsers();
    } catch (error) {
      if (error instanceof UnauthorizedError) {
        return;
      }

      console.error('Failed to create user:', error);
      setSubmitError(t('userAdmin.createError'));
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleUpdateSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    if (!selectedUserId) {
      return;
    }

    setIsUpdatingUser(true);
    setUpdateError(null);
    setUpdateSuccess(null);

    try {
      await fetchWithAuth(`/api/v1/users/${selectedUserId}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          firstName: editFormState.firstName.trim(),
          lastName: editFormState.lastName.trim(),
          roles: editFormState.roles,
          enabled: editFormState.enabled,
        }),
      });

      setUpdateSuccess(t('userAdmin.updateSuccess'));
      await loadUsers();
    } catch (error) {
      if (error instanceof UnauthorizedError) {
        return;
      }

      console.error('Failed to update user:', error);
      setUpdateError(t('userAdmin.updateError'));
    } finally {
      setIsUpdatingUser(false);
    }
  };

  const handlePasswordResetSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    if (!selectedUserId) {
      return;
    }

    setIsResettingPassword(true);
    setResetPasswordError(null);
    setResetPasswordSuccess(null);

    try {
      await fetchWithAuth(`/api/v1/users/${selectedUserId}/password-reset`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ password: resetPassword }),
      });

      setResetPassword('');
      setResetPasswordSuccess(t('userAdmin.resetSuccess'));
      await loadUsers();
    } catch (error) {
      if (error instanceof UnauthorizedError) {
        return;
      }

      console.error('Failed to reset user password:', error);
      setResetPasswordError(t('userAdmin.resetError'));
    } finally {
      setIsResettingPassword(false);
    }
  };

  return (
    <div className="user-admin-page">
      <section className="user-admin-card user-admin-intro">
        <h1>{t('userAdmin.title')}</h1>
        <p>{t('userAdmin.intro')}</p>
      </section>

      <div className="user-admin-layout">
        <section className="user-admin-card" aria-labelledby="create-user-title">
          <div className="user-admin-section-header">
            <h2 id="create-user-title">{t('userAdmin.createSection')}</h2>
            <p>{t('userAdmin.createSectionDesc')}</p>
          </div>

          <form className="user-admin-form" onSubmit={handleSubmit}>
            <label className="user-admin-field">
              <span>{t('userAdmin.usernameLabel')}</span>
              <input
                type="text"
                name="username"
                value={formState.username}
                onChange={handleInputChange}
                required
                autoComplete="username"
              />
            </label>

            <label className="user-admin-field">
              <span>{t('userAdmin.passwordLabel')}</span>
              <input
                type="password"
                name="password"
                value={formState.password}
                onChange={handleInputChange}
                required
                autoComplete="new-password"
              />
            </label>

            <div className="user-admin-form-grid">
              <label className="user-admin-field">
                <span>{t('userAdmin.firstNameLabel')}</span>
                <input
                  type="text"
                  name="firstName"
                  autoComplete="given-name"
                  value={formState.firstName}
                  onChange={handleInputChange}
                  required
                />
              </label>

              <label className="user-admin-field">
                <span>{t('userAdmin.lastNameLabel')}</span>
                <input
                  type="text"
                  name="lastName"
                  autoComplete="family-name"
                  value={formState.lastName}
                  onChange={handleInputChange}
                  required
                />
              </label>
            </div>

            <fieldset className="user-admin-fieldset">
              <legend>{t('userAdmin.rolesLegend')}</legend>
              <div className="user-admin-checkbox-list">
                {roleOptions.map((roleOption) => (
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
              <span>{t('userAdmin.enabledLabel')}</span>
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
              {isSubmitting ? t('userAdmin.createButtonLoading') : t('userAdmin.createButton')}
            </button>
          </form>
        </section>

        <section className="user-admin-card" aria-labelledby="users-list-title">
          <div className="user-admin-section-header">
            <h2 id="users-list-title">{t('userAdmin.usersSection')}</h2>
            <p>{t('userAdmin.userCount', { count: sortedUsers.length })}</p>
          </div>

          {loadError && (
            <p className="user-admin-feedback user-admin-feedback-error" role="alert">
              {loadError}
            </p>
          )}

          {isLoading ? (
            <p className="user-admin-empty-state" role="status">
              {t('userAdmin.loading')}
            </p>
          ) : sortedUsers.length === 0 ? (
            <p className="user-admin-empty-state">{t('userAdmin.emptyState')}</p>
          ) : (
            <div className="user-admin-table-wrapper">
              <table className="user-admin-table">
                <thead>
                  <tr>
                    <th scope="col">{t('userAdmin.columnUser')}</th>
                    <th scope="col">{t('userAdmin.columnRoles')}</th>
                    <th scope="col">{t('userAdmin.columnStatus')}</th>
                    <th scope="col">{t('userAdmin.columnActions')}</th>
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
                          {user.enabled ? t('userAdmin.statusEnabled') : t('userAdmin.statusDisabled')}
                        </span>
                      </td>
                      <td className="user-admin-table-actions">
                        <button
                          type="button"
                          className="user-admin-action-button"
                          onClick={() => openEditPanel(user)}
                          aria-label={`${t('userAdmin.editButton')} ${user.username}`}
                        >
                          {t('userAdmin.editButton')}
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>
      </div>

      {selectedUser && (
        <section
          ref={editPanelRef}
          className="user-admin-card user-admin-edit-panel"
          aria-labelledby="user-admin-edit-title"
          role="region"
        >
          <div className="user-admin-section-header user-admin-section-header-with-action">
            <div>
              <h2 id="user-admin-edit-title">{t('userAdmin.editTitle')}</h2>
              <p>{t('userAdmin.editDesc', { username: selectedUser.username })}</p>
            </div>
            <button
              type="button"
              className="user-admin-close-button"
              onClick={closeEditPanel}
              aria-label={t('userAdmin.closePanelLabel')}
            >
              {t('userAdmin.closeButton')}
            </button>
          </div>

          <form className="user-admin-form" onSubmit={handleUpdateSubmit}>
            <div className="user-admin-form-grid">
              <label className="user-admin-field">
                <span>{t('userAdmin.firstNameLabel')}</span>
                <input
                  ref={firstEditFieldRef}
                  type="text"
                  name="firstName"
                  autoComplete="given-name"
                  value={editFormState.firstName}
                  onChange={handleEditInputChange}
                  required
                />
              </label>

              <label className="user-admin-field">
                <span>{t('userAdmin.lastNameLabel')}</span>
                <input
                  type="text"
                  name="lastName"
                  autoComplete="family-name"
                  value={editFormState.lastName}
                  onChange={handleEditInputChange}
                  required
                />
              </label>
            </div>

            <fieldset className="user-admin-fieldset">
              <legend>{t('userAdmin.rolesLegend')}</legend>
              <div className="user-admin-checkbox-list">
                {roleOptions.map((roleOption) => (
                  <label key={roleOption.value} className="user-admin-checkbox">
                    <input
                      type="checkbox"
                      checked={editFormState.roles.includes(roleOption.value)}
                      onChange={() => toggleEditRole(roleOption.value)}
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
                checked={editFormState.enabled}
                onChange={handleEditInputChange}
              />
              <span>{t('userAdmin.enabledLabel')}</span>
            </label>

            {updateSuccess && (
              <p className="user-admin-feedback user-admin-feedback-success" role="status">
                {updateSuccess}
              </p>
            )}

            {updateError && (
              <p className="user-admin-feedback user-admin-feedback-error" role="alert">
                {updateError}
              </p>
            )}

            <button
              type="submit"
              className="user-admin-submit"
              disabled={isUpdatingUser || editFormState.roles.length === 0}
            >
              {t('userAdmin.updateButton')}
            </button>
          </form>

          <form className="user-admin-reset-form" onSubmit={handlePasswordResetSubmit}>
            <h3 className="user-admin-reset-title">{t('userAdmin.resetPasswordSection')}</h3>

            <label className="user-admin-field">
              <span>{t('userAdmin.newPasswordLabel')}</span>
              <input
                type="password"
                value={resetPassword}
                onChange={handleResetPasswordInputChange}
                autoComplete="new-password"
                minLength={8}
                required
              />
            </label>

            {resetPasswordSuccess && (
              <p className="user-admin-feedback user-admin-feedback-success" role="status">
                {resetPasswordSuccess}
              </p>
            )}

            {resetPasswordError && (
              <p className="user-admin-feedback user-admin-feedback-error" role="alert">
                {resetPasswordError}
              </p>
            )}

            <button type="submit" className="user-admin-submit" disabled={isResettingPassword || resetPassword.length === 0}>
              {isResettingPassword ? t('userAdmin.resetPasswordButtonLoading') : t('userAdmin.resetPasswordButton')}
            </button>
          </form>
        </section>
      )}
    </div>
  );
};
