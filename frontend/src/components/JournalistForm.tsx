import React, { useState } from 'react';
import type { JournalistCreate } from '../types';
import { useI18n } from '../i18n/useI18n';

interface Props {
  onSubmit: (data: JournalistCreate) => void;
  initialData?: JournalistCreate;
}

export const JournalistForm: React.FC<Props> = ({ onSubmit, initialData }) => {
  const { t } = useI18n();
  const [formData, setFormData] = useState<JournalistCreate>(
    initialData || {
      firstName: '',
      lastName: '',
      globalEmail: '',
      globalPhone: '',
    }
  );

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSubmit(formData);
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div>
        <label htmlFor="firstName" className="block text-sm font-medium text-gray-700">{t('journalistForm.labelFirstName')}</label>
        <input
          id="firstName"
          type="text"
          name="firstName"
          autoComplete="given-name"
          value={formData.firstName}
          onChange={handleChange}
          required
          className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"
        />
      </div>
      <div>
        <label htmlFor="lastName" className="block text-sm font-medium text-gray-700">{t('journalistForm.labelLastName')}</label>
        <input
          id="lastName"
          type="text"
          name="lastName"
          autoComplete="family-name"
          value={formData.lastName}
          onChange={handleChange}
          required
          className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"
        />
      </div>
      <div>
        <label htmlFor="globalEmail" className="block text-sm font-medium text-gray-700">{t('journalistForm.labelEmail')}</label>
        <input
          id="globalEmail"
          type="email"
          name="globalEmail"
          autoComplete="email"
          value={formData.globalEmail}
          onChange={handleChange}
          className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"
        />
      </div>
      <div>
        <label htmlFor="globalPhone" className="block text-sm font-medium text-gray-700">{t('journalistForm.labelPhone')}</label>
        <input
          id="globalPhone"
          type="text"
          name="globalPhone"
          autoComplete="tel"
          value={formData.globalPhone}
          onChange={handleChange}
          className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"
        />
      </div>
      <button
        type="submit"
        className="inline-flex justify-center rounded-md border border-transparent bg-indigo-600 py-2 px-4 text-sm font-medium text-white shadow-sm hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2"
      >
        {t('journalistForm.submit')}
      </button>
    </form>
  );
};
