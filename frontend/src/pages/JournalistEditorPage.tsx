import React, { useEffect, useMemo, useState } from 'react';
import { useLocation, useNavigate, useParams } from 'react-router-dom';
import { ApiError, fetchWithAuth, UnauthorizedError } from '../api/apiClient';
import { JournalistForm } from '../components/JournalistForm';
import { useI18n } from '../i18n/useI18n';
import type { JournalistProfile, JournalistWrite, LookupOption } from '../types';

interface JournalistEditorPageProps {
  mode: 'create' | 'edit';
}

interface SearchLocationState {
  fromSearch?: string;
}

const isSearchLocationState = (state: unknown): state is SearchLocationState => {
  if (typeof state !== 'object' || state === null) {
    return false;
  }

  const { fromSearch } = state as { fromSearch?: unknown };
  return fromSearch === undefined || typeof fromSearch === 'string';
};

const mapProfileToWrite = (profile: JournalistProfile, mediaOptions: LookupOption[]): JournalistWrite => ({
  firstName: profile.firstName,
  lastName: profile.lastName,
  globalEmail: profile.globalEmail ?? '',
  globalPhone: profile.globalPhone ?? '',
  activities: profile.activities.map((activity) => ({
    id: activity.id,
    mediaId: mediaOptions.find((media) => media.name === activity.mediaName)?.id ?? '',
    specificEmail: activity.specificEmail ?? '',
    specificPhone: activity.specificPhone ?? '',
    themeIds: activity.themes.map((theme) => theme.id),
  })),
});

const buildReturnTarget = (location: ReturnType<typeof useLocation>, fallback = '/') => {
  const state = location.state;
  const fromSearch = isSearchLocationState(state) ? state.fromSearch : undefined;
  return fromSearch ? `/?${fromSearch.replace(/^\?/, '')}` : fallback;
};

export const JournalistEditorPage: React.FC<JournalistEditorPageProps> = ({ mode }) => {
  const { t } = useI18n();
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const location = useLocation();
  const [mediaOptions, setMediaOptions] = useState<LookupOption[]>([]);
  const [themeOptions, setThemeOptions] = useState<LookupOption[]>([]);
  const [initialData, setInitialData] = useState<JournalistWrite | undefined>();
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [saveError, setSaveError] = useState<string | null>(null);

  const title = useMemo(() => (mode === 'edit' ? t('journalistEditor.editTitle') : t('journalistEditor.createTitle')), [mode, t]);

  useEffect(() => {
    let cancelled = false;

    const loadEditorData = async () => {
      try {
        const [mediaResponse, themeResponse] = await Promise.all([
          fetchWithAuth('/api/v1/media'),
          fetchWithAuth('/api/v1/themes'),
        ]);
        const [mediaData, themeData] = await Promise.all([
          mediaResponse.json() as Promise<LookupOption[]>,
          themeResponse.json() as Promise<LookupOption[]>,
        ]);

        if (cancelled) {
          return;
        }

        setMediaOptions(mediaData);
        setThemeOptions(themeData);

        if (mode === 'edit') {
          if (!id) {
            setLoadError(t('journalistEditor.loadError'));
            return;
          }

          const response = await fetchWithAuth(`/api/v1/journalists/${id}`);
          const profile = (await response.json()) as JournalistProfile;

          if (cancelled) {
            return;
          }

          setInitialData(mapProfileToWrite(profile, mediaData));
        } else {
          setInitialData({
            firstName: '',
            lastName: '',
            globalEmail: '',
            globalPhone: '',
            activities: [],
          });
        }

        setLoadError(null);
      } catch (error) {
        if (!(error instanceof UnauthorizedError)) {
          console.error('Failed to load journalist editor:', error);
          setLoadError(error instanceof ApiError ? error.message : t('journalistEditor.loadError'));
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    };

    void loadEditorData();

    return () => {
      cancelled = true;
    };
  }, [id, mode, t]);

  const handleSubmit = async (data: JournalistWrite) => {
    setSaving(true);
    setSaveError(null);

    try {
      const response = await fetchWithAuth(
        mode === 'edit' && id ? `/api/v1/journalists/${id}` : '/api/v1/journalists',
        {
          method: mode === 'edit' ? 'PUT' : 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(data),
        },
      );
      await response.json();
      navigate(buildReturnTarget(location), { replace: true });
    } catch (error) {
      if (!(error instanceof UnauthorizedError)) {
        console.error('Failed to save journalist:', error);
        setSaveError(error instanceof ApiError ? error.message : t('journalistEditor.saveError'));
      }
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return <div role="status">{t('journalistEditor.loading')}</div>;
  }

  if (loadError) {
    return <div role="alert">{loadError}</div>;
  }

  return (
    <div className="journalist-editor-page">
      <section className="journalist-editor-card journalist-editor-intro">
        <h1>{title}</h1>
        <p>{t('journalistEditor.intro')}</p>
      </section>

      <section className="journalist-editor-card">
        {saveError && <div role="alert">{saveError}</div>}
        {initialData && (
          <JournalistForm
            onSubmit={handleSubmit}
            initialData={initialData}
            mediaOptions={mediaOptions}
            themeOptions={themeOptions}
            isSubmitting={saving}
          />
        )}
      </section>
    </div>
  );
};
