import { Routes, Route, Link, useNavigate, useLocation } from 'react-router-dom';
import { useEffect } from 'react';
import { JournalistSearchPage } from './pages/JournalistSearchPage';
import { JournalistProfilePage } from './pages/JournalistProfilePage';
import { UserGuidePage } from './pages/UserGuidePage';
import { UserAdminPage } from './pages/UserAdminPage';
import { LoginPage } from './pages/LoginPage';
import { ProtectedRoute } from './components/ProtectedRoute';
import { useAuth } from './context/AuthContext';
import './styles/Layout.css';

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

  const roleLabels = currentUser?.roles.map((role) => ({
    code: role,
    label: role === 'ADMIN' ? 'Administrateur' : 'Utilisateur',
  })) ?? [];

  return (
    <nav className="main-nav">
      <div className="nav-content">
        <div className="nav-inner">
          <span className="logo-text">TV Journalists</span>
          <div className="nav-links-group">
            {!isLoginPage && isAuthenticated && (
              <div className="nav-links">
                <Link to="/" className="nav-link">
                  Recherche
                </Link>
                <Link to="/guide" className="nav-link">
                  Guide Utilisateur
                </Link>
                {currentUser?.roles.includes('ADMIN') && (
                  <Link to="/admin/users" className="nav-link">
                    Utilisateurs
                  </Link>
                )}
              </div>
            )}

            {!isLoginPage && (
              <div className="nav-user-panel">
                {isLoading && <span className="nav-status">Chargement...</span>}

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
                      Se déconnecter
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
            <Route element={<ProtectedRoute requiredRole="ADMIN" />}>
              <Route path="/admin/users" element={<UserAdminPage />} />
            </Route>
          </Route>
        </Routes>
      </main>
    </div>
  );
}

export default App;
