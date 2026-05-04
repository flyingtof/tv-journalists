import React, { useCallback, useEffect, useRef, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import type { JournalistListItem, Page } from '../types';
import { JournalistList } from '../components/JournalistList';
import { Autocomplete } from '../components/Autocomplete';
import { ApiError, fetchWithAuth, UnauthorizedError } from '../api/apiClient';
import { useI18n } from '../i18n/useI18n';
import '../styles/Search.css';

interface SearchFilters {
  name: string;
  media: string[];
  themes: string[];
}

interface SortState {
  sortBy: string;
  direction: 'asc' | 'desc';
}

interface NamedOption {
  name: string;
}

const DEFAULT_PAGE_SIZE = 10;
const DEFAULT_SORT: SortState = { sortBy: 'lastName', direction: 'asc' };
const DEBOUNCE_MS = 150;

const parseNonNegativeInt = (value: string | null, fallback: number) => {
  if (value === null) {
    return fallback;
  }
  const parsed = Number(value);
  return Number.isFinite(parsed) && parsed >= 0 ? Math.floor(parsed) : fallback;
};

const parsePositiveInt = (value: string | null, fallback: number) => {
  if (value === null) {
    return fallback;
  }
  const parsed = Number(value);
  return Number.isFinite(parsed) && parsed > 0 ? Math.floor(parsed) : fallback;
};

const areStringArraysEqual = (left: string[], right: string[]) =>
  left.length === right.length && left.every((value, index) => value === right[index]);

const areFiltersEqual = (left: SearchFilters, right: SearchFilters) =>
  left.name === right.name &&
  areStringArraysEqual(left.media, right.media) &&
  areStringArraysEqual(left.themes, right.themes);

const buildParams = (searchFilters: SearchFilters, page: number, pageSize: number, sort: SortState) => {
  const params = new URLSearchParams();
  if (searchFilters.name) {
    params.append('name', searchFilters.name);
  }
  searchFilters.media.forEach((media) => params.append('media', media));
  searchFilters.themes.forEach((theme) => params.append('themes', theme));
  params.append('page', String(page));
  params.append('size', String(pageSize));
  params.append('sort', `${sort.sortBy},${sort.direction}`);
  return params;
};

export const JournalistSearchPage: React.FC = () => {
  const { t } = useI18n();
  const [journalists, setJournalists] = useState<Page<JournalistListItem> | null>(null);
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(DEFAULT_PAGE_SIZE);
  const [sort, setSort] = useState<SortState>(DEFAULT_SORT);
  const [filters, setFilters] = useState<SearchFilters>({
    name: '',
    media: [],
    themes: [],
  });
  const [mediaList, setMediaList] = useState<string[]>([]);
  const [themeList, setThemeList] = useState<string[]>([]);
  const [searchError, setSearchError] = useState<string | null>(null);
  const navigate = useNavigate();
  const location = useLocation();
  const urlDebounceRef = useRef<number | null>(null);
  const restoringRef = useRef(false);
  const latestFiltersRef = useRef(filters);
  const skipFirstRef = useRef(true);

  useEffect(() => {
    latestFiltersRef.current = filters;
  }, [filters]);

  const fetchSuggestions = useCallback(
    async (path: string, setter: React.Dispatch<React.SetStateAction<string[]>>, errorMessage: string) => {
      try {
        const response = await fetchWithAuth(path);
        const data = (await response.json()) as NamedOption[];
        setter(data.map((item) => item.name));
      } catch (error) {
        if (!(error instanceof UnauthorizedError)) {
          console.error(errorMessage, error);
        }
      }
    },
    [],
  );

  useEffect(() => {
    void fetchSuggestions('/api/v1/media', setMediaList, 'Failed to fetch media list:');
    void fetchSuggestions('/api/v1/themes', setThemeList, 'Failed to fetch themes list:');
  }, [fetchSuggestions]);

  const doSearch = useCallback(async (searchFilters: SearchFilters, nextPage: number, nextPageSize: number, nextSort: SortState) => {
    const params = buildParams(searchFilters, nextPage, nextPageSize, nextSort);

    try {
      const response = await fetchWithAuth(`/api/v1/journalists?${params.toString()}`);
      const data = (await response.json()) as Page<JournalistListItem>;
      setJournalists(data);
      setSearchError(null);
    } catch (error) {
      if (!(error instanceof UnauthorizedError)) {
        console.error('Failed to fetch journalists:', error);
        if (error instanceof ApiError) {
          setSearchError(error.message);
        } else {
          setSearchError(t('journalistSearch.loadError'));
        }
      }
    }
  }, [t]);

  const updateUrl = useCallback(
    (searchFilters: SearchFilters, nextPage: number, nextPageSize: number, nextSort: SortState, replace = true) => {
      const params = buildParams(searchFilters, nextPage, nextPageSize, nextSort);
      const search = params.toString();
      const target = search ? `?${search}` : '';

      if (restoringRef.current || target === location.search) {
        return;
      }

      if (urlDebounceRef.current) {
        window.clearTimeout(urlDebounceRef.current);
      }

      urlDebounceRef.current = window.setTimeout(() => {
        if (target === location.search) {
          return;
        }

        navigate(target, { replace });
        urlDebounceRef.current = null;
      }, DEBOUNCE_MS);
    },
    [location.search, navigate],
  );

  const updateFilters = useCallback(
    (updater: (current: SearchFilters) => SearchFilters) => {
      const current = latestFiltersRef.current;
      const next = updater(current);
      if (next === current) {
        return;
      }

      latestFiltersRef.current = next;
      setFilters(next);
      setPage(0);
      updateUrl(next, 0, pageSize, sort, false);
    },
    [pageSize, sort, updateUrl],
  );

  const handleSearch = useCallback(() => {
    setPage(0);
    updateUrl(filters, 0, pageSize, sort, false);
    void doSearch(filters, 0, pageSize, sort);
  }, [doSearch, filters, pageSize, sort, updateUrl]);

  const handleSort = (newSortBy: string) => {
    setSort((currentSort) => ({
      sortBy: newSortBy,
      direction: currentSort.sortBy === newSortBy && currentSort.direction === 'asc' ? 'desc' : 'asc',
    }));
  };

  useEffect(() => {
    if (skipFirstRef.current) {
      skipFirstRef.current = false;
      return;
    }

    updateUrl(latestFiltersRef.current, page, pageSize, sort, true);
  }, [page, pageSize, sort, updateUrl]);

  useEffect(() => {
    let isMounted = true;

    const runSearchFromLocation = async () => {
      const params = new URLSearchParams(location.search.replace(/^\?/, ''));
      const name = params.get('name') || '';
      const media = params.getAll('media');
      const themes = params.getAll('themes');
      const nextPage = parseNonNegativeInt(params.get('page'), 0);
      const nextPageSize = parsePositiveInt(params.get('size'), DEFAULT_PAGE_SIZE);
      const sortParam = params.get('sort') || `${DEFAULT_SORT.sortBy},${DEFAULT_SORT.direction}`;
      const [sortBy, direction] = sortParam.split(',');
      const nextSort: SortState = {
        sortBy: sortBy || DEFAULT_SORT.sortBy,
        direction: direction === 'desc' ? 'desc' : 'asc',
      };
      const nextFilters: SearchFilters = { name, media, themes };

      restoringRef.current = true;

      if (isMounted) {
        setFilters((current) => (areFiltersEqual(current, nextFilters) ? current : nextFilters));
        setPage((current) => (current === nextPage ? current : nextPage));
        setPageSize((current) => (current === nextPageSize ? current : nextPageSize));
        setSort((current) =>
          current.sortBy === nextSort.sortBy && current.direction === nextSort.direction ? current : nextSort,
        );
      }

      await doSearch(nextFilters, nextPage, nextPageSize, nextSort);
      restoringRef.current = false;
    };

    void runSearchFromLocation();

    return () => {
      isMounted = false;
    };
  }, [doSearch, location.search]);

  useEffect(() => {
    return () => {
      if (urlDebounceRef.current) {
        window.clearTimeout(urlDebounceRef.current);
        urlDebounceRef.current = null;
      }
    };
  }, []);

  const handleChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    updateFilters((current) => ({ ...current, name: event.target.value }));
  };

  const addTheme = (theme: string) => {
    if (!theme) {
      return;
    }

    updateFilters((current) =>
      current.themes.includes(theme)
        ? current
        : { ...current, themes: [...current.themes, theme] },
    );
  };

  const removeTheme = (theme: string) => {
    updateFilters((current) => ({
      ...current,
      themes: current.themes.filter((existingTheme) => existingTheme !== theme),
    }));
  };

  const addMedia = (media: string) => {
    if (!media) {
      return;
    }

    updateFilters((current) =>
      current.media.includes(media)
        ? current
        : { ...current, media: [...current.media, media] },
    );
  };

  const removeMedia = (media: string) => {
    updateFilters((current) => ({
      ...current,
      media: current.media.filter((existingMedia) => existingMedia !== media),
    }));
  };

  const handlePageSizeChange = (event: React.ChangeEvent<HTMLSelectElement>) => {
    setPageSize(Number(event.target.value));
    setPage(0);
  };

  return (
    <div className="search-container">
      <h1 className="search-title">{t('journalistSearch.title')}</h1>

      <form onSubmit={(event) => { event.preventDefault(); handleSearch(); }} className="search-form">
        <div className="search-grid">
          <div className="search-field">
            <label htmlFor="name" className="field-label">{t('journalistSearch.nameLabel')}</label>
            <input
              type="text"
              id="name"
              name="name"
              placeholder={t('journalistSearch.namePlaceholder')}
              value={filters.name}
              onChange={handleChange}
              className="field-input"
            />
          </div>
          <div className="search-field">
            <label htmlFor="media" className="field-label">{t('journalistSearch.mediaLabel')}</label>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
              <div style={{ width: '100%' }}>
                <Autocomplete
                  suggestions={mediaList}
                  onSelect={addMedia}
                  name="media"
                  id="media"
                  placeholder={t('journalistSearch.mediaPlaceholder')}
                />
              </div>
              <div className="tags-container">
                {filters.media.map((media) => (
                  <div key={media} className="tag">
                    <span>{media}</span>
                    <button type="button" onClick={() => removeMedia(media)} className="tag-remove" aria-label="Remove media">&times;</button>
                  </div>
                ))}
              </div>
            </div>
          </div>
          <div className="search-field">
            <label htmlFor="themes" className="field-label">{t('journalistSearch.themesLabel')}</label>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
              <div style={{ width: '100%' }}>
                <Autocomplete
                  suggestions={themeList}
                  onSelect={addTheme}
                  name="themes"
                  id="themes"
                  placeholder={t('journalistSearch.themesPlaceholder')}
                />
              </div>
              <div className="tags-container">
                {filters.themes.map((theme) => (
                  <div key={theme} className="tag">
                    <span>{theme}</span>
                    <button type="button" onClick={() => removeTheme(theme)} className="tag-remove" aria-label={`Remove ${theme}`}>&times;</button>
                  </div>
                ))}
              </div>
            </div>
          </div>
          <button type="submit" className="search-button">
            {t('journalistSearch.search')}
          </button>
        </div>
      </form>

      {searchError && (
        <div role="alert" className="error-message">
          {searchError}
        </div>
      )}

      {journalists && (
        <div className="pagination-sticky">
          <div className="pagination-controls">
            <button
              onClick={() => setPage(0)}
              disabled={journalists.first}
              className="pagination-button"
              aria-label={t('journalistSearch.pagination.first')}
            >
              &laquo;
            </button>
            <button
              onClick={() => setPage((currentPage) => currentPage - 1)}
              disabled={journalists.first}
              className="pagination-button"
              aria-label={t('journalistSearch.pagination.previous')}
            >
              &lsaquo;
            </button>
            <span className="pagination-info">
              {(() => {
                const total = journalists.totalElements ?? 0;
                const size = journalists.size ?? 0;
                const pageNum = journalists.number ?? 0;
                if (total === 0) return t('journalistSearch.pagination.emptyState');
                const start = pageNum * size + 1;
                const end = Math.min((pageNum + 1) * size, total);
                return t('journalistSearch.pagination.summary', { start, end, total });
              })()}
            </span>
            <button
              onClick={() => setPage((currentPage) => currentPage + 1)}
              disabled={journalists.last}
              className="pagination-button"
              aria-label={t('journalistSearch.pagination.next')}
            >
              &rsaquo;
            </button>
            <button
              onClick={() => setPage(journalists.totalPages - 1)}
              disabled={journalists.last}
              className="pagination-button"
              aria-label={t('journalistSearch.pagination.last')}
            >
              &raquo;
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
