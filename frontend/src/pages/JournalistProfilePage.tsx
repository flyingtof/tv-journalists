import React, { useEffect, useState } from 'react';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import type { JournalistProfile } from '../types';
import { ApiError, fetchWithAuth, UnauthorizedError } from '../api/apiClient';
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
  const { id } = useParams<{ id: string }>();
  const [journalist, setJournalist] = useState<JournalistProfile | null>(null);
  const [loading, setLoading] = useState(true);
  const [visible, setVisible] = useState(false);
  const [loadError, setLoadError] = useState<string | null>(null);

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
            setLoadError('Journaliste introuvable');
          } else {
            setLoadError('Impossible de charger la fiche journaliste. Veuillez reessayer.');
          }
        }
      } finally {
        setLoading(false);
      }
    };

    fetchJournalist();
  }, [id]);

  // trigger entrance animation
  useEffect(() => {
    const t = setTimeout(() => setVisible(true), 20);
    return () => clearTimeout(t);
  }, []);

  if (loading) return <div style={{ padding: '24px' }}>Chargement...</div>;
  if (loadError) return <div style={{ padding: '24px' }} role="alert">{loadError}</div>;
  if (!journalist) return <div style={{ padding: '24px' }} role="alert">Journaliste introuvable</div>;

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
      </div>
      
      <div className="profile-card">
        <h2 className="card-title">Informations de Contact</h2>
        <p className="card-text"><strong>Email:</strong> {journalist.globalEmail || 'N/A'}</p>
        <p className="card-text"><strong>Téléphone:</strong> {journalist.globalPhone || 'N/A'}</p>
      </div>

      <div className="profile-card">
        <h2 className="card-title">Activités Média</h2>
        {journalist.activities.length === 0 ? (
          <p className="no-data">Aucune activité média enregistrée.</p>
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
    <button onClick={handleBack} className="back-button" aria-label="Retour">
      ← Retour
    </button>
  );
};
