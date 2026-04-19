import React, { useState, useEffect, useRef } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import type { Journalist, Page } from '../types';
import { JournalistList } from '../components/JournalistList';
import { Autocomplete } from '../components/Autocomplete';
import { fetchWithAuth, UnauthorizedError } from '../api/apiClient';
import '../styles/Search.css';

export const JournalistSearchPage: React.FC = () => {
  const [journalists, setJournalists] = useState<Page<Journalist> | null>(null);
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [sort, setSort] = useState({ sortBy: 'lastName', direction: 'asc' });
  const [filters, setFilters] = useState({
    name: '',
    media: [] as string[],
    themes: [] as string[],
  });
  const [mediaList, setMediaList] = useState<string[]>([]);
  const [themeList, setThemeList] = useState<string[]>([]);
  const navigate = useNavigate();
  const location = useLocation();
  const urlDebounceRef = useRef<number | null>(null);
  const DEBOUNCE_MS = 150;
  const restoringRef = useRef(false);

  useEffect(() => {
    const fetchMedia = async () => {
      try {
        const res = await fetchWithAuth('/api/v1/media');
        const data = await res.json();
        setMediaList(data.map((m: { name: string }) => m.name));
      } catch (error) {
        if (!(error instanceof UnauthorizedError)) {
          console.error('Failed to fetch media list:', error);
        }
      }
    };
    fetchMedia();
    const fetchThemes = async () => {
      try {
        const res = await fetchWithAuth('/api/v1/themes');
        const data = await res.json();
        setThemeList(data.map((t: { name: string }) => t.name));
      } catch (error) {
        if (!(error instanceof UnauthorizedError)) {
          console.error('Failed to fetch themes list:', error);
        }
      }
    };
    fetchThemes();
  }, []);

  // Build URLSearchParams from state or provided overrides
  const buildParams = (searchFilters: any, p: number, ps: number, s: { sortBy: string; direction: string }) => {
    const params = new URLSearchParams();
    if (searchFilters.name) params.append('name', searchFilters.name);
    if (searchFilters.media) {
      if (Array.isArray(searchFilters.media)) {
        searchFilters.media.forEach((m: string) => params.append('media', m));
      } else {
        params.append('media', String(searchFilters.media));
      }
    }
    if (searchFilters.themes && Array.isArray(searchFilters.themes)) {
      searchFilters.themes.forEach((t: string) => params.append('themes', t));
    }
    params.append('page', String(p));
    params.append('size', String(ps));
    params.append('sort', `${s.sortBy},${s.direction}`);
    return params;
  };

  const doSearch = async (searchFilters: any, p: number, ps: number, s: { sortBy: string; direction: string }) => {
    const params = buildParams(searchFilters, p, ps, s);
    try {
      const res = await fetchWithAuth(`/api/v1/journalists?${params.toString()}`);
      const data = await res.json();

      if (data.content && data.page) {
        const pageData = data.page;
        const content = data.content;
        setJournalists({
          content: content,
          totalPages: pageData.totalPages,
          number: pageData.number,
          size: pageData.size,
          totalElements: pageData.totalElements,
          first: pageData.number === 0,
          last: pageData.number >= pageData.totalPages - 1,
          numberOfElements: content.length,
          empty: content.length === 0,
        });
      } else if (data.content) {
        setJournalists(data);
      } else if (Array.isArray(data)) {
        setJournalists({
          content: data,
          totalPages: 1,
          number: 0,
          first: true,
          last: true,
          size: data.length,
          totalElements: data.length,
          numberOfElements: data.length,
          empty: data.length === 0,
        });
      }
    } catch (error) {
      if (!(error instanceof UnauthorizedError)) {
        console.error('Failed to fetch journalists:', error);
      }
    }
  };

  const updateUrl = (searchFilters: any, p: number, ps: number, s: { sortBy: string; direction: string }, replace = true) => {
    const params = buildParams(searchFilters, p, ps, s);
    const search = params.toString();
    const target = search ? `?${search}` : '';
    // if we are currently restoring from location.search, avoid updating the URL
    if (restoringRef.current) return;
    // avoid scheduling if URL already matches
    if (target === location.search) return;

    // debounce navigate calls to avoid rapid successive navigations
    if (urlDebounceRef.current) {
      window.clearTimeout(urlDebounceRef.current);
    }
    urlDebounceRef.current = window.setTimeout(() => {
      // re-check to avoid navigating to the same URL in case it changed meanwhile
      if (target === location.search) return;
      navigate(target, { replace });
      urlDebounceRef.current = null;
    }, DEBOUNCE_MS) as unknown as number;
  };

  // Public handleSearch used by the form submit and other handlers
  const handleSearch = () => {
    setPage(0);
    updateUrl(filters, 0, pageSize, sort, false);
    doSearch(filters, 0, pageSize, sort);
  };

  const handleSort = (newSortBy: string) => {
    setSort(currentSort => ({
      sortBy: newSortBy,
      direction: currentSort.sortBy === newSortBy && currentSort.direction === 'asc' ? 'desc' : 'asc'
    }));
  };

  // Keep URL in sync when page/size/sort changes
  const skipFirstRef = useRef(true);
  useEffect(() => {
    // Skip the initial mount invocation to avoid overwriting query params restored from history
    if (skipFirstRef.current) {
      skipFirstRef.current = false;
      return;
    }
    updateUrl(filters, page, pageSize, sort, true);
  }, [page, pageSize, sort]);

  // When location.search changes (back/forward or URL update), parse and run the search
  useEffect(() => {
    const run = async () => {
      const params = new URLSearchParams(location.search.replace(/^\?/, ''));
      const name = params.get('name') || '';
      const media = params.getAll('media') || [];
      const themes = params.getAll('themes') || [];
      const p = Number(params.get('page') ?? 0);
      const ps = Number(params.get('size') ?? pageSize);
      const sortParam = params.get('sort') || `${sort.sortBy},${sort.direction}`;
      const [sortBy, direction] = sortParam.split(',');

      // mark that we are restoring state so updateUrl is a no-op while we apply it
      restoringRef.current = true;

      // Update state only if different to avoid loops
      setFilters(prev => {
        const prevThemes = Array.isArray(prev.themes) ? prev.themes : [];
        const prevMedia = Array.isArray(prev.media) ? prev.media : [];
        if (prev.name === name && JSON.stringify(prevMedia) === JSON.stringify(media) && JSON.stringify(prevThemes) === JSON.stringify(themes)) return prev;
        return { ...prev, name, media, themes };
      });
      setPage(prev => (prev === p ? prev : p));
      setPageSize(prev => (prev === ps ? prev : ps));
      setSort(prev => (prev.sortBy === (sortBy || 'lastName') && prev.direction === (direction || 'asc') ? prev : { sortBy: sortBy || 'lastName', direction: direction || 'asc' }));

      await doSearch({ name, media, themes }, p, ps, { sortBy: sortBy || 'lastName', direction: direction || 'asc' });

      // allow updates to URL again
      restoringRef.current = false;
    };

    run();
  }, [location.search]);

  // cleanup debounce timer on unmount
  useEffect(() => {
    return () => {
      if (urlDebounceRef.current) {
        window.clearTimeout(urlDebounceRef.current);
        urlDebounceRef.current = null;
      }
    };
  }, []);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    // If changing themes input directly (typing), keep it for Autocomplete internal input value only.
    if (name === 'themes') {
      // We don't set filters.themes from raw input here; Autocomplete will call onSelect to add tags.
      return;
    }
    const newFilters = { ...filters, [name]: value };
    setFilters(newFilters);
    setPage(0);
    updateUrl(newFilters, 0, pageSize, sort, false);
  };

  // media selection handled inline where Autocomplete onSelect used

  const addTheme = (theme: string) => {
    if (!theme) return;
    setFilters(prev => {
      if (prev.themes.includes(theme)) return prev;
      const next = { ...prev, themes: [...prev.themes, theme] };
      setPage(0);
      updateUrl(next, 0, pageSize, sort, false);
      return next;
    });
  };

  const removeTheme = (theme: string) => {
    setFilters(prev => {
      const nextThemes = prev.themes.filter(t => t !== theme);
      const next = { ...prev, themes: nextThemes };
      setPage(0);
      updateUrl(next, 0, pageSize, sort, false);
      return next;
    });
  };

  const handlePageSizeChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setPageSize(Number(e.target.value));
    setPage(0);
  };

  return (
    <div className="search-container">
      <h1 className="search-title">Recherche de journalistes</h1>
      
      <form onSubmit={(e) => { e.preventDefault(); handleSearch(); }} className="search-form">
        <div className="search-grid">
          <div className="search-field">
            <label htmlFor="name" className="field-label">Nom</label>
            <input
              type="text"
              id="name"
              name="name"
              placeholder="ex: Jean Dupont"
              value={filters.name}
              onChange={handleChange}
              className="field-input"
            />
          </div>
          <div className="search-field">
            <label htmlFor="media" className="field-label">Média</label>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
              <div style={{ width: '100%' }}>
                <Autocomplete
                  suggestions={mediaList}
                  value={''}
                  onChange={() => { /* internal only */ }}
                  onSelect={(selected) => {
                    // add media if not present
                    setFilters(prev => {
                      if (prev.media.includes(selected)) return prev;
                      const next = { ...prev, media: [...prev.media, selected] };
                      setPage(0);
                      updateUrl(next, 0, pageSize, sort, false);
                      return next;
                    });
                  }}
                  name="media"
                  id="media"
                  placeholder="ex: Le Monde"
                />
              </div>
              <div className="tags-container">
                {filters.media.map(m => (
                  <div key={m} className="tag">
                    <span>{m}</span>
                    <button onClick={() => {
                      // remove this media
                      const next = { ...filters, media: filters.media.filter(x => x !== m) };
                      setFilters(next);
                      setPage(0);
                      updateUrl(next, 0, pageSize, sort, false);
                    }} className="tag-remove" aria-label={`Remove media`}>&times;</button>
                  </div>
                ))}
              </div>
            </div>
          </div>
          <div className="search-field">
            <label htmlFor="themes" className="field-label">Thèmes</label>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
              <div style={{ width: '100%' }}>
                <Autocomplete
                  suggestions={themeList}
                  value={''}
                  onChange={() => { /* internal only */ }}
                  onSelect={(selected) => {
                    addTheme(selected);
                  }}
                  name="themes"
                  id="themes"
                  placeholder="Ajouter un thème..."
                />
              </div>
              <div className="tags-container">
                {filters.themes.map((t) => (
                  <div key={t} className="tag">
                    <span>{t}</span>
                    <button onClick={() => removeTheme(t)} className="tag-remove" aria-label={`Remove ${t}`}>&times;</button>
                  </div>
                ))}
              </div>
            </div>
          </div>
          <button type="submit" className="search-button">
            Rechercher
          </button>
        </div>
      </form>

      {journalists && (
        <div className="pagination-sticky">
          <div className="pagination-controls">
            <button 
              onClick={() => setPage(p => p - 1)} 
              disabled={journalists.first}
              className="pagination-button"
            >
              Précédent
            </button>
            <span className="pagination-info">
              {(() => {
                const total = journalists.totalElements ?? 0;
                const size = journalists.size ?? 0;
                const pageNum = journalists.number ?? 0; // zero-based
                if (total === 0) return `0-0 sur 0`;
                const start = pageNum * size + 1;
                const end = Math.min((pageNum + 1) * size, total);
                return `${start}-${end} sur ${total}`;
              })()}
            </span>
            <button 
              onClick={() => setPage(p => p + 1)} 
              disabled={journalists.last}
              className="pagination-button"
            >
              Suivant
            </button>
            <select value={pageSize} onChange={handlePageSizeChange} className="pagination-select">
              <option value={5}>5</option>
              <option value={10}>10</option>
              <option value={20}>20</option>
              <option value={50}>50</option>
            </select>
          </div>
        </div>
      )}

      <JournalistList 
        journalists={journalists ? journalists.content : []} 
        onSort={handleSort}
        sort={sort}
      />
    </div>
  );
};
