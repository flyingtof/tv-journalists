import { BrowserRouter as Router, Routes, Route, Link, useNavigate, useLocation } from 'react-router-dom';
import { useEffect } from 'react';
import { JournalistSearchPage } from './pages/JournalistSearchPage';
import { JournalistProfilePage } from './pages/JournalistProfilePage';
import { UserGuidePage } from './pages/UserGuidePage';
import { LoginPage } from './pages/LoginPage';
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

  return (
    <nav className="main-nav">
      <div className="nav-content">
        <div className="nav-inner">
          <span className="logo-text">TV Journalists</span>
          <div className="nav-links">
            <Link to="/" className="nav-link">
              Recherche
            </Link>
            <Link to="/guide" className="nav-link">
              Guide Utilisateur
            </Link>
            {!isLoginPage && (
              <a href="/api/logout" className="nav-link">
                Se déconnecter
              </a>
            )}
          </div>
        </div>
      </div>
    </nav>
  );
}

function App() {
  return (
    <Router>
      <div className="app-container">
        <AuthRedirect />
        <Navigation />

        <main className="main-content">
          <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/" element={<JournalistSearchPage />} />
            <Route path="/journalists/:id" element={<JournalistProfilePage />} />
            <Route path="/guide" element={<UserGuidePage />} />
          </Routes>
        </main>
      </div>
    </Router>
  );
}

export default App;
