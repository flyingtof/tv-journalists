import { Routes, Route, Link, useNavigate, useLocation } from 'react-router-dom';
import { useEffect } from 'react';
import { JournalistSearchPage } from './pages/JournalistSearchPage';
import { JournalistProfilePage } from './pages/JournalistProfilePage';
import { UserGuidePage } from './pages/UserGuidePage';
import { UserAdminPage } from './pages/UserAdminPage';
import { ThemeAdminPage } from './pages/ThemeAdminPage';
import { LoginPage } from './pages/LoginPage';
import { ProtectedRoute } from './components/ProtectedRoute';
import { useAuth } from './context/AuthContext';
import { useI18n } from './i18n/useI18n';
import type { UserRole } from './types';
import './styles/Layout.css';

const THEME_ADMIN_ROLES: UserRole[] = ['ADMIN', 'THEME_MANAGER'];
const USER_ADMIN_ROLES: UserRole[] = ['ADMIN'];

function getRoleLabel(role: UserRole, t: (key: string) => string): string {
  switch (role) {
    case 'ADMIN':
      return t('app.role.admin');
    case 'THEME_MANAGER':
      return t('app.role.themeManager');
    default:
      return t('app.role.user');
  }
}

// Restores the page the user was on before being redirected to /login
const AuthRedirect: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  useEffect(() => {
    // Only redirect if we are NOT on the login page itself
    if (location.pathname === '/login') return;

    const redirect = sessionStorage.getItem('redirectAfterLogin');
    if (redirect) {
      sessionStorage.removeItem('redirectAfterLogin');
      navigate(redirect, { replace: true });
    }
  }, [location.pathname, navigate]);
  return null;
};

function Navigation() {
  const location = useLocation();
  const isLoginPage = location.pathname === '/login';
  const { currentUser, isAuthenticated, isLoading } = useAuth();
  const { t } = useI18n();
  const canManageThemes = currentUser?.roles.some((role) => THEME_ADMIN_ROLES.includes(role)) ?? false;
  const canManageUsers = currentUser?.roles.some((role) => USER_ADMIN_ROLES.includes(role)) ?? false;

  const roleLabels = currentUser?.roles.map((role) => ({
    code: role,
    label: getRoleLabel(role, t),
  })) ?? [];

  return (
    <nav className="main-nav">
      <div className="nav-content">
        <div className="nav-inner">
          <span className="logo-text">{t('app.logo')}</span>
          <div className="nav-links-group">
            {!isLoginPage && isAuthenticated && (
              <div className="nav-links">
                <Link to="/" className="nav-link">
                  {t('app.nav.search')}
                </Link>
                <Link to="/guide" className="nav-link">
                  {t('app.nav.userGuide')}
                </Link>
                {canManageUsers && (
                  <Link to="/admin/users" className="nav-link">
                    {t('app.nav.users')}
                  </Link>
                )}
                {canManageThemes && (
                  <Link to="/admin/themes" className="nav-link">
                    {t('app.nav.themes')}
                  </Link>
                )}
              </div>
            )}

            {!isLoginPage && (
              <div className="nav-user-panel">
                {isLoading && <span className="nav-status">{t('app.nav.loading')}</span>}

                {currentUser && (
                  <>
                    <div className="nav-user-details">
                      <span className="nav-user-name">
                        {currentUser.firstName} {currentUser.lastName}
                      </span>
                      <div className="nav-role-list">
                        {roleLabels.map((role) => (
                          <span
                            key={role.code}
                            className={`nav-role-badge${role.code === 'ADMIN' ? ' nav-role-badge-admin' : ''}`}
                          >
                            {role.label}
                          </span>
                        ))}
                      </div>
                    </div>

                    <a href="/api/logout" className="nav-link">
                      {t('app.nav.logout')}
                    </a>
                  </>
                )}
              </div>
            )}
          </div>
        </div>
      </div>
    </nav>
  );
}

function App() {
  return (
    <div className="app-container">
      <AuthRedirect />
      <Navigation />

      <main className="main-content">
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route element={<ProtectedRoute />}>
            <Route path="/" element={<JournalistSearchPage />} />
            <Route path="/journalists/:id" element={<JournalistProfilePage />} />
            <Route path="/guide" element={<UserGuidePage />} />
            <Route element={<ProtectedRoute requiredRoles={USER_ADMIN_ROLES} />}>
              <Route path="/admin/users" element={<UserAdminPage />} />
            </Route>
            <Route element={<ProtectedRoute requiredRoles={THEME_ADMIN_ROLES} />}>
              <Route path="/admin/themes" element={<ThemeAdminPage />} />
            </Route>
          </Route>
        </Routes>
      </main>
    </div>
  );
}

export default App;
