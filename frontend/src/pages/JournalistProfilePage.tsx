import React, { useEffect, useState } from 'react';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import type { JournalistProfile } from '../types';
import { ApiError, fetchWithAuth, UnauthorizedError } from '../api/apiClient';
import { useI18n } from '../i18n/useI18n';
import { useAuth } from '../context/AuthContext';
import '../styles/Profile.css';

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

export const JournalistProfilePage: React.FC = () => {
  const { t } = useI18n();
  const { id } = useParams<{ id: string }>();
  const { currentUser } = useAuth();
  const [journalist, setJournalist] = useState<JournalistProfile | null>(null);
  const [loading, setLoading] = useState(true);
  const [visible, setVisible] = useState(false);
  const [loadError, setLoadError] = useState<string | null>(null);

  const canEdit = currentUser?.roles.includes('JOURNALIST_MANAGER') || currentUser?.roles.includes('ADMIN');

  useEffect(() => {
    const fetchJournalist = async () => {
      if (!id) return;
      try {
        const res = await fetchWithAuth(`/api/v1/journalists/${id}`);
        const data = (await res.json()) as JournalistProfile;
        setJournalist(data);
        setLoadError(null);
      } catch (error) {
        if (!(error instanceof UnauthorizedError)) {
          console.error('Failed to fetch journalist profile:', error);
          if (error instanceof ApiError && error.status === 404) {
            setLoadError(t('journalistProfile.notFound'));
          } else {
            setLoadError(t('journalistProfile.loadError'));
          }
        }
      } finally {
        setLoading(false);
      }
    };

    fetchJournalist();
  }, [id, t]);

  // trigger entrance animation
  useEffect(() => {
    const t = setTimeout(() => setVisible(true), 20);
    return () => clearTimeout(t);
  }, []);

  if (loading) return <div style={{ padding: '24px' }}>{t('journalistProfile.loading')}</div>;
  if (loadError) return <div style={{ padding: '24px' }} role="alert">{loadError}</div>;
  if (!journalist) return <div style={{ padding: '24px' }} role="alert">{t('journalistProfile.notFound')}</div>;

  return (
    <div 
      className="profile-root" 
      style={{ 
        opacity: visible ? 1 : 0, 
        transform: visible ? 'translateY(0)' : 'translateY(8px)' 
      }}
    >
      <div className="profile-header">
        <BackButton />
        <h1 className="profile-title">
          {journalist.firstName} {journalist.lastName}
        </h1>
        {canEdit && (
          <div className="profile-actions">
            <EditButton journalistId={journalist.id} />
            <DeleteButton journalistId={journalist.id} />
          </div>
        )}
      </div>
      
      <div className="profile-card">
        <h2 className="card-title">{t('journalistProfile.contact.title')}</h2>
        <p className="card-text"><strong>Email:</strong> {journalist.globalEmail || 'N/A'}</p>
        <p className="card-text"><strong>Téléphone:</strong> {journalist.globalPhone || 'N/A'}</p>
      </div>

      <div className="profile-card">
        <h2 className="card-title">{t('journalistProfile.activities.title')}</h2>
        {journalist.activities.length === 0 ? (
          <p className="no-data">{t('journalistProfile.activities.empty')}</p>
        ) : (
          <ul className="activity-list">
            {journalist.activities.map((activity, index) => (
              <li 
                key={activity.id} 
                className="activity-item" 
                style={{ 
                  opacity: visible ? 1 : 0, 
                  transform: visible ? 'none' : 'translateY(6px)', 
                  transitionDelay: `${index * 40}ms` 
                }}
              >
                <p className="card-text"><strong>{activity.mediaName}</strong> ({activity.role})</p>
                {activity.specificEmail && <p className="card-text" style={{ marginTop: '4px' }}>Email: {activity.specificEmail}</p>}
                <div style={{ marginTop: '8px' }}>
                  {activity.themes.map((theme) => (
                    <span key={theme.id} className="theme-tag">
                      {theme.name}
                    </span>
                  ))}
                </div>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
};

const BackButton: React.FC = () => {
  const { t } = useI18n();
  const navigate = useNavigate();
  const location = useLocation();

  const handleBack = (e: React.MouseEvent) => {
    e.preventDefault();
    const fromSearch = isSearchLocationState(location.state) ? location.state.fromSearch : undefined;
    if (fromSearch) {
      const target = fromSearch ? `/?${fromSearch.replace(/^\?/, '')}` : '/';
      navigate(target, { replace: false });
      return;
    }

    if (window.history.length > 1) {
      navigate(-1);
    } else {
      navigate('/');
    }
  };

  return (
    <button onClick={handleBack} className="back-button" aria-label={t('journalistProfile.back')}>
      ← {t('journalistProfile.back')}
    </button>
  );
};

interface EditButtonProps {
  journalistId: string;
}

const EditButton: React.FC<EditButtonProps> = ({ journalistId }) => {
  const { t } = useI18n();
  const navigate = useNavigate();

  const handleEdit = () => {
    navigate(`/journalists/${journalistId}/edit`);
  };

  return (
    <a 
      href={`/journalists/${journalistId}/edit`}
      className="edit-link"
      onClick={(e) => {
        e.preventDefault();
        handleEdit();
      }}
    >
      {t('journalistProfile.edit')}
    </a>
  );
};

interface DeleteButtonProps {
  journalistId: string;
  onDeleteSuccess?: () => void;
}

const DeleteButton: React.FC<DeleteButtonProps> = ({ journalistId, onDeleteSuccess }) => {
  const { t } = useI18n();
  const navigate = useNavigate();
  const [isDeleting, setIsDeleting] = useState(false);

  const handleDelete = async () => {
    if (!confirm(t('journalistProfile.deleteConfirm'))) {
      return;
    }

    setIsDeleting(true);
    try {
      const res = await fetchWithAuth(`/api/v1/journalists/${journalistId}`, {
        method: 'DELETE',
      });
      
      if (!res.ok) {
        throw new Error('Failed to delete');
      }

      onDeleteSuccess?.();
      navigate('/');
    } catch (error) {
      console.error('Failed to delete journalist:', error);
      alert(t('journalistProfile.deleteError'));
    } finally {
      setIsDeleting(false);
    }
  };

  return (
    <button 
      onClick={handleDelete}
      disabled={isDeleting}
      className="delete-button"
    >
      {isDeleting ? t('journalistProfile.deleting') : t('journalistProfile.delete')}
    </button>
  );
};
