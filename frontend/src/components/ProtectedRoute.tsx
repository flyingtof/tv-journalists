import type { PropsWithChildren } from 'react';
import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import type { UserRole } from '../types';

interface ProtectedRouteProps extends PropsWithChildren {
  requiredRoles?: UserRole[];
  redirectTo?: string;
  unauthorizedRedirectTo?: string;
}

export const ProtectedRoute = ({
  children,
  requiredRoles = [],
  redirectTo = '/login',
  unauthorizedRedirectTo = '/',
}: ProtectedRouteProps) => {
  const { currentUser, isAuthenticated, isLoading } = useAuth();
  const location = useLocation();

  if (isLoading) {
    return (
      <div role="status" aria-live="polite">
        Chargement de la session...
      </div>
    );
  }

  if (!isAuthenticated) {
    const currentPath = location.pathname + location.search;
    if (currentPath !== redirectTo) {
      sessionStorage.setItem('redirectAfterLogin', currentPath);
    }

    return <Navigate to={redirectTo} replace />;
  }

  if (requiredRoles.length > 0 && !requiredRoles.some((role) => currentUser?.roles.includes(role))) {
    return <Navigate to={unauthorizedRedirectTo} replace />;
  }

  return children ? <>{children}</> : <Outlet />;
};
