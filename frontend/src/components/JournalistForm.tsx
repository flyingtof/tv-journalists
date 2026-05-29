import React, { useEffect, useMemo, useState } from 'react';
import { useI18n } from '../i18n/useI18n';
import type { JournalistActivityWrite, JournalistWrite, LookupOption } from '../types';

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
  role: overrides.role ?? '',
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

const isBlankActivity = (activity: JournalistActivityWrite) =>
  !activity.mediaId && !activity.role && !activity.specificEmail && !activity.specificPhone && activity.themeIds.length === 0;

export const JournalistForm: React.FC<Props> = ({
  onSubmit,
  initialData,
  mediaOptions = [],
  themeOptions = [],
  submitLabel,
  isSubmitting = false,
}) => {
  const { t } = useI18n();
  const [formData, setFormData] = useState<JournalistWrite>(() => normalizeInitialData(initialData));

  useEffect(() => {
    setFormData(normalizeInitialData(initialData));
  }, [initialData]);

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
  };

  const removeActivity = (index: number) => {
    setFormData((prev) => ({
      ...prev,
      activities: prev.activities.filter((_, currentIndex) => currentIndex !== index),
    }));
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div>
        <label htmlFor="firstName" className="block text-sm font-medium text-gray-700">
          {t('journalistForm.labelFirstName')}
        </label>
        <input
          id="firstName"
          type="text"
          name="firstName"
          autoComplete="given-name"
          value={formData.firstName}
          onChange={handleChange}
          required
          disabled={isSubmitting}
          className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"
        />
      </div>
      <div>
        <label htmlFor="lastName" className="block text-sm font-medium text-gray-700">
          {t('journalistForm.labelLastName')}
        </label>
        <input
          id="lastName"
          type="text"
          name="lastName"
          autoComplete="family-name"
          value={formData.lastName}
          onChange={handleChange}
          required
          disabled={isSubmitting}
          className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"
        />
      </div>
      <div>
        <label htmlFor="globalEmail" className="block text-sm font-medium text-gray-700">
          {t('journalistForm.labelEmail')}
        </label>
        <input
          id="globalEmail"
          type="email"
          name="globalEmail"
          autoComplete="email"
          value={formData.globalEmail ?? ''}
          onChange={handleChange}
          disabled={isSubmitting}
          className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"
        />
      </div>
      <div>
        <label htmlFor="globalPhone" className="block text-sm font-medium text-gray-700">
          {t('journalistForm.labelPhone')}
        </label>
        <input
          id="globalPhone"
          type="text"
          name="globalPhone"
          autoComplete="tel"
          value={formData.globalPhone ?? ''}
          onChange={handleChange}
          disabled={isSubmitting}
          className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"
        />
      </div>

      <section className="space-y-4">
        <div className="flex items-center justify-between">
          <h2 className="text-base font-semibold text-gray-900">{t('journalistEditor.activitiesSection')}</h2>
          <button
            type="button"
            onClick={addActivity}
            disabled={isSubmitting}
            className="inline-flex items-center rounded-md border border-gray-300 px-3 py-2 text-sm font-medium text-gray-700"
          >
            {t('journalistEditor.addActivity')}
          </button>
        </div>

        {visibleActivities.map((activity, index) => {
          const activityNumber = index + 1;
          const mediaLabel = t('journalistForm.activityMedia', { index: activityNumber });
          const roleLabel = t('journalistForm.activityRole', { index: activityNumber });
          const emailLabel = t('journalistForm.activityEmail', { index: activityNumber });
          const phoneLabel = t('journalistForm.activityPhone', { index: activityNumber });
          const themeLegend = t('journalistForm.activityThemes', { index: activityNumber });

          return (
            <div key={activity.id ?? `activity-${index}`} className="space-y-4 rounded-md border border-gray-200 p-4">
              <div className="flex items-center justify-between">
                <h3 className="text-sm font-medium text-gray-800">{t('journalistEditor.activityTitle', { index: activityNumber })}</h3>
                {formData.activities.length > 0 && (
                  <button
                    type="button"
                    onClick={() => removeActivity(index)}
                    disabled={isSubmitting}
                    className="text-sm font-medium text-red-600"
                  >
                    {t('journalistEditor.removeActivity')}
                  </button>
                )}
              </div>

              <div>
                <label htmlFor={`media-${index}`} className="block text-sm font-medium text-gray-700">
                  {mediaLabel}
                </label>
                <select
                  id={`media-${index}`}
                  value={activity.mediaId}
                  onChange={(event) =>
                    updateActivity(index, (current) => ({ ...current, mediaId: event.target.value }))
                  }
                  disabled={isSubmitting}
                  className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"
                >
                  <option value="">{t('journalistSearch.mediaPlaceholder')}</option>
                  {mediaOptions.map((media) => (
                    <option key={media.id} value={media.id}>
                      {media.name}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label htmlFor={`role-${index}`} className="block text-sm font-medium text-gray-700">
                  {roleLabel}
                </label>
                <input
                  id={`role-${index}`}
                  type="text"
                  value={activity.role}
                  onChange={(event) =>
                    updateActivity(index, (current) => ({ ...current, role: event.target.value }))
                  }
                  disabled={isSubmitting}
                  className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"
                />
              </div>

              <div>
                <label htmlFor={`specificEmail-${index}`} className="block text-sm font-medium text-gray-700">
                  {emailLabel}
                </label>
                <input
                  id={`specificEmail-${index}`}
                  type="email"
                  value={activity.specificEmail ?? ''}
                  onChange={(event) =>
                    updateActivity(index, (current) => ({ ...current, specificEmail: event.target.value }))
                  }
                  disabled={isSubmitting}
                  className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"
                />
              </div>

              <div>
                <label htmlFor={`specificPhone-${index}`} className="block text-sm font-medium text-gray-700">
                  {phoneLabel}
                </label>
                <input
                  id={`specificPhone-${index}`}
                  type="text"
                  value={activity.specificPhone ?? ''}
                  onChange={(event) =>
                    updateActivity(index, (current) => ({ ...current, specificPhone: event.target.value }))
                  }
                  disabled={isSubmitting}
                  className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"
                />
              </div>

              <fieldset>
                <legend className="block text-sm font-medium text-gray-700">{themeLegend}</legend>
                <div className="mt-2 space-y-2">
                  {themeOptions.map((theme) => {
                    const checked = activity.themeIds.includes(theme.id);
                    return (
                      <label key={theme.id} className="flex items-center gap-2 text-sm text-gray-700">
                        <input
                          type="checkbox"
                          checked={checked}
                          onChange={(event) =>
                            updateActivity(index, (current) => ({
                              ...current,
                              themeIds: event.target.checked
                                ? [...current.themeIds, theme.id]
                                : current.themeIds.filter((id) => id !== theme.id),
                            }))
                          }
                          disabled={isSubmitting}
                        />
                        {theme.name}
                      </label>
                    );
                  })}
                </div>
              </fieldset>
            </div>
          );
        })}
      </section>

      <button
        type="submit"
        disabled={isSubmitting}
        className="inline-flex justify-center rounded-md border border-transparent bg-indigo-600 py-2 px-4 text-sm font-medium text-white shadow-sm hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 disabled:opacity-60"
      >
        {submitLabel ?? t('journalistForm.submit')}
      </button>
    </form>
  );
};
