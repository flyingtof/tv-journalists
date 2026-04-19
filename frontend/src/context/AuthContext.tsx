import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type PropsWithChildren,
} from 'react';
import { fetchAuthBootstrap, fetchWithAuth, UnauthorizedError } from '../api/apiClient';
import type { CurrentUser } from '../types';

export interface AuthContextValue {
  currentUser: CurrentUser | null;
  isLoading: boolean;
  refreshCurrentUser: () => Promise<CurrentUser | null>;
  isAuthenticated: boolean;
  isAdmin: boolean;
}

// eslint-disable-next-line react-refresh/only-export-components
export const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export const AuthProvider = ({ children }: PropsWithChildren) => {
  const [currentUser, setCurrentUser] = useState<CurrentUser | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  const loadCurrentUser = useCallback(async (fetchCurrentUser: () => Promise<Response>) => {
    setIsLoading(true);

    try {
      const response = await fetchCurrentUser();
      const user = (await response.json()) as CurrentUser;
      setCurrentUser(user);
      return user;
    } catch (error) {
      if (error instanceof UnauthorizedError) {
        setCurrentUser(null);
        return null;
      }

      setCurrentUser(null);
      throw error;
    } finally {
      setIsLoading(false);
    }
  }, []);

  const refreshCurrentUser = useCallback(
    () => loadCurrentUser(() => fetchWithAuth('/api/v1/auth/me')),
    [loadCurrentUser],
  );

  const bootstrapCurrentUser = useCallback(
    () => loadCurrentUser(() => fetchAuthBootstrap('/api/v1/auth/me')),
    [loadCurrentUser],
  );

  useEffect(() => {
    void bootstrapCurrentUser().catch((error) => {
      console.error('Failed to load current user:', error);
    });
  }, [bootstrapCurrentUser]);

  const value = useMemo<AuthContextValue>(
    () => ({
      currentUser,
      isLoading,
      refreshCurrentUser,
      isAuthenticated: currentUser !== null,
      isAdmin: currentUser?.roles.includes('ADMIN') ?? false,
    }),
    [currentUser, isLoading, refreshCurrentUser],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

// eslint-disable-next-line react-refresh/only-export-components
export const useAuth = () => {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }

  return context;
};
