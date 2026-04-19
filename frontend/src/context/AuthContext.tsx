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

  const fetchCurrentUser = useCallback(async (requestCurrentUser: () => Promise<Response>) => {
    try {
      const response = await requestCurrentUser();
      return (await response.json()) as CurrentUser;
    } catch (error) {
      if (error instanceof UnauthorizedError) {
        return null;
      }

      throw error;
    }
  }, []);

  const refreshCurrentUser = useCallback(async () => {
    setIsLoading(true);

    try {
      const user = await fetchCurrentUser(() => fetchWithAuth('/api/v1/auth/me'));
      setCurrentUser(user);
      return user;
    } catch (error) {
      setCurrentUser(null);
      throw error;
    } finally {
      setIsLoading(false);
    }
  }, [fetchCurrentUser]);

  useEffect(() => {
    let isMounted = true;

    const bootstrapCurrentUser = async () => {
      try {
        const user = await fetchCurrentUser(() => fetchAuthBootstrap('/api/v1/auth/me'));
        if (isMounted) {
          setCurrentUser(user);
        }
      } catch (error) {
        if (isMounted) {
          setCurrentUser(null);
        }
        console.error('Failed to load current user:', error);
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    };

    void bootstrapCurrentUser();

    return () => {
      isMounted = false;
    };
  }, [fetchCurrentUser]);

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
