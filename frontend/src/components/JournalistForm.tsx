import React, { useMemo, useState } from 'react';
import { useI18n } from '../i18n/useI18n';
import { Autocomplete } from './Autocomplete';
import '../styles/JournalistForm.css';
import type { JournalistActivityWrite, JournalistWrite, LookupOption } from '../types';

const EMPTY_LOOKUP_OPTIONS: LookupOption[] = [];

interface Props {
  onSubmit: (data: JournalistWrite) => void;
  initialData?: JournalistWrite;
  mediaOptions?: LookupOption[];
  themeOptions?: LookupOption[];
  submitLabel?: string;
  isSubmitting?: boolean;
}

const createActivityDraft = (overrides: Partial<JournalistActivityWrite> = {}): JournalistActivityWrite => ({
  id: overrides.id,
  mediaId: overrides.mediaId ?? '',
  specificEmail: overrides.specificEmail ?? '',
  specificPhone: overrides.specificPhone ?? '',
  themeIds: overrides.themeIds ?? [],
});

const normalizeInitialData = (initialData?: JournalistWrite): JournalistWrite => ({
  firstName: initialData?.firstName ?? '',
  lastName: initialData?.lastName ?? '',
  globalEmail: initialData?.globalEmail ?? '',
  globalPhone: initialData?.globalPhone ?? '',
  activities: initialData?.activities?.length
    ? initialData.activities.map((activity) => createActivityDraft(activity))
    : [],
});

const buildMediaDrafts = (data: JournalistWrite, mediaOptions: LookupOption[]) =>
  data.activities.map((activity) => mediaOptions.find((media) => media.id === activity.mediaId)?.name ?? '');

const isBlankActivity = (activity: JournalistActivityWrite) =>
  !activity.mediaId && !activity.specificEmail && !activity.specificPhone && activity.themeIds.length === 0;

export const JournalistForm: React.FC<Props> = ({
  onSubmit,
  initialData,
  mediaOptions,
  themeOptions,
  submitLabel,
  isSubmitting = false,
}) => {
  const { t } = useI18n();
  const resolvedMediaOptions = mediaOptions ?? EMPTY_LOOKUP_OPTIONS;
  const resolvedThemeOptions = themeOptions ?? EMPTY_LOOKUP_OPTIONS;
  const [formData, setFormData] = useState<JournalistWrite>(() => normalizeInitialData(initialData));
  const [mediaDrafts, setMediaDrafts] = useState<string[]>(() =>
    buildMediaDrafts(normalizeInitialData(initialData), resolvedMediaOptions),
  );

  const visibleActivities = useMemo(() => {
    return formData.activities.length > 0 ? formData.activities : [createActivityDraft()];
  }, [formData.activities]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSubmit({
      ...formData,
      activities: formData.activities.filter((activity) => !isBlankActivity(activity)),
    });
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const updateActivity = (index: number, updater: (activity: JournalistActivityWrite) => JournalistActivityWrite) => {
    setFormData((prev) => ({
      ...prev,
      activities: visibleActivities.map((activity, currentIndex) =>
        currentIndex === index ? updater(activity) : activity,
      ),
    }));
  };

  const addActivity = () => {
    setFormData((prev) => ({
      ...prev,
      activities: [...prev.activities, createActivityDraft()],
    }));
    setMediaDrafts((prev) => [...prev, '']);
  };

  const removeActivity = (index: number) => {
    setFormData((prev) => ({
      ...prev,
      activities: prev.activities.filter((_, currentIndex) => currentIndex !== index),
    }));
    setMediaDrafts((prev) => prev.filter((_, currentIndex) => currentIndex !== index));
  };

  return (
    <form onSubmit={handleSubmit} className="journalist-form">
      <div className="journalist-form-card">
        <div className="journalist-form-grid">
          <div className="journalist-form-field">
            <label htmlFor="firstName">
              {t('journalistForm.labelFirstName')}
            </label>
            <input
              id="firstName"
              type="text"
              name="firstName"
              className="journalist-form-input"
              autoComplete="given-name"
              value={formData.firstName}
              onChange={handleChange}
              required
              disabled={isSubmitting}
            />
          </div>
          <div className="journalist-form-field">
            <label htmlFor="lastName">
              {t('journalistForm.labelLastName')}
            </label>
            <input
              id="lastName"
              type="text"
              name="lastName"
              className="journalist-form-input"
              autoComplete="family-name"
              value={formData.lastName}
              onChange={handleChange}
              required
              disabled={isSubmitting}
            />
          </div>
          <div className="journalist-form-field">
            <label htmlFor="globalEmail">
              {t('journalistForm.labelEmail')}
            </label>
            <input
              id="globalEmail"
              type="email"
              name="globalEmail"
              className="journalist-form-input"
              autoComplete="email"
              value={formData.globalEmail ?? ''}
              onChange={handleChange}
              disabled={isSubmitting}
            />
          </div>
          <div className="journalist-form-field">
            <label htmlFor="globalPhone">
              {t('journalistForm.labelPhone')}
            </label>
            <input
              id="globalPhone"
              type="text"
              name="globalPhone"
              className="journalist-form-input"
              autoComplete="tel"
              value={formData.globalPhone ?? ''}
              onChange={handleChange}
              disabled={isSubmitting}
            />
          </div>
        </div>

        <section className="journalist-form-activities-section">
          <div className="journalist-form-activities-header">
            <div>
              <h2>{t('journalistEditor.activitiesSection')}</h2>
              <p className="journalist-form-section-description">
                {t('journalistEditor.activitiesHint')}
              </p>
            </div>
            <button
              type="button"
              onClick={addActivity}
              disabled={isSubmitting}
              className="journalist-form-add-activity"
            >
              {t('journalistEditor.addActivity')}
            </button>
          </div>

          {visibleActivities.map((activity, index) => {
            const activityNumber = index + 1;
            const mediaLabel = t('journalistForm.activityMedia', { index: activityNumber });
            const emailLabel = t('journalistForm.activityEmail', { index: activityNumber });
            const phoneLabel = t('journalistForm.activityPhone', { index: activityNumber });
            const themeLegend = t('journalistForm.activityThemes', { index: activityNumber });

          return (
            <div key={activity.id ?? `activity-${index}`} className="journalist-form-activity-card">
              <div className="journalist-form-activity-card-header">
                <h3>{t('journalistEditor.activityTitle', { index: activityNumber })}</h3>
                {formData.activities.length > 0 && (
                  <button
                    type="button"
                    onClick={() => removeActivity(index)}
                    disabled={isSubmitting}
                    className="journalist-form-remove-activity"
                  >
                    {t('journalistEditor.removeActivity')}
                  </button>
                )}
              </div>

              <div className="journalist-form-activity-grid">
                <div className="journalist-form-field">
                  <label htmlFor={`media-${index}`}>
                    {mediaLabel}
                  </label>
                  <Autocomplete
                    id={`media-${index}`}
                    name={`media-${index}`}
                    suggestions={resolvedMediaOptions.map((m) => m.name)}
                    value={mediaDrafts[index] ?? resolvedMediaOptions.find((m) => m.id === activity.mediaId)?.name ?? ''}
                    onChange={(event) => {
                      const value = event.target.value;
                      setMediaDrafts((prev) => {
                        const next = [...prev];
                        next[index] = value;
                        return next;
                      });
                    }}
                    onSelect={(name) => {
                      const selected = resolvedMediaOptions.find((m) => m.name === name);
                      if (selected) {
                        setMediaDrafts((prev) => {
                          const next = [...prev];
                          next[index] = selected.name;
                          return next;
                        });
                        updateActivity(index, (current) => ({
                          ...current,
                          mediaId: selected.id,
                        }));
                      }
                    }}
                    placeholder={t('journalistSearch.mediaPlaceholder')}
                  />
                </div>

                <div className="journalist-form-field">
                  <label htmlFor={`specificEmail-${index}`}>
                    {emailLabel}
                  </label>
                  <input
                    id={`specificEmail-${index}`}
                    type="email"
                    className="journalist-form-input"
                    value={activity.specificEmail ?? ''}
                    onChange={(event) =>
                      updateActivity(index, (current) => ({ ...current, specificEmail: event.target.value }))
                    }
                    disabled={isSubmitting}
                  />
                </div>

                <div className="journalist-form-field">
                  <label htmlFor={`specificPhone-${index}`}>
                    {phoneLabel}
                  </label>
                  <input
                    id={`specificPhone-${index}`}
                    type="text"
                    className="journalist-form-input"
                    value={activity.specificPhone ?? ''}
                    onChange={(event) =>
                      updateActivity(index, (current) => ({ ...current, specificPhone: event.target.value }))
                    }
                    disabled={isSubmitting}
                  />
                </div>

                <fieldset className="journalist-form-fieldset journalist-form-activity-grid-full">
                  <legend>{themeLegend}</legend>
                  <div className="journalist-form-theme-picker">
                    <Autocomplete
                      id={`themes-${index}`}
                      name={`themes-${index}`}
                      suggestions={resolvedThemeOptions.map((t) => t.name)}
                      onSelect={(name) => {
                        const selected = resolvedThemeOptions.find((t) => t.name === name);
                        if (selected && !activity.themeIds.includes(selected.id)) {
                          updateActivity(index, (current) => ({
                            ...current,
                            themeIds: [...current.themeIds, selected.id],
                          }));
                        }
                      }}
                      placeholder={t('journalistSearch.themesPlaceholder')}
                    />
                    <div className="journalist-form-tag-list">
                      {activity.themeIds.map((themeId) => {
                        const theme = resolvedThemeOptions.find((opt) => opt.id === themeId);
                        return theme ? (
                          <div key={themeId} className="journalist-form-tag">
                            <span>{theme.name}</span>
                            <button
                              type="button"
                              onClick={() =>
                                updateActivity(index, (current) => ({
                                  ...current,
                                  themeIds: current.themeIds.filter((id) => id !== themeId),
                                }))
                              }
                              className="journalist-form-tag-remove"
                              aria-label={`Remove ${theme.name}`}
                            >
                              &times;
                            </button>
                          </div>
                        ) : null;
                      })}
                    </div>
                  </div>
                </fieldset>
              </div>
            </div>
          );
          })}
        </section>

        <button
          type="submit"
          disabled={isSubmitting}
          className="journalist-form-submit"
        >
          {submitLabel ?? t('journalistForm.submit')}
        </button>
      </div>
    </form>
  );
};
