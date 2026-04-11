import React from 'react';
import type { Journalist } from '../types';
import { Link, useLocation } from 'react-router-dom';
import '../styles/JournalistList.css';

interface Props {
  journalists: Journalist[];
  onSort: (sortBy: string) => void;
  sort: { sortBy: string; direction: string };
}

export const JournalistList: React.FC<Props> = ({ journalists, onSort, sort }) => {
  const location = useLocation();
  if (journalists.length === 0) {
    return (
      <div className="empty-state">
        <p>Aucun journaliste trouvé.</p>
      </div>
    );
  }

  const getSortIndicator = (column: string) => {
    if (sort.sortBy === column) {
      return (
        <span className="sort-indicator">
          {sort.direction === 'asc' ? '▲' : '▼'}
        </span>
      );
    }
    return null;
  };

  return (
    <div className="list-container">
      <table className="journalist-table">
        <thead className="table-head">
          <tr className="table-header-row">
            <th className="table-header">
              <button 
                onClick={() => onSort('lastName')} 
                className="sort-button"
              >
                Nom {getSortIndicator('lastName')}
              </button>
            </th>
            <th className="table-header">Email</th>
            <th className="table-header">Médias</th>
          </tr>
        </thead>
        <tbody>
          {journalists.map((journalist) => (
            <tr key={journalist.id} className="table-row">
              <td className="table-cell">
                <Link 
                  to={`/journalists/${journalist.id}`}
                  state={{ fromSearch: location.search }}
                  className="cell-name-link"
                >
                  {journalist.firstName} {journalist.lastName}
                </Link>
              </td>
              <td className="table-cell cell-secondary">
                {journalist.globalEmail || '-'}
              </td>
              <td className="table-cell cell-secondary">
                {journalist.activities.length > 0 ? (
                  <span>{journalist.activities.map(a => a.mediaName).join(', ')}</span>
                ) : (
                  <span>-</span>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};
