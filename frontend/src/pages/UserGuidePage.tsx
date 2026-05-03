import React from 'react';
import { useI18n } from '../i18n/useI18n';

export const UserGuidePage: React.FC = () => {
  const { t } = useI18n();

  return (
    <div style={{ padding: '24px' }}>
      <h1 style={{ fontSize: '24px', fontWeight: 'bold', marginBottom: '24px' }}>{t('guide.title')}</h1>
      
      <p style={{ marginBottom: '16px' }}>{t('guide.introduction')}</p>

      <section style={{ marginBottom: '32px' }}>
        <h2 style={{ fontSize: '20px', fontWeight: '600', marginBottom: '12px' }}>{t('guide.profiles.heading')}</h2>
        <p>{t('guide.profiles.description')}</p>
        <ul style={{ listStylePosition: 'inside', paddingLeft: '20px' }}>
          <li style={{ marginBottom: '8px' }}><strong>{t('guide.profiles.globalInfo')}</strong> : {t('guide.profiles.globalInfoDesc')}</li>
          <li style={{ marginBottom: '8px' }}><strong>{t('guide.profiles.activities')}</strong> : {t('guide.profiles.activitiesDesc')}</li>
        </ul>
      </section>

      <section style={{ marginBottom: '32px' }}>
        <h2 style={{ fontSize: '20px', fontWeight: '600', marginBottom: '12px' }}>{t('guide.search.heading')}</h2>
        <p>{t('guide.search.description')}</p>
        <ul style={{ listStylePosition: 'inside', paddingLeft: '20px' }}>
          <li style={{ marginBottom: '8px' }}><strong>{t('guide.search.byName')}</strong> : {t('guide.search.byNameDesc')}</li>
          <li style={{ marginBottom: '8px' }}><strong>{t('guide.search.byMedia')}</strong> : {t('guide.search.byMediaDesc')}</li>
          <li style={{ marginBottom: '8px' }}><strong>{t('guide.search.byThemes')}</strong> : {t('guide.search.byThemesDesc')}</li>
        </ul>
      </section>

      <section style={{ marginBottom: '32px' }}>
        <h2 style={{ fontSize: '20px', fontWeight: '600', marginBottom: '12px' }}>{t('guide.interactions.heading')}</h2>
        <p>{t('guide.interactions.description')}</p>
        <ul style={{ listStylePosition: 'inside', paddingLeft: '20px' }}>
          <li style={{ marginBottom: '8px' }}><strong>{t('guide.interactions.notes')}</strong> : {t('guide.interactions.notesDesc')}</li>
          <li style={{ marginBottom: '8px' }}><strong>{t('guide.interactions.context')}</strong> : {t('guide.interactions.contextDesc')}</li>
        </ul>
      </section>

      <section style={{ marginBottom: '32px' }}>
        <h2 style={{ fontSize: '20px', fontWeight: '600', marginBottom: '12px' }}>{t('guide.themes.heading')}</h2>
        <p>{t('guide.themes.description')}</p>
      </section>
    </div>
  );
};

