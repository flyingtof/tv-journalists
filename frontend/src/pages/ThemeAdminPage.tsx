import { useCallback, useEffect, useMemo, useState, type ChangeEvent, type FormEvent } from 'react';
import { ApiError, fetchWithAuth, UnauthorizedError } from '../api/apiClient';
import '../styles/ThemeAdmin.css';
import type { Theme } from '../types';

interface ThemeFormState {
  name: string;
}

const initialFormState = (): ThemeFormState => ({
  name: '',
});

const sortThemesAlphabetically = (themes: Theme[]) =>
  [...themes].sort((left, right) => left.name.localeCompare(right.name, 'fr', { sensitivity: 'base' }));

const isConflictError = (error: unknown) => error instanceof ApiError && error.status === 409;

export const ThemeAdminPage = () => {
  const [themes, setThemes] = useState<Theme[]>([]);
  const [filter, setFilter] = useState('');
  const [selectedThemeId, setSelectedThemeId] = useState<string | null>(null);
  const [formState, setFormState] = useState<ThemeFormState>(initialFormState);
  const [isLoading, setIsLoading] = useState(true);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [feedback, setFeedback] = useState<{ type: 'success' | 'error'; message: string } | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const fetchThemes = useCallback(async () => {
    const response = await fetchWithAuth('/api/v1/themes');
    return (await response.json()) as Theme[];
  }, []);

  const loadThemes = useCallback(async (options?: { preserveList?: boolean }) => {
    const preserveList = options?.preserveList ?? false;

    if (!preserveList) {
      setIsLoading(true);
    }
    setLoadError(null);

    try {
      const data = await fetchThemes();
      setThemes(data);
    } catch (error) {
      if (error instanceof UnauthorizedError) {
        return;
      }

      console.error('Failed to load themes:', error);
      setLoadError('Impossible de charger les thèmes.');
    } finally {
      if (!preserveList) {
        setIsLoading(false);
      }
    }
  }, [fetchThemes]);

  useEffect(() => {
    void loadThemes();
  }, [loadThemes]);

  const sortedThemes = useMemo(() => sortThemesAlphabetically(themes), [themes]);

  const selectedTheme = useMemo(
    () => sortedThemes.find((theme) => theme.id === selectedThemeId) ?? null,
    [selectedThemeId, sortedThemes],
  );

  const filteredThemes = useMemo(() => {
    const normalizedFilter = filter.trim().toLocaleLowerCase('fr');

    if (!normalizedFilter) {
      return sortedThemes;
    }

    return sortedThemes.filter((theme) => theme.name.toLocaleLowerCase('fr').includes(normalizedFilter));
  }, [filter, sortedThemes]);

  useEffect(() => {
    if (!selectedTheme) {
      setSelectedThemeId(null);
      setFormState(initialFormState());
      return;
    }

    setFormState({ name: selectedTheme.name });
  }, [selectedTheme]);

  const clearFeedback = () => {
    setFeedback(null);
  };

  const handleFilterChange = (event: ChangeEvent<HTMLInputElement>) => {
    setFilter(event.target.value);
  };

  const handleNameChange = (event: ChangeEvent<HTMLInputElement>) => {
    setFormState({ name: event.target.value });
    clearFeedback();
  };

  const handleThemeSelection = (theme: Theme) => {
    setSelectedThemeId(theme.id);
    clearFeedback();
  };

  const handleCancel = () => {
    setSelectedThemeId(null);
    setFormState(initialFormState());
    clearFeedback();
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    const payload = { name: formState.name.trim() };
    if (!payload.name) {
      return;
    }

    setIsSubmitting(true);
    clearFeedback();

    try {
      if (selectedTheme) {
        await fetchWithAuth(`/api/v1/themes/${selectedTheme.id}`, {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(payload),
        });
        setFeedback({ type: 'success', message: 'Thème mis à jour.' });
      } else {
        await fetchWithAuth('/api/v1/themes', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(payload),
        });
        setSelectedThemeId(null);
        setFormState(initialFormState());
        setFeedback({ type: 'success', message: 'Thème créé.' });
      }

      await loadThemes({ preserveList: true });
    } catch (error) {
      if (error instanceof UnauthorizedError) {
        return;
      }

      console.error(`Failed to ${selectedTheme ? 'update' : 'create'} theme:`, error);
      setFeedback({
        type: 'error',
        message: selectedTheme ? 'Impossible de mettre à jour le thème.' : 'Impossible de créer le thème.',
      });
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDelete = async () => {
    if (!selectedTheme) {
      return;
    }

    if (!window.confirm(`Supprimer définitivement le thème « ${selectedTheme.name} » ?`)) {
      return;
    }

    setIsSubmitting(true);
    clearFeedback();

    try {
      await fetchWithAuth(`/api/v1/themes/${selectedTheme.id}`, {
        method: 'DELETE',
      });

      setSelectedThemeId(null);
      setFormState(initialFormState());
      setFeedback({ type: 'success', message: 'Thème supprimé.' });
      await loadThemes({ preserveList: true });
    } catch (error) {
      if (error instanceof UnauthorizedError) {
        return;
      }

      console.error('Failed to delete theme:', error);
      setFeedback({
        type: 'error',
        message: isConflictError(error)
          ? 'Impossible de supprimer ce thème car il est encore utilisé.'
          : 'Impossible de supprimer le thème.',
      });
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="theme-admin-page">
      <section className="theme-admin-card theme-admin-intro">
        <h1>Gestion des thèmes</h1>
        <p>Créez, mettez à jour et retirez les thèmes disponibles pour l’administration éditoriale.</p>
      </section>

      <div className="theme-admin-layout">
        <section className="theme-admin-card" aria-labelledby="theme-list-title">
          <div className="theme-admin-section-header">
            <h2 id="theme-list-title">Thèmes existants</h2>
            <p>Filtrez la liste et choisissez un thème à modifier.</p>
          </div>

          <label className="theme-admin-field" htmlFor="theme-filter">
            <span>Filtrer les thèmes</span>
            <input
              id="theme-filter"
              type="search"
              value={filter}
              onChange={handleFilterChange}
              placeholder="Rechercher un thème"
            />
          </label>

          {loadError && (
            <p role="alert" className="theme-admin-feedback theme-admin-feedback-error">
              {loadError}
            </p>
          )}

          {!loadError && isLoading && <p className="theme-admin-empty-state">Chargement des thèmes...</p>}

          {!loadError && !isLoading && filteredThemes.length === 0 && (
            <p className="theme-admin-empty-state">
              {filter.trim() ? 'Aucun thème ne correspond à ce filtre.' : 'Aucun thème disponible.'}
            </p>
          )}

          {!loadError && !isLoading && filteredThemes.length > 0 && (
            <ul className="theme-admin-list" aria-label="Liste des thèmes">
              {filteredThemes.map((theme) => {
                const isSelected = theme.id === selectedThemeId;

                return (
                  <li key={theme.id} className="theme-admin-list-item">
                    <button
                      type="button"
                      className={`theme-admin-list-button${isSelected ? ' theme-admin-list-button-selected' : ''}`}
                      aria-label={`Sélectionner le thème ${theme.name}`}
                      aria-pressed={isSelected}
                      disabled={isSubmitting}
                      onClick={() => handleThemeSelection(theme)}
                    >
                      {theme.name}
                    </button>
                  </li>
                );
              })}
            </ul>
          )}
        </section>

        <section className="theme-admin-card" aria-labelledby="theme-editor-title">
          <div className="theme-admin-section-header theme-admin-section-header-with-action">
            <div>
              <h2 id="theme-editor-title">{selectedTheme ? 'Modifier un thème' : 'Créer un thème'}</h2>
              <p>
                {selectedTheme
                  ? 'Ajustez le nom du thème sélectionné ou supprimez-le si nécessaire.'
                  : 'Ajoutez un nouveau thème à la liste disponible.'}
              </p>
            </div>

            {selectedTheme && (
              <button type="button" className="theme-admin-secondary-button" onClick={handleCancel}>
                Annuler
              </button>
            )}
          </div>

          {selectedTheme && (
            <p className="theme-admin-selection-summary">
              Thème sélectionné : <strong>{selectedTheme.name}</strong>
            </p>
          )}

          {feedback && (
            <p
              role={feedback.type === 'error' ? 'alert' : 'status'}
              className={`theme-admin-feedback ${feedback.type === 'error' ? 'theme-admin-feedback-error' : 'theme-admin-feedback-success'}`}
            >
              {feedback.message}
            </p>
          )}

          <form className="theme-admin-form" onSubmit={handleSubmit}>
            <label className="theme-admin-field" htmlFor="theme-name">
              <span>Nom du thème</span>
              <input
                id="theme-name"
                type="text"
                name="name"
                value={formState.name}
                onChange={handleNameChange}
                required
                disabled={isSubmitting}
              />
            </label>

            <div className="theme-admin-actions">
              <button
                type="submit"
                className="theme-admin-submit"
                disabled={isSubmitting || !formState.name.trim()}
              >
                {selectedTheme ? 'Enregistrer le thème' : 'Créer'}
              </button>

              {selectedTheme && (
                <button
                  type="button"
                  className="theme-admin-delete-button"
                  onClick={handleDelete}
                  disabled={isSubmitting}
                >
                  Supprimer
                </button>
              )}
            </div>
          </form>
        </section>
      </div>
    </div>
  );
};
